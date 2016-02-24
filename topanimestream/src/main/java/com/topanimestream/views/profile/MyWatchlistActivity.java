package com.topanimestream.views.profile;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import com.topanimestream.App;
import com.topanimestream.adapters.WatchListAdapter;
import com.topanimestream.models.OdataRequestInfo;
import com.topanimestream.models.WatchedAnime;
import com.topanimestream.utilities.ODataUtils;
import com.topanimestream.R;
import com.topanimestream.utilities.ToolbarUtils;
import com.topanimestream.views.AnimeDetailsActivity;
import com.topanimestream.views.MainActivity;
import com.topanimestream.views.TASBaseActivity;

import butterknife.Bind;

public class MyWatchlistActivity extends TASBaseActivity {


    @Bind(R.id.txtNoWatch)
    TextView txtNoWatch;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.progressBarLoading)
    ProgressBar progressBarLoading;

    @Bind(R.id.recyclerView)
    RecyclerView mRecyclerView;

    WatchListAdapter mAdapter;
    private int currentSkip = 0;
    private int currentLimit = 40;
    private boolean isEndOfList = false;
    private boolean isLoading = false;

    private ArrayList<WatchedAnime> mItems = new ArrayList<>();
    private LinearLayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_watchlist);

        //toolbar.setTitle(anime.getName());
        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ToolbarUtils.updateToolbarHeight(this, toolbar);

        mLayoutManager = new LinearLayoutManager(MyWatchlistActivity.this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addOnScrollListener(mScrollListener);

        mAdapter = new WatchListAdapter(MyWatchlistActivity.this, mItems);
        mAdapter.setOnItemClickListener(mOnItemClickListener);

        mRecyclerView.setAdapter(mAdapter);

        //Initial data
        GetWatchList();
    }

    private RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            int mVisibleItemCount = mLayoutManager.getChildCount();
            int mTotalItemCount = mLayoutManager.getItemCount() - (mAdapter.isLoading() ? 1 : 0);
            int mFirstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();

            if (App.IsNetworkConnected()) {
                int lastInScreen = mFirstVisibleItem + mVisibleItemCount;

                if ((lastInScreen >= mTotalItemCount - 6) && !isLoading) {
                    if (!isEndOfList) {
                        currentSkip += currentLimit;
                        GetWatchList();
                    }
                }
            }
        }
    };

    private WatchListAdapter.OnItemClickListener mOnItemClickListener = new WatchListAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(final View view, final WatchedAnime watchedAnime, final int position) {
            Intent intent = new Intent(MyWatchlistActivity.this, AnimeDetailsActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable("Anime", watchedAnime.getAnime());
            intent.putExtras(bundle);
            startActivityForResult(intent, MainActivity.UpdateWatchCode);
        }

        @Override
        public void onDeleteClick(View v, WatchedAnime watchedAnime, int position) {
            mAdapter.deleteItem(position);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void GetWatchList() {
        isLoading = true;
        if(mAdapter.getItemCount() != 0) {
            mAdapter.addLoading();
        }
        else {
            progressBarLoading.setVisibility(View.VISIBLE);
        }

        ODataUtils.GetEntityList(getString(R.string.odata_path) + "MyWatchedAnimes?$expand=Anime,WatchType&$orderby=LastWatchedDate&$top=" + currentLimit + "&$skip=" + currentSkip + "&$count=true", WatchedAnime.class, new ODataUtils.Callback<ArrayList<WatchedAnime>>() {
            @Override
            public void onSuccess(ArrayList<WatchedAnime> watchedAnimes, OdataRequestInfo info) {
                mAdapter.removeLoading();


                int currentItemCount = mAdapter.getItemCount();
                int nextTotal = currentItemCount + watchedAnimes.size();

                if(info.getCount() == nextTotal)
                    isEndOfList = true;

                if (nextTotal > 0)
                    txtNoWatch.setVisibility(View.GONE);
                else
                    txtNoWatch.setVisibility(View.VISIBLE);

                progressBarLoading.setVisibility(View.GONE);

                mAdapter.addItems(watchedAnimes);

                isLoading = false;
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(MyWatchlistActivity.this, getString(R.string.error_loading_watchlist), Toast.LENGTH_LONG).show();
                progressBarLoading.setVisibility(View.GONE);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if(requestCode == MainActivity.UpdateWatchCode)
        {
            setResult(MainActivity.UpdateWatchCode, intent);
        }
    }

}
