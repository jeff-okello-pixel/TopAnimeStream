package com.topanimestream.views.profile;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;
import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;
import com.topanimestream.App;
import com.topanimestream.R;
import com.topanimestream.adapters.VoteListAdapter;
import com.topanimestream.managers.AnimationManager;
import com.topanimestream.models.CurrentUser;
import com.topanimestream.models.Vote;
import com.topanimestream.utilities.AsyncTaskTools;
import com.topanimestream.utilities.Utils;
import com.topanimestream.utilities.WcfDataServiceUtility;
import com.topanimestream.views.AnimeDetailsActivity;
import com.topanimestream.views.TASBaseActivity;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import butterknife.Bind;

public class MyVotesActivity extends TASBaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    private VoteListAdapter adapter;
    private int currentSkip = 0;
    private int currentLimit = 40;
    private boolean isLoading = false;
    private boolean loadmore = false;
    private boolean hasResults = false;
    private MyVoteTask task;

    @Bind(R.id.listViewMyVotes)
    ListView listViewMyVotes;

    @Bind(R.id.progressBarLoadMore)
    ProgressBar progressBarLoadMore;

    @Bind(R.id.txtNoVotes)
    TextView txtNoVotes;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_my_votes);

        toolbar.setTitle(getString(R.string.my_votes));
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        setSupportActionBar(toolbar);

        listViewMyVotes.setOnItemClickListener(this);
        listViewMyVotes.setOnScrollListener(new AbsListView.OnScrollListener() {

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
                            task = new MyVoteTask();
                            AsyncTaskTools.execute(task);
                        } else if (task == null) {
                            loadmore = false;
                            task = new MyVoteTask();
                            currentSkip = 0;
                            AsyncTaskTools.execute(task);
                        }
                    }
                }
            }
        });

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        AnimationManager.ActivityFinish(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Vote vote = (Vote) listViewMyVotes.getAdapter().getItem(position);
        Intent intent = new Intent(MyVotesActivity.this, AnimeDetailsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("Anime", vote.getAnime());
        intent.putExtras(bundle);
        startActivity(intent);
        AnimationManager.ActivityStart(MyVotesActivity.this);
    }


    public class MyVoteTask extends AsyncTask<Void, Void, String> {
        private String url;
        private ArrayList<Vote> newVotes = new ArrayList<Vote>();
        @Override
        protected void onPreExecute() {
            progressBarLoadMore.setVisibility(View.VISIBLE);
            isLoading = true;
            url = new WcfDataServiceUtility(getString(R.string.odata_path)).getEntity("Votes").formatJson().filter("AccountId%20eq%20" + App.currentUser.getAccountId()).orderby("AddedDate%20desc").skip(currentSkip).top(currentLimit).expand("Anime/Genres,Anime/AnimeInformations,Anime/Status,Anime/AnimeSources").build();
        }

        @Override
        protected String doInBackground(Void... params) {
            hasResults = false;
            if (!App.IsNetworkConnected()) {
                return getString(R.string.error_internet_connection);
            }

            try {
                JSONObject json = Utils.GetJson(url);
                if (!json.isNull("error")) {
                    try {
                        int error = json.getInt("error");
                        if (error == 401) {
                            return "401";
                        }
                    } catch (Exception e) {
                        return null;
                    }
                }
                JSONArray jsonVotes = json.getJSONArray("value");
                if(jsonVotes.length() > 0)
                    hasResults = true;

                Gson gson = new Gson();
                for (int i = 0; i < jsonVotes.length(); i++) {
                    newVotes.add(gson.fromJson(jsonVotes.getJSONObject(i).toString(), Vote.class));
                }
                return null;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return getString(R.string.error_loading_your_votes);
        }

        @Override
        protected void onPostExecute(String error) {
            if (error != null) {
                if (error.equals("401")) {
                    Toast.makeText(MyVotesActivity.this, getString(R.string.have_been_logged_out), Toast.LENGTH_LONG).show();
                    startActivity(new Intent(MyVotesActivity.this, LoginActivity.class));
                    finish();
                } else {
                    Toast.makeText(MyVotesActivity.this, error, Toast.LENGTH_LONG).show();
                }
            } else {
                if (loadmore) {
                    for (Vote vote : newVotes) {
                        adapter.add(vote);
                    }
                    adapter.update();
                } else {
                    adapter = new VoteListAdapter(MyVotesActivity.this, newVotes);
                    SwingBottomInAnimationAdapter swingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(adapter);
                    swingBottomInAnimationAdapter.setAbsListView(listViewMyVotes);
                    assert swingBottomInAnimationAdapter.getViewAnimator() != null;
                    swingBottomInAnimationAdapter.getViewAnimator().setInitialDelayMillis(300);
                    listViewMyVotes.setAdapter(swingBottomInAnimationAdapter);
                }

                isLoading = false;
                progressBarLoadMore.setVisibility(View.GONE);

                if (listViewMyVotes.getAdapter().getCount() > 0)
                    txtNoVotes.setVisibility(View.GONE);
                else
                    txtNoVotes.setVisibility(View.VISIBLE);

            }
        }
    }

}
