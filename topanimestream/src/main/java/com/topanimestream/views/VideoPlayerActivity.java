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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.gson.Gson;
import com.topanimestream.App;
import com.topanimestream.R;
import com.topanimestream.custom.StrokedRobotoTextView;
import com.topanimestream.models.Anime;
import com.topanimestream.models.Episode;
import com.topanimestream.models.Language;
import com.topanimestream.models.Source;
import com.topanimestream.models.Subtitle;
import com.topanimestream.models.subs.Caption;
import com.topanimestream.models.subs.FormatASS;
import com.topanimestream.models.subs.FormatSRT;
import com.topanimestream.models.subs.TimedTextObject;
import com.topanimestream.utilities.AsyncTaskTools;
import com.topanimestream.utilities.FileUtils;
import com.topanimestream.utilities.StorageUtils;
import com.topanimestream.utilities.Utils;
import com.topanimestream.utilities.WcfDataServiceUtility;

import org.json.JSONArray;
import org.json.JSONObject;

public class VideoPlayerActivity extends Activity implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener, VideoControllerView.VideoControllerCallback {
    private RelativeLayout videoSurfaceContainer;
    SurfaceView surfaceView;
    MediaPlayer player;
    VideoControllerView controller;
    private int mVideoWidth;
    private int mVideoHeight;
    private TimedTextObject mSubs;
    private Handler mDisplayHandler;
    private Caption mLastSub = null;
    private StrokedRobotoTextView txtSubtitle;
    private File mSubsFile;
    private boolean checkForSubtitle;
    private ProgressBar loadingSpinner;
    private Anime anime;
    private Episode currentEpisode;
    private Subtitle currentEpisodeSubtitle;
    private ArrayList<Subtitle> subtitles = new ArrayList<Subtitle>();
    private ArrayList<Source> sources = new ArrayList<Source>();
    public static File getStorageLocation(Context context) {
        return new File(StorageUtils.getIdealCacheDirectory(context).toString() + "/subs/");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_Blue);
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_video_player);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        videoSurfaceContainer = (RelativeLayout)findViewById(R.id.videoSurfaceContainer);
        loadingSpinner = (ProgressBar) findViewById(R.id.loadingSpinner);
        //TODO check prefs style
        txtSubtitle = (StrokedRobotoTextView)findViewById(R.id.txtSubtitle);
        txtSubtitle.setTextColor(Color.WHITE);
        txtSubtitle.setTextSize(16);
        txtSubtitle.setStrokeColor(Color.BLACK);
        txtSubtitle.setStrokeWidth(2, 2);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mDisplayHandler = new Handler(Looper.getMainLooper());
        surfaceView = (SurfaceView) findViewById(R.id.videoSurface);
        surfaceView.getHolder().addCallback(this);

        player = new MediaPlayer();
        player.setOnPreparedListener(this);
        Bundle bundle = getIntent().getExtras();
        anime = bundle.getParcelable("anime");
        currentEpisode = bundle.getParcelable("episodeToPlay");
        controller = new VideoControllerView(VideoPlayerActivity.this, true, anime.getEpisodes(), currentEpisode);
        AsyncTaskTools.execute(new GetSourcesAndSubsTask());
    }
    private long getCurrentTime()
    {
        try {
            return player.getCurrentPosition();
        }catch(IllegalStateException e){
            e.printStackTrace();
        }

        return 0;
    }
    protected void checkSubs() {
        if(mSubs != null) {
            Collection<Caption> subtitles = mSubs.captions.values();
            double currentTime = getCurrentTime();
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
    private class GetSourcesAndSubsTask extends AsyncTask<Void, Void, String> {

        public GetSourcesAndSubsTask() {

        }
        private String getSourcesUrl;
        private String getSubsUrl;
        @Override
        protected void onPreExecute() {
            sources = new ArrayList<Source>();
            subtitles = new ArrayList<Subtitle>();
            if(!anime.isMovie()) {
                getSourcesUrl = new WcfDataServiceUtility(getString(R.string.anime_data_service_path)).getEntity("GetSources").queryString("animeId", String.valueOf(anime.getAnimeId())).queryString("episodeId", String.valueOf(currentEpisode.getEpisodeId())).expand("Link/Language").formatJson().build();
                getSubsUrl = new WcfDataServiceUtility(getString(R.string.anime_data_service_path)).getEntity("Subtitles").filter("AnimeId%20eq%20" + anime.getAnimeId() + "%20and%20EpisodeId%20eq%20" + currentEpisode.getEpisodeId()).expand("Language").formatJson().build();
            }
            else {
                getSourcesUrl = new WcfDataServiceUtility(getString(R.string.anime_data_service_path)).getEntity("GetSources").queryString("animeId", String.valueOf(anime.getAnimeId())).expand("Link/Language").formatJson().build();
                getSubsUrl = new WcfDataServiceUtility(getString(R.string.anime_data_service_path)).getEntity("Subtitles").filter("AnimeId%20eq%20" + anime.getAnimeId()).expand("Language").formatJson().build();
            }
        }

        @Override
        protected String doInBackground(Void... params) {
            if(!App.IsNetworkConnected())
            {
                return getString(R.string.error_internet_connection);
            }

            try
            {
                Gson gson = new Gson();
                JSONObject jsonSources = Utils.GetJson(getSourcesUrl);
                JSONArray sourceArray = jsonSources.getJSONArray("value");
                for(int i = 0; i < sourceArray.length(); i++)
                {
                    sources.add(gson.fromJson(sourceArray.getJSONObject(i).toString(), Source.class));

                }

                JSONObject jsonSubtitles = Utils.GetJson(getSubsUrl);
                JSONArray subtitleArray = jsonSubtitles.getJSONArray("value");
                for(int i = 0; i < subtitleArray.length(); i++)
                {
                    subtitles.add(gson.fromJson(subtitleArray.getJSONObject(i).toString(), Subtitle.class));

                }

                return null;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return getString(R.string.error_loading_sources);
        }

        @Override
        protected void onPostExecute(String error) {
            if(error != null)
            {
                Toast.makeText(VideoPlayerActivity.this, error, Toast.LENGTH_LONG).show();
            }
            else
            {
                //TODO check prefs and play video
                //episodeToPlay will be null if it is a movie
                try {
                    //player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    player.setDataSource(VideoPlayerActivity.this, Uri.parse(sources.get(0).getUrl()));
                    player.prepareAsync();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (SecurityException e) {
                    e.printStackTrace();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
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
                        fileName = anime.getName() + "-" + currentEpisode.getEpisodeNumber() + "-" + currentEpisodeSubtitle.getLanguage().getISO639();
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
    // End SurfaceHolder.Callback

    // Implement MediaPlayer.OnPreparedListener
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
        loadingSpinner.setVisibility(View.GONE);
    }
    // End MediaPlayer.OnPreparedListener

    // Implement VideoMediaController.MediaPlayerControl
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
        catch(IllegalStateException e){
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
        catch(IllegalStateException e){
            return 0;
        }


    }

    @Override
    public boolean isPlaying() {
        return player.isPlaying();
    }

    @Override
    public void pause() {
        player.pause();
    }

    @Override
    public void seekTo(int i) {
        player.seekTo(i);
    }

    @Override
    public void start() {
        player.start();
    }

    @Override
    public boolean isFullScreen() {
        return false;
    }

    @Override
    public void toggleFullScreen() {
        
    }

    @Override
    public void SubtitleSelected(Subtitle subtitle) {

        if(currentEpisodeSubtitle == null || (subtitle.getSubtitleId() != currentEpisodeSubtitle.getSubtitleId())) {
            checkForSubtitle = false;
            txtSubtitle.setText("");
            currentEpisodeSubtitle = subtitle;
            AsyncTaskTools.execute(new SubtitleTask());
        }else{
            Toast.makeText(VideoPlayerActivity.this,getString(R.string.already_have) + currentEpisodeSubtitle.getLanguage().getName() + " " + getString(R.string.subtitles).toLowerCase(), Toast.LENGTH_SHORT).show();
        }
    }
    public void ResetMediaPlayer()
    {
        if(player != null) {
            player.stop();
            player.release();
        }
        currentEpisodeSubtitle = null;
        checkForSubtitle = false;
        txtSubtitle.setText("");
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
        currentEpisode = episode;

        ResetMediaPlayer();

        AsyncTaskTools.execute(new GetSourcesAndSubsTask());
    }
    public void ChangeVideoSource(Source source)
    {
        ResetMediaPlayer();
        try {
            player.setDataSource(VideoPlayerActivity.this, Uri.parse(source.getUrl()));
            player.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void QualitySelected(Source source) {
        ChangeVideoSource(source);
    }

    @Override
    public void LanguageSelected(Language language) {
        for(Source source:sources)
        {
            if(source.getLink().getLanguage().getLanguageId() == language.getLanguageId())
            {
                ChangeVideoSource(source);
                break;
            }
        }


    }

}
