package com.topanimestream.views;


import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.topanimestream.Utilities.AsyncTaskTools;
import com.topanimestream.R;
import com.topanimestream.Utilities.Utils;

public class VideoActivity extends Activity {

    //UI controls
    private VideoView videoView;
    private String mp4Url;
    private ProgressBar progLoading;
    private int mirrorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_video);
        Intent intent = getIntent();
        mirrorId = intent.getIntExtra("MirrorId", 0);
        mp4Url = intent.getStringExtra("Mp4Url");
        videoView = (VideoView) findViewById(R.id.videoView);
        progLoading = (ProgressBar) findViewById(R.id.progLoading);

        Uri mp4Uri = Uri.parse(mp4Url);

        videoView.setVideoURI(mp4Uri);
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
        videoView.start();
        progLoading.setVisibility(View.VISIBLE);
        /*
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            progLoading.setVisibility(View.GONE);
        }*/

        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i2) {
                AsyncTaskTools.execute(new Utils.ReportMirror(mirrorId, VideoActivity.this));
                return false;
            }
        });
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                progLoading.setVisibility(View.GONE);

            }
        });

    }


}
