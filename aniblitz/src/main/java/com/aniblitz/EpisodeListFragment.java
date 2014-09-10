package com.aniblitz;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

import com.aniblitz.adapters.EpisodeListAdapter;
import com.aniblitz.interfaces.EpisodesLoadedEvent;
import com.aniblitz.models.Anime;
import com.aniblitz.models.Episode;
import com.aniblitz.models.Mirror;
import com.aniblitz.models.Provider;
public class EpisodeListFragment extends Fragment implements OnItemClickListener {

	public boolean hasResults = false;
	private ListView listView;
	private String fragmentName;
	private ArrayList<Episode> episodes;
	private ArrayList<Episode> filteredEpisodes;
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
	public EpisodeListFragment()
	{

	}
	public static EpisodeListFragment newInstance(String fragmentName, int animeId, String animeName, String animeDescription, String animePoster) {
		EpisodeListFragment ttFrag = new EpisodeListFragment();
	    Bundle args = new Bundle();
	    args.putString("fragmentName", fragmentName);
        args.putInt("animeId", animeId);
        args.putString("animeName", animeName);
        args.putString("animeDescription", animeDescription);
        args.putString("animePoster", animePoster);
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
		final Episode episode = filteredEpisodes.get(position);

		 
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

        txtNoEpisode = (TextView)rootView.findViewById(R.id.txtNoEpisode);
		listView = (ListView)rootView.findViewById(R.id.listView);
		listView.setFastScrollEnabled(true);
		listView.setOnItemClickListener(this);
		
		  if (savedInstanceState != null) 
		  {
			  filteredEpisodes = savedInstanceState.getParcelableArrayList("filteredEpisodes");
			  this.animeName = savedInstanceState.getString("animeName");
			  this.animeDescription = savedInstanceState.getString("animeDescription");
			  this.animePoster = savedInstanceState.getString("animePoster");
			  listView.setAdapter(new EpisodeListAdapter(this.getActivity(), episodes, animeName, animeDescription, animePoster));
	      }
          else
          {
              AsyncTaskTools.execute(new EpisodesTask());
          }

		return rootView;
    }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            outState.putParcelableArrayList("filteredEpisodes", filteredEpisodes);
            outState.putString("animeName", animeName);
            outState.putString("animeDescription", animeDescription);
            outState.putString("animePoster", animePoster);
            super.onSaveInstanceState(outState);
    }

    private class EpisodesTask extends AsyncTask<Void, Void, String> {

        public EpisodesTask()
        {

        }
        private final String URL = "http://lanbox.ca/AnimeServices/AnimeDataService.svc/Episodes()?$filter=AnimeId%20eq%20" + animeId + "%20and%20Mirrors/any(m:m/AnimeSource/LanguageId%20eq%20" + prefs.getString("prefLanguage", "1") + ")&$expand=Mirrors/AnimeSource,Mirrors/Provider,EpisodeInformations&$format=json";

        @Override
        protected void onPreExecute()
        {
            busyDialog = Utils.showBusyDialog(getString(R.string.loading_anime_details), getActivity());
        };
        @Override
        protected String doInBackground(Void... params)
        {

            JSONObject json = Utils.GetJson(URL);
            JSONArray episodesArray = new JSONArray();

            try {
                episodesArray = json.getJSONArray("value");
            } catch (JSONException e) {
                return null;
            }
            for(int i = 0;i<episodesArray.length();i++)
            {
                JSONObject episodeJson;
                try {
                    episodeJson = episodesArray.getJSONObject(i);
                    Episode episode = new Episode(episodeJson);

                    episodes.add(episode);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }

            }
            return "Success";
        }

        @Override
        protected void onPostExecute(String result)
        {
            if(result == null)
            {
                Toast.makeText(getActivity(), r.getString(R.string.error_loading_episodes), Toast.LENGTH_LONG).show();
            }
            else
            {
                if(episodes != null && episodes.size() > 0)
                {
                    filteredEpisodes = new ArrayList<Episode>();
                    if(fragmentName.equals("Dubbed"))
                    {
                        for(Episode episode:episodes)
                        {
                            for(Mirror mirror : episode.getMirrors())
                            {
                                if(!mirror.getAnimeSource().isSubbed() && String.valueOf(mirror.getAnimeSource().getLanguageId()).equals(prefs.getString("prefLanguage", "1")))
                                {
                                    filteredEpisodes.add(episode);
                                    break;
                                }
                            }

                        }
                    }
                    else if(fragmentName.equals("Subbed"))
                    {
                        for(Episode episode:episodes)
                        {
                            for(Mirror mirror : episode.getMirrors())
                            {
                                if(mirror.getAnimeSource().isSubbed() && String.valueOf(mirror.getAnimeSource().getLanguageId()).equals(prefs.getString("prefLanguage", "1")))
                                {
                                    filteredEpisodes.add(episode);
                                    break;
                                }
                            }

                        }
                    }

                    if(filteredEpisodes.size() == 0)
                    {
                        listView.setFastScrollEnabled(false);
                        txtNoEpisode.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        txtNoEpisode.setVisibility(View.GONE);
                    }

                    listView.setAdapter(new EpisodeListAdapter(getActivity(), filteredEpisodes, animeName, animeDescription, animePoster));

                }
            }

            Utils.dismissBusyDialog(busyDialog);
        }

    }
}
