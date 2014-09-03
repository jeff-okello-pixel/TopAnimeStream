package com.aniblitz;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.json.JSONArray;
import org.json.JSONObject;

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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.aniblitz.adapters.AnimeListAdapter;
import com.aniblitz.adapters.MenuArrayAdapter;
import com.aniblitz.models.Anime;
import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;

public class MainActivity extends ActionBarActivity implements OnItemClickListener{

	private DrawerLayout mDrawerLayout;
	private boolean firstTime;
	private boolean drawerIsOpened;
	private ListView listView;
	private ActionBarDrawerToggle mDrawerToggle;
	private boolean doubleBackToExitPressedOnce;
	private MenuItem menuItem;
	private ArrayList<String> mItems;
	private MenuItem menuShowAsGrid;
	private MenuItem menuShowAsList;
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
	private ArrayList<AnimeListAdapter> animeAdapters;
	private AlertDialog alertLanguages;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.Theme_Blue);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		r = getResources();
		animeAdapters = new ArrayList<AnimeListAdapter>();
		animes = new ArrayList<Anime>();
		mItems = new ArrayList<String>();
		
		tabTitles = new String[] {r.getString(R.string.tab_all), r.getString(R.string.tab_serie), r.getString(R.string.tab_movie), r.getString(R.string.tab_cartoon)};
		
		
		viewPager = (ViewPager)findViewById(R.id.pager);
		viewPager.setOffscreenPageLimit(1);
		mAdapter = new PagerAdapter(getSupportFragmentManager());
		viewPager.setAdapter(mAdapter);
		tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		tabs.setViewPager(viewPager);
		tabs.setDividerColor(r.getColor(R.color.blueTab));
		tabs.setUnderlineColor(r.getColor(R.color.blueTab));
		//tabs.setTextColor(Color.parseColor("#55a73d"));
		tabs.setIndicatorColor(r.getColor(R.color.blueTab));
		tabs.setTabBackground("background_tab_darkblue");
		tabs.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			 
		    @Override
		    public void onPageSelected(int position) {
		        // on changing the page
		        // make respected tab selected
		       // actionbar.setSelectedNavigationItem(position);
		    }
		 
		    @Override
		    public void onPageScrolled(int arg0, float arg1, int arg2) {
		    }
		 
		    @Override
		    public void onPageScrollStateChanged(int arg0) {
		    }
		});
		

		
		if(savedInstanceState != null)
		{
			drawerIsOpened = savedInstanceState.getBoolean("drawerIsOpened");
		}
		
		ActionBar actionBar = getSupportActionBar();
		actionBar.setTitle(Html.fromHtml("<font color=#f0f0f0>" + getString(R.string.app_name) + "</font>"));
		listView = (ListView)findViewById(R.id.left_drawer);
        listView.setOnItemClickListener(this);
        listView.setCacheColorHint(0);
        listView.setScrollingCacheEnabled(false);
        listView.setScrollContainer(false);
        listView.setSmoothScrollbarEnabled(true);
        listView.setAdapter(new MenuArrayAdapter(this, r.getStringArray(R.array.menu_drawer)));
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

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Editor editor = prefs.edit();
        editor.clear();
        editor.commit();
		String languageId = prefs.getString("prefLanguage", "0");
		if(languageId.equals("0"))
		{
			final CharSequence[] items = { getString(R.string.language_english), getString(R.string.language_french), getString(R.string.language_spanish) };
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
					(new AnimeTask()).execute();
	     	    }
	     	});

	     	alertLanguages = alertBuilder.create();
	     	alertLanguages.setCancelable(false);
	     	alertLanguages.setOnKeyListener(new Dialog.OnKeyListener() {

	            @Override
	            public boolean onKey(DialogInterface arg0, int keyCode,
	                    KeyEvent event) {
	                if (keyCode == KeyEvent.KEYCODE_BACK) {
	                	(new AnimeTask()).execute();
	                    alertLanguages.dismiss();
	                }
	                return true;
	            }
	        });
	     	alertLanguages.show();
		}
		else
		{
			(new AnimeTask()).execute();
		}
	}
	public class PagerAdapter extends FragmentPagerAdapter {
	    public PagerAdapter(FragmentManager fm) {
	        super(fm);
	    }

	    @Override
	    public Fragment getItem(int index) {
	    	switch(index)
	    	{
	    		//All
	    		case 0:
	    			allFragment = AnimeListFragment.newInstance(getString(R.string.tab_all));
	    			setFragmentAnimes();
	    			return allFragment;
		    	//Serie
		    	case 1:
		    		serieFragment = AnimeListFragment.newInstance(getString(R.string.tab_serie));
		    		setFragmentAnimes();
		    		return serieFragment;
		    	//Movie
		    	case 2:
		    		movieFragment = AnimeListFragment.newInstance(getString(R.string.tab_movie));
		    		setFragmentAnimes();
		    		return movieFragment;
		    	//Cartoon
		    	case 3:
		    		cartoonFragment = AnimeListFragment.newInstance(getString(R.string.tab_cartoon));
		    		setFragmentAnimes();
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
	    outState.putBoolean("drawerIsOpened",drawerIsOpened);
	    super.onSaveInstanceState(outState);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
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

				for(AnimeListAdapter adapter:animeAdapters)
				{
					adapter.getFilter().filter(query);
				}
				return false;
			}

			@Override
			public boolean onQueryTextSubmit(String arg0) {
                /*
				MenuItemCompat.collapseActionView(menuItem);
				searchView.setQuery("", false);*/
				return false;
			}
			 
		 });
	    searchView.setOnSuggestionListener(new OnSuggestionListener(){

		@Override
		public boolean onSuggestionSelect(int position) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean onSuggestionClick(int position) {
			// TODO Auto-generated method stub
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
		menuShowAsGrid = menu.findItem(R.id.action_show_as_grid);      
		menuShowAsList = menu.findItem(R.id.action_show_as_list);
		
	    return true;
	}
	@Override
	  public boolean onOptionsItemSelected(MenuItem item) {
			 switch (item.getItemId()) {
			    case android.R.id.home:
			    	if(!drawerIsOpened)
			    	{
			    		mDrawerLayout.openDrawer(listView);
			    	}
			    	else
			    	{
			    		mDrawerLayout.closeDrawer(listView);
			    	}
			    	break;
			    case R.id.action_show_as_grid:
			    	menuShowAsGrid.setVisible(false);
			    	menuShowAsList.setVisible(true);
			    	break;
			    case R.id.action_show_as_list:
			    	menuShowAsList.setVisible(false);
			    	menuShowAsGrid.setVisible(true);
			    break;
			    case R.id.action_settings:
			    	startActivity(new Intent(MainActivity.this,Settings.class));
			    	break;
			 }
	    return true;
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
			switch(position)
			{
				case 0:
					startActivity(new Intent(MainActivity.this,FavoriteActivity.class));
					break;
				case 1:
					startActivity(new Intent(MainActivity.this,HistoryActivity.class));
					break;
				case 2:
			    	Intent sendIntent = new Intent();
			    	sendIntent.setAction(Intent.ACTION_SEND);
			    	if(App.isPro)
			    	{
				    	sendIntent.putExtra(Intent.EXTRA_TEXT,
					    	    "https://play.google.com/store/apps/details?id=com.aniblitz.pro");
			    	}
			    	else
			    	{
			    	sendIntent.putExtra(Intent.EXTRA_TEXT,
			    	    "https://play.google.com/store/apps/details?id=com.aniblitz");
			    	}
			    	sendIntent.setType("text/plain");
			    	startActivity(sendIntent);
					break;
				case 3:
					startActivity(new Intent(MainActivity.this,Settings.class));
					break;
			}

	}
private class AnimeTask extends AsyncTask<Void, Void, String> {
		
		public AnimeTask()
		{

		}
		private final String URL = "http://lanbox.ca/AnimeServices/AnimeDataService.svc/Animes?$format=json&$expand=AnimeSources,Genres,AnimeInformations&$filter=AnimeSources/any(as:as/LanguageId%20eq%20" + prefs.getString("prefLanguage", "1") + ")";
		
		@Override
	    protected void onPreExecute()
	    {
			busyDialog = Utils.showBusyDialog(r.getString(R.string.loading_anime_list), MainActivity.this);
	    };      
	    @Override
	    protected String doInBackground(Void... params)
	    {   
	    	
	    	JSONObject json = Utils.GetJson(URL);
	    	JSONArray animeArray = new JSONArray();
	    	
	    	try {
	    		animeArray = json.getJSONArray("value");
			} catch (Exception e) {
				return null;
			}
	    	for(int i = 0;i<animeArray.length();i++)
	    	{
	    		JSONObject animeJson;
				try {
					animeJson = animeArray.getJSONObject(i);
		    		animes.add(new Anime(animeJson));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

	    	}
		    return "Success";
		}     
		    
	    @Override
	    protected void onPostExecute(String result)
	    {
	    	if(result == null)
	    	{
	    		Toast.makeText(MainActivity.this, r.getString(R.string.error_loading_animes), Toast.LENGTH_LONG).show();
	    	}
	    	Collections.sort(animes, new Comparator<Anime>() {
	            public int compare(Anime a1, Anime a2) {
	                return a1.getName().toLowerCase().compareTo(a2.getName().toLowerCase());
	            }
	        });

	    	setFragmentAnimes();
	    	Utils.dismissBusyDialog(busyDialog);
	    }
	
	}
	private void setFragmentAnimes()
	{
		animeAdapters.clear();
		AnimeListAdapter adapter = null;
    	if(allFragment != null)
    	{
    		adapter = allFragment.setAnimes(animes, getString(R.string.tab_all));
    		if(adapter != null)
    			animeAdapters.add(adapter);
    	}
    	if(serieFragment != null)
    	{
    		adapter = serieFragment.setAnimes(animes, getString(R.string.tab_serie));
    		if(adapter != null)
    			animeAdapters.add(adapter);
    	}
    	if(movieFragment != null)
    	{
    		adapter = movieFragment.setAnimes(animes, getString(R.string.tab_movie));
    		if(adapter != null)
    			animeAdapters.add(adapter);
    	}
    	if(cartoonFragment != null)
    	{
    		adapter = cartoonFragment.setAnimes(animes, getString(R.string.tab_cartoon));
    		if(adapter != null)
    			animeAdapters.add(adapter);
    	}
	}
}
