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

import com.aniblitz.interfaces.EpisodesLoadedEvent;
import com.aniblitz.interfaces.MovieLoadedEvent;
import com.aniblitz.models.Episode;
import com.aniblitz.models.Mirror;
import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
public class EpisodesContainerFragment extends Fragment implements EpisodesLoadedEvent, MovieLoadedEvent {

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
	int index = 0;
	App app;
	public Dialog busyDialog;
	private SharedPreferences prefs;

	private void setFragmentEpisodes(ArrayList<Episode> episodes, String animeName, String animeDescription, String animePoster)
	{
		if(subbedEpisodeFragment != null)
            subbedEpisodeFragment.setEpisodes(episodes, "Subbed", animeName, animeDescription, animePoster);
		if(dubbedEpisodeFragment != null)
            dubbedEpisodeFragment.setEpisodes(episodes, "Dubbed", animeName, animeDescription, animePoster);
	}
    private void setFragmentProviders(ArrayList<Mirror> mirrors)
    {
        subbedProviderFragment = new ProviderListFragment();
        dubbedProviderFragment = new ProviderListFragment();

        FragmentManager fm = getChildFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(subbedEpisodeFragment.getId(),subbedProviderFragment);
        ft.replace(dubbedEpisodeFragment.getId(), dubbedProviderFragment);
        ft.commit();

        subbedProviderFragment.setProviders(mirrors, "Subbed");
        dubbedProviderFragment.setProviders(mirrors, "Dubbed");
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
                    switch(index)
                    {
                        //Subbed
                        case 0:
                            subbedEpisodeFragment = EpisodeListFragment.newInstance("Subbed");
                            return subbedEpisodeFragment;
                        //Dubbed
                        case 1:
                            dubbedEpisodeFragment = EpisodeListFragment.newInstance("Dubbed");
                            return dubbedEpisodeFragment;
	
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
	@Override
	public void onEpisodesLoaded(ArrayList<Episode> episodes, String animeName, String animeDescription, String animePoster) {
		setFragmentEpisodes(episodes, animeName, animeDescription, animePoster);
		
	}
    @Override
    public void onMovieLoaded(ArrayList<Mirror> mirrors) {
        setFragmentProviders(mirrors);
    }
    public interface ProviderFragmentCoordinator {
        void onEpisodeSelected(Episode episode, String type);
    }
}
