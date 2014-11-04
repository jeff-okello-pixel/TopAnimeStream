package com.aniblitz;

import com.aniblitz.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.VideoView;

import com.aniblitz.R;

public class VideoActivity extends Activity {

    //UI controls
    private VideoView videoView;
    private String mp4Url;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_video);
        Intent intent = getIntent();
        mp4Url = intent.getStringExtra("Mp4Url");
        videoView = (VideoView) findViewById(R.id.videoView);
        this.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        Uri mp4Uri = Uri.parse(mp4Url);
        videoView.setVideoURI(mp4Uri);
        videoView.start();

    }




}
