package com.topanimestream.views;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

import com.topanimestream.App;
import com.topanimestream.R;
import com.topanimestream.custom.StrokedRobotoTextView;
import com.topanimestream.models.subs.Caption;
import com.topanimestream.models.subs.FormatASS;
import com.topanimestream.models.subs.FormatSRT;
import com.topanimestream.models.subs.TimedTextObject;
import com.topanimestream.utilities.AsyncTaskTools;
import com.topanimestream.utilities.FileUtils;
import com.topanimestream.utilities.StorageUtils;
import com.topanimestream.utilities.Utils;

public class VideoPlayerActivity extends Activity implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener, VideoControllerView.VideoControllerCallback {

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
    Thread threadCheckSubs = new Thread() {
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
        }
    };
    public static File getStorageLocation(Context context) {
        return new File(StorageUtils.getIdealCacheDirectory(context).toString() + "/subs/");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_Blue);
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_video_player);

        txtSubtitle = (StrokedRobotoTextView)findViewById(R.id.txtSubtitle);
        txtSubtitle.setTextColor(Color.WHITE);
        txtSubtitle.setTextSize(16);
        txtSubtitle.setStrokeColor(Color.BLACK);
        txtSubtitle.setStrokeWidth(2,2);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mDisplayHandler = new Handler(Looper.getMainLooper());
        surfaceView = (SurfaceView) findViewById(R.id.videoSurface);
        SurfaceHolder videoHolder = surfaceView.getHolder();
        videoHolder.addCallback(this);

        player = new MediaPlayer();
        controller = new VideoControllerView(this, true);

        
        try {
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setDataSource(this, Uri.parse("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4"));
            player.setOnPreparedListener(this);
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
    private long getCurrentTime()
    {
        return player.getCurrentPosition();
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
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    FileInputStream fileInputStream = new FileInputStream(mSubsFile);
                    FormatSRT formatSRT = new FormatSRT();
                    mSubs = formatSRT.parseFile(mSubsFile.toString(), FileUtils.inputstreamToCharsetString(fileInputStream).split("\n"));
                    checkForSubtitle = true;
                    threadCheckSubs.start();

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
        }.execute();
    }
    private class SubtitleTask extends AsyncTask<Void, Void, String> {


            public SubtitleTask() {

            }
            private static final String NAMESPACE = "http://tempuri.org/";
            final String SOAP_ACTION = "http://tempuri.org/IAnimeService/";
            private String subUrl;

            @Override
            protected void onPreExecute() {
                subUrl = getString(R.string.sub_host_path) + "10/5_Centimeters_Per_Second_en.srt";
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
                    //final String fileName = media.videoId + "-" + languageCode;
                    //TODO choose a unique name per subtitle
                    final String fileName = "subtitle";
                    final File srtPath = new File(subsDirectory, fileName + ".srt");

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
                    } else if (subUrl.contains(".srt")) {
                        FormatSRT formatSRT = new FormatSRT();
                        subtitleObject = formatSRT.parseFile(subUrl, inputText);
                    }

                    if (subtitleObject != null) {
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
                    //TODO choose another srt name
                    mSubsFile = new File(getStorageLocation(VideoPlayerActivity.this), "subtitle.srt");
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
        player.prepareAsync();
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
        player.start();
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
        return player.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        return player.getDuration();
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
    public void SubtitleSelected() {
        AsyncTaskTools.execute(new SubtitleTask());
    }

}
