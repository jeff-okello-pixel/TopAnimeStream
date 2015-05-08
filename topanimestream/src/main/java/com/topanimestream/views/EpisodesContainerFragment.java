package com.topanimestream.views;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;

import java.util.ArrayList;

import com.topanimestream.App;
import com.topanimestream.models.Anime;
import com.topanimestream.models.AnimeSource;
import com.topanimestream.models.Episode;
import com.topanimestream.models.Mirror;
import com.topanimestream.R;

public class EpisodesContainerFragment extends Fragment {

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
    private boolean subbed = false;
    private boolean dubbed = false;
    App app;
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
        app = (App) getActivity().getApplication();
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
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1);
            rootView.setLayoutParams(params);
        }
        r = getResources();
        viewPager = (ViewPager) rootView.findViewById(R.id.pager);
        tabs = (PagerSlidingTabStrip) rootView.findViewById(R.id.tabs);

        if (savedInstanceState != null) {
            subbed = savedInstanceState.getBoolean("subbed");
            dubbed = savedInstanceState.getBoolean("dubbed");
            createViewPager();
        }
        else
        {
            String language = prefs.getString("prefLanguage", "1");
            for (AnimeSource animeSource : anime.getAnimeSources()) {
                if (String.valueOf(animeSource.getLanguageId()).equals(language) && animeSource.isSubbed()) {
                    subbed = true;
                } else if (String.valueOf(animeSource.getLanguageId()).equals(language) && !animeSource.isSubbed()) {
                    dubbed = true;
                }
            }
            createViewPager();
        }
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("subbed", subbed);
        outState.putBoolean("dubbed", dubbed);
        super.onSaveInstanceState(outState);

    }

    public class PagerAdapter extends FragmentPagerAdapter {
        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int index) {

            switch (index) {
                //Subbed
                case 0:
                    if (subbed) {
                        return getSubbedPagerFragment();
                    } else {
                        return getDubbedPagerFragment();
                    }
                    //Dubbed
                case 1:
                    return getDubbedPagerFragment();

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

    private Fragment getSubbedPagerFragment() {
        /*
        String language = prefs.getString("prefLanguage", "1");
        if (!anime.isMovie()) {
            if (subbedEpisodeFragment == null)
                subbedEpisodeFragment = EpisodeListFragment.newInstance("Subbed", anime.getAnimeId(), anime.getName(), anime.getDescription(getActivity()), anime.getPosterPath("500"), anime.getRelativeBackdropPath(null), anime.getGenresFormatted(), String.valueOf(anime.getRating()));
            return subbedEpisodeFragment;
        } else {
            for (AnimeSource animeSource : anime.getAnimeSources()) {
                if (String.valueOf(animeSource.getLanguageId()).equals(language) && animeSource.isSubbed()) {
                    if (subbedProviderFragment == null)
                        subbedProviderFragment = ProviderListFragment.newInstance(animeSource.getAnimeSourceId(), null, "Subbed", anime);
                    return subbedProviderFragment;
                }
            }
            if (subbedProviderFragment == null)
                subbedProviderFragment = ProviderListFragment.newInstance(-1, null, "Subbed", anime);
            return subbedProviderFragment;

        }*/
        return null;
    }

    private Fragment getDubbedPagerFragment() {
        /*
        String language = prefs.getString("prefLanguage", "1");
        if (!anime.isMovie()) {
            if (dubbedEpisodeFragment == null)
                dubbedEpisodeFragment = EpisodeListFragment.newInstance("Dubbed", anime.getAnimeId(), anime.getName(), anime.getDescription(getActivity()), anime.getPosterPath("500"), anime.getRelativeBackdropPath(null), anime.getGenresFormatted(), String.valueOf(anime.getRating()));
            return dubbedEpisodeFragment;
        } else {
            for (AnimeSource animeSource : anime.getAnimeSources()) {
                if (String.valueOf(animeSource.getLanguageId()).equals(language) && !animeSource.isSubbed()) {
                    if (dubbedProviderFragment == null)
                        dubbedProviderFragment = ProviderListFragment.newInstance(animeSource.getAnimeSourceId(), null, "Dubbed", anime);
                    return dubbedProviderFragment;
                }
            }
            if (dubbedProviderFragment == null)
                dubbedProviderFragment = ProviderListFragment.newInstance(-1, null, "Dubbed", anime);
            return dubbedProviderFragment;
        }*/
        return null;
    }

    private void createViewPager() {
        tabTitles = new String[]{r.getString(R.string.tab_subbed), r.getString(R.string.tab_dubbed)};
        if (!dubbed) {
            tabTitles = new String[]{getString(R.string.tab_subbed)};
        } else if (!subbed) {
            tabTitles = new String[]{getString(R.string.tab_dubbed)};
        }

        mAdapter = new PagerAdapter(getChildFragmentManager());
        viewPager.setAdapter(mAdapter);
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
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
    }
}
