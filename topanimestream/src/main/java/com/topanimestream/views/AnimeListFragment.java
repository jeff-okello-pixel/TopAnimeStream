package com.topanimestream.views;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import com.google.gson.Gson;
import com.topanimestream.App;
import com.topanimestream.adapters.AnimeGridAdapter;
import com.topanimestream.utilities.AsyncTaskTools;
import com.topanimestream.utilities.Utils;
import com.topanimestream.utilities.WcfDataServiceUtility;
import com.topanimestream.adapters.AnimeListAdapter;
import com.topanimestream.managers.AnimationManager;
import com.topanimestream.models.Anime;
import com.topanimestream.models.Mirror;
import com.topanimestream.R;
import com.topanimestream.views.profile.LoginActivity;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class AnimeListFragment extends Fragment implements OnItemClickListener {

    public int currentSkip = 0;
    public int currentLimit = 40;
    public boolean isLoading = false;
    public boolean loadmore = false;
    public boolean hasResults = false;
    private GridView gridView;
    private ArrayList<Anime> animes;
    private ProgressBar progressBarLoadMore;
    private String fragmentName;
    private Resources r;
    public Dialog busyDialog;
    public ArrayList<Mirror> mirrors;
    public int animeId;
    private SharedPreferences prefs;
    private AnimeTask task;
    private AnimeListAdapter adapter;
    private TextView txtNoAnime;
    private String customOrder;
    private String customFilter;
    private AnimeGridAdapter mAdapter;
    private GridLayoutManager mLayoutManager;
    private Integer mColumns = 2;
    private ArrayList<Anime> mItems = new ArrayList<>();
    private boolean mEndOfListReached = false;

    private int mFirstVisibleItem, mVisibleItemCount, mTotalItemCount = 0, mLoadingTreshold = mColumns * 3, mPreviousTotal = 0;

    @InjectView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    public AnimeListFragment() {

    }

    public static AnimeListFragment newInstance(String fragmentName) {
        AnimeListFragment ttFrag = new AnimeListFragment();
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

        loadmore = false;
        task = new AnimeTask(customOrder, customFilter);
        currentSkip = 0;
        AsyncTaskTools.execute(task);

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

        Anime anime = (Anime) gridView.getAdapter().getItem(position);
        //HD
        if(anime.getLinks() != null && anime.getLinks().size() > 0) {
            Intent intent = new Intent(this.getActivity(), AnimeDetailsActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable("Anime", anime);
            intent.putExtras(bundle);
            startActivity(intent);

        }
        else
        {
            Intent intent = new Intent(this.getActivity(), OldAnimeDetailsActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable("Anime", anime);
            intent.putExtras(bundle);
            startActivity(intent);
        }
        AnimationManager.ActivityStart(getActivity());

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    public void refresh(String orderBy, String filter) {
        currentSkip = 0;
        if (adapter != null)
            adapter.clear();
        loadmore = false;
        customOrder = orderBy;
        customFilter = filter;
        AsyncTaskTools.execute(new AnimeTask(customOrder, customFilter));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_anime_list, container, false);
        ButterKnife.inject(this, rootView);
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        r = getResources();
        customOrder = ((MainActivity) getActivity()).order;
        customFilter = ((MainActivity) getActivity()).filter;

        mColumns = getResources().getInteger(R.integer.overview_cols);
        mLoadingTreshold = mColumns * 3;

        mLayoutManager = new GridLayoutManager(getActivity(), mColumns);
        mRecyclerView.setLayoutManager(mLayoutManager);


        /*
        if(savedInstanceState != null)
            isDesc = savedInstanceState.getBoolean("isDesc");
        else
            isDesc = getArguments().getBoolean("isDesc");*/
        /*
        animes = new ArrayList<Anime>();
        fragmentName = getArguments().getString("fragmentName");
        txtNoAnime = (TextView) rootView.findViewById(R.id.txtNoAnime);
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
        });*/
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
        public void onItemClick(final View view, final Anime item, final int position) {
            /**
             * We shouldn't really be doing the palette loading here without any ui feedback,
             * but it should be really quick
             */
            RecyclerView.ViewHolder holder = mRecyclerView.getChildViewHolder(view);
            if (holder instanceof AnimeGridAdapter.ViewHolder) {

            } else {
                //showLoadingDialog(item);
            }

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
            //progressBarLoadMore.setVisibility(View.VISIBLE);
            if (!mAdapter.isLoading()) mAdapter.addLoading();
            isLoading = true;
            WcfDataServiceUtility wcfCall = new WcfDataServiceUtility(getString(R.string.anime_data_service_path)).getEntity("Animes").formatJson().expand("Genres,AnimeInformations,Status,Links,AnimeSources").select("*,Links/LinkId,Genres,AnimeInformations,Status,AnimeSources").skip(currentSkip).top(currentLimit);
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
                    if (mAdapter.isLoading()) mAdapter.removeLoading();
                    mAdapter.setItems(newAnimes);
                    /*
                    if (loadmore) {

                        for (Anime anime : newAnimes) {
                            adapter.add(anime);
                        }
                        adapter.update();
                    } else {
                        adapter = new AnimeListAdapter(AnimeListFragment.this.getActivity(), newAnimes);
                        gridView.setAdapter(adapter);
                    }*/


                } else {
                    if (result.equals("401")) {
                        Toast.makeText(getActivity(), getActivity().getString(R.string.have_been_logged_out), Toast.LENGTH_LONG).show();
                        AnimeListFragment.this.startActivity(new Intent(AnimeListFragment.this.getActivity(), LoginActivity.class));
                        AnimeListFragment.this.getActivity().finish();
                    }
                }
                isLoading = false;
                /*
                progressBarLoadMore.setVisibility(View.GONE);

                if (gridView.getAdapter().getCount() == 0) {
                    txtNoAnime.setVisibility(View.VISIBLE);
                    gridView.setVisibility(View.GONE);
                } else {
                    txtNoAnime.setVisibility(View.GONE);
                    gridView.setVisibility(View.VISIBLE);
                }*/
            } catch (Exception e)//catch all exception... handle orientation change
            {
                e.printStackTrace();
            }

        }

    }
}
