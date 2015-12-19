package com.topanimestream.views;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.topanimestream.App;
import com.topanimestream.R;
import com.topanimestream.custom.StrokedRobotoTextView;
import com.topanimestream.models.Anime;
import com.topanimestream.models.Episode;
import com.topanimestream.models.Language;
import com.topanimestream.models.OdataRequestInfo;
import com.topanimestream.models.Source;
import com.topanimestream.models.Subtitle;
import com.topanimestream.models.subs.Caption;
import com.topanimestream.models.subs.FormatASS;
import com.topanimestream.models.subs.FormatSRT;
import com.topanimestream.models.subs.TimedTextObject;
import com.topanimestream.preferences.Prefs;
import com.topanimestream.utilities.AsyncTaskTools;
import com.topanimestream.utilities.FileUtils;
import com.topanimestream.utilities.ImageUtils;
import com.topanimestream.utilities.ODataUtils;
import com.topanimestream.utilities.PrefUtils;
import com.topanimestream.utilities.StorageUtils;
import com.topanimestream.utilities.Utils;
import com.topanimestream.utilities.WcfDataServiceUtility;

import org.json.JSONArray;
import org.json.JSONObject;

import butterknife.Bind;

public class VideoPlayerActivity extends TASBaseActivity implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener, VideoControllerView.VideoControllerCallback {
    MediaPlayer player;
    VideoControllerView controller;
    private TimedTextObject mSubs;
    private Handler mDisplayHandler;
    private Caption mLastSub = null;
    private File mSubsFile;
    private boolean checkForSubtitle;
    private Anime anime;
    private Episode currentEpisode;
    private Subtitle currentEpisodeSubtitle;
    private ArrayList<Subtitle> subtitles = new ArrayList<Subtitle>();
    private ArrayList<Source> sources = new ArrayList<Source>();
    private String currentVideoLanguageId;
    private String currentVideoQuality;
    private double lastSubtitleTime;
    private int videoTime = -1;
    public static File getStorageLocation(Context context) {
        return new File(StorageUtils.getIdealCacheDirectory(context).toString() + "/subs/");
    }

    @Bind(R.id.loadingSpinner)
    ProgressBar loadingSpinner;

    @Bind(R.id.imgLoading)
    ImageView imgLoading;

    @Bind(R.id.videoSurfaceContainer)
    RelativeLayout videoSurfaceContainer;

    @Bind(R.id.txtSubtitle)
    StrokedRobotoTextView txtSubtitle;

    @Bind(R.id.videoSurface)
    SurfaceView surfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState, R.layout.activity_video_player);

        txtSubtitle.setTextColor(PrefUtils.get(this, Prefs.SUBTITLE_COLOR, Color.WHITE));
        txtSubtitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, PrefUtils.get(this, Prefs.SUBTITLE_SIZE, 16));
        txtSubtitle.setStrokeColor(PrefUtils.get(this, Prefs.SUBTITLE_STROKE_COLOR, Color.BLACK));
        txtSubtitle.setStrokeWidth(TypedValue.COMPLEX_UNIT_DIP,PrefUtils.get(this, Prefs.SUBTITLE_STROKE_WIDTH, 2));
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mDisplayHandler = new Handler(Looper.getMainLooper());
        surfaceView.getHolder().addCallback(this);

        player = new MediaPlayer();
        player.setOnPreparedListener(this);
        Bundle bundle = getIntent().getExtras();
        anime = bundle.getParcelable("anime");
        currentEpisode = bundle.getParcelable("episodeToPlay");
        controller = new VideoControllerView(VideoPlayerActivity.this, true, anime.getEpisodes(), currentEpisode);

        if(currentEpisode != null)
        {
            App.imageLoader.displayImage(ImageUtils.resizeImage(getString(R.string.image_host_path) + currentEpisode.getScreenshotHD(), 780), imgLoading);
        }
        GetSourcesAndSubs();
    }
    private long getCurrentTime()
    {
        try {
            return player.getCurrentPosition();
        }catch(Exception e){
            e.printStackTrace();
        }

        return 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        checkForSubtitle = false;
        if(player != null)
        {
            try {
                player.stop();
                player.release();
            }catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    protected void checkSubs() {
        if(mSubs != null) {
            Collection<Caption> subtitles = mSubs.captions.values();
            double currentTime = getCurrentTime();
            if(lastSubtitleTime > currentTime)
            {
                showTimedCaptionText(null);
            }
            lastSubtitleTime = currentTime;
            if (mLastSub != null && currentTime >= mLastSub.start.getMilliseconds() && currentTime <= mLastSub.end.getMilliseconds()) {
                showTimedCaptionText(mLastSub);
            } else {
                for (Caption caption : subtitles) {
                    if (currentTime >= caption.start.getMilliseconds() && currentTime <= caption.end.getMilliseconds()) {
                        mLastSub = caption;

                        showTimedCaptionText(caption);
                        break;
                    } else if (currentTime > caption.end.getMilliseconds()) {
                        showTimedCaptionText(null);
                    }
                }
            }
        }
    }
    private void startSubtitles() {
       AsyncTaskTools.execute(
               new AsyncTask<Void, Void, Void>() {
                   @Override
                   protected Void doInBackground(Void... voids) {
                       try {
                           FileInputStream fileInputStream = new FileInputStream(mSubsFile);
                           FormatSRT formatSRT = new FormatSRT();
                           mSubs = formatSRT.parseFile(mSubsFile.toString(), FileUtils.inputstreamToCharsetString(fileInputStream).split("\n"));
                           checkForSubtitle = true;
                           (new Thread() {
                               @Override
                               public void run() {
                                   try {
                                       while(checkForSubtitle) {
                                           checkSubs();
                                           sleep(50);
                                       }
                                   } catch (InterruptedException e) {
                                       e.printStackTrace();
                                   }

                                   return;
                               }
                           }).start();

                       } catch (FileNotFoundException e) {
                           if (e.getMessage().contains("EBUSY")) {
                               startSubtitles();
                           }
                           e.printStackTrace();
                       } catch (IOException e) {
                           e.printStackTrace();
                       }
                       return null;
                   }
               });
    }

    public void GetSourcesAndSubs()
    {
        String getSourcesUrl;
        String getSubsUrl;
        sources = new ArrayList<Source>();
        subtitles = new ArrayList<Subtitle>();

        if(!anime.isMovie()) {
            http://www.topanimestream.com/odata/Subtitles?$filter=AnimeId%20eq%201656%20and%20EpisodeId%20eq%2029259&$expand=Language
            getSourcesUrl = getString(R.string.odata_path) + "GetSources(animeId=" + anime.getAnimeId() + ",episodeId=" + currentEpisode.getEpisodeId() + ")?$expand=Link($expand=Language)";
            //new WcfDataServiceUtility(getString(R.string.odata_path)).getEntity("GetSources").queryString("animeId", String.valueOf(anime.getAnimeId())).queryString("episodeId", String.valueOf(currentEpisode.getEpisodeId())).expand("Link/Language").formatJson().build();
            getSubsUrl = getString(R.string.odata_path) + "Subtitles?$filter=AnimeId%20eq%20" + anime.getAnimeId() + "%20and%20EpisodeId%20eq%20" + currentEpisode.getEpisodeId() + "&$expand=Language";
            //new WcfDataServiceUtility(getString(R.string.odata_path)).getEntity("Subtitles").filter("AnimeId%20eq%20" + anime.getAnimeId() + "%20and%20EpisodeId%20eq%20" + currentEpisode.getEpisodeId()).expand("Language").formatJson().build();
        }
        else {
            getSourcesUrl = new WcfDataServiceUtility(getString(R.string.odata_path)).getEntity("GetSources").queryString("animeId", String.valueOf(anime.getAnimeId())).expand("Link/Language").formatJson().build();
            getSubsUrl = new WcfDataServiceUtility(getString(R.string.odata_path)).getEntity("Subtitles").filter("AnimeId%20eq%20" + anime.getAnimeId()).expand("Language").formatJson().build();
        }

        ODataUtils.GetEntityList(getSourcesUrl, Source.class, new ODataUtils.Callback<ArrayList<Source>>() {
            @Override
            public void onSuccess(ArrayList<Source> newSources, OdataRequestInfo info) {
                String defaultLanguageId = PrefUtils.get(VideoPlayerActivity.this, Prefs.DEFAULT_VIDEO_LANGUAGE, "3");
                String defaultQuality = PrefUtils.get(VideoPlayerActivity.this, Prefs.DEFAULT_VIDEO_QUALITY, "1080p");
                String defaultSubtitle = PrefUtils.get(VideoPlayerActivity.this, Prefs.DEFAULT_VIDEO_SUBTITLE_LANGUAGE, "1");

                sources = newSources;

                SelectSourceAndPlay(defaultLanguageId, defaultQuality, defaultSubtitle);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(VideoPlayerActivity.this, getString(R.string.error_loading_sources), Toast.LENGTH_LONG).show();
            }
        });

        ODataUtils.GetEntityList(getSubsUrl, Subtitle.class, new ODataUtils.Callback<ArrayList<Subtitle>>() {
            @Override
            public void onSuccess(ArrayList<Subtitle> newSubtitles, OdataRequestInfo info) {
                subtitles = newSubtitles;
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(VideoPlayerActivity.this, getString(R.string.error_loading_subtitles), Toast.LENGTH_LONG).show();
            }
        });
    }

    private class SubtitleTask extends AsyncTask<Void, Void, String> {
            String fileName;

            public SubtitleTask() {

            }
            private String subUrl;

            @Override
            protected void onPreExecute() {
                subUrl = getString(R.string.sub_host_path) + currentEpisodeSubtitle.getRelativePath();
            }

            @Override
            protected String doInBackground(Void... params) {
                if(!App.IsNetworkConnected())
                {
                    return getString(R.string.error_internet_connection);
                }
                InputStream input = null;
                HttpURLConnection connection = null;
                try
                {
                    final File subsDirectory = getStorageLocation(VideoPlayerActivity.this);
                    if(!anime.isMovie())
                        fileName = anime.getName() + "-" + currentEpisode.getEpisodeNumber() + "-" + currentEpisodeSubtitle.getLanguage().getISO639() + currentEpisodeSubtitle.getSpecification().replace(" ", "-");
                    else
                        fileName = anime.getName() + "-"  + currentEpisodeSubtitle.getLanguage().getISO639();

                    //http://stackoverflow.com/questions/13204807/max-file-name-length-in-android
                    //We need to make sure the fileName is not over 127 characters
                     if(fileName.length() > 127) {
                        int characterToRemove = fileName.length() - 127;
                        //Remove characters from the anime name.
                        fileName = anime.getName().substring(0, anime.getName().length() - characterToRemove) + "-" + currentEpisode.getEpisodeNumber() + "-" + currentEpisodeSubtitle.getLanguage().getISO639();
                    }
                    fileName = fileName + ".srt";
                    final File srtPath = new File(subsDirectory, fileName);

                    if (srtPath.exists()) {
                        return null;
                    }
                    URL url = new URL(subUrl);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.connect();

                    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                        return getString(R.string.error_downloading_subtitle);
                    }

                    input = connection.getInputStream();

                    TimedTextObject subtitleObject = null;

                    String inputString = FileUtils.inputstreamToCharsetString(input);
                    String[] inputText = inputString.split("\n|\r\n");
                    if (subUrl.contains(".ass") || subUrl.contains(".ssa")) {
                        FormatASS formatASS = new FormatASS();
                        subtitleObject = formatASS.parseFile(subUrl, inputText);
                    }
                    else if (subUrl.contains(".srt")) {
                        FormatSRT formatSRT = new FormatSRT();
                        subtitleObject = formatSRT.parseFile(subUrl, inputText);
                    }

                    if (subtitleObject != null) {
                        subtitleObject.setOffset(3700);
                        FileUtils.saveStringFile(subtitleObject.toSRT(), srtPath);
                    }
                    return null;
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                return getString(R.string.error_downloading_subtitle);
            }

            @Override
            protected void onPostExecute(String error) {
                if(error != null)
                {
                    Toast.makeText(VideoPlayerActivity.this, error, Toast.LENGTH_LONG).show();
                }
                else
                {
                    mSubsFile = new File(getStorageLocation(VideoPlayerActivity.this), fileName);
                    startSubtitles();
                }
            }
    }
    protected void showTimedCaptionText(final Caption text) {
        mDisplayHandler.post(new Runnable() {
            @Override
            public void run() {
                if (text == null) {
                    if (txtSubtitle.getText().length() > 0) {
                        txtSubtitle.setText("");
                    }
                    return;
                }
                SpannableStringBuilder styledString = (SpannableStringBuilder) Html.fromHtml(text.content);

                ForegroundColorSpan[] toRemoveSpans = styledString.getSpans(0, styledString.length(), ForegroundColorSpan.class);
                for (ForegroundColorSpan remove : toRemoveSpans) {
                    styledString.removeSpan(remove);
                }

                if (!txtSubtitle.getText().toString().equals(styledString.toString())) {
                    txtSubtitle.setText(styledString);
                }
            }
        });
    }
    private void setVideoSize() {

        if(Utils.getScreenOrientation(VideoPlayerActivity.this) == Configuration.ORIENTATION_PORTRAIT) {
            // // Get the dimensions of the video
            int videoWidth = player.getVideoWidth();
            int videoHeight = player.getVideoHeight();
            float videoProportion = (float) videoWidth / (float) videoHeight;

            // Get the width of the screen
            int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
            int screenHeight = getWindowManager().getDefaultDisplay().getHeight();
            float screenProportion = (float) screenWidth / (float) screenHeight;

            // Get the SurfaceView layout parameters
            android.view.ViewGroup.LayoutParams lp = surfaceView.getLayoutParams();
            if (videoProportion > screenProportion) {
                lp.width = screenWidth;
                lp.height = (int) ((float) screenWidth / videoProportion);
            } else {
                lp.width = (int) (videoProportion * (float) screenHeight);
                lp.height = screenHeight;
            }

            // Commit the layout parameters
            surfaceView.setLayoutParams(lp);
        }
        else
        {

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            surfaceView.setLayoutParams(params);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(controller.mCanTouchAgain && event.getAction() == MotionEvent.ACTION_DOWN)
            controller.show();
        return false;
    }

    // Implement SurfaceHolder.Callback
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    	player.setDisplay(holder);

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setVideoSize();


    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        controller.setMediaPlayer(this);
        controller.setAnchorView((RelativeLayout) findViewById(R.id.videoSurfaceContainer));
        setVideoSize();
        controller.SetSubtitles(subtitles);
        controller.SetSources(sources);
        controller.ShowMenuItems();
        txtSubtitle.setText("");
        player.start();
        if(videoTime != -1)
        {
            player.seekTo(videoTime);
            videoTime = -1;
        }
        loadingSpinner.setVisibility(View.GONE);
        imgLoading.setVisibility(View.GONE);
        imgLoading.setImageResource(android.R.color.transparent);
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        try {
            if (!player.isPlaying())
                return 0;

            return player.getCurrentPosition();
        }
        catch(Exception e){
            return 0;
        }


    }

    @Override
    public int getDuration() {
        try {
            if (!player.isPlaying())
                return 0;

            return player.getDuration();
        }
        catch(Exception e){
            return 0;
        }


    }

    @Override
    public boolean isPlaying() {
        try {
            return player.isPlaying();
        }catch(Exception e){}
        return false;
    }

    @Override
    public void pause() {
        try {
        player.pause();
        }catch(Exception e){}
    }

    @Override
    public void seekTo(int i) {
        try {
        player.seekTo(i);
        }catch(Exception e){}
    }

    @Override
    public void start() {
        try {
        player.start();
        }catch(Exception e){}
    }

    @Override
    public boolean isFullScreen() {
        return false;
    }

    @Override
    public void toggleFullScreen() {
        
    }

    @Override
    public int GetCurrentSubtitlePosition() {
        if(currentEpisodeSubtitle == null)
            return -1;

        for(int i = 0; i < subtitles.size(); i++)
        {
            if(subtitles.get(i).getSubtitleId() == currentEpisodeSubtitle.getSubtitleId())
            {
                return i;
            }
        }

        return -1;
    }

    @Override
    public String GetCurrentLanguageId() {
        return currentVideoLanguageId;
    }

    @Override
    public String GetCurrentQuality() {
        return currentVideoQuality;
    }

    @Override
    public void SubtitleSelected(Subtitle subtitle) {

        if(currentEpisodeSubtitle == null || subtitle.getSubtitleId() == 0|| (subtitle.getSubtitleId() != currentEpisodeSubtitle.getSubtitleId())) {
            checkForSubtitle = false;
            txtSubtitle.setText("");
            currentEpisodeSubtitle = subtitle;

            if(subtitle.getSubtitleId() != 0)
                AsyncTaskTools.execute(new SubtitleTask());
        }
    }
    public void ResetMediaPlayer(boolean stopSubtitles)
    {
        if(player != null) {
            player.stop();
            player.release();
        }
        if(stopSubtitles) {
            currentEpisodeSubtitle = null;
            checkForSubtitle = false;
            txtSubtitle.setText("");
        }
        player = new MediaPlayer();
        player.setOnPreparedListener(this);
        videoSurfaceContainer.removeView(surfaceView);
        surfaceView = new SurfaceView(VideoPlayerActivity.this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        surfaceView.setLayoutParams(params);
        videoSurfaceContainer.addView(surfaceView, 0);
        surfaceView.getHolder().addCallback(this);

    }
    @Override
    public void EpisodeSelected(Episode episode) {
        loadingSpinner.setVisibility(View.VISIBLE);
        App.imageLoader.displayImage(ImageUtils.resizeImage(getString(R.string.image_host_path) + episode.getScreenshotHD(), 300), imgLoading);
        imgLoading.setVisibility(View.VISIBLE);
        currentEpisode = episode;

        ResetMediaPlayer(true);

        GetSourcesAndSubs();
    }

    @Override
    public void QualitySelected(String quality) {
        videoTime = player.getCurrentPosition();
        ResetMediaPlayer(false);
        SelectSourceAndPlay(currentVideoLanguageId, quality, String.valueOf(currentEpisodeSubtitle.getLanguageId()));
    }

    @Override
    public void LanguageSelected(Language language) {
        videoTime = player.getCurrentPosition();
        String subtitleLanguage = null;
        if(currentEpisodeSubtitle != null)
        {
            subtitleLanguage = String.valueOf(currentEpisodeSubtitle.getLanguageId());
        }
        ResetMediaPlayer(true);

        if(subtitleLanguage != null)
        {
            String defaultSubtitleLanguage = PrefUtils.get(VideoPlayerActivity.this, Prefs.DEFAULT_VIDEO_SUBTITLE_LANGUAGE, "1");
            if(!subtitleLanguage.equals(defaultSubtitleLanguage))
            {
                Toast.makeText(VideoPlayerActivity.this, getString(R.string.subtitles_reset_default), Toast.LENGTH_LONG).show();
            }

            SelectSourceAndPlay(String.valueOf(language.getLanguageId()), currentVideoQuality, defaultSubtitleLanguage);

        }
        else {
            SelectSourceAndPlay(String.valueOf(language.getLanguageId()), currentVideoQuality, "0");
        }

    }

    private void SelectSourceAndPlay(String language, String quality, String subtitleLanguageId)
    {
        if(sources.size() < 1)
        {
            //What?!
            Toast.makeText(VideoPlayerActivity.this, getString(R.string.error_loading_video), Toast.LENGTH_LONG).show();
            return;
        }
        ArrayList<Source> goodLanguageSources = new ArrayList<Source>();


        //player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        for(Source source:sources)
        {
            if(String.valueOf(source.getLink().getLanguageId()).equals(language))
            {
                goodLanguageSources.add(source);
            }
        }

        //The default language is not found.
        if(!language.equals("3") && goodLanguageSources.size() < 1)
        {
            for(Source source:sources)
            {
                //check if there's any japanese source
                if(String.valueOf(source.getLink().getLanguageId()).equals("3"))
                {
                    goodLanguageSources.add(source);
                }
            }

            if(goodLanguageSources.size() > 0)
            {
                //notify the user that his default language has not been found...
                Toast.makeText(VideoPlayerActivity.this, getString(R.string.default_language_not_available), Toast.LENGTH_LONG).show();
            }
        }

        //The default language and the japanese is not available...
        //grab anything at this point
        //Something wrong probably happened since the only language offered is english and japanese
        if(goodLanguageSources.size() < 1)
        {
            goodLanguageSources = sources;
        }

        Source sourceToPlay = null;
        //Check the quality

        for(Source source:goodLanguageSources)
        {
            if(source.getQuality().equals(quality))
            {
                sourceToPlay = source;
                break;
            }
        }
        //The default quality was not found
        if(sourceToPlay == null)
        {
            for(Source source:goodLanguageSources)
            {
                if(source.getQuality().equals("1080p"))
                {
                    sourceToPlay = source;
                    break;
                }
            }

            if(sourceToPlay == null)
            {
                for(Source source:goodLanguageSources)
                {
                    if(source.getQuality().equals("720p"))
                    {
                        sourceToPlay = source;
                        break;
                    }
                }
            }

            if(sourceToPlay == null)
            {
                for(Source source:goodLanguageSources)
                {
                    if(source.getQuality().equals("360p"))
                    {
                        sourceToPlay = source;
                        break;
                    }
                }
            }

            if(sourceToPlay != null)
            {
                //notify the user that his default quality has not been found...
                Toast.makeText(VideoPlayerActivity.this, getString(R.string.quality_not_available) + " " + sourceToPlay.getQuality() + getString(R.string.has_been_selected), Toast.LENGTH_LONG).show();
            }
            else
            {
                //What?! play anything
                sourceToPlay = sources.get(0);
            }
        }

        //Check the subtitle prefs
        Subtitle subtitleToShow = null;
        if(!subtitleLanguageId.equals("0"))
        {
            for(Subtitle sub:subtitles)
            {
                if(String.valueOf(sub.getLanguageId()).equals(subtitleLanguageId))
                {
                    subtitleToShow = sub;
                    if(subtitleToShow.getSpecification() == null || subtitleToShow.getSpecification().equals(""))
                    {
                        break;//by default we want to one with no specification.
                    }
                }
            }
            //Default subtitle language not found.
            if(subtitleToShow == null)
            {
                //notify the user that his default subtitle language is not available.
                Toast.makeText(VideoPlayerActivity.this, getString(R.string.default_subtitle_not_available), Toast.LENGTH_LONG).show();
            }
            else
            {
                SubtitleSelected(subtitleToShow);
            }

        }
        currentVideoQuality = sourceToPlay.getQuality();
        currentVideoLanguageId = String.valueOf(sourceToPlay.getLink().getLanguageId());

        try {
            player.setDataSource(VideoPlayerActivity.this, Uri.parse(sourceToPlay.getUrl()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        player.prepareAsync();
    }

}
