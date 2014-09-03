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
	public static EpisodeListFragment newInstance(String fragmentName) {
		EpisodeListFragment ttFrag = new EpisodeListFragment();
	    Bundle args = new Bundle();
	    args.putString("fragmentName", fragmentName);
	    ttFrag.setArguments(args);
	    return ttFrag;
	}
	public void setEpisodes(final ArrayList<Episode> episodes, String fragmentName, String animeName, String animeDescription, String animePoster)
	{
		this.episodes = episodes;
		this.fragmentName = fragmentName;
		this.animeName = animeName;
		this.animeDescription = animeDescription;
		this.animePoster = animePoster;
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
			
			if(this.getActivity() != null)
			{
				if(filteredEpisodes.size() == 0)
				{
					listView.setFastScrollEnabled(false);
					txtNoEpisode.setVisibility(View.VISIBLE);
				}
				else
				{
					txtNoEpisode.setVisibility(View.GONE);
				}

		        listView.setAdapter(new EpisodeListAdapter(this.getActivity(), filteredEpisodes, animeName, animeDescription, animePoster));
			}
		}
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
		final ArrayList<String> providers = new ArrayList<String>();
		final ArrayList<Mirror> mirrors = new ArrayList<Mirror>();
		for(Mirror mirror:episode.getMirrors())
		{
			if(String.valueOf(mirror.getAnimeSource().getLanguageId()).equals(prefs.getString("prefLanguage", "1")))
			{
				if(fragmentName.equals("Dubbed"))
				{
					if(mirror.getAnimeSource().isSubbed())
						continue;
				}
				else if(fragmentName.equals("Subbed"))
				{
					if(!mirror.getAnimeSource().isSubbed())
						continue;
				}

			}
			else
				continue;
			mirrors.add(mirror);
			providers.add(mirror.getProvider().getName());	
		}
		 
		EpisodesContainerFragment.ProviderFragmentCoordinator providerFragmentCoordinator = (EpisodesContainerFragment.ProviderFragmentCoordinator) getActivity();
		providerFragmentCoordinator.onEpisodeSelected(mirrors);
		/*
		final CharSequence[] items = providers.toArray(new CharSequence[providers.size()]);
     	final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(EpisodeListFragment.this.getActivity());
     	alertBuilder.setTitle("Choose a provider");
     	alertBuilder.setItems(items, new DialogInterface.OnClickListener() {
     	    public void onClick(DialogInterface dialog, int item) {
				(new Utils.GetMp4(mirrors.get(item), getActivity())).execute();
     	    }
     	});
     	alertProviders = alertBuilder.create();
     	alertProviders.show();
     	Intent intent = new Intent(getActivity(),ProviderActivity.class);
     	Bundle bundle = new Bundle();
     	bundle.putParcelableArrayList("Mirrors", mirrors);
     	intent.putExtras(bundle);
     	startActivity(intent);*/
		
	}
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) { 
		prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final View rootView = inflater.inflate(R.layout.fragment_episode_list, container, false);
        r = getResources();
        fragmentName = getArguments().getString("fragmentName");
        txtNoEpisode = (TextView)rootView.findViewById(R.id.txtNoEpisode);
		listView = (ListView)rootView.findViewById(R.id.listView);
		listView.setFastScrollEnabled(true);
		listView.setOnItemClickListener(this);
		
		  if (savedInstanceState != null) 
		  {
			  this.episodes = savedInstanceState.getParcelableArrayList("episodes");
			  filteredEpisodes = this.episodes;
			  this.animeName = savedInstanceState.getString("animeName");
			  this.animeDescription = savedInstanceState.getString("animeDescription");
			  this.animePoster = savedInstanceState.getString("animePoster");
			  listView.setAdapter(new EpisodeListAdapter(this.getActivity(), episodes, animeName, animeDescription, animePoster));
	      } 

		return rootView;
    }
	
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("episodes", episodes);
        outState.putString("animeName", animeName);
        outState.putString("animeDescription", animeDescription);
        outState.putString("animePoster", animePoster);
        
        super.onSaveInstanceState(outState);
    }
private class ContentAdapter extends ArrayAdapter<String> implements SectionIndexer {
    	
    	private String mSections = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    	
		public ContentAdapter(Context context, int textViewResourceId,
				List<String> objects) {
			super(context, textViewResourceId, objects);
		}

		@Override
		public int getPositionForSection(int section) {
			// If there is no item for current section, previous section will be selected
			for (int i = section; i >= 0; i--) {
				for (int j = 0; j < getCount(); j++) {
					if (i == 0) {
						// For numeric section
						for (int k = 0; k <= 9; k++) {
							if (StringMatcher.match(String.valueOf(getItem(j).charAt(0)), String.valueOf(k)))
								return j;
						}
					} else {
						if (StringMatcher.match(String.valueOf(getItem(j).charAt(0)), String.valueOf(mSections.charAt(i))))
							return j;
					}
				}
			}
			return 0;
		}

		@Override
		public int getSectionForPosition(int position) {
			return 0;
		}

		@Override
		public Object[] getSections() {
			String[] sections = new String[mSections.length()];
			for (int i = 0; i < mSections.length(); i++)
				sections[i] = String.valueOf(mSections.charAt(i));
			return sections;
		}
    }

}
