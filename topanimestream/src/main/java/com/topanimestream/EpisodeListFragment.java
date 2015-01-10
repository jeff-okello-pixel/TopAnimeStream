package com.topanimestream;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

import com.topanimestream.adapters.EpisodeListAdapter;
import com.topanimestream.models.Episode;
import com.topanimestream.models.Mirror;
import com.topanimestream.R;

public class EpisodeListFragment extends Fragment implements OnItemClickListener {

	public boolean hasResults = false;
	private ListView listView;
	private String fragmentName;
	private ArrayList<Episode> episodes;
	private TextView txtNoEpisode;
	private AlertDialog alertProviders;
	private int animeId;
	private ArrayList<Mirror> mirrors;
	private Resources r;
	int index = 0;
	App app;
	public Dialog busyDialog;
	private SharedPreferences prefs;
	private String animeName;
	private String animePoster;
	private String animeDescription;
    private String animeBackdrop;
    private String animeGenres;
    private String animeRating;
	public EpisodeListFragment()
	{

	}
	public static EpisodeListFragment newInstance(String fragmentName, int animeId, String animeName, String animeDescription, String animePoster, String animeBackdrop, String animeGenres, String animeRating) {
		EpisodeListFragment ttFrag = new EpisodeListFragment();
	    Bundle args = new Bundle();
	    args.putString("fragmentName", fragmentName);
        args.putInt("animeId", animeId);
        args.putString("animeName", animeName);
        args.putString("animeDescription", animeDescription);
        args.putString("animePoster", animePoster);
        args.putString("animeBackdrop", animeBackdrop);
        args.putString("animeGenres", animeGenres);
        args.putString("animeRating", animeRating);
	    ttFrag.setArguments(args);
	    return ttFrag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (App)getActivity().getApplication();

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
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		final Episode episode = episodes.get(position);


		EpisodesContainerFragment.ProviderFragmentCoordinator providerFragmentCoordinator = (EpisodesContainerFragment.ProviderFragmentCoordinator) getActivity();
		providerFragmentCoordinator.onEpisodeSelected(episode, fragmentName);
		
	}
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) { 
		prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final View rootView = inflater.inflate(R.layout.fragment_episode_list, container, false);
        Bundle bundle = getArguments();
        episodes = new ArrayList<Episode>();
        fragmentName = bundle.getString("fragmentName");
        animeId = bundle.getInt("animeId");
        animeDescription = bundle.getString("animeDescription");
        animeName = bundle.getString("animeName");
        animePoster = bundle.getString("animePoster");
        animeBackdrop = bundle.getString("animeBackdrop");
        animeGenres = bundle.getString("animeGenres");
        animeRating = bundle.getString("animeRating");

        txtNoEpisode = (TextView)rootView.findViewById(R.id.txtNoEpisode);
		listView = (ListView)rootView.findViewById(R.id.listView);
		listView.setFastScrollEnabled(true);
		listView.setOnItemClickListener(this);
		
		  if (savedInstanceState != null) 
		  {
			  episodes = savedInstanceState.getParcelableArrayList("episodes");
			  this.animeName = savedInstanceState.getString("animeName");
			  this.animeDescription = savedInstanceState.getString("animeDescription");
			  this.animePoster = savedInstanceState.getString("animePoster");
              this.animeBackdrop = savedInstanceState.getString("animeBackdrop");
              if(episodes != null && episodes.size() > 0)
			    listView.setAdapter(new EpisodeListAdapter(this.getActivity(), episodes, animeName, animeDescription, animePoster, animeBackdrop, animeGenres, animeRating));
              else
              {
                  listView.setVisibility(View.GONE);
                  txtNoEpisode.setVisibility(View.VISIBLE);
              }
	      }

		return rootView;
    }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            outState.putParcelableArrayList("episodes", episodes);
            outState.putString("animeName", animeName);
            outState.putString("animeDescription", animeDescription);
            outState.putString("animePoster", animePoster);
            outState.putString("animeBackdrop", animeBackdrop);
            super.onSaveInstanceState(outState);
    }

    public void setEpisodes(ArrayList<Episode> episodes)
    {
        this.episodes = episodes;
        Collections.sort(this.episodes, new Episode());
        if(episodes.size() == 0)
        {
            listView.setFastScrollEnabled(false);
            txtNoEpisode.setVisibility(View.VISIBLE);
        }
        else
        {
            txtNoEpisode.setVisibility(View.GONE);
        }

        listView.setAdapter(new EpisodeListAdapter(getActivity(), episodes, animeName, animeDescription, animePoster, animeBackdrop, animeGenres, animeRating));
    }


}
