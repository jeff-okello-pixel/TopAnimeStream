package com.topanimestream.views;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;

import java.util.ArrayList;
import java.util.Locale;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.topanimestream.App;
import com.topanimestream.custom.CoordinatedHeader;
import com.topanimestream.models.Anime;
import com.topanimestream.models.OdataRequestInfo;
import com.topanimestream.models.WatchedVideo;
import com.topanimestream.preferences.Prefs;
import com.topanimestream.utilities.AsyncTaskTools;
import com.topanimestream.utilities.ImageUtils;
import com.topanimestream.utilities.NetworkUtil;
import com.topanimestream.utilities.ODataUtils;
import com.topanimestream.utilities.PrefUtils;
import com.topanimestream.utilities.ToolbarUtils;
import com.topanimestream.utilities.Utils;
import com.topanimestream.adapters.MenuArrayAdapter;
import com.topanimestream.managers.AnimationManager;
import com.topanimestream.managers.DialogManager;
import com.topanimestream.managers.VersionManager;
import com.topanimestream.R;
import com.topanimestream.views.profile.LoginActivity;
import com.topanimestream.views.profile.MyFavoritesActivity;
import com.topanimestream.views.profile.MyProfileActivity;
import com.topanimestream.views.profile.MyWatchlistActivity;

import butterknife.Bind;

public class MainActivity extends TASBaseActivity implements OnItemClickListener, App.Connection {

    private boolean drawerIsOpened;
    private ActionBarDrawerToggle mDrawerToggle;
    private boolean doubleBackToExitPressedOnce;
    private PagerAdapter mAdapter;
    private String[] tabTitles;
    private AnimeListFragment serieFragment;
    private AnimeListFragment movieFragment;
    private LatestUpdatesFragment latestUpdatesFragment;
    private MenuArrayAdapter menuAdapter;
    private String spinnerOrderByValue;
    private String spinnerStatusValue;
    private String spinnerDubbedSubbedValue;
    private String spinnerCategoryValue;
    public String filter = "";
    public String order = "";
    private TextView txtTitle;
    final static public int UpdateWatchCode = 1000;
    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.txtNoConnection)
    TextView txtNoConnection;

    @Bind(R.id.pager)
    ViewPager viewPager;

    @Bind(R.id.tabs)
    PagerSlidingTabStrip tabs;

    @Bind(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @Bind(R.id.navigationView)
    NavigationView navigationView;

    @Bind(R.id.layRecentlyWatched)
    RelativeLayout layRecentlyWatched;

    @Bind(R.id.activity_home_header)
    CoordinatedHeader header;

    @Bind(R.id.imgWatchedBackdrop)
    ImageView imgWatchedBackdrop;

    @Bind(R.id.txtWatchedTitle)
    TextView txtWatchedTitle;

    @Bind(R.id.progressBarWatched)
    ProgressBar progressBarWatched;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_main);
        setSupportActionBar(toolbar);
        ToolbarUtils.updateToolbarHeight(this, toolbar);

        UpdateRecentlyWatched();

        tabTitles = new String[]{getString(R.string.tab_serie), getString(R.string.tab_movie), getString(R.string.updates)};

        //fill default filter dialog spinner values
        if (savedInstanceState != null) {
            drawerIsOpened = savedInstanceState.getBoolean("drawerIsOpened");
            order = savedInstanceState.getString("order");
            filter = savedInstanceState.getString("filter");
            spinnerCategoryValue = savedInstanceState.getString("spinnerCategoryValue");
            spinnerDubbedSubbedValue = savedInstanceState.getString("spinnerDubbedSubbedValue");
            spinnerStatusValue = savedInstanceState.getString("spinnerStatusValue");
            spinnerOrderByValue = savedInstanceState.getString("spinnerOrderByValue");

            serieFragment = (AnimeListFragment) getSupportFragmentManager().getFragment(savedInstanceState, "serieFragment");
            movieFragment = (AnimeListFragment) getSupportFragmentManager().getFragment(savedInstanceState, "movieFragment");
            latestUpdatesFragment = (LatestUpdatesFragment) getSupportFragmentManager().getFragment(savedInstanceState, "latestUpdatesFragment");
        } else {
            spinnerDubbedSubbedValue = getString(R.string.tab_all);
            spinnerStatusValue = getString(R.string.tab_all);
            spinnerCategoryValue = getString(R.string.tab_all);
            spinnerOrderByValue = getString(R.string.most_popular);
            filterToDataServiceQuery(spinnerOrderByValue, spinnerStatusValue, spinnerDubbedSubbedValue, spinnerCategoryValue);

            if (PrefUtils.get(this, Prefs.SHOW_UPDATE, true)) {
                VersionManager.checkUpdate(this, false);
            }
        }

        /*
        listView.setOnItemClickListener(this);
        listView.setCacheColorHint(0);
        listView.setScrollingCacheEnabled(false);
        listView.setScrollContainer(false);
        listView.setSmoothScrollbarEnabled(true);

        menuAdapter = new MenuArrayAdapter(this, getResources().getStringArray(R.array.menu_drawer_full));

        listView.setAdapter(menuAdapter);
*/
        if (mDrawerLayout != null) {
            mDrawerLayout.setDrawerListener(new DrawerListener());
            mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

            mDrawerToggle = new ActionBarDrawerToggle(
                    this,  mDrawerLayout, toolbar,
                    R.string.app_name, R.string.app_name);
            mDrawerToggle.syncState();

            navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(MenuItem item) {
                    mDrawerLayout.closeDrawers();
                    return true;
                }
            });

            //TODO fix image
            View header = navigationView.getHeaderView(0);
            ImageView imgHeaderBackground = (ImageView) header.findViewById(R.id.imgHeaderBackground);
            TextView txtUsername = (TextView) header.findViewById(R.id.txtUsername);
            TextView txtJoinedDate = (TextView) header.findViewById(R.id.txtJoinedDate);
            TextView txtAbout = (TextView) header.findViewById(R.id.txtAbout);

            imgHeaderBackground.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.attackontitanbackdrop));
            txtUsername.setText(App.currentUser.getUsername().substring(0, 1).toUpperCase() + App.currentUser.getUsername().substring(1));
            txtJoinedDate.setText("Since: " + DateUtils.getRelativeTimeSpanString(App.currentUser.getAddedDate().getTime(), System.currentTimeMillis(), DateUtils.FORMAT_ABBREV_ALL));
            txtAbout.setText(App.currentUser.getAbout());
        }

        SetViewPager();

        App.SetEvent(this);
        setPagerVisibility(App.networkConnection);

        final int identifier = getResources().getIdentifier("action_bar_title", "id", "android");
        txtTitle = (TextView) findViewById(identifier);
        if (txtTitle != null) {
            //txtTitle = (TextView) actionBar.getCustomView().findViewById(R.id.txtTitle);
            txtTitle.setTextColor(Color.parseColor("#f5f5f5"));
            txtTitle.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            txtTitle.setMarqueeRepeatLimit(1);
            // in order to start strolling, it has to be focusable and focused
            txtTitle.setFocusable(true);
            txtTitle.setFocusableInTouchMode(true);
            txtTitle.requestFocus();

        }
        setTitleWithFilter();
    }

    private void UpdateRecentlyWatched()
    {
        progressBarWatched.setVisibility(View.VISIBLE);
        ODataUtils.GetEntityList(getString(R.string.odata_path) + "MyWatchedVideos?$expand=Episode,Anime&$orderby=LastWatchedDate%20desc&$top=1", WatchedVideo.class, new ODataUtils.Callback<ArrayList<WatchedVideo>>() {

            @Override
            public void onSuccess(ArrayList<WatchedVideo> watchedVideos, OdataRequestInfo info) {
                final WatchedVideo watchedVideo = watchedVideos.get(0);
                Picasso.with(MainActivity.this)
                        .load(getString(R.string.image_host_path) + ImageUtils.resizeImage(watchedVideo.getAnime().getBackdropPath(), 500))
                        .into(imgWatchedBackdrop, new Callback() {
                            @Override
                            public void onSuccess() {
                                progressBarWatched.setVisibility(View.GONE);
                                layRecentlyWatched.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        final Dialog loadingDialog = DialogManager.showBusyDialog(getString(R.string.loading_anime), MainActivity.this);
                                        ODataUtils.GetEntity(getString(R.string.odata_path) + "Animes(" + watchedVideo.getAnime().getAnimeId() + ")?$expand=Genres,AnimeInformations,Status,Episodes($expand=Links,EpisodeInformations)", Anime.class, new ODataUtils.Callback<Anime>() {
                                            @Override
                                            public void onSuccess(Anime anime, OdataRequestInfo info) {
                                                loadingDialog.dismiss();
                                                Intent intent = new Intent(MainActivity.this, VideoPlayerActivity.class);
                                                intent.putExtra("anime", anime);
                                                intent.putExtra("episodeToPlay", watchedVideo.getEpisode());
                                                MainActivity.this.startActivityForResult(intent, UpdateWatchCode);
                                            }

                                            @Override
                                            public void onFailure(Exception e) {
                                                loadingDialog.dismiss();
                                            }
                                        });
                                    }
                                });
                            }

                            @Override
                            public void onError() {
                                progressBarWatched.setVisibility(View.GONE);
                            }
                        });

                txtWatchedTitle.setText(watchedVideo.getAnime().getName() + " - Episode " + watchedVideo.getEpisode().getEpisodeNumber());

            }

            @Override
            public void onFailure(Exception e) {
                progressBarWatched.setVisibility(View.GONE);

            }
        });

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void SetViewPager() {
        viewPager.setOffscreenPageLimit(1);
        mAdapter = new PagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(mAdapter);
        viewPager.setOffscreenPageLimit(mAdapter.getCount());

        tabs.setViewPager(viewPager);

        tabs.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int state) {
                if (state != ViewPager.SCROLL_STATE_IDLE) {
                    //wait until the pager is idle to animate the header
                    return;
                }
                header.restoreCoordinate(viewPager.getCurrentItem(), 150);
            }
        });
        tabs.setShouldExpand(true);
        tabs.setDividerColor(getResources().getColor(R.color.blueTab));
        tabs.setUnderlineColor(getResources().getColor(R.color.blueTab));
        //tabs.setTextColor(Color.parseColor("#55a73d"));
        tabs.setIndicatorColor(getResources().getColor(R.color.blueTab));
        tabs.setTabBackground("background_tab_darkblue");

    }

    @Override
    public void ConnectionChanged(int connectionType) {
        setPagerVisibility(connectionType);
    }

    private void setPagerVisibility(int connectionType) {
        if (connectionType != NetworkUtil.TYPE_NOT_CONNECTED) {
            //connected
            txtNoConnection.setVisibility(View.GONE);
            viewPager.setVisibility(View.VISIBLE);
            tabs.setVisibility(View.VISIBLE);
        } else {
            //not connected
            txtNoConnection.setVisibility(View.VISIBLE);
            viewPager.setVisibility(View.GONE);
            tabs.setVisibility(View.GONE);
        }
    }

    public class PagerAdapter extends FragmentStatePagerAdapter {
        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int index) {
            switch (index) {
                //Serie
                case 0:
                    serieFragment = AnimeListFragment.newInstance(getString(R.string.tab_serie), AnimeListFragment.Mode.NORMAL, order, filter, 0);
                    return serieFragment;
                //Movie
                case 1:
                    movieFragment = AnimeListFragment.newInstance(getString(R.string.tab_movie), AnimeListFragment.Mode.NORMAL, order, filter, 1);
                    return movieFragment;
                //Updates
                case 2:
                    latestUpdatesFragment = LatestUpdatesFragment.newInstance(2);
                    return latestUpdatesFragment;
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

    private void setTitleWithFilter() {
        String title = "Anime List";
        if (!spinnerCategoryValue.equals(getString(R.string.tab_all))) {
            title += " - " + spinnerCategoryValue;
        }
        if (!spinnerDubbedSubbedValue.equals(getString(R.string.tab_all))) {
            title += " - " + spinnerDubbedSubbedValue;
        }
        if (!spinnerStatusValue.equals(getString(R.string.tab_all))) {
            title += " - " + spinnerStatusValue;
        }

        if (!spinnerOrderByValue.equals(getString(R.string.most_popular))) {
            title += "(" + spinnerOrderByValue + ")";
        }
        setTitle(title);
        if (txtTitle != null) {
            txtTitle.setText(title + "</font>");
            txtTitle.requestFocus();
        } else
            setTitle(title);
    }

    private class DrawerListener implements DrawerLayout.DrawerListener {
        @Override
        public void onDrawerOpened(View drawerView) {
            mDrawerToggle.onDrawerOpened(drawerView);
            if (txtTitle != null)
                txtTitle.setText(getString(R.string.app_name));
            else
                setTitle(getString(R.string.app_name));
            drawerIsOpened = true;
        }

        @Override
        public void onDrawerClosed(View drawerView) {
            mDrawerToggle.onDrawerClosed(drawerView);
            setTitleWithFilter();
            drawerIsOpened = false;
        }

        @Override
        public void onDrawerSlide(View drawerView, float slideOffset) {
            mDrawerToggle.onDrawerSlide(drawerView, slideOffset);
        }

        @Override
        public void onDrawerStateChanged(int newState) {
            mDrawerToggle.onDrawerStateChanged(newState);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("drawerIsOpened", drawerIsOpened);
        outState.putString("order", order);
        outState.putString("filter", filter);
        outState.putString("spinnerStatusValue", spinnerStatusValue);
        outState.putString("spinnerDubbedSubbedValue", spinnerDubbedSubbedValue);
        outState.putString("spinnerCategoryValue", spinnerCategoryValue);
        outState.putString("spinnerOrderByValue", spinnerOrderByValue);
        if (serieFragment != null && serieFragment.isAdded())
            getSupportFragmentManager().putFragment(outState, "serieFragment", serieFragment);
        if (movieFragment != null && movieFragment.isAdded())
            getSupportFragmentManager().putFragment(outState, "movieFragment", movieFragment);
        if (latestUpdatesFragment != null && latestUpdatesFragment.isAdded())
            getSupportFragmentManager().putFragment(outState, "latestUpdatesFragment", latestUpdatesFragment);
        super.onSaveInstanceState(outState);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.action_filter:
                final Dialog dialog = new Dialog(this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.filter_dialog);
                dialog.setTitle("Filter");

                final Spinner spinnerOrderBy = (Spinner) dialog.findViewById(R.id.spinnerOrderBy);
                final Spinner spinnerStatus = (Spinner) dialog.findViewById(R.id.spinnerStatus);
                final Spinner spinnerDubbedSubbed = (Spinner) dialog.findViewById(R.id.spinnerDubbedSubbed);
                final Spinner spinnerCategory = (Spinner) dialog.findViewById(R.id.spinnerCategory);
                spinnerOrderBy.setSelection(((ArrayAdapter) spinnerOrderBy.getAdapter()).getPosition(spinnerOrderByValue));
                spinnerStatus.setSelection(((ArrayAdapter) spinnerStatus.getAdapter()).getPosition(spinnerStatusValue));
                spinnerDubbedSubbed.setSelection(((ArrayAdapter) spinnerDubbedSubbed.getAdapter()).getPosition(spinnerDubbedSubbedValue));
                spinnerCategory.setSelection(((ArrayAdapter) spinnerCategory.getAdapter()).getPosition(spinnerCategoryValue));
                Button btnApply = (Button) dialog.findViewById(R.id.btnApply);
                btnApply.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        spinnerOrderByValue = spinnerOrderBy.getSelectedItem().toString();
                        spinnerStatusValue = spinnerStatus.getSelectedItem().toString();
                        spinnerDubbedSubbedValue = spinnerDubbedSubbed.getSelectedItem().toString();
                        spinnerCategoryValue = spinnerCategory.getSelectedItem().toString();
                        filterToDataServiceQuery(spinnerOrderByValue, spinnerStatusValue, spinnerDubbedSubbedValue, spinnerCategoryValue);

                        refreshFragment(serieFragment, order, filter);
                        refreshFragment(movieFragment, order, filter);

                        dialog.dismiss();

                        setTitleWithFilter();

                    }
                });
                dialog.show();

                return true;
            case R.id.action_settings:
                startActivity(new Intent(MainActivity.this, PreferencesActivity.class));
                return true;
            case R.id.action_search:
                startActivity(new Intent(MainActivity.this, AnimeSearchActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void filterToDataServiceQuery(String selectedOrder, String selectedStatus, String selectedDubbedSubbed, String selectCategory) {
        filter = "";
        order = "";
        String newFilter = "";
        ArrayList<String> filters = new ArrayList<String>();


        if (selectedOrder.equals(getString(R.string.alphabetical_az)))
            order += "OriginalName";
        else if (selectedOrder.equals(getString(R.string.alphabetical_za)))
            order += "OriginalName%20desc";
        else if (selectedOrder.equals(getString(R.string.latest_release)))
            order += "ReleasedDate";
        else if (selectedOrder.equals(getString(R.string.oldest_release))) {
            order += "ReleasedDate";
            filters.add("ReleasedDate%20ne%20null");
        } else if (selectedOrder.equals(getString(R.string.recently_added)))
            order += "AddedDate";
        else if (selectedOrder.equals(getString(R.string.most_popular))) {
            order += "Rating%20desc";
        } else if (selectedOrder.equals(getString(R.string.less_popular))) {
            order += "Rating";
            filters.add("Rating%20ne%20null%20and%20VoteCount%20gt%200");
        }


        if (selectedStatus.equals(getString(R.string.complete)))
            filters.add("Status/StatusId%20eq%2063");
        else if (selectedStatus.equals(getString(R.string.ongoing)))
            filters.add("Status/StatusId%20eq%2064");


        if (selectedDubbedSubbed.equals(getString(R.string.tab_dubbed)))
            filters.add("AnimeSources/any(as:as/IsSubbed%20eq%20false)");
        else if (selectedDubbedSubbed.equals(getString(R.string.tab_subbed)))
            filters.add("AnimeSources/any(as:as/IsSubbed%20eq%20true)");


        if (!selectCategory.equals(getString(R.string.tab_all)))
            filters.add("Genres/any(g:g/GenreId%20eq%20" + Utils.GenreNameToId(selectCategory) + ")");

        filter = TextUtils.join("%20and%20", filters);

        if (!filter.equals(""))
            filter = "%20and%20" + filter;
    }

    public void refreshFragment(AnimeListFragment frag, String orderBy, String filter) {
        if (frag != null) {
            if (frag.isAdded())
                frag.refresh(orderBy, filter);
        }
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, getString(R.string.back_again), Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;

            }
        }, 2000);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String menuItem = menuAdapter.getItem(position);
        if(menuItem.equals(getString(R.string.menu_myprofile))){
            startActivity(new Intent(MainActivity.this, MyProfileActivity.class));
            AnimationManager.ActivityStart(this);
        }
        else if (menuItem.equals(getString(R.string.menu_favorites))) {
            startActivity(new Intent(MainActivity.this, MyFavoritesActivity.class));
            AnimationManager.ActivityStart(this);
        } else if (menuItem.equals(getString(R.string.menu_history))) {
            startActivity(new Intent(MainActivity.this, MyWatchlistActivity.class));
            AnimationManager.ActivityStart(this);
        } else if (menuItem.equals(getString(R.string.menu_share))) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "http://www.topanimestream.com/en/android/");
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        } else if (menuItem.equals(getString(R.string.menu_settings))) {
            startActivity(new Intent(MainActivity.this, PreferencesActivity.class));
            AnimationManager.ActivityStart(this);
        } else if (menuItem.equals(getString(R.string.menu_logout))) {
            AsyncTaskTools.execute(new LogoutTask());
        }


    }

    private class LogoutTask extends AsyncTask<Void, Void, String> {
        private Dialog busyDialog;

        private static final String NAMESPACE = "http://tempuri.org/";
        final String SOAP_ACTION = "http://tempuri.org/IAnimeService/";
        private String URL;
        private String method = "LogOut";

        @Override
        protected void onPreExecute() {
            busyDialog = DialogManager.showBusyDialog(getString(R.string.logging_out), MainActivity.this);
            URL = getString(R.string.odata_path);
        }

        @Override
        protected String doInBackground(Void... params) {
            if (!App.IsNetworkConnected()) {
                return getString(R.string.error_internet_connection);
            }
            SoapObject request = new SoapObject(NAMESPACE, method);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            request.addProperty("token", App.accessToken);

            envelope.headerOut = new Element[1];
            Element lang = new Element().createElement("", "Lang");
            lang.addChild(Node.TEXT, Locale.getDefault().getLanguage());
            envelope.headerOut[0] = lang;
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);
            HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
            SoapPrimitive result = null;
            try {
                androidHttpTransport.call(SOAP_ACTION + method, envelope);
                result = (SoapPrimitive) envelope.getResponse();

                return null;
            } catch (Exception e) {
                if (e instanceof SoapFault) {
                    return e.getMessage();
                }

                e.printStackTrace();
            }
            return getString(R.string.error_logout);
        }

        @Override
        protected void onPostExecute(String error) {
            DialogManager.dismissBusyDialog(busyDialog);
            if (error != null) {
                Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG).show();
            } else {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                prefs.edit().putString("AccessToken", null).commit();
                App.accessToken = null;
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (App.languageChanged) {
            App.languageChanged = false;
            Utils.restartActivity(this);
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == UpdateWatchCode)
        {
            UpdateRecentlyWatched();
        }
    }

    public interface WatchedVideoCallback
    {
        WatchedVideo VideoWatched();
    }
}
