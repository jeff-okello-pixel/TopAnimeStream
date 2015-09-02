package com.topanimestream.views;

import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.topanimestream.App;
import com.topanimestream.R;
import com.topanimestream.adapters.AnimeGridAdapter;
import com.topanimestream.adapters.LatestEpisodesAdapter;
import com.topanimestream.adapters.LatestEpisodesGridAdapter;
import com.topanimestream.managers.AnimationManager;
import com.topanimestream.managers.DialogManager;
import com.topanimestream.models.Anime;
import com.topanimestream.models.Episode;
import com.topanimestream.models.Link;
import com.topanimestream.utilities.AsyncTaskTools;
import com.topanimestream.utilities.Utils;
import com.topanimestream.utilities.WcfDataServiceUtility;
import com.topanimestream.views.profile.LoginActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class LatestUpdatesFragment extends Fragment {

    public int currentSkip = 0;
    public int currentLimit = 40;
    public boolean isLoading = false;
    public boolean loadmore = false;
    public boolean hasResults = false;
    public Dialog busyDialog;
    private GridLayoutManager mLayoutManager;
    private Integer mColumns = 2;
    private LatestEpisodesTask task;
    private LatestEpisodesGridAdapter mAdapter;
    private ArrayList<Link> mItems = new ArrayList<>();
    private int mFirstVisibleItem, mVisibleItemCount, mTotalItemCount = 0, mLoadingTreshold = mColumns * 3;

    @Bind(R.id.recyclerView)
    RecyclerView mRecyclerView;

    @Bind(R.id.progressBarLoading)
    ProgressBar progressBarLoading;

    @Bind(R.id.txtNoUpdates)
    TextView txtNoUpdates;

    public LatestUpdatesFragment() {

    }

    public static LatestUpdatesFragment newInstance() {
        LatestUpdatesFragment ttFrag = new LatestUpdatesFragment();
        return ttFrag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadmore = false;
        currentSkip = 0;
        AsyncTaskTools.execute(new LatestEpisodesTask());
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setOnScrollListener(mScrollListener);
        //adapter should only ever be created once on fragment initialise.
        mAdapter = new LatestEpisodesGridAdapter(getActivity(), mItems, mColumns);
        mAdapter.setOnItemClickListener(mOnItemClickListener);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        mAdapter.setOnItemClickListener(mOnItemClickListener);
    }
    private LatestEpisodesGridAdapter.OnItemClickListener mOnItemClickListener = new LatestEpisodesGridAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View v, Link item, int position) {

            AsyncTaskTools.execute(new LoadAnimeAndEpisodesTask(item));
        }

    };
    private RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            mVisibleItemCount = mLayoutManager.getChildCount();
            mTotalItemCount = mLayoutManager.getItemCount() - (mAdapter.isLoading() ? 1 : 0);
            mFirstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();

            if (App.IsNetworkConnected()) {
                int lastInScreen = mFirstVisibleItem + mVisibleItemCount;

                if ((lastInScreen >= mTotalItemCount - 6) && !(isLoading)) {
                    if (hasResults) {
                        currentSkip += currentLimit;
                        loadmore = true;

                        task = new LatestEpisodesTask();
                        AsyncTaskTools.execute(task);
                    } else if (task == null) {
                        loadmore = false;
                        task = new LatestEpisodesTask();
                        currentSkip = 0;
                        AsyncTaskTools.execute(task);
                    }
                }
            }
        }
    };
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_latest_updates_list, container, false);
        ButterKnife.bind(this, rootView);

        mColumns = getResources().getInteger(R.integer.overview_cols);
        mLoadingTreshold = mColumns * 3;

        mLayoutManager = new GridLayoutManager(getActivity(), mColumns);
        mRecyclerView.setLayoutManager(mLayoutManager);
        return rootView;
    }

    private class LatestEpisodesTask extends AsyncTask<Void, Void, String> {
        private ArrayList<Link> newLinks = new ArrayList<Link>();

        public LatestEpisodesTask() {
        }

        private String URL;

        @Override
        protected void onPreExecute() {
            if(mAdapter.getItemCount() != 0) {
                mAdapter.addLoading();
            }
            else {
                progressBarLoading.setVisibility(View.VISIBLE);
            }

            isLoading = true;
            URL = new WcfDataServiceUtility(getString(R.string.anime_data_service_path)).getEntity("LatestDistinctLinks").formatJson().expand("Anime,Episode").skip(currentSkip).top(currentLimit).orderby("AddedDate%20desc").build();

        }

        @Override
        protected String doInBackground(Void... params) {

            JSONObject json = Utils.GetJson(URL);
            if (json == null) {
                return null;
            }
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
            JSONArray linksArray = new JSONArray();

            try {
                linksArray = json.getJSONArray("value");
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            hasResults = false;
            Gson gson = new Gson();
            for (int i = 0; i < linksArray.length(); i++) {
                hasResults = true;
                try {
                    newLinks.add(gson.fromJson(linksArray.getJSONObject(i).toString(), Link.class));
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            return "Success";
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                if (result == null) {
                    Toast.makeText(getActivity(), getActivity().getString(R.string.error_loading_animes), Toast.LENGTH_LONG).show();
                } else if (result.equals("Success")) {
                    if(progressBarLoading.isShown()) {
                        progressBarLoading.setVisibility(View.GONE);
                    }

                    mAdapter.setItems(newLinks);


                } else {
                    if (result.equals("401")) {
                        Toast.makeText(getActivity(), getActivity().getString(R.string.have_been_logged_out), Toast.LENGTH_LONG).show();
                        LatestUpdatesFragment.this.startActivity(new Intent(LatestUpdatesFragment.this.getActivity(), LoginActivity.class));
                        LatestUpdatesFragment.this.getActivity().finish();
                    }
                }
                isLoading = false;
            } catch (Exception e)//catch all exception... handle orientation change
            {
                e.printStackTrace();
            }
        }

    }

    public class LoadAnimeAndEpisodesTask extends AsyncTask<Void, Void, String> {
        private Dialog busyDialog;
        private String animeUrl;
        private String episodesUrl;
        private Link link;
        private Anime anime;
        public LoadAnimeAndEpisodesTask(Link link) {
            this.link = link;
        }

        @Override
        protected void onPreExecute() {
            busyDialog = DialogManager.showBusyDialog(getString(R.string.loading_anime), getActivity());
            animeUrl = new WcfDataServiceUtility(getString(R.string.anime_data_service_path)).getEntity("Animes").filter("AnimeId%20eq%20" + link.getAnimeId()).expand("Genres,AnimeInformations,Status,Episodes/Links,Episodes/EpisodeInformations").formatJson().build();
        }


        @Override
        protected String doInBackground(Void... params) {
            try {
                JSONObject jsonAnime = Utils.GetJson(animeUrl);
                String errors = Utils.checkDataServiceErrors(jsonAnime, getString(R.string.error_loading_reviews));
                if (errors != null)
                    return errors;
                Gson gson = new Gson();
                anime = gson.fromJson(jsonAnime.getJSONArray("value").getJSONObject(0).toString(), Anime.class);
                for(int i = 0; i < anime.getEpisodes().size(); i++)
                {
                    //Remove all episodes without a link.
                    Episode episode = anime.getEpisodes().get(i);
                    if(episode.getLinks() == null || episode.getLinks().size() < 1)
                    {
                        anime.getEpisodes().remove(i);
                    }
                }

                return null;
            } catch (Exception e) {
                return getString(R.string.error_loading_reviews);

            }
        }

        @Override
        protected void onPostExecute(String error) {
            try {
                if (error != null) {
                    if (error.equals("401")) {
                        Toast.makeText(getActivity(), getString(R.string.have_been_logged_out), Toast.LENGTH_LONG).show();
                        getActivity().startActivity(new Intent(getActivity(), LoginActivity.class));
                        getActivity().finish();
                    } else {
                        Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Intent intent = new Intent(getActivity(), VideoPlayerActivity.class);
                    intent.putExtra("anime", anime);
                    intent.putExtra("episodeToPlay", link.getEpisode());
                    startActivity(intent);
                }

            } catch (Exception e)//catch all exception, handle orientation change
            {
                e.printStackTrace();
            }

            DialogManager.dismissBusyDialog(busyDialog);
        }
    }
}
