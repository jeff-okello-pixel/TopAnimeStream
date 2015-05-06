package com.topanimestream.views;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
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
import com.topanimestream.adapters.AnimeListAdapter;
import com.topanimestream.adapters.LatestEpisodesAdapter;
import com.topanimestream.managers.AnimationManager;
import com.topanimestream.models.Anime;
import com.topanimestream.models.Link;
import com.topanimestream.models.Mirror;
import com.topanimestream.utilities.AsyncTaskTools;
import com.topanimestream.utilities.Utils;
import com.topanimestream.utilities.WcfDataServiceUtility;
import com.topanimestream.views.profile.LoginActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class LatestEpisodesFragment extends Fragment implements OnItemClickListener {

    public int currentSkip = 0;
    public int currentLimit = 40;
    public boolean isLoading = false;
    public boolean loadmore = false;
    public boolean hasResults = false;
    private GridView gridView;
    private ArrayList<Link> links;
    private ProgressBar progressBarLoadMore;
    private String fragmentName;
    private Resources r;
    App app;
    public Dialog busyDialog;
    public ArrayList<Mirror> mirrors;
    public int animeId;
    private SharedPreferences prefs;
    private LatestEpisodesTask task;
    private LatestEpisodesAdapter adapter;
    private TextView txtNoEpisodes;

    public LatestEpisodesFragment() {

    }

    public static LatestEpisodesFragment newInstance(String fragmentName) {
        LatestEpisodesFragment ttFrag = new LatestEpisodesFragment();
        Bundle args = new Bundle();
        args.putString("fragmentName", fragmentName);
        ttFrag.setArguments(args);
        return ttFrag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fragmentName = this.getArguments().getString("fragmentName");
        app = (App) getActivity().getApplication();

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
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {

        Link link = (Link) gridView.getAdapter().getItem(position);
        //TODO play episode

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_latest_episodes_list, container, false);
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        r = getResources();
        /*
        if(savedInstanceState != null)
            isDesc = savedInstanceState.getBoolean("isDesc");
        else
            isDesc = getArguments().getBoolean("isDesc");*/
        links = new ArrayList<Link>();
        fragmentName = getArguments().getString("fragmentName");
        txtNoEpisodes = (TextView) rootView.findViewById(R.id.txtNoEpisodes);
        progressBarLoadMore = (ProgressBar) rootView.findViewById(R.id.progressBarLoadMore);
        gridView = (GridView) rootView.findViewById(R.id.gridView);
        gridView.setFastScrollEnabled(true);
        gridView.setOnItemClickListener(this);
        gridView.setScrollingCacheEnabled(false);

        gridView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                if (app.IsNetworkConnected()) {
                    int lastInScreen = firstVisibleItem + visibleItemCount;

                    if ((lastInScreen >= totalItemCount - 6) && !(isLoading)) {
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
        });
        return rootView;
    }

    private class LatestEpisodesTask extends AsyncTask<Void, Void, String> {
        private ArrayList<Link> newLinks = new ArrayList<Link>();

        public LatestEpisodesTask() {
        }

        private String URL;

        @Override
        protected void onPreExecute() {
            progressBarLoadMore.setVisibility(View.VISIBLE);
            isLoading = true;
            URL = new WcfDataServiceUtility(getString(R.string.anime_data_service_path)).getEntity("Links").formatJson().expand("Anime,Episode").skip(currentSkip).top(currentLimit).orderby("AddedDate%20desc").build();

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
                    if (loadmore) {

                        for (Link link : newLinks) {
                            adapter.add(link);
                        }
                        adapter.update();
                    } else {
                        adapter = new LatestEpisodesAdapter(LatestEpisodesFragment.this.getActivity(), newLinks);
                        gridView.setAdapter(adapter);
                    }


                } else {
                    if (result.equals("401")) {
                        Toast.makeText(getActivity(), getActivity().getString(R.string.have_been_logged_out), Toast.LENGTH_LONG).show();
                        LatestEpisodesFragment.this.startActivity(new Intent(LatestEpisodesFragment.this.getActivity(), LoginActivity.class));
                        LatestEpisodesFragment.this.getActivity().finish();
                    }
                }
                isLoading = false;
                progressBarLoadMore.setVisibility(View.GONE);

                if (gridView.getAdapter().getCount() == 0) {
                    txtNoEpisodes.setVisibility(View.VISIBLE);
                    gridView.setVisibility(View.GONE);
                } else {
                    txtNoEpisodes.setVisibility(View.GONE);
                    gridView.setVisibility(View.VISIBLE);
                }
            } catch (Exception e)//catch all exception... handle orientation change
            {
                e.printStackTrace();
            }
        }

    }
}
