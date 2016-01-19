package com.topanimestream.views;

import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import com.google.gson.Gson;
import com.topanimestream.App;
import com.topanimestream.adapters.AnimeGridAdapter;
import com.topanimestream.custom.CoordinatedHeader;
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

    public static AnimeListFragment newInstance(String fragmentName, Mode mode, String orderby, String filter, int index) {
        AnimeListFragment ttFrag = new AnimeListFragment();
        Bundle args = new Bundle();
        args.putString("fragmentName", fragmentName);
        args.putString("orderby", orderby);
        args.putString("filter", filter);
        args.putInt("index", index);
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

        //if (mMode == Mode.SEARCH)
            //mEmptyView.setText(getString(R.string.no_search_results));

        //don't load initial data in search mode
        if (mMode != Mode.SEARCH && mAdapter.getItemCount() == 1) {
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

        mMode = (Mode) getArguments().getSerializable(EXTRA_MODE);
        customOrder = getArguments().getString("orderby", null);
        customFilter = getArguments().getString("filter", null);

        mColumns = getResources().getInteger(R.integer.overview_cols);
        mLoadingTreshold = mColumns * 3;

        mLayoutManager = new GridLayoutManager(getActivity(), mColumns);

        mLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                switch(mAdapter.getItemViewType(position)){
                    case AnimeGridAdapter.TYPE_HEADER:
                        return mLayoutManager.getSpanCount();
                    case AnimeGridAdapter.TYPE_NORMAL:
                        return 1;
                    case AnimeGridAdapter.TYPE_LOADING:
                        return 1;
                    default:
                        return -1;
                }
            }
        });
        mRecyclerView.setLayoutManager(mLayoutManager);

        return rootView;
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addOnScrollListener(mScrollListener);

        final CoordinatedHeader header = (CoordinatedHeader) getActivity().findViewById(R.id.activity_home_header);
        final View anchor = getActivity().findViewById(R.id.tabs);
        final Toolbar toolbar =(Toolbar) getActivity().findViewById(R.id.toolbar);
        final RelativeLayout layRecentlyWatched = (RelativeLayout) getActivity().findViewById(R.id.layRecentlyWatched);
        final int index = getArguments().getInt("index");

        if(mMode == Mode.NORMAL) {
            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    int firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();
                    // Determine the maximum allowed scroll height
                    final int maxScrollHeight = header.getHeight() - anchor.getHeight();

                    //Anchor the header if the recentlywatched is not there
                    if (layRecentlyWatched.getVisibility() == View.GONE) {
                        header.storeCoordinate(index, toolbar.getHeight());

                    } else if (firstVisibleItem != 0) {
                        // If the first item has scrolled off screen, anchor the header
                        header.storeCoordinate(index, -maxScrollHeight + toolbar.getHeight());
                        return;
                    }

                    final View firstChild = recyclerView.getChildAt(firstVisibleItem);
                    if (firstChild == null) {
                        return;
                    }


                    // Determine the offset to scroll the header
                    final float offset = Math.min(-firstChild.getY(), maxScrollHeight);
                    header.storeCoordinate(index, -offset + toolbar.getHeight());
                }

            });
        }

        //adapter should only ever be created once on fragment initialise.
        mAdapter = new AnimeGridAdapter(getActivity(), mItems, mColumns);
        if(mMode == Mode.NORMAL)
            mAdapter.setUseHeader(true);
        else
            mAdapter.setUseHeader(false);

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
            getActivity().startActivityForResult(intent, MainActivity.UpdateWatchCode);
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

        if(mAdapter.getBasicItemCount() != 0) {
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
            CallService(url);
        }
        else if(mMode == Mode.SEARCH) {
            String url = getString(R.string.odata_path) + "Animes?$filter=startswith(tolower(OriginalName),%20%27" + searchQuery.toLowerCase() + "%27)&$orderby=IsAvailable%20desc&$top=30&$count=true";
            CallService(url);
        }
    }

    private void CallService(String url)
    {
        ODataUtils.GetEntityList(url, Anime.class, new ODataUtils.Callback<ArrayList<Anime>>() {
            @Override
            public void onSuccess(ArrayList<Anime> animes, OdataRequestInfo info) {
                int currentItemCount = 0;
                if(mAdapter.getItemCount() != 0)
                    currentItemCount = mAdapter.getBasicItemCount() - 1; //remove the loading

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
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
