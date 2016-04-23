package com.topanimestream.views;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.topanimestream.R;
import com.topanimestream.beaming.BeamManager;
import com.topanimestream.dialogfragments.BeamDeviceSelectorDialogFragment;

import butterknife.ButterKnife;

public class TASBaseActivity extends AppCompatActivity implements BeamManager.BeamListener {

    protected Boolean mShowCasting = false;

    protected void onCreate(Bundle savedInstanceState, int layoutId) {
        super.onCreate(savedInstanceState);
        setContentView(layoutId);

        ButterKnife.bind(this);

    }

    protected void onHomePressed() {
        Intent upIntent = NavUtils.getParentActivityIntent(this);
        if (upIntent != null && NavUtils.shouldUpRecreateTask(this, upIntent)) {
            // This activity is NOT part of this app's task, so create a new task
            // when navigating up, with a synthesized back stack.
            TaskStackBuilder.create(this)
                    // Add all of this activity's parents to the back stack
                    .addNextIntentWithParentStack(upIntent)
                            // Navigate up to the closest parent
                    .startActivities();
        } else {
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.activity_base, menu);

        BeamManager beamManager = BeamManager.getInstance(this);
        Boolean castingVisible = mShowCasting && beamManager.hasCastDevices();
        MenuItem item = menu.findItem(R.id.action_casting);
        item.setVisible(castingVisible);
        item.setIcon(beamManager.isConnected() ? R.drawable.ic_av_beam_connected : R.drawable.ic_av_beam_disconnected);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onHomePressed();
                return true;
            case R.id.action_casting:
                BeamDeviceSelectorDialogFragment.show(getFragmentManager());
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        BeamManager.getInstance(this).addListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        BeamManager.getInstance(this).removeListener(this);
    }


    @Override
    public void updateBeamIcon() {
        supportInvalidateOptionsMenu();
    }

    public void setShowCasting(boolean b) {
        mShowCasting = b;
    }
}
