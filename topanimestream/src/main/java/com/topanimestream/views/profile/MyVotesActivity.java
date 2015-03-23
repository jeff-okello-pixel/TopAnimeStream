package com.topanimestream.views.profile;

import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.topanimestream.App;
import com.topanimestream.R;
import com.topanimestream.adapters.VoteListAdapter;
import com.topanimestream.managers.AnimationManager;
import com.topanimestream.managers.DialogManager;
import com.topanimestream.models.Anime;
import com.topanimestream.models.CurrentUser;
import com.topanimestream.models.Vote;
import com.topanimestream.utilities.Utils;
import com.topanimestream.utilities.WcfDataServiceUtility;
import com.topanimestream.views.AnimeDetailsActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class MyVotesActivity extends ActionBarActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    private ListView listViewMyVotes;
    private ArrayList<Vote> votes;
    private TextView txtNoVotes;
    private VoteListAdapter adapter;
    public MyVotesActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_Blue);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_votes);
        listViewMyVotes = (ListView) findViewById(R.id.listViewMyVotes);
        txtNoVotes = (TextView) findViewById(R.id.txtNoVotes);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setTitle(Html.fromHtml("<font color=#f0f0f0>" + getString(R.string.my_votes) + "</font>"));

        listViewMyVotes.setOnItemClickListener(this);

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
        int animeId = vote.getAnimeId();

        Intent intent = new Intent(MyVotesActivity.this, AnimeDetailsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("Anime", vote.getAnime());
        intent.putExtras(bundle);
        startActivity(intent);
        AnimationManager.ActivityStart(MyVotesActivity.this);
    }


    public class MyVoteTask extends AsyncTask<Void, Void, String> {
        private Dialog busyDialog;
        private String url;
        @Override
        protected void onPreExecute() {
            busyDialog = DialogManager.showBusyDialog(getString(R.string.loading_your_votes), MyVotesActivity.this);
            votes = new ArrayList<Vote>();
            url = new WcfDataServiceUtility(getString(R.string.anime_data_service_path)).getEntity("Votes").formatJson().filter("AccountId%20eq%20" + CurrentUser.AccountId).orderby("AddedDate%20desc").build();
        }

        @Override
        protected String doInBackground(Void... params) {
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
                Gson gson = new Gson();
                for (int i = 0; i < jsonVotes.length(); i++) {
                    votes.add(gson.fromJson(jsonVotes.toString(), Vote.class));
                }
                return null;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return getString(R.string.error_loading_your_votes);
        }

        @Override
        protected void onPostExecute(String error) {
            DialogManager.dismissBusyDialog(busyDialog);
            if (error != null) {
                if (error.equals("401")) {
                    Toast.makeText(MyVotesActivity.this, getString(R.string.have_been_logged_out), Toast.LENGTH_LONG).show();
                    startActivity(new Intent(MyVotesActivity.this, LoginActivity.class));
                    finish();
                } else {
                    Toast.makeText(MyVotesActivity.this, error, Toast.LENGTH_LONG).show();
                }
            } else {
                adapter = new VoteListAdapter(MyVotesActivity.this, votes);
                SwingBottomInAnimationAdapter swingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(adapter);
                swingBottomInAnimationAdapter.setAbsListView(listViewMyVotes);
                assert swingBottomInAnimationAdapter.getViewAnimator() != null;
                swingBottomInAnimationAdapter.getViewAnimator().setInitialDelayMillis(300);
                listViewMyVotes.setAdapter(swingBottomInAnimationAdapter);
                if (votes.size() > 0)
                    txtNoVotes.setVisibility(View.GONE);
                else
                    txtNoVotes.setVisibility(View.VISIBLE);


            }


        }

    }


}
