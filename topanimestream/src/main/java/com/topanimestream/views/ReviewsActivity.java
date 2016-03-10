package com.topanimestream.views;
import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;
import com.topanimestream.App;
import com.topanimestream.managers.DialogManager;
import com.topanimestream.utilities.AsyncTaskTools;
import com.topanimestream.R;
import com.topanimestream.utilities.Utils;
import com.topanimestream.utilities.WcfDataServiceUtility;
import com.topanimestream.adapters.ReviewListAdapter;
import com.topanimestream.managers.AnimationManager;
import com.topanimestream.models.CurrentUser;
import com.topanimestream.models.Review;
import com.topanimestream.views.profile.LoginActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.Bind;


public class ReviewsActivity extends TASBaseActivity implements AdapterView.OnItemClickListener {

    private int currentSkip = 0;
    private int currentLimit = 40;
    private boolean isLoading = false;
    private boolean loadmore = false;
    private boolean hasResults = false;
    private ReviewsTask task;
    private int animeId;
    private ReviewListAdapter adapter;
    private Review currentUserReview;
    private final int ADD_REVIEW = 0;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.listViewReviews)
    ListView listViewReviews;

    @Bind(R.id.progressBarLoadMore)
    ProgressBar progressBarLoadMore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_reviews);
        Intent intent = getIntent();
        animeId = intent.getExtras().getInt("animeId");
        currentUserReview = intent.getExtras().getParcelable("currentUserReview");

        toolbar.setTitle(getString(R.string.reviews));
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        setSupportActionBar(toolbar);

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
                            task = new ReviewsTask(false);
                            AsyncTaskTools.execute(task);
                        } else if (task == null) {
                            loadmore = false;
                            task = new ReviewsTask(false);
                            currentSkip = 0;
                            AsyncTaskTools.execute(task);
                        }
                    }
                }
            }
        });

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        AnimationManager.ActivityFinish(this);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == ADD_REVIEW) {
            if(data != null) {
                int reviewId = data.getIntExtra("reviewId", 0);
                if (reviewId > 0) {
                    loadmore = false;
                    task = new ReviewsTask(true);
                    currentSkip = 0;
                    AsyncTaskTools.execute(task);

                }
            }
        }

    }
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        Review review = (Review) adapterView.getItemAtPosition(position);
        if(review.getAccountId() == App.currentUser.getAccountId())
        {
            Intent intent = new Intent(ReviewsActivity.this, ManageReviewActivity.class);
            intent.putExtra("animeId", animeId);
            intent.putExtra("currentUserReview", currentUserReview);
            startActivityForResult(intent, ADD_REVIEW);
        }
        else if(review.getSeparatorTitle() != null && review.getSeparatorTitle().equals(getString(R.string.no_review_yet)))
        {
            Intent intent = new Intent(ReviewsActivity.this, ManageReviewActivity.class);
            intent.putExtra("animeId", animeId);
            startActivityForResult(intent, ADD_REVIEW);
        }
    }

    public class ReviewsTask extends AsyncTask<Void, Void, String> {
        private Dialog busyDialog;
        private String url;
        private String userReviewUrl;
        private boolean loadUserReview = false;
        private ArrayList<Review> newReviews = new ArrayList<Review>();
        public ReviewsTask(boolean loadUserReview)
        {
            this.loadUserReview = loadUserReview;
        }
        @Override
        protected void onPreExecute() {
            busyDialog = DialogManager.showBusyDialog(getString(R.string.loading_reviews), ReviewsActivity.this);
            progressBarLoadMore.setVisibility(View.VISIBLE);
            isLoading = true;
            url = new WcfDataServiceUtility(getString(R.string.odata_path)).getEntity("Reviews").filter("AnimeId%20eq%20" + animeId + "%20and%20AccountId%20ne%20" + App.currentUser.getAccountId()).expand("Account").skip(currentSkip).top(currentLimit).formatJson().build();
            userReviewUrl = new WcfDataServiceUtility(getString(R.string.odata_path)).getEntity("Reviews").formatJson().filter("AccountId%20eq%20" + App.currentUser.getAccountId() + "%20and%20AnimeId%20eq%20" + animeId).expand("Account").build();
        }


        @Override
        protected String doInBackground(Void... params) {
            try {
                JSONObject json = Utils.GetJson(url);
                String errors = Utils.checkDataServiceErrors(json, getString(R.string.error_loading_reviews));
                if (errors != null)
                    return errors;
                Gson gson = new Gson();
                if(loadUserReview) {
                    JSONObject jsonUserReview = Utils.GetJson(userReviewUrl);
                    errors = Utils.checkDataServiceErrors(jsonUserReview, getString(R.string.error_loading_anime_details));
                    if (errors != null)
                        return errors;
                    if (jsonUserReview.getJSONArray("value").length() > 0) {
                        currentUserReview = gson.fromJson(jsonUserReview.getJSONArray("value").getJSONObject(0).toString(), Review.class);
                        //AnimeDetailsActivity.currentUserReview = currentUserReview;
                    }
                    else
                    {
                        //AnimeDetailsActivity.currentUserReview = null;
                        currentUserReview = null;
                    }

                }

                JSONArray jsonReviews = json.getJSONArray("value");

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
                        if(newReviews.size() < 1 && currentUserReview == null)
                            noOtherReview = Review.CreateReviewSeparator(getString(R.string.nobody_reviewed_yet));
                        else if(newReviews.size() < 1)
                            noOtherReview = Review.CreateReviewSeparator(getString(R.string.no_other_review));

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
                        listViewReviews.setAdapter(adapter);
                    }

                    isLoading = false;


                }
                progressBarLoadMore.setVisibility(View.GONE);

            } catch (Exception e)//catch all exception, handle orientation change
            {
                e.printStackTrace();
            }

            DialogManager.dismissBusyDialog(busyDialog);
        }

    }

}
