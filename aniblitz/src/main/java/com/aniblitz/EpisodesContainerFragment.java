package com.aniblitz;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.aniblitz.adapters.EpisodeListAdapter;
import com.aniblitz.models.Anime;
import com.aniblitz.models.AnimeSource;
import com.aniblitz.models.Episode;
import com.aniblitz.models.Mirror;
import com.aniblitz.models.Provider;
import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class EpisodesContainerFragment extends Fragment{

	public boolean hasResults = false;
	private ArrayList<Episode> subbedEpisodes;
    private ArrayList<Episode> dubbedEpisodes;
	private AlertDialog alertProviders;
	private String[] tabTitles;
	private ViewPager viewPager;
	private PagerAdapter mAdapter;
	private PagerSlidingTabStrip tabs;
	private EpisodeListFragment subbedEpisodeFragment;
	private EpisodeListFragment dubbedEpisodeFragment;
    private ProviderListFragment subbedProviderFragment;
    private ProviderListFragment dubbedProviderFragment;
	private ArrayList<Mirror> mirrors;
	private Resources r;
    private Anime anime;
    private ArrayList<Episode> episodes;
	App app;
	public Dialog busyDialog;
	private SharedPreferences prefs;

    public static EpisodesContainerFragment newInstance(Anime anime) {
        EpisodesContainerFragment frag = new EpisodesContainerFragment();
        Bundle args = new Bundle();
        args.putParcelable("anime", anime);
        frag.setArguments(args);
        return frag;
    }
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (App)getActivity().getApplication();
        anime = getArguments().getParcelable("anime");

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

		prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final View rootView = inflater.inflate(R.layout.fragment_episodes, container, false);
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            rootView.setLayoutParams(params);
        }
        r = getResources();
		tabTitles = new String[] {r.getString(R.string.tab_subbed), r.getString(R.string.tab_dubbed)};
		viewPager = (ViewPager)rootView.findViewById(R.id.pager);
		mAdapter = new PagerAdapter(getChildFragmentManager());
		viewPager.setAdapter(mAdapter);
		prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		tabs = (PagerSlidingTabStrip) rootView.findViewById(R.id.tabs);
		tabs.setViewPager(viewPager);
		tabs.setDividerColor(r.getColor(R.color.blueTab));
		tabs.setUnderlineColor(r.getColor(R.color.blueTab));
		//tabs.setTextColor(Color.parseColor("#55a73d"));
		tabs.setIndicatorColor(r.getColor(R.color.blueTab));
		tabs.setTabBackground("background_tab_darkblue");
		tabs.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			 
		    @Override
		    public void onPageSelected(int position) {

		    }
		 
		    @Override
		    public void onPageScrolled(int arg0, float arg1, int arg2) {
		    }
		 
		    @Override
		    public void onPageScrollStateChanged(int arg0) {
		    }
		});
        if(!anime.isMovie() && savedInstanceState == null)
            AsyncTaskTools.execute(new EpisodesTask());
        return rootView;
    }


    public class PagerAdapter extends FragmentPagerAdapter {
		    public PagerAdapter(FragmentManager fm) {
		        super(fm);
		    }
	
		    @Override
		    public Fragment getItem(int index) {
                    String language = prefs.getString("prefLanguage", "1");
                    switch(index)
                    {
                        //Subbed
                        case 0:
                            if(!anime.isMovie()) {
                                if(subbedEpisodeFragment == null)
                                    subbedEpisodeFragment = EpisodeListFragment.newInstance("Subbed", anime.getAnimeId(), anime.getName(), anime.getDescription(), anime.getPosterPath("500"));
                                return subbedEpisodeFragment;
                            }
                            else
                            {
                                for(AnimeSource animeSource: anime.getAnimeSources())
                                {
                                    if(String.valueOf(animeSource.getLanguageId()).equals(language) && animeSource.isSubbed())
                                    {
                                        if(subbedProviderFragment == null)
                                            subbedProviderFragment = ProviderListFragment.newInstance(animeSource.getAnimeSourceId(), new ArrayList<Mirror>(), "Subbed");
                                        return subbedProviderFragment;
                                    }
                                }
                                if(subbedProviderFragment == null)
                                    subbedProviderFragment = ProviderListFragment.newInstance(-1, new ArrayList<Mirror>(), "Subbed");
                                return subbedProviderFragment;

                            }
                        //Dubbed
                        case 1:
                            if(!anime.isMovie()) {
                                if(dubbedEpisodeFragment == null)
                                    dubbedEpisodeFragment = EpisodeListFragment.newInstance("Dubbed", anime.getAnimeId(), anime.getName(), anime.getDescription(), anime.getPosterPath("500"));
                                return dubbedEpisodeFragment;
                            }
                            else
                            {
                                for(AnimeSource animeSource: anime.getAnimeSources())
                                {
                                    if(String.valueOf(animeSource.getLanguageId()).equals(language) && !animeSource.isSubbed())
                                    {
                                        if(dubbedProviderFragment == null)
                                            dubbedProviderFragment = ProviderListFragment.newInstance(animeSource.getAnimeSourceId(), new ArrayList<Mirror>(), "Dubbed");
                                        return dubbedProviderFragment;
                                    }
                                }
                                if(dubbedProviderFragment == null)
                                    dubbedProviderFragment = ProviderListFragment.newInstance(-1, new ArrayList<Mirror>(), "Dubbed");
                                return dubbedProviderFragment;
                            }
	
		    	}
		    	return null;
		    }
		    @Override
	        public CharSequence getPageTitle(int position) {
	            return tabTitles[position];
	        }
	
		    @Override
		    public int getCount() {
		        return tabTitles.length;
		    }
		}

    public interface ProviderFragmentCoordinator {
        void onEpisodeSelected(Episode episode, String type);
    }

    private class EpisodesTask extends AsyncTask<Void, Void, String> {

        public EpisodesTask()
        {

        }
        private final String URL = "http://lanbox.ca/AnimeServices/AnimeDataService.svc/Episodes()?$filter=AnimeId%20eq%20" + anime.getAnimeId() + "%20and%20Mirrors/any(m:m/AnimeSource/LanguageId%20eq%20" + prefs.getString("prefLanguage", "1") + ")&$expand=Mirrors/AnimeSource,Mirrors/Provider,EpisodeInformations&$format=json";

        @Override
        protected void onPreExecute()
        {
            busyDialog = Utils.showBusyDialog(getString(R.string.loading_anime_details), getActivity());
            episodes = new ArrayList<Episode>();
            subbedEpisodes = new ArrayList<Episode>();
            dubbedEpisodes = new ArrayList<Episode>();
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

                    for(Episode episode:episodes)
                    {
                        for(Mirror mirror : episode.getMirrors())
                        {
                            if(!mirror.getAnimeSource().isSubbed() && String.valueOf(mirror.getAnimeSource().getLanguageId()).equals(prefs.getString("prefLanguage", "1")))
                            {
                                dubbedEpisodes.add(episode);
                                break;
                            }
                            else if(mirror.getAnimeSource().isSubbed() && String.valueOf(mirror.getAnimeSource().getLanguageId()).equals(prefs.getString("prefLanguage", "1")))
                            {
                                subbedEpisodes.add(episode);
                                break;
                            }
                        }

                    }
                    subbedEpisodeFragment.setEpisodes(subbedEpisodes);
                    dubbedEpisodeFragment.setEpisodes(dubbedEpisodes);


                }
            }

            Utils.dismissBusyDialog(busyDialog);
        }

    }
}
