/*
 * Copyright (C) 2013 yixia.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aniblitz;

import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnErrorListener;
import io.vov.vitamio.MediaPlayer.OnTimedTextListener;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


@SuppressLint("NewApi")
public class VideoViewSubtitle extends Activity {

	private String path = null;
	private VideoView mVideoView;
	private long mPosition = 0;
	private RelativeLayout.LayoutParams paramsNotFullscreen;    
	private Handler mHideHandler = new Handler();
	private Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {

			hideSystemUi();

		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
	            WindowManager.LayoutParams.FLAG_FULLSCREEN);
		if (!LibsChecker.checkVitamioLibs(this))
			return;
		setContentView(R.layout.subtitle2);
		hideSystemUi();

		getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(

		new OnSystemUiVisibilityChangeListener() {

		@Override
		public void onSystemUiVisibilityChange(int visibility) {
			if (visibility == 0) 
			{
				mHideHandler.postDelayed(mHideRunnable, 2000);
			}
		}

		});


		Bundle bundle = getIntent().getExtras();
		path = bundle.getString("videoPath");
		if(path == null)
		{
			Toast.makeText(this, "Could not find the video to play", Toast.LENGTH_LONG).show();
			finish();
		}
		mVideoView = (VideoView) findViewById(R.id.surface_view);
		
		mVideoView.setVideoPath(path);

		mVideoView.setMediaController(new MediaController(this));
		mVideoView.requestFocus();
		mVideoView.setOnErrorListener(new OnErrorListener() {
			
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				Builder alert = new AlertDialog.Builder(VideoViewSubtitle.this);
				alert.setTitle("Error");
				alert.setMessage("The video cannot be played, please try another provider.");
				alert.setPositiveButton("OK",new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				});
	
				alert.show();  
				
				return true;
			}
		});
		mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mediaPlayer) {
				// optional need Vitamio 4.0
				mediaPlayer.setPlaybackSpeed(1.0f);


			}
		});

		
	}
	private void hideSystemUi() {
	
	getWindow().getDecorView().setSystemUiVisibility(
	
		View.SYSTEM_UI_FLAG_LAYOUT_STABLE
		
		| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
		
		| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
		
		| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
		 
		| View.SYSTEM_UI_FLAG_FULLSCREEN
		
		| View.SYSTEM_UI_FLAG_IMMERSIVE);
	
	}
       
	@Override
	protected void onPause() {
		mPosition = mVideoView.getCurrentPosition();
		mVideoView.stopPlayback();
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mVideoView.start();
	}

}
