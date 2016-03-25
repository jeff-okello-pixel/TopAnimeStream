package com.topanimestream.views;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.ArrayList;

import com.topanimestream.App;
import com.topanimestream.adapters.EpisodeListAdapter;
import com.topanimestream.models.Anime;
import com.topanimestream.models.OdataRequestInfo;
import com.topanimestream.R;
import com.topanimestream.utilities.ODataUtils;
import com.topanimestream.models.Episode;
import butterknife.Bind;
import butterknife.ButterKnife;

public class EpisodeListFragment extends Fragment  {

    @Bind(R.id.txtNoEpisode)
    TextView txtNoEpisode;

    @Bind(R.id.recyclerView)
    RecyclerView mRecyclerView;

    public int currentSkip = 0;
    public int currentLimit = 40;
    public boolean isLoading = false;
    public boolean isEndOfList = false;
    private int mFirstVisibleItem;
    private int mVisibleItemCount;
    private int mTotalItemCount = 0;
    private LinearLayoutManager mLayoutManager;
    private EpisodeListAdapter mAdapter;
    private EpisodeListCallback callback;
    private Anime anime;
    public EpisodeListFragment() {

    }

    public static EpisodeListFragment newInstance(Anime anime) {
        EpisodeListFragment ttFrag = new EpisodeListFragment();
        Bundle args = new Bundle();
        args.putParcelable("anime", anime);
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

    }

    public void setEpisodeListCallback(EpisodeListCallback callback)
    {
        this.callback = callback;
    }

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
                        GetEpisodes();
                    }
                }
            }
        }
    };

    private EpisodeListAdapter.OnItemClickListener mOnItemClickListener = new EpisodeListAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(final View view, final Episode episode, final int position) {
            callback.OnEpisodeSelected(episode, mAdapter.getItems());
        }
    };

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_episode_list, container, false);
        ButterKnife.bind(this, rootView);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerView.addOnScrollListener(mScrollListener);


        if (savedInstanceState != null) {
            anime = savedInstanceState.getParcelable("anime");
            currentSkip = savedInstanceState.getInt("skip");
        }
        else
            anime = getArguments().getParcelable("anime");

        //adapter should only ever be created once on fragment initialise.
        mAdapter = new EpisodeListAdapter(getActivity(), anime);
        mAdapter.setOnItemClickListener(mOnItemClickListener);
        mRecyclerView.setAdapter(mAdapter);

        if(savedInstanceState == null)
            GetEpisodes();//initial data

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        anime.setEpisodes(mAdapter.getItems());
        outState.putParcelable("anime", anime);
        outState.putInt("skip", currentSkip);
        super.onSaveInstanceState(outState);
    }

    public void GetEpisodes()
    {
        isLoading = true;
        mAdapter.addLoading();
        ODataUtils.GetEntityList(getString(R.string.odata_path) + "Episodes?$filter=AnimeId%20eq%20" + anime.getAnimeId() + "&$expand=EpisodeInformations,Links&$orderby=Order%20desc&$top=" + currentLimit + "&$skip=" + currentSkip + "&$count=true", Episode.class, new ODataUtils.EntityCallback<ArrayList<Episode>>() {
            @Override
            public void onSuccess(ArrayList<Episode> episodes, OdataRequestInfo info) {
                isLoading = false;
                mAdapter.removeLoading();

                int currentItemCount = mAdapter.getItemCount() - (mAdapter.useHeader() ? 1 : 0);

                if(info.getCount() == currentItemCount + episodes.size())
                    isEndOfList = true;

                mAdapter.addItems(episodes);

                callback.EpisodesLoaded(episodes);
            }

            @Override
            public void onFailure(Exception e) {
                isLoading = false;
                //TODO Also show a retry button?
                txtNoEpisode.setVisibility(View.VISIBLE);
            }
        });
    }

    public interface EpisodeListCallback
    {
        void OnEpisodeSelected(Episode episode, ArrayList<Episode> episodes);
        void EpisodesLoaded(ArrayList<Episode> episodes);
    }

}
