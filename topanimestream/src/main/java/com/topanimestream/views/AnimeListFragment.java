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
import com.topanimestream.models.OdataRequestInfo;
import com.topanimestream.utilities.AsyncTaskTools;
import com.topanimestream.utilities.ODataUtils;
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
    public boolean isEndOfList = false;
    private String fragmentName;
    public Dialog busyDialog;
    public int animeId;
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
    private int mFirstVisibleItem, mVisibleItemCount, mTotalItemCount = 0, mLoadingTreshold = mColumns * 3;

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
            currentSkip = 0;
            GetAnimes(customOrder, customFilter);
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
        currentSkip = 0;
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

        GetAnimes(null, null);
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    public void refresh(String orderBy, String filter) {
        //TODO refresh the fragment
        currentSkip = 0;
        mAdapter.clearItems();
        customOrder = orderBy;
        customFilter = filter;
        GetAnimes(customOrder, customFilter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_anime_list, container, false);
        ButterKnife.bind(this, rootView);

        customOrder = getArguments().getString("orderby", null);
        customFilter = getArguments().getString("filter", null);

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
        mRecyclerView.addOnScrollListener(mScrollListener);

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
            Intent intent = new Intent(getActivity(), AnimeDetailsActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable("Anime", anime);
            intent.putExtras(bundle);
            startActivity(intent);
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
                    if (!isEndOfList) {
                        currentSkip += currentLimit;
                        GetAnimes(customOrder, customFilter);
                    }
                }
            }
        }
    };

    public void GetAnimes(String customOrder, String customFilter)
    {
        isLoading = true;
        if(mAdapter.getItemCount() != 0) {
            mAdapter.addLoading();
        }
        else {
            progressBarLoading.setVisibility(View.VISIBLE);
        }

        String filter = "&$filter=IsAvailable%20eq%20true";
        if (fragmentName.equals(getString(R.string.tab_movie)))
            filter += "%20and%20IsMovie%20eq%20true";
        else if (fragmentName.equals(getString(R.string.tab_serie)))
            filter += "%20and%20IsMovie%20eq%20false";

        if (customFilter != null && !customFilter.equals(""))
            filter += customFilter;

        if(customOrder == null)
            customOrder = "";
        else
            customOrder = "&$orderby=" + customOrder;

        if(mMode == Mode.NORMAL) {
            String url = getString(R.string.odata_path) + "Animes?$expand=Genres,AnimeInformations,Status&$skip=" + currentSkip + "&$top=" + currentLimit + filter + customOrder + "&$count=true";
            ODataUtils.GetEntityList(url, Anime.class, new ODataUtils.Callback<ArrayList<Anime>>() {
                @Override
                public void onSuccess(ArrayList<Anime> animes, OdataRequestInfo info) {
                    int currentItemCount = 0;
                    if(mAdapter.getItemCount() != 0)
                        currentItemCount = mAdapter.getItemCount() - 1; //remove the loading

                    if(info.getCount() == currentItemCount + animes.size())
                        isEndOfList = true;

                    isLoading = false;
                    if(progressBarLoading.isShown()) {
                        progressBarLoading.setVisibility(View.GONE);
                    }

                    mAdapter.setItems(animes);
                }

                @Override
                public void onFailure(Exception e) {
                    isLoading = false;
                }
            });
        }
        else if(mMode == Mode.SEARCH) {
            //TODO
        }
    }
}
