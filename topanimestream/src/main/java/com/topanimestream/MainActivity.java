package com.topanimestream;

import java.util.ArrayList;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.support.v7.widget.SearchView.OnSuggestionListener;
import android.text.Html;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.topanimestream.R;
import com.topanimestream.adapters.MenuArrayAdapter;
import com.topanimestream.managers.AnimationManager;
import com.topanimestream.managers.DialogManager;
import com.topanimestream.managers.VersionManager;
import com.topanimestream.models.Anime;
import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
import com.fwwjt.pacjz173199.AdView;
import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.google.sample.castcompanionlibrary.cast.callbacks.VideoCastConsumerImpl;
import com.google.sample.castcompanionlibrary.widgets.MiniController;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;

public class MainActivity extends ActionBarActivity implements OnItemClickListener, App.Connection{

	private DrawerLayout mDrawerLayout;
	private boolean firstTime;
	private boolean drawerIsOpened;
	private ListView listView;
	private ActionBarDrawerToggle mDrawerToggle;
	private boolean doubleBackToExitPressedOnce;
    private TextView txtNoConnection;
	private MenuItem menuItem;
	private ArrayList<String> mItems;
    private MenuItem menuSortAz;
    private MenuItem menuSortZa;
    public boolean isDesc = false;
	private ArrayList<Anime> animes;
    private PagerAdapter mAdapter;
    private Resources r;
    private ViewPager viewPager;
    private PagerSlidingTabStrip tabs;
	private String[] tabTitles;
	private AnimeListFragment allFragment;
	private AnimeListFragment serieFragment;
	private AnimeListFragment movieFragment;
	private AnimeListFragment cartoonFragment;
	private Dialog busyDialog;
	private SharedPreferences prefs;
    private App app;
	private AlertDialog alertLanguages;
    private MiniController mMini;
    private VideoCastConsumerImpl mCastConsumer;
    private MenuItem mediaRouteMenuItem;
    private MenuItem menuBuyPro;
    private MenuArrayAdapter menuAdapter;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.Theme_Blue);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        App.accessToken = prefs.getString("AccessToken","");
        if(Utils.isProInstalled(this))
        {
            SQLiteHelper sqlLite = new SQLiteHelper(this);
            if(!sqlLite.isPro()) {
                sqlLite.setPro(true);
                DialogManager.ShowUpgradedToProDialog(this);
            }
            else
                App.isPro = true;

            sqlLite.close();
        }
		r = getResources();
		animes = new ArrayList<Anime>();
		mItems = new ArrayList<String>();
		
		tabTitles = new String[] {r.getString(R.string.tab_all), r.getString(R.string.tab_serie), r.getString(R.string.tab_movie), r.getString(R.string.tab_cartoon)};
        app = (App)getApplication();
        txtNoConnection = (TextView)findViewById(R.id.txtNoConnection);
		viewPager = (ViewPager)findViewById(R.id.pager);
        tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		
		if(savedInstanceState != null)
		{
			drawerIsOpened = savedInstanceState.getBoolean("drawerIsOpened");
            isDesc = savedInstanceState.getBoolean("isDesc");
            allFragment = (AnimeListFragment) getSupportFragmentManager().getFragment(savedInstanceState, "allFragment");
            serieFragment = (AnimeListFragment) getSupportFragmentManager().getFragment(savedInstanceState, "serieFragment");
            movieFragment = (AnimeListFragment) getSupportFragmentManager().getFragment(savedInstanceState, "movieFragment");
            cartoonFragment = (AnimeListFragment) getSupportFragmentManager().getFragment(savedInstanceState, "cartoonFragment");
		}
        else
        {

            if(App.isGooglePlayVersion)
            {
                AppRater.app_launched(this);
                if(prefs.getBoolean("ShowWelcomeDialog", true))
                {
                    if(!App.isPro) {
                        DialogManager.ShowWelcomeDialog(this);
                    }
                }
            }
            else
            {
                if(App.accessToken != null && !App.accessToken.equals("") && !App.isGooglePlayVersion)
                {
                    AsyncTaskTools.execute(new ValidTokenTask());
                    if(prefs.getBoolean("ShowUpdate", true))
                    {
                        VersionManager.checkUpdate(this, false);
                    }
                }
                else
                {
                    startActivity(new Intent(MainActivity.this,LoginActivity.class));
                    finish();
                }
            }
        }

		ActionBar actionBar = getSupportActionBar();
        actionBar.setIcon(android.R.color.transparent);
		actionBar.setTitle(Html.fromHtml("<font color=#f0f0f0>" + getString(R.string.app_name) + "</font>"));
		listView = (ListView)findViewById(R.id.left_drawer);
        listView.setOnItemClickListener(this);
        listView.setCacheColorHint(0);
        listView.setScrollingCacheEnabled(false);
        listView.setScrollContainer(false);
        listView.setSmoothScrollbarEnabled(true);

        if(App.isGooglePlayVersion)
            menuAdapter = new MenuArrayAdapter(this, r.getStringArray(R.array.menu_drawer_google_play));
        else
            menuAdapter = new MenuArrayAdapter(this, r.getStringArray(R.array.menu_drawer_full));

        listView.setAdapter(menuAdapter);

		mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
		
		if(mDrawerLayout != null)
        {
		    actionBar.setDisplayHomeAsUpEnabled(true);
		    actionBar.setHomeButtonEnabled(true);
            mDrawerLayout.setDrawerListener(new DrawerListener());
            mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
            
            mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,R.drawable.ic_navigation_drawer, R.string.app_name, R.string.app_name);
            mDrawerToggle.syncState();
            
			if(firstTime)
			{
				drawerIsOpened = true;
				mDrawerLayout.openDrawer(listView);
				firstTime = false;
			}
        }
        else
        {
        	actionBar.setDisplayHomeAsUpEnabled(false);
        	actionBar.setHomeButtonEnabled(false);
        }


        /*
        Editor editor = prefs.edit();
        editor.clear();
        editor.commit();*/
		String languageId = prefs.getString("prefLanguage", "0");
		if(languageId.equals("0") && !App.isGooglePlayVersion)
		{
            CharSequence[] items = null;
            if(App.phoneLanguage.equals("1"))
                items = new CharSequence[]{ getString(R.string.language_english) + " " + getString(R.string.parenthese_default), getString(R.string.language_french), getString(R.string.language_spanish) };
            else if(App.phoneLanguage.equals("2"))
                items = new CharSequence[]{ getString(R.string.language_english), getString(R.string.language_french) + " " + getString(R.string.parenthese_default), getString(R.string.language_spanish) };
            else if(App.phoneLanguage.equals("4"))
                items = new CharSequence[]{ getString(R.string.language_english), getString(R.string.language_french), getString(R.string.language_spanish) + " " + getString(R.string.parenthese_default) };

	     	final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
	     	alertBuilder.setTitle(r.getString(R.string.title_alert_languages));
	     	alertBuilder.setItems(items, new DialogInterface.OnClickListener() {
	     	    public void onClick(DialogInterface dialog, int item) {
	     	    	Editor editor = prefs.edit();
					switch(item)
					{
						case 0:
							editor.putString("prefLanguage", "1");
							break;
						case 1:
							editor.putString("prefLanguage", "2");
							break;
						case 2:
							editor.putString("prefLanguage", "4");
							break;
					}
					editor.commit();
                    app.setLocale();
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
                        Editor editor = prefs.edit();
                        editor.putString("prefLanguage", App.phoneLanguage);
                        editor.commit();
	                    alertLanguages.dismiss();
                        SetViewPager();
                        app.setLocale();
                        Utils.restartActivity(MainActivity.this);
	                }
	                return true;
	            }
	        });
            try {
                //leaked error
                alertLanguages.show();
            }catch(Exception e){}
		}
        else if(App.isGooglePlayVersion)
        {
            Editor editor = prefs.edit();
            editor.putString("prefLanguage", "4");
            editor.commit();
            SetViewPager();
        }
        else
        {
            SetViewPager();
        }

        App.SetEvent(this);
        setPagerVisibility(App.networkConnection);


        if(App.isPro)
        {
            AdView adView = (AdView)findViewById(R.id.adView);
            ((ViewGroup)adView.getParent()).removeView(adView);

       

        
            VideoCastManager.checkGooglePlaySevices(this);

            App.getCastManager(this);

            // -- Adding MiniController
            mMini = (MiniController) findViewById(R.id.miniController);
            App.mCastMgr.addMiniController(mMini);

            mCastConsumer = new VideoCastConsumerImpl();
            App.mCastMgr.reconnectSessionIfPossible(this, false);
        }
	}

    private void SetViewPager()
    {
        viewPager.setOffscreenPageLimit(1);
        mAdapter = new PagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(mAdapter);

        tabs.setViewPager(viewPager);
        tabs.setDividerColor(r.getColor(R.color.blueTab));
        tabs.setUnderlineColor(r.getColor(R.color.blueTab));
        //tabs.setTextColor(Color.parseColor("#55a73d"));
        tabs.setIndicatorColor(r.getColor(R.color.blueTab));
        tabs.setTabBackground("background_tab_darkblue");

    }

    @Override
    public void ConnectionChanged(int connectionType) {
        setPagerVisibility(connectionType);
    }

    private void setPagerVisibility(int connectionType)
    {
        if(connectionType != NetworkUtil.TYPE_NOT_CONNECTED)
        {
            //connected
            txtNoConnection.setVisibility(View.GONE);
            viewPager.setVisibility(View.VISIBLE);
            tabs.setVisibility(View.VISIBLE);
        }
        else
        {
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
	    	switch(index)
	    	{
	    		//All
	    		case 0:
	    			allFragment = AnimeListFragment.newInstance(getString(R.string.tab_all), isDesc);
	    			return allFragment;
		    	//Serie
		    	case 1:
		    		serieFragment = AnimeListFragment.newInstance(getString(R.string.tab_serie), isDesc);
		    		return serieFragment;
		    	//Movie
		    	case 2:
		    		movieFragment = AnimeListFragment.newInstance(getString(R.string.tab_movie), isDesc);
		    		return movieFragment;
		    	//Cartoon
		    	case 3:
		    		cartoonFragment = AnimeListFragment.newInstance(getString(R.string.tab_cartoon), isDesc);
		    		return cartoonFragment;
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

	  private class DrawerListener implements DrawerLayout.DrawerListener {
	        @Override
	        public void onDrawerOpened(View drawerView) {
	                mDrawerToggle.onDrawerOpened(drawerView);
	                drawerIsOpened = true;
	        }

	        @Override
	        public void onDrawerClosed(View drawerView) {
	                mDrawerToggle.onDrawerClosed(drawerView);
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
	protected void onSaveInstanceState (Bundle outState) {
	    outState.putBoolean("drawerIsOpened", drawerIsOpened);
        outState.putBoolean("isDesc", isDesc);
        if(allFragment != null && allFragment.isAdded())
            getSupportFragmentManager().putFragment(outState,"allFragment", allFragment);
        if(serieFragment != null && serieFragment.isAdded())
            getSupportFragmentManager().putFragment(outState, "serieFragment", serieFragment);
        if(movieFragment != null && movieFragment.isAdded())
            getSupportFragmentManager().putFragment(outState, "movieFragment", movieFragment);
        if(cartoonFragment != null && cartoonFragment.isAdded())
            getSupportFragmentManager().putFragment(outState, "cartoonFragment", cartoonFragment);
	    super.onSaveInstanceState(outState);



	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
        if(App.isPro)
        {
            mediaRouteMenuItem = App.mCastMgr.addMediaRouterButton(menu, R.id.media_route_menu_item);
        }
		menuItem=menu.findItem(R.id.search_widget);
		menuItem=menu.findItem(R.id.search_widget);
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
	    final SearchView searchView = (SearchView)MenuItemCompat.getActionView(menu.findItem(R.id.search_widget));
	    TextView textView = (TextView) searchView.findViewById(R.id.search_src_text);
	    if(textView != null)
	    {
	    	textView.setTextColor(Color.WHITE);
	    	textView.setHintTextColor(Color.WHITE);
	    }
	    searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
	    
	    searchView.setOnQueryTextListener(new OnQueryTextListener(){
			 
			@Override
			public boolean onQueryTextChange(String query) {
				return false;
			}

			@Override
			public boolean onQueryTextSubmit(String arg0) {

				MenuItemCompat.collapseActionView(menuItem);
				searchView.setQuery("", false);
                AnimationManager.ActivityStart(MainActivity.this);
				return false;
			}
			 
		 });
	    searchView.setOnSuggestionListener(new OnSuggestionListener(){

		@Override
		public boolean onSuggestionSelect(int position) {
			return false;
		}

		@Override
		public boolean onSuggestionClick(int position) {
			MenuItemCompat.collapseActionView(menuItem);
			searchView.setQuery("", false);
			return false;
		}
		 
	 });
		return true;
	}
	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
        menuBuyPro = menu.findItem(R.id.action_buypro);
        if(App.isPro)
            menuBuyPro.setVisible(false);

        menuSortAz = menu.findItem(R.id.action_sortaz);
        menuSortZa = menu.findItem(R.id.action_sortza);

        if(isDesc)
        {
            menuSortAz.setVisible(true);
            menuSortZa.setVisible(false);
        }
        else
        {
            menuSortAz.setVisible(false);
            menuSortZa.setVisible(true);
        }
	    return true;
	}
	@Override
	  public boolean onOptionsItemSelected(MenuItem item) {
			 switch (item.getItemId()) {
			    case android.R.id.home:
			    	if(!drawerIsOpened)
			    		mDrawerLayout.openDrawer(listView);
			    	else
			    		mDrawerLayout.closeDrawer(listView);
			    	break;
                case R.id.action_sortaz:
                    menuSortAz.setVisible(false);
                    menuSortZa.setVisible(true);
                    isDesc = false;
                    refreshFragment(allFragment, isDesc);
                    refreshFragment(serieFragment, isDesc);
                    refreshFragment(movieFragment, isDesc);
                    refreshFragment(cartoonFragment, isDesc);
                 break;
                case R.id.action_buypro:
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.topanimestream.pro")));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=com.topanimestream.pro")));
                    }

                break;
                case R.id.action_sortza:
                    menuSortAz.setVisible(true);
                    menuSortZa.setVisible(false);
                    isDesc = true;
                    refreshFragment(allFragment, isDesc);
                    refreshFragment(serieFragment, isDesc);
                    refreshFragment(movieFragment, isDesc);
                    refreshFragment(cartoonFragment, isDesc);
                    break;
			    case R.id.action_settings:
			    	startActivity(new Intent(MainActivity.this,Settings.class));
			    	break;
			 }
	    return true;
	}
    public void refreshFragment(AnimeListFragment frag, boolean isDesc)
    {
        if(frag != null)
        {
            if(frag.isAdded())
                frag.refresh();
        }
    }
	@Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, r.getString(R.string.back_again), Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
             doubleBackToExitPressedOnce=false;   

            }
        }, 2000);
    }
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
	{
        String menuItem = menuAdapter.getItem(position);
        if(menuItem.equals(getString(R.string.menu_favorites)))
        {
            startActivity(new Intent(MainActivity.this,FavoriteActivity.class));
            AnimationManager.ActivityStart(this);
        }
        else if(menuItem.equals(getString(R.string.menu_history)))
        {
            startActivity(new Intent(MainActivity.this,HistoryActivity.class));
            AnimationManager.ActivityStart(this);
        }
        else if(menuItem.equals(getString(R.string.menu_share)))
        {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            if(App.isPro)
            {
                sendIntent.putExtra(Intent.EXTRA_TEXT,
                        "https://play.google.com/store/apps/details?id=com.topanimestream.pro");
            }
            else
            {
                sendIntent.putExtra(Intent.EXTRA_TEXT,
                        "https://play.google.com/store/apps/details?id=com.topanimestream");
            }
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        }
        else if(menuItem.equals(getString(R.string.menu_settings)))
        {
            startActivity(new Intent(MainActivity.this,Settings.class));
            AnimationManager.ActivityStart(this);
        }
        else if(menuItem.equals(getString(R.string.menu_logout)))
        {
            AsyncTaskTools.execute(new LogoutTask());
        }
        else if(menuItem.equals(getString(R.string.menu_pro)))
        {
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
            busyDialog = Utils.showBusyDialog(getString(R.string.logging_out), MainActivity.this);
            URL = getString(R.string.anime_service_path);
        }

        ;

        @Override
        protected String doInBackground(Void... params) {
            if(!App.IsNetworkConnected())
            {
                return getString(R.string.error_internet_connection);
            }
            SoapObject request = new SoapObject(NAMESPACE, method);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            request.addProperty("token", App.accessToken);

            envelope.headerOut = new Element[1];
            Element lang = new Element().createElement("", "Lang");
            lang.addChild(Node.TEXT, Locale.getDefault().getLanguage());
            envelope.headerOut[0] = lang;
            envelope .dotNet = true;
            envelope.setOutputSoapObject(request);
            HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
            SoapPrimitive result = null;
            try
            {
                androidHttpTransport.call(SOAP_ACTION + method, envelope);
                result = (SoapPrimitive)envelope.getResponse();

                return null;
            }
            catch (Exception e)
            {
                if(e instanceof SoapFault)
                {
                    return e.getMessage();
                }

                e.printStackTrace();
            }
            return getString(R.string.error_logout);
        }

        @Override
        protected void onPostExecute(String error) {
            Utils.dismissBusyDialog(busyDialog);
            if(error != null)
            {
                Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG).show();
            }
            else
            {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                prefs.edit().putString("AccessToken", null).commit();
                App.accessToken = null;
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
        }
    }
    @Override
    protected void onPause()
    {
        if(App.isPro)
        {
            App.mCastMgr.decrementUiCounter();
            App.mCastMgr.removeVideoCastConsumer(mCastConsumer);
        }
        super.onPause();
    }
    @Override
    protected void onResume() {
        if(App.isPro)
        {
            App.getCastManager(this);
            if (null != App.mCastMgr) {
                App.mCastMgr.addVideoCastConsumer(mCastConsumer);
                App.mCastMgr.incrementUiCounter();
            }
        }
        super.onResume();
        if(App.languageChanged)
        {
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
        @Override
        protected void onPreExecute() {
            busyDialog = Utils.showBusyDialog(getString(R.string.logging), MainActivity.this);
            URL = getString(R.string.anime_service_path);
        }

        @Override
        protected String doInBackground(Void... params) {
            if(!App.IsNetworkConnected())
            {
                return getString(R.string.error_internet_connection);
            }
            SoapObject request = new SoapObject(NAMESPACE, method);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            request.addProperty("token", App.accessToken);

            envelope.headerOut = new Element[1];
            Element lang = new Element().createElement("", "Lang");
            lang.addChild(Node.TEXT, Locale.getDefault().getLanguage());
            envelope.headerOut[0] = lang;
            envelope .dotNet = true;
            envelope.setOutputSoapObject(request);
            HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
            SoapPrimitive result = null;
            try
            {
                androidHttpTransport.call(SOAP_ACTION + method, envelope);
                result = (SoapPrimitive)envelope.getResponse();
                isValidToken = Boolean.valueOf(result.toString());
                return null;
            }
            catch (Exception e)
            {
                if(e instanceof SoapFault)
                {
                    return e.getMessage();
                }

                e.printStackTrace();
            }
            return getString(R.string.error_login);
        }

        @Override
        protected void onPostExecute(String error) {
            try {
                Utils.dismissBusyDialog(busyDialog);
            }catch(Exception e){}
            if(error != null)
            {
                Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG).show();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
            else
            {
                if(!isValidToken)
                {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }
            }
        }
    }
}
