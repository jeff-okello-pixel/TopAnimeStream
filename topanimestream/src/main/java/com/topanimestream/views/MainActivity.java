package com.topanimestream.views;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
import com.google.gson.Gson;
import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.google.sample.castcompanionlibrary.cast.callbacks.VideoCastConsumerImpl;
import com.google.sample.castcompanionlibrary.widgets.MiniController;

import org.json.JSONObject;
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

import com.topanimestream.App;
import com.topanimestream.preferences.Prefs;
import com.topanimestream.utilities.AsyncTaskTools;
import com.topanimestream.utilities.NetworkUtil;
import com.topanimestream.utilities.PrefUtils;
import com.topanimestream.utilities.ToolbarUtils;
import com.topanimestream.utilities.Utils;
import com.topanimestream.utilities.WcfDataServiceUtility;
import com.topanimestream.adapters.MenuArrayAdapter;
import com.topanimestream.managers.AnimationManager;
import com.topanimestream.managers.DialogManager;
import com.topanimestream.managers.VersionManager;
import com.topanimestream.models.Account;
import com.topanimestream.R;
import com.topanimestream.models.CurrentUser;
import com.topanimestream.views.profile.LoginActivity;
import com.topanimestream.views.profile.MyFavoritesActivity;
import com.topanimestream.views.profile.MyProfileActivity;
import com.topanimestream.views.profile.MyWatchlistActivity;

import butterknife.Bind;

public class MainActivity extends TASBaseActivity implements OnItemClickListener, App.Connection {

    private boolean firstTime;
    private boolean drawerIsOpened;
    private ActionBarDrawerToggle mDrawerToggle;
    private boolean doubleBackToExitPressedOnce;
    private PagerAdapter mAdapter;
    private String[] tabTitles;
    private AnimeListFragment serieFragment;
    private AnimeListFragment movieFragment;
    private LatestEpisodesFragment latestEpisodesFragment;
    private Dialog busyDialog;
    private AlertDialog alertLanguages;
    private VideoCastConsumerImpl mCastConsumer;
    private MenuArrayAdapter menuAdapter;
    private String spinnerOrderByValue;
    private String spinnerStatusValue;
    private String spinnerDubbedSubbedValue;
    private String spinnerCategoryValue;
    public String filter = "";
    public String order = "";
    private TextView txtTitle;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.txtNoConnection)
    TextView txtNoConnection;

    @Bind(R.id.pager)
    ViewPager viewPager;

    @Bind(R.id.tabs)
    PagerSlidingTabStrip tabs;

    @Bind(R.id.left_drawer)
    ListView listView;

    @Bind(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @Bind(R.id.miniController)
    MiniController mMini;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_main);

        setSupportActionBar(toolbar);
        ToolbarUtils.updateToolbarHeight(this, toolbar);

        App.accessToken = PrefUtils.get(this, Prefs.ACCESS_TOKEN , "");

        tabTitles = new String[]{getString(R.string.tab_serie), getString(R.string.tab_movie), getString(R.string.latest_episodes)};

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
            latestEpisodesFragment = (LatestEpisodesFragment) getSupportFragmentManager().getFragment(savedInstanceState, "latestEpisodesFragment");
        } else {
            spinnerDubbedSubbedValue = getString(R.string.tab_all);
            spinnerStatusValue = getString(R.string.tab_all);
            spinnerCategoryValue = getString(R.string.tab_all);
            spinnerOrderByValue = getString(R.string.most_popular);
            filterToDataServiceQuery(spinnerOrderByValue, spinnerStatusValue, spinnerDubbedSubbedValue, spinnerCategoryValue);
            if (App.accessToken != null && !App.accessToken.equals("")) {
                //TODO make sure validtokentask is not necessary
                //AsyncTaskTools.execute(new ValidTokenTask());
                if (PrefUtils.get(this, Prefs.SHOW_UPDATE, true)) {
                    VersionManager.checkUpdate(this, false);
                }
            } else {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
        }

        listView.setOnItemClickListener(this);
        listView.setCacheColorHint(0);
        listView.setScrollingCacheEnabled(false);
        listView.setScrollContainer(false);
        listView.setSmoothScrollbarEnabled(true);

        menuAdapter = new MenuArrayAdapter(this, getResources().getStringArray(R.array.menu_drawer_full));

        listView.setAdapter(menuAdapter);

        if (mDrawerLayout != null) {
            mDrawerLayout.setDrawerListener(new DrawerListener());
            mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

            mDrawerToggle = new ActionBarDrawerToggle(
                    this,  mDrawerLayout, toolbar,
                    R.string.app_name, R.string.app_name);
            mDrawerToggle.syncState();

            if (firstTime) {
                drawerIsOpened = true;
                mDrawerLayout.openDrawer(listView);
                firstTime = false;
            }
        }

        if (PrefUtils.get(this,Prefs.LOCALE, "0").equals("0")) {
            CharSequence[] items = null;
            if (App.phoneLanguage.equals("1"))
                items = new CharSequence[]{getString(R.string.language_english) + " " + getString(R.string.parenthese_default), getString(R.string.language_french), getString(R.string.language_spanish)};
            else if (App.phoneLanguage.equals("2"))
                items = new CharSequence[]{getString(R.string.language_english), getString(R.string.language_french) + " " + getString(R.string.parenthese_default), getString(R.string.language_spanish)};
            else if (App.phoneLanguage.equals("4"))
                items = new CharSequence[]{getString(R.string.language_english), getString(R.string.language_french), getString(R.string.language_spanish) + " " + getString(R.string.parenthese_default)};

            final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
            alertBuilder.setTitle(getString(R.string.title_alert_languages));
            alertBuilder.setItems(items, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    switch (item) {
                        case 0:
                            PrefUtils.save(MainActivity.this, Prefs.LOCALE, "1");
                            break;
                        case 1:
                            PrefUtils.save(MainActivity.this, Prefs.LOCALE, "2");
                            break;
                        case 2:
                            PrefUtils.save(MainActivity.this, Prefs.LOCALE, "4");
                            break;
                    }
                    App.setLocale();
                    Utils.restartActivity(MainActivity.this);
                }
            });

            alertLanguages = alertBuilder.create();
            alertLanguages.setCancelable(false);
            alertLanguages.setOnKeyListener(new Dialog.OnKeyListener() {

                @Override
                public boolean onKey(DialogInterface arg0, int keyCode,
                                     KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        PrefUtils.save(MainActivity.this, Prefs.LOCALE, App.phoneLanguage);
                        alertLanguages.dismiss();
                        SetViewPager();
                        App.setLocale();
                        Utils.restartActivity(MainActivity.this);
                    }
                    return true;
                }
            });
            try {
                //leaked error
                alertLanguages.show();
            } catch (Exception e) {
            }
        } else {
            SetViewPager();
        }

        App.SetEvent(this);
        setPagerVisibility(App.networkConnection);

        VideoCastManager.checkGooglePlaySevices(this);

        App.getCastManager(this);

        // -- Adding MiniController
        App.mCastMgr.addMiniController(mMini);

        mCastConsumer = new VideoCastConsumerImpl();
        App.mCastMgr.reconnectSessionIfPossible(this, false);

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void SetViewPager() {
        viewPager.setOffscreenPageLimit(1);
        mAdapter = new PagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(mAdapter);

        tabs.setViewPager(viewPager);
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
                    serieFragment = AnimeListFragment.newInstance(getString(R.string.tab_serie), AnimeListFragment.Mode.NORMAL, order, filter);
                    return serieFragment;
                //Movie
                case 1:
                    movieFragment = AnimeListFragment.newInstance(getString(R.string.tab_movie), AnimeListFragment.Mode.NORMAL, order, filter);
                    return movieFragment;
                //Latest episode
                case 2:
                    latestEpisodesFragment = LatestEpisodesFragment.newInstance("Latest Episodes");
                    return latestEpisodesFragment;
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
        if (latestEpisodesFragment != null && latestEpisodesFragment.isAdded())
            getSupportFragmentManager().putFragment(outState, "latestEpisodesFragment", latestEpisodesFragment);
        super.onSaveInstanceState(outState);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        App.mCastMgr.addMediaRouterButton(menu, R.id.media_route_menu_item);

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
                if (!drawerIsOpened)
                    mDrawerLayout.openDrawer(listView);
                else
                    mDrawerLayout.closeDrawer(listView);
                break;
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

                break;
            case R.id.action_settings:
                startActivity(new Intent(MainActivity.this, PreferencesActivity.class));
                break;
            case R.id.action_search:
                startActivity(new Intent(MainActivity.this, AnimeSearchActivity.class));
                break;
        }
        return true;
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
        } else if (menuItem.equals(getString(R.string.menu_pro))) {
            DialogManager.ShowBuyProDialog(this);
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
            URL = getString(R.string.anime_service_path);
        }

        ;

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
        App.mCastMgr.decrementUiCounter();
        App.mCastMgr.removeVideoCastConsumer(mCastConsumer);
        super.onPause();
    }

    @Override
    protected void onResume() {
        App.getCastManager(this);
        if (null != App.mCastMgr) {
            App.mCastMgr.addVideoCastConsumer(mCastConsumer);
            App.mCastMgr.incrementUiCounter();
        }
        super.onResume();
        if (App.languageChanged) {
            App.languageChanged = false;
            Utils.restartActivity(this);
        }


    }

    private class ValidTokenTask extends AsyncTask<Void, Void, String> {

        private static final String NAMESPACE = "http://tempuri.org/";
        final String SOAP_ACTION = "http://tempuri.org/IAnimeService/";
        private String URL;
        private String method = "IsValidToken";
        private boolean isValidToken = false;
        private String username;

        @Override
        protected void onPreExecute() {
            busyDialog = DialogManager.showBusyDialog(getString(R.string.logging), MainActivity.this);
            URL = getString(R.string.anime_service_path);

            username = PrefUtils.get(MainActivity.this, Prefs.USERNAME, null);
        }

        @Override
        protected String doInBackground(Void... params) {
            if (!App.IsNetworkConnected()) {
                return getString(R.string.error_internet_connection);
            }
            if(username == null)
                return getString(R.string.error_login);
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
                isValidToken = Boolean.valueOf(result.toString());
                JSONObject jsonAccount = Utils.GetJson(new WcfDataServiceUtility(getString(R.string.anime_data_service_path)).getEntity("Accounts").filter("Username%20eq%20%27" + username + "%27").expand("Roles").formatJson().build());
                Gson gson = new Gson();
                Account account = gson.fromJson(jsonAccount.getJSONArray("value").getJSONObject(0).toString(), Account.class);
                //Set global current user
                CurrentUser.SetCurrentUser(account);
                return null;
            } catch (Exception e) {
                if (e instanceof SoapFault) {
                    return e.getMessage();
                }

                e.printStackTrace();
            }
            return getString(R.string.error_login);
        }

        @Override
        protected void onPostExecute(String error) {
            try {
                DialogManager.dismissBusyDialog(busyDialog);
            } catch (Exception e) {
            }
            if (error != null) {
                Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG).show();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            } else {
                if (!isValidToken) {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }

            }
        }
    }
}
