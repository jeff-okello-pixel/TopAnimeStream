package com.topanimestream;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;
import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;
import com.topanimestream.adapters.ReviewListAdapter;
import com.topanimestream.managers.AnimationManager;
import com.topanimestream.models.CurrentUser;
import com.topanimestream.models.Review;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;


public class ReviewsActivity extends ActionBarActivity implements AdapterView.OnItemClickListener {


    private SharedPreferences prefs;
    private int currentSkip = 0;
    private int currentLimit = 40;
    private boolean isLoading = false;
    private boolean loadmore = false;
    private boolean hasResults = false;
    private ListView listViewReviews;
    private ReviewsTask task;
    private ProgressBar progressBarLoadMore;
    private int animeId;
    private ReviewListAdapter adapter;
    private Review currentUserReview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_Blue);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Intent intent = getIntent();
        animeId = intent.getExtras().getInt("animeId");
        currentUserReview = intent.getExtras().getParcelable("currentUserReview");
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(Html.fromHtml("<font color=#f0f0f0>" + getString(R.string.reviews) + "</font>"));
        listViewReviews = (ListView) findViewById(R.id.listViewReviews);
        progressBarLoadMore = (ProgressBar) findViewById(R.id.progressBarLoadMore);

        listViewReviews.setFastScrollEnabled(true);
        listViewReviews.setOnItemClickListener(this);
        listViewReviews.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                if (App.IsNetworkConnected()) {
                    int lastInScreen = firstVisibleItem + visibleItemCount;

                    if ((lastInScreen >= totalItemCount - 6) && !(isLoading)) {
                        if (hasResults) {
                            currentSkip += currentLimit;
                            loadmore = true;
                            task = new ReviewsTask();
                            AsyncTaskTools.execute(task);
                        } else if (task == null) {
                            loadmore = false;
                            task = new ReviewsTask();
                            currentSkip = 0;
                            AsyncTaskTools.execute(task);
                        }
                    }
                }
            }
        });

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
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        
    }

    public class ReviewsTask extends AsyncTask<Void, Void, String> {
        private Dialog busyDialog;
        private String url;
        private ArrayList<Review> newReviews = new ArrayList<Review>();
        @Override
        protected void onPreExecute() {
            busyDialog = Utils.showBusyDialog(getString(R.string.loading_reviews), ReviewsActivity.this);
            progressBarLoadMore.setVisibility(View.VISIBLE);
            isLoading = true;
            url = new WcfDataServiceUtility(getString(R.string.anime_data_service_path)).getEntity("Reviews").filter("AnimeId%20eq%20" + animeId + "%20and%20AccountId%20ne%20" + CurrentUser.AccountId).expand("Account").skip(currentSkip).top(currentLimit).formatJson().build();
        }


        @Override
        protected String doInBackground(Void... params) {
            try {
                JSONObject json = Utils.GetJson(url);
                String errors = Utils.checkDataServiceErrors(json, getString(R.string.error_loading_reviews));
                if (errors != null)
                    return errors;

                JSONArray jsonReviews = json.getJSONArray("value");
                Gson gson = new Gson();
                for(int i = 0; i < jsonReviews.length(); i++)
                {
                    newReviews.add(gson.fromJson(jsonReviews.getJSONObject(i).toString(), Review.class));
                }

                return null;
            }
            catch(Exception e)
            {
                return getString(R.string.error_loading_reviews);

            }
        }

        @Override
        protected void onPostExecute(String error) {
            try {
                if (error != null) {
                    if (error.equals("401")) {
                        Toast.makeText(ReviewsActivity.this, getString(R.string.have_been_logged_out), Toast.LENGTH_LONG).show();
                        ReviewsActivity.this.startActivity(new Intent(ReviewsActivity.this, LoginActivity.class));
                        ReviewsActivity.this.finish();
                    } else {
                        Toast.makeText(ReviewsActivity.this, error, Toast.LENGTH_LONG).show();
                    }
                }
                else
                {
                    if (loadmore) {
                        for (Review review : newReviews) {
                            adapter.add(review);
                        }
                        adapter.update();
                    } else {
                        adapter = new ReviewListAdapter(ReviewsActivity.this, new ArrayList<Review>());
                        //create the separators / empty reviews
                        Review yourReviewSeparator = Review.CreateReviewSeparator(getString(R.string.your_review));
                        Review otherReviewSeparator = Review.CreateReviewSeparator(getString(R.string.other_reviews));

                        if(currentUserReview == null)
                            currentUserReview = Review.CreateReviewSeparator(getString(R.string.no_review_yet));

                        Review noOtherReview = null;
                        if(newReviews.size() < 1)
                            noOtherReview = Review.CreateReviewSeparator(getString(R.string.nobody_reviewed_yet));

                        adapter.addSeparatorItem(yourReviewSeparator);

                        if(currentUserReview.getSeparatorTitle() != null && !currentUserReview.getSeparatorTitle().equals(""))
                            adapter.addAddReviewItem(currentUserReview);
                        else
                            adapter.add(currentUserReview);

                        adapter.addSeparatorItem(otherReviewSeparator);

                        //add all reparator and empty reviews
                        if(noOtherReview != null)
                            adapter.addNoReviewItem(noOtherReview);

                        for(int i = 0; i < newReviews.size(); i++)
                            adapter.add(newReviews.get(i));

                        adapter.update();

                        SwingBottomInAnimationAdapter swingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(adapter);
                        swingBottomInAnimationAdapter.setAbsListView(listViewReviews);
                        assert swingBottomInAnimationAdapter.getViewAnimator() != null;
                        swingBottomInAnimationAdapter.getViewAnimator().setInitialDelayMillis(300);
                        listViewReviews.setAdapter(swingBottomInAnimationAdapter);
                    }

                    isLoading = false;


                }
                progressBarLoadMore.setVisibility(View.GONE);

            } catch (Exception e)//catch all exception, handle orientation change
            {
                e.printStackTrace();
            }

            Utils.dismissBusyDialog(busyDialog);
        }

    }

}
