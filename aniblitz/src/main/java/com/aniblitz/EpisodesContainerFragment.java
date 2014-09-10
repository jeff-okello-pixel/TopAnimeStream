package com.aniblitz;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.res.Resources;
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

import com.aniblitz.models.Anime;
import com.aniblitz.models.AnimeSource;
import com.aniblitz.models.Episode;
import com.aniblitz.models.Mirror;
import com.aniblitz.models.Provider;
import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
public class EpisodesContainerFragment extends Fragment{

	public boolean hasResults = false;
	private ArrayList<Episode> filteredEpisodes;
	private AlertDialog alertProviders;
	private String[] tabTitles;
	private ViewPager viewPager;
	private PagerAdapter mAdapter;
	private PagerSlidingTabStrip tabs;
	private EpisodeListFragment subbedEpisodeFragment;
	private EpisodeListFragment dubbedEpisodeFragment;
    private ProviderListFragment subbedProviderFragment;
    private ProviderListFragment dubbedProviderFragment;
	private int animeId;
	private ArrayList<Mirror> mirrors;
	private Resources r;
    private String type;
    private Anime anime;
	App app;
	public Dialog busyDialog;
	private SharedPreferences prefs;

    public static EpisodesContainerFragment newInstance(String type, Anime anime) {
        EpisodesContainerFragment frag = new EpisodesContainerFragment();
        Bundle args = new Bundle();
        args.putString("type", type);
        args.putParcelable("anime", anime);
        frag.setArguments(args);
        return frag;
    }
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (App)getActivity().getApplication();
		type = getArguments().getString("type");
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
                            if(type.equals("episodes")) {
                                subbedEpisodeFragment = EpisodeListFragment.newInstance("Subbed", anime.getAnimeId(), anime.getName(), anime.getDescription(), anime.getPosterPath("500"));
                                return subbedEpisodeFragment;
                            }
                            else
                            {
                                for(AnimeSource animeSource: anime.getAnimeSources())
                                {
                                    if(String.valueOf(animeSource.getLanguageId()).equals(language) && animeSource.isSubbed())
                                    {
                                        subbedProviderFragment = ProviderListFragment.newInstance(animeSource.getAnimeSourceId(), new ArrayList<Mirror>(), "Subbed");
                                        return subbedProviderFragment;
                                    }
                                }

                                subbedProviderFragment = ProviderListFragment.newInstance(-1, new ArrayList<Mirror>(), "Subbed");
                                return subbedProviderFragment;

                            }
                        //Dubbed
                        case 1:
                            if(type.equals("episodes")) {
                                dubbedEpisodeFragment = EpisodeListFragment.newInstance("Dubbed", anime.getAnimeId(), anime.getName(), anime.getDescription(), anime.getPosterPath("500"));
                                return dubbedEpisodeFragment;
                            }
                            else
                            {
                                for(AnimeSource animeSource: anime.getAnimeSources())
                                {
                                    if(String.valueOf(animeSource.getLanguageId()).equals(language) && !animeSource.isSubbed())
                                    {
                                        dubbedProviderFragment = ProviderListFragment.newInstance(animeSource.getAnimeSourceId(), new ArrayList<Mirror>(), "Dubbed");
                                        return dubbedProviderFragment;
                                    }
                                }
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
}
