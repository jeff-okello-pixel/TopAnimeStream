package com.topanimestream.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.MenuItem;
import android.view.View;

import com.topanimestream.R;
import com.topanimestream.beaming.BeamManager;
import com.topanimestream.beaming.server.BeamServerService;
import com.topanimestream.models.StreamInfo;

public class BeamPlayerActivity extends TASBaseActivity {
    private BeamPlayerFragment mFragment;
    private BeamManager mBeamManager = BeamManager.getInstance(this);
    private StreamInfo mStreamInfo;
    private Long mResumePosition;
    private String mTitle;

    public static Intent startActivity(Context context, @NonNull StreamInfo info) {
        return startActivity(context, info, 0);
    }

    public static Intent startActivity(Context context, @NonNull StreamInfo info, long resumePosition) {
        Intent i = new Intent(context, BeamPlayerActivity.class);

        if (info == null){
            throw new IllegalArgumentException("StreamInfo must not be null");
        }

        i.putExtra(INFO, info);
        i.putExtra(RESUME_POSITION, resumePosition);
        context.startActivity(i);
        return i;
    }

    public final static String INFO = "stream_info";
    public final static String RESUME_POSITION = "resume_position";

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        super.onCreate(savedInstanceState, R.layout.activity_beamplayer);

        setShowCasting(true);

        BeamServerService.getServer().start();

        mStreamInfo = getIntent().getParcelableExtra(INFO);

        mResumePosition = getIntent().getLongExtra(RESUME_POSITION, 0);

        mTitle = mStreamInfo.getTitle() == null ? "video" : mStreamInfo.getTitle();

        /*
        File subsLocation = new File(SubsProvider.getStorageLocation(context), media.videoId + "-" + subLanguage + ".srt");
        BeamServer.setCurrentSubs(subsLocation);
         */

        mFragment = (BeamPlayerFragment) getSupportFragmentManager().findFragmentById(R.id.beam_fragment);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                showExitDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        showExitDialog();
    }

    private void showExitDialog() {
        //TODO stop video here?
        /*
        OptionDialogFragment.show(getSupportFragmentManager(), getString(R.string.leave_videoplayer_title), String.format(getString(R.string.leave_videoplayer_message), mTitle), getString(android.R.string.yes), getString(android.R.string.no), new OptionDialogFragment.Listener() {
            @Override
            public void onSelectionPositive() {
                mBeamManager.stopVideo();
                BeamServerService.getServer().stop();
                if (mService != null)
                    mService.stopStreaming();
                finish();
            }

            @Override
            public void onSelectionNegative() {
            }
        });*/
    }


    public StreamInfo getInfo() {
        return mStreamInfo;
    }


    public Long getResumePosition() {
        return mResumePosition;
    }

}
