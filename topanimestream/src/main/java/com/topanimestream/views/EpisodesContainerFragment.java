package com.topanimestream.views;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
import com.topanimestream.App;
import com.topanimestream.models.Anime;
import com.topanimestream.models.AnimeSource;
import com.topanimestream.models.Episode;
import com.topanimestream.R;
import com.topanimestream.preferences.Prefs;
import com.topanimestream.utilities.PrefUtils;

public class EpisodesContainerFragment extends Fragment {

    private String[] tabTitles;
    private ViewPager viewPager;
    private PagerAdapter mAdapter;
    private PagerSlidingTabStrip tabs;
    private OldEpisodeListFragment subbedEpisodeFragment;
    private OldEpisodeListFragment dubbedEpisodeFragment;
    private ProviderListFragment subbedProviderFragment;
    private ProviderListFragment dubbedProviderFragment;
    private Anime anime;
    private boolean subbed = false;
    private boolean dubbed = false;

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
        super.onCreate(savedInstanceState);
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
        final View rootView = inflater.inflate(R.layout.fragment_episodes, container, false);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1);
            rootView.setLayoutParams(params);
        }

        viewPager = (ViewPager) rootView.findViewById(R.id.pager);
        tabs = (PagerSlidingTabStrip) rootView.findViewById(R.id.tabs);

        if (savedInstanceState != null) {
            subbed = savedInstanceState.getBoolean("subbed");
            dubbed = savedInstanceState.getBoolean("dubbed");
            createViewPager();
        }
        else
        {
            String language = PrefUtils.get(getActivity(), Prefs.LOCALE, "1");
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

        if (!anime.isMovie()) {
            if (subbedEpisodeFragment == null)
                subbedEpisodeFragment = OldEpisodeListFragment.newInstance("Subbed", anime);
            return subbedEpisodeFragment;
        } else {
            for (AnimeSource animeSource : anime.getAnimeSources()) {
                if (String.valueOf(animeSource.getLanguageId()).equals(App.currentLanguageId) && animeSource.isSubbed()) {
                    if (subbedProviderFragment == null)
                        subbedProviderFragment = ProviderListFragment.newInstance(animeSource.getAnimeSourceId(), null, "Subbed", anime);
                    return subbedProviderFragment;
                }
            }
            if (subbedProviderFragment == null)
                subbedProviderFragment = ProviderListFragment.newInstance(-1, null, "Subbed", anime);
            return subbedProviderFragment;

        }
    }

    private Fragment getDubbedPagerFragment() {
        if (!anime.isMovie()) {
            if (dubbedEpisodeFragment == null)
                dubbedEpisodeFragment = OldEpisodeListFragment.newInstance("Dubbed", anime);
            return dubbedEpisodeFragment;
        } else {
            for (AnimeSource animeSource : anime.getAnimeSources()) {
                if (String.valueOf(animeSource.getLanguageId()).equals(App.currentLanguageId) && !animeSource.isSubbed()) {
                    if (dubbedProviderFragment == null)
                        dubbedProviderFragment = ProviderListFragment.newInstance(animeSource.getAnimeSourceId(), null, "Dubbed", anime);
                    return dubbedProviderFragment;
                }
            }
            if (dubbedProviderFragment == null)
                dubbedProviderFragment = ProviderListFragment.newInstance(-1, null, "Dubbed", anime);
            return dubbedProviderFragment;
        }
    }

    private void createViewPager() {
        tabTitles = new String[]{getString(R.string.tab_subbed), getString(R.string.tab_dubbed)};
        if (!dubbed) {
            tabTitles = new String[]{getString(R.string.tab_subbed)};
        } else if (!subbed) {
            tabTitles = new String[]{getString(R.string.tab_dubbed)};
        }

        mAdapter = new PagerAdapter(getChildFragmentManager());
        viewPager.setAdapter(mAdapter);
        tabs.setViewPager(viewPager);
        tabs.setDividerColor(getResources().getColor(R.color.blueTab));
        tabs.setUnderlineColor(getResources().getColor(R.color.blueTab));
        //tabs.setTextColor(Color.parseColor("#55a73d"));
        tabs.setIndicatorColor(getResources().getColor(R.color.blueTab));
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
