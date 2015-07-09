package com.topanimestream.views;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.support.v7.widget.SearchView.OnSuggestionListener;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.topanimestream.App;
import com.topanimestream.managers.DialogManager;
import com.topanimestream.preferences.Prefs;
import com.topanimestream.utilities.PrefUtils;
import com.topanimestream.utilities.Utils;
import com.topanimestream.utilities.WcfDataServiceUtility;
import com.topanimestream.adapters.AnimeListAdapter;
import com.topanimestream.managers.AnimationManager;
import com.topanimestream.models.Anime;
import com.topanimestream.models.AnimeSource;
import com.topanimestream.R;
import com.topanimestream.views.profile.LoginActivity;

public class AnimeSearchActivity extends ActionBarActivity  {
    private String query;
    private MenuItem menuItem;
    private Toolbar toolbar;
    public int animeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_Blue);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anime_search);

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        query = getIntent().getStringExtra(SearchManager.QUERY);
        Utils.SaveRecentSearch(this, query);
        query = query.replace(" ", "%20");

        if (toolbar != null) {
            toolbar.setTitle(query);
            toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
            setSupportActionBar(toolbar);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search, menu);
        menuItem = menu.findItem(R.id.search_widget);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.search_widget));
        TextView textView = (TextView) searchView.findViewById(R.id.search_src_text);
        if (textView != null) {
            textView.setTextColor(Color.WHITE);
            textView.setHintTextColor(Color.WHITE);
        }
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        searchView.setOnQueryTextListener(new OnQueryTextListener() {

            @Override
            public boolean onQueryTextChange(String arg0) {

                return false;
            }

            @Override
            public boolean onQueryTextSubmit(String arg0) {
                MenuItemCompat.collapseActionView(menuItem);
                searchView.setQuery("", false);
                return false;
            }

        });
        searchView.setOnSuggestionListener(new OnSuggestionListener() {

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
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                AnimationManager.ActivityFinish(this);
                break;
            case R.id.action_settings:
                break;
        }

        return true;
    }
/*
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        Anime anime = animes.get(position);
        //HD
        if(anime.getLinks() != null && anime.getLinks().size() > 0) {
            animeId = anime.getAnimeId();

            Intent intent = new Intent(this, AnimeDetailsActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable("Anime", anime);
            intent.putExtras(bundle);
            startActivity(intent);

        }
        else
        {
            Intent intent = new Intent(this, OldAnimeDetailsActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable("Anime", anime);
            intent.putExtras(bundle);
            startActivity(intent);
        }
        AnimationManager.ActivityStart(this);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        query = intent.getStringExtra(SearchManager.QUERY);
        MenuItemCompat.collapseActionView(menuItem);
        toolbar.setTitle(query);
        Utils.SaveRecentSearch(this, query);
        query = query.replace(" ", "%20");
        (new SearchAnimeTask()).execute();

    }*/

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        AnimationManager.ActivityFinish(this);
    }
}
