package com.topanimestream.views;


import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.topanimestream.utilities.AsyncTaskTools;
import com.topanimestream.R;
import com.topanimestream.utilities.Utils;

import butterknife.Bind;

public class VideoActivity extends TASBaseActivity {

    private String mp4Url;
    private int mirrorId;

    @Bind(R.id.videoView)
    VideoView videoView;

    @Bind(R.id.progLoading)
    ProgressBar progLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_video);

        Intent intent = getIntent();
        mirrorId = intent.getIntExtra("MirrorId", 0);
        mp4Url = intent.getStringExtra("Mp4Url");

        Uri mp4Uri = Uri.parse(mp4Url);

        videoView.setVideoURI(mp4Uri);
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
        videoView.start();
        progLoading.setVisibility(View.VISIBLE);

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
