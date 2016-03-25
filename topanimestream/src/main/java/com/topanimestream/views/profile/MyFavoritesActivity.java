package com.topanimestream.views.profile;

import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.ArrayList;
import java.util.Collections;

import com.google.gson.Gson;
import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;
import com.topanimestream.App;
import com.topanimestream.adapters.FavoriteListAdapter;
import com.topanimestream.adapters.WatchListAdapter;
import com.topanimestream.models.Favorite;
import com.topanimestream.models.OdataRequestInfo;
import com.topanimestream.utilities.AsyncTaskTools;
import com.topanimestream.utilities.ODataUtils;
import com.topanimestream.utilities.ToolbarUtils;
import com.topanimestream.utilities.Utils;
import com.topanimestream.utilities.WcfDataServiceUtility;
import com.topanimestream.managers.AnimationManager;
import com.topanimestream.managers.DialogManager;
import com.topanimestream.models.Anime;
import com.topanimestream.R;
import com.topanimestream.views.AnimeDetailsActivity;
import com.topanimestream.views.MainActivity;
import com.topanimestream.views.TASBaseActivity;

import butterknife.Bind;

public class MyFavoritesActivity extends TASBaseActivity {

    @Bind(R.id.txtNoFavorite)
    TextView txtNoFavorite;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.progressBarLoading)
    ProgressBar progressBarLoading;

    @Bind(R.id.recyclerView)
    RecyclerView mRecyclerView;

    FavoriteListAdapter mAdapter;
    private int currentSkip = 0;
    private int currentLimit = 40;
    private boolean isEndOfList = false;
    private boolean isLoading = false;
    final static public int UpdateFavoritesCode = 1001;

    private ArrayList<Favorite> mItems = new ArrayList<>();
    private LinearLayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_favorite);
        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ToolbarUtils.updateToolbarHeight(this, toolbar);

        mLayoutManager = new LinearLayoutManager(MyFavoritesActivity.this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addOnScrollListener(mScrollListener);

        mAdapter = new FavoriteListAdapter(MyFavoritesActivity.this, mItems);
        mAdapter.setOnItemClickListener(mOnItemClickListener);

        mRecyclerView.setAdapter(mAdapter);

        //Initial data
        GetFavorites();

    }

    private FavoriteListAdapter.OnItemClickListener mOnItemClickListener = new FavoriteListAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View v, Favorite favorite, int position) {
            Intent intent = new Intent(MyFavoritesActivity.this, AnimeDetailsActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable("Anime", favorite.getAnime());
            intent.putExtras(bundle);
            startActivityForResult(intent, MainActivity.UpdateWatchCode);
        }

        @Override
        public void onDeleteClick(View v, Favorite favorite, int position) {
            String jsonBodyString = "{animeId: " + favorite.getAnime().getAnimeId() + "}";
            ODataUtils.Post(getString(R.string.odata_path) + "Favorites/RemoveFromMyFavorites", jsonBodyString, new ODataUtils.Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(MyFavoritesActivity.this, R.string.error_deleting_favorite, Toast.LENGTH_LONG).show();
                }
            });

            mAdapter.deleteItem(position);
        }
    };

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
                        GetFavorites();
                    }
                }
            }
        }
    };

    public void GetFavorites()
    {
        isLoading = true;
        if(mAdapter.getItemCount() != 0) {
            mAdapter.addLoading();
        }
        else {
            progressBarLoading.setVisibility(View.VISIBLE);
        }

        ODataUtils.GetEntityList(getString(R.string.odata_path) + "MyFavorites?$expand=Anime($expand=AnimeInformations)&$orderby=Order&$skip=" + currentSkip + "&$top=" + currentLimit + "&$count=true", Favorite.class, new ODataUtils.EntityCallback<ArrayList<Favorite>>() {
            @Override
            public void onSuccess(ArrayList<Favorite> favorites, OdataRequestInfo info) {
                mAdapter.removeLoading();

                int currentItemCount = mAdapter.getItemCount();
                int nextTotal = currentItemCount + favorites.size();

                if(info.getCount() == nextTotal)
                    isEndOfList = true;

                if (nextTotal > 0)
                    txtNoFavorite.setVisibility(View.GONE);
                else
                    txtNoFavorite.setVisibility(View.VISIBLE);

                progressBarLoading.setVisibility(View.GONE);

                mAdapter.addItems(favorites);

                isLoading = false;
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(MyFavoritesActivity.this, getString(R.string.error_loading_favorites), Toast.LENGTH_LONG).show();
                progressBarLoading.setVisibility(View.GONE);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if(requestCode == MainActivity.UpdateWatchCode)
            setResult(MainActivity.UpdateWatchCode, intent);

        if(App.shouldUpdateFavorites){
            App.shouldUpdateFavorites = false;
            mAdapter.clear();
            GetFavorites();
        }

    }


}
