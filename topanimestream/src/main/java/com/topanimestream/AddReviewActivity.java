package com.topanimestream;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.view.DragEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.topanimestream.managers.AnimationManager;


public class AddReviewActivity extends ActionBarActivity implements View.OnTouchListener {


    private SharedPreferences prefs;
    private int animeId;
    private TextView lblArtRating;
    private TextView lblCharacterRating;
    private TextView lblStoryRating;
    private TextView lblSoundRating;
    private TextView lblEnjoymentRating;

    private RatingBar rtbArtRating;
    private RatingBar rtbCharacterRating;
    private RatingBar rtbStoryRating;
    private RatingBar rtbSoundRating;
    private RatingBar rtbEnjoymentRating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_Blue);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_review);
        lblArtRating = (TextView) findViewById(R.id.lblArtRating);
        lblCharacterRating = (TextView) findViewById(R.id.lblCharacterRating);
        lblStoryRating = (TextView) findViewById(R.id.lblStoryRating);
        lblSoundRating = (TextView) findViewById(R.id.lblSoundRating);
        lblEnjoymentRating = (TextView) findViewById(R.id.lblEnjoymentRating);

        rtbArtRating = (RatingBar) findViewById(R.id.rtbArtRating);
        rtbCharacterRating = (RatingBar) findViewById(R.id.rtbCharacterRating);
        rtbStoryRating = (RatingBar) findViewById(R.id.rtbStoryRating);
        rtbSoundRating = (RatingBar) findViewById(R.id.rtbSoundRating);
        rtbEnjoymentRating = (RatingBar) findViewById(R.id.rtbEnjoymentRating);

        rtbArtRating.setOnTouchListener(this);
        rtbCharacterRating.setOnTouchListener(this);
        rtbStoryRating.setOnTouchListener(this);
        rtbSoundRating.setOnTouchListener(this);
        rtbEnjoymentRating.setOnTouchListener(this);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Intent intent = getIntent();
        animeId = intent.getExtras().getInt("animeId");

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(Html.fromHtml("<font color=#f0f0f0>" + getString(R.string.add_review) + "</font>"));

    }
    public static String floatToStringWithoutDot(float d) {
        int i = (int) d;
        return d == i ? String.valueOf(i) : String.valueOf(d);
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

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        RatingBar ratingBar = (RatingBar)view;
        switch(ratingBar.getId())
        {
            case R.id.rtbArtRating:
                lblArtRating.setText(floatToStringWithoutDot(rtbArtRating.getRating()) + "/5");
                break;
            case R.id.rtbCharacterRating:
                lblCharacterRating.setText(floatToStringWithoutDot(rtbCharacterRating.getRating()) + "/5");
                break;
            case R.id.rtbStoryRating:
                lblStoryRating.setText(floatToStringWithoutDot(rtbStoryRating.getRating()) + "/5");
                break;
            case R.id.rtbSoundRating:
                lblSoundRating.setText(floatToStringWithoutDot(rtbSoundRating.getRating()) + "/5");
                break;
            case R.id.rtbEnjoymentRating:
                lblEnjoymentRating.setText(floatToStringWithoutDot(rtbEnjoymentRating.getRating()) + "/5");
                break;
        }

        return false;
    }
}
