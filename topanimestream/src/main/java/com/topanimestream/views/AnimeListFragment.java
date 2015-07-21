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
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.topanimestream.App;
import com.topanimestream.adapters.AnimeGridAdapter;
import com.topanimestream.utilities.AsyncTaskTools;
import com.topanimestream.utilities.Utils;
import com.topanimestream.utilities.WcfDataServiceUtility;
import com.topanimestream.managers.AnimationManager;
import com.topanimestream.models.Anime;
import com.topanimestream.R;
import com.topanimestream.views.profile.LoginActivity;

import butterknife.Bind;
import butterknife.ButterKnife;

public class AnimeListFragment extends Fragment {

    public static final String EXTRA_MODE = "extra_mode";

    public int currentSkip = 0;
    public int currentLimit = 40;
    public boolean isLoading = false;
    public boolean loadmore = false;
    public boolean hasResults = false;
    private String fragmentName;
    public Dialog busyDialog;
    public int animeId;
    private AnimeTask task;
    private String customOrder;
    private String customFilter;
    private AnimeGridAdapter mAdapter;
    private GridLayoutManager mLayoutManager;
    private Integer mColumns = 2;
    private ArrayList<Anime> mItems = new ArrayList<>();
    private Mode mMode;
    private String searchQuery;

    public enum Mode {
        NORMAL, SEARCH
    }
    private int mFirstVisibleItem, mVisibleItemCount, mTotalItemCount = 0, mLoadingTreshold = mColumns * 3, mPreviousTotal = 0;

    @Bind(R.id.recyclerView)
    RecyclerView mRecyclerView;

    @Bind(R.id.progressBarLoading)
    ProgressBar progressBarLoading;

    public static AnimeListFragment newInstance(String fragmentName, Mode mode, String orderby, String filter) {
        AnimeListFragment ttFrag = new AnimeListFragment();
        Bundle args = new Bundle();
        args.putString("fragmentName", fragmentName);
        args.putString("orderby", orderby);
        args.putString("filter", filter);
        args.putSerializable(EXTRA_MODE, mode);
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
        fragmentName = getArguments().getString("fragmentName");
        mMode = (Mode) getArguments().getSerializable(EXTRA_MODE);

        //if (mMode == Mode.SEARCH)
            //mEmptyView.setText(getString(R.string.no_search_results));

        //don't load initial data in search mode
        if (mMode != Mode.SEARCH && mAdapter.getItemCount() == 0) {
            loadmore = false;
            currentSkip = 0;
            AsyncTaskTools.execute(new AnimeTask(customOrder, customFilter));
        }
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void triggerSearch(String query) {
        this.searchQuery = query;
        if (!isAdded())
            return;

        if (mAdapter == null)
            return;

        mItems.clear();
        mAdapter.clearItems();//clear out adapter

        if (searchQuery.equals("")) {
            return; //don't do a search for empty queries
        }

        //TODO call search
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    public void refresh(String orderBy, String filter) {
        //TODO refresh the fragment
        currentSkip = 0;

        loadmore = false;
        customOrder = orderBy;
        customFilter = filter;
        AsyncTaskTools.execute(new AnimeTask(customOrder, customFilter));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_anime_list, container, false);
        ButterKnife.bind(this, rootView);

        customOrder = getArguments().getString("orderby", "");
        customFilter = getArguments().getString("filter", "");

        mColumns = getResources().getInteger(R.integer.overview_cols);
        mLoadingTreshold = mColumns * 3;

        mLayoutManager = new GridLayoutManager(getActivity(), mColumns);
        mRecyclerView.setLayoutManager(mLayoutManager);

        return rootView;
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setOnScrollListener(mScrollListener);
        //adapter should only ever be created once on fragment initialise.
        mAdapter = new AnimeGridAdapter(getActivity(), mItems, mColumns);
        mAdapter.setOnItemClickListener(mOnItemClickListener);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        mAdapter.setOnItemClickListener(mOnItemClickListener);
    }
    private AnimeGridAdapter.OnItemClickListener mOnItemClickListener = new AnimeGridAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(final View view, final Anime anime, final int position) {
            //HD
            if(anime.getLinks() != null && anime.getLinks().size() > 0) {
                Intent intent = new Intent(getActivity(), AnimeDetailsActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("Anime", anime);
                intent.putExtras(bundle);
                startActivity(intent);

            }
            else
            {
                Intent intent = new Intent(getActivity(), OldAnimeDetailsActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("Anime", anime);
                intent.putExtras(bundle);
                startActivity(intent);
            }
            AnimationManager.ActivityStart(getActivity());

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

                        task = new AnimeTask(customOrder, customFilter);
                        AsyncTaskTools.execute(task);
                    } else if (task == null) {
                        loadmore = false;
                        task = new AnimeTask(customOrder, customFilter);
                        currentSkip = 0;
                        AsyncTaskTools.execute(task);
                    }
                }
            }
        }
    };
    private class AnimeTask extends AsyncTask<Void, Void, String> {
        private ArrayList<Anime> newAnimes = new ArrayList<Anime>();
        private String customOrderBy;
        private String customFilter;

        public AnimeTask(String orderBy, String filter) {
            this.customOrderBy = orderBy;
            this.customFilter = filter;
        }

        private String URL;

        @Override
        protected void onPreExecute() {
            if(mAdapter.getItemCount() != 0)
                mAdapter.addLoading();
            else
                progressBarLoading.setVisibility(View.VISIBLE);

            isLoading = true;

            WcfDataServiceUtility wcfCall = null;

            if(mMode == Mode.NORMAL)
                wcfCall = new WcfDataServiceUtility(getString(R.string.anime_data_service_path)).getEntity("Animes").formatJson().expand("Genres,AnimeInformations,Status,Links,AnimeSources").select("*,Links/LinkId,Genres,AnimeInformations,Status,AnimeSources").skip(currentSkip).top(currentLimit);
            else if(mMode == Mode.SEARCH) {
                try {
                    wcfCall = new WcfDataServiceUtility(getString(R.string.anime_data_service_path)).getEntity("Search").formatJson().addParameter("query", "%27" + URLEncoder.encode(searchQuery, "UTF-8").replace("%27", "%27%27") + "%27").expand("AnimeSources,Genres,AnimeInformations,Links").skip(currentSkip).top(currentLimit);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            String filter = "";

            if (fragmentName.equals(getString(R.string.tab_movie)))
                filter += "IsMovie%20eq%20true";
            else if (fragmentName.equals(getString(R.string.tab_serie)))
                filter += "IsMovie%20eq%20false";
            if (customFilter != null && !customFilter.equals(""))
                filter += customFilter;

            wcfCall.filter(filter);

            if (customOrderBy != null && !customOrderBy.equals(""))
                wcfCall.orderby(customOrderBy);


            URL = wcfCall.build();
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
            JSONArray animeArray = new JSONArray();

            try {
                animeArray = json.getJSONArray("value");
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            hasResults = false;
            Gson gson = new Gson();
            for (int i = 0; i < animeArray.length(); i++) {
                hasResults = true;
                JSONObject animeJson;
                try {
                    animeJson = animeArray.getJSONObject(i);
                    newAnimes.add(gson.fromJson(animeJson.toString(), Anime.class));
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
                    if(progressBarLoading.isShown())
                        progressBarLoading.setVisibility(View.GONE);

                    mAdapter.setItems(newAnimes);
                } else {
                    if (result.equals("401")) {
                        Toast.makeText(getActivity(), getActivity().getString(R.string.have_been_logged_out), Toast.LENGTH_LONG).show();
                        AnimeListFragment.this.startActivity(new Intent(AnimeListFragment.this.getActivity(), LoginActivity.class));
                        AnimeListFragment.this.getActivity().finish();
                    }
                }
                isLoading = false;

            } catch (Exception e)//catch all exception... handle orientation change
            {
                e.printStackTrace();
            }

        }

    }
}
