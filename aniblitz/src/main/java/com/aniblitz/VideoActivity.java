package com.aniblitz;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import com.aniblitz.R;
import com.aniblitz.models.Mirror;

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
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            progLoading.setVisibility(View.GONE);
        }
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
                mp.start();
                mp.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                    @Override
                    public void onVideoSizeChanged(MediaPlayer mp, int arg1,
                                                   int arg2) {
                        progLoading.setVisibility(View.GONE);
                        mp.start();
                    }
                });

            }
        });

    }




}
