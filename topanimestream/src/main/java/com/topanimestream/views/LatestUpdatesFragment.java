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
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.topanimestream.App;
import com.topanimestream.R;
import com.topanimestream.adapters.LatestUpdatesGridAdapter;
import com.topanimestream.custom.CoordinatedHeader;
import com.topanimestream.managers.DialogManager;
import com.topanimestream.models.Anime;
import com.topanimestream.models.Episode;
import com.topanimestream.models.Link;
import com.topanimestream.models.OdataRequestInfo;
import com.topanimestream.models.Update;
import com.topanimestream.utilities.AsyncTaskTools;
import com.topanimestream.utilities.ODataUtils;
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
    public Dialog busyDialog;
    private GridLayoutManager mLayoutManager;
    private Integer mColumns = 2;
    private LatestUpdatesGridAdapter mAdapter;
    private ArrayList<Update> mItems = new ArrayList<>();
    private boolean isEndOfList;
    private int mFirstVisibleItem, mVisibleItemCount, mTotalItemCount = 0, mLoadingTreshold = mColumns * 3;

    @Bind(R.id.recyclerView)
    RecyclerView mRecyclerView;

    @Bind(R.id.progressBarLoading)
    ProgressBar progressBarLoading;

    @Bind(R.id.txtNoUpdates)
    TextView txtNoUpdates;

    public LatestUpdatesFragment() {

    }

    public static LatestUpdatesFragment newInstance(int index) {
        LatestUpdatesFragment ttFrag = new LatestUpdatesFragment();
        Bundle args = new Bundle();
        args.putInt("index", index);
        ttFrag.setArguments(args);
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
        currentSkip = 0;
        GetLatestUpdates();
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
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();
                // Determine the maximum allowed scroll height
                final int maxScrollHeight = header.getHeight() - anchor.getHeight();

                //Anchor the header if the recentlywatched is not there
                if(layRecentlyWatched.getVisibility() == View.GONE)
                {
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
        //adapter should only ever be created once on fragment initialise.
        mAdapter = new LatestUpdatesGridAdapter(getActivity(), mItems, mColumns);
        mAdapter.setOnItemClickListener(mOnItemClickListener);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        mAdapter.setOnItemClickListener(mOnItemClickListener);
    }
    private LatestUpdatesGridAdapter.OnItemClickListener mOnItemClickListener = new LatestUpdatesGridAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View v, final Update update, int position) {

            final Dialog loadingDialog = DialogManager.showBusyDialog(getString(R.string.loading_anime), getActivity());
            ODataUtils.GetEntity(getString(R.string.odata_path) + "Animes(" + update.getAnimeId() + ")?$expand=Genres,AnimeInformations,Status,Episodes($expand=Links,EpisodeInformations)", Anime.class, new ODataUtils.EntityCallback<Anime>() {
                @Override
                public void onSuccess(Anime anime, OdataRequestInfo info) {
                    loadingDialog.dismiss();
                    Intent intent = new Intent(getActivity(), VideoPlayerActivity.class);
                    intent.putExtra("anime", anime);
                    intent.putExtra("episodeToPlay", update.getEpisode());
                    getActivity().startActivityForResult(intent, MainActivity.UpdateWatchCode);
                }

                @Override
                public void onFailure(Exception e) {
                    loadingDialog.dismiss();
                }
            });
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
                        GetLatestUpdates();
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
        mLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                switch(mAdapter.getItemViewType(position)){
                    case LatestUpdatesGridAdapter.TYPE_HEADER:
                        return mLayoutManager.getSpanCount();
                    case LatestUpdatesGridAdapter.TYPE_NORMAL:
                        return 1;
                    case LatestUpdatesGridAdapter.TYPE_LOADING:
                        return 1;
                    default:
                        return -1;
                }
            }
        });
        mRecyclerView.setLayoutManager(mLayoutManager);
        return rootView;
    }
    private void GetLatestUpdates()
    {
        isLoading = true;

        if(mAdapter.getBasicItemCount() != 0) {
            mAdapter.addLoading();
        }
        else {
            progressBarLoading.setVisibility(View.VISIBLE);
        }



        ODataUtils.GetEntityList(getString(R.string.odata_path) + "Updates?$expand=Anime,Episode,Language&$orderby=LastUpdatedDate%20desc" + "&$skip=" + currentSkip + "&$top=" + currentLimit + "&$count=true", Update.class, new ODataUtils.EntityCallback<ArrayList<Update>>() {
            @Override
            public void onSuccess(ArrayList<Update> updates, OdataRequestInfo info) {
                int currentItemCount = 0;
                if(mAdapter.getItemCount() != 0)
                    currentItemCount = mAdapter.getBasicItemCount() - 1; //remove the loading

                if(info.getCount() == currentItemCount + updates.size())
                    isEndOfList = true;

                isLoading = false;

                if(progressBarLoading.isShown()) {
                    progressBarLoading.setVisibility(View.GONE);
                }

                mAdapter.setItems(updates);


            }

            @Override
            public void onFailure(Exception e) {
                isLoading = false;

            }
        });


    }
}
