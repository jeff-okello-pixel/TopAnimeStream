package com.topanimestream.views;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.ArrayList;
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
    RecyclerView recyclerView;

    private int animeId;

    public EpisodeListFragment() {

    }

    public static EpisodeListFragment newInstance(int animeId) {
        EpisodeListFragment ttFrag = new EpisodeListFragment();
        Bundle args = new Bundle();
        args.putInt("animeId", animeId);
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

        animeId = getArguments().getInt("animeId");

        if (savedInstanceState != null) {
            animeId = savedInstanceState.getInt("animeId");
        }

        ODataUtils.GetEntityList(getString(R.string.odata_path) + "Episodes?$filter=AnimeId%20eq%20" + animeId + "&$expand=EpisodeInformations,Links&$orderby=Order", Episode.class, new ODataUtils.Callback<ArrayList<Episode>>() {
            @Override
            public void onSuccess(ArrayList<Episode> episodes, OdataRequestInfo info) {



            }

            @Override
            public void onFailure(Exception e) {
                //TODO Also show a retry button?
                txtNoEpisode.setVisibility(View.VISIBLE);
            }
        });
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("animeId", animeId);
        super.onSaveInstanceState(outState);
    }

}
