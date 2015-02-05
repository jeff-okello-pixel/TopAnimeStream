package com.topanimestream;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.view.MenuItem;
import android.widget.Toast;

import com.topanimestream.managers.AnimationManager;


public class ReviewsActivity extends ActionBarActivity {


    private SharedPreferences prefs;
    private int currentSkip = 0;
    private int currentLimit = 100;
    private boolean isLoading = false;
    private boolean loadmore = false;
    private boolean hasResults = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_Blue);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(Html.fromHtml("<font color=#f0f0f0>" + getString(R.string.reviews) + "</font>"));

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                AnimationManager.ActivityFinish(this);
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        AnimationManager.ActivityFinish(this);
    }

    public class ReviewsTask extends AsyncTask<Void, Void, String> {
        private Dialog busyDialog;

        @Override
        protected void onPreExecute() {
            busyDialog = Utils.showBusyDialog(getString(R.string.loading_reviews), ReviewsActivity.this);

        }


        @Override
        protected String doInBackground(Void... params) {


            return "Success";
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                if (result == null) {
                    Toast.makeText(ReviewsActivity.this, getString(R.string.error_loading_reviews), Toast.LENGTH_LONG).show();
                } else {

                }

                Utils.dismissBusyDialog(busyDialog);
            } catch (Exception e)//catch all exception, handle orientation change
            {
                e.printStackTrace();
            }


        }

    }

}
