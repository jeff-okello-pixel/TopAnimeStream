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

public class AnimeSearchActivity extends ActionBarActivity implements OnItemClickListener {
    private ArrayList<Anime> animes;
    private String query;
    private ListView listView;
    public ArrayList<String> mItems;
    private Dialog busyDialog;
    private TextView txtNoResult;
    private MenuItem menuItem;
    public AlertDialog alertType;
    private Resources r;
    public ArrayList<AnimeSource> animeSources;
    private AlertDialog alertProviders;
    private Toolbar toolbar;
    public int animeId;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_Blue);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anime_search);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        r = getResources();
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        txtNoResult = (TextView) findViewById(R.id.txtNoResult);
        animes = new ArrayList<Anime>();
        mItems = new ArrayList<String>();
        query = getIntent().getStringExtra(SearchManager.QUERY);
        Utils.SaveRecentSearch(this, query);
        query = query.replace(" ", "%20");
        listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(this);

        if (toolbar != null) {
            toolbar.setTitle(query);
            toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
            setSupportActionBar(toolbar);
        }
        (new SearchAnimeTask()).execute();
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

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        AnimationManager.ActivityFinish(this);
    }

    private class SearchAnimeTask extends AsyncTask<Void, Void, String> {

        public SearchAnimeTask() {

        }

        private String URL;

        protected void onPreExecute() {
            try {
                URL = new WcfDataServiceUtility(getString(R.string.anime_data_service_path)).getEntity("Search").formatJson().addParameter("query", "%27" + URLEncoder.encode(query, "UTF-8").replace("%27", "%27%27") + "%27").filter("AnimeSources/any(as:as/LanguageId%20eq%20" + PrefUtils.get(App.getContext(), Prefs.LOCALE, "1") + ")").expand("AnimeSources,Genres,AnimeInformations,Links").build();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            busyDialog = DialogManager.showBusyDialog(getString(R.string.loading_search), AnimeSearchActivity.this);
            animes = new ArrayList<Anime>();
            mItems = new ArrayList<String>();
            listView.setAdapter(null);
        }

        @Override
        protected String doInBackground(Void... params) {

            JSONObject json = Utils.GetJson(URL);
            if(json == null)
                return null;
            if (!json.isNull("error")) {
                try {
                    int error = json.getInt("error");
                    if (error == 401) {
                        return "401";
                    }
                } catch (Exception e) {
                    return null;
                }
            }
            JSONArray animeArray = new JSONArray();

            try {
                animeArray = json.getJSONArray("value");
            } catch (Exception e) {
                return null;
            }
            Gson gson = new Gson();
            for (int i = 0; i < animeArray.length(); i++) {
                JSONObject animeJson;
                try {
                    animeJson = animeArray.getJSONObject(i);
                    animes.add(gson.fromJson(animeJson.toString(), Anime.class));
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            return "Success";
        }

        @Override
        protected void onPostExecute(String result) {
            if (result == null) {
                Toast.makeText(AnimeSearchActivity.this, r.getString(R.string.error_loading_animes), Toast.LENGTH_LONG).show();
                finish();
                return;
            } else if (result.equals("401")) {
                Toast.makeText(AnimeSearchActivity.this, getString(R.string.have_been_logged_out), Toast.LENGTH_LONG).show();
                startActivity(new Intent(AnimeSearchActivity.this, LoginActivity.class));
                finish();
                return;
            }
            if (animes.size() > 0) {
                txtNoResult.setVisibility(View.GONE);
                listView.setAdapter(new AnimeListAdapter(AnimeSearchActivity.this, animes));
            } else {
                txtNoResult.setVisibility(View.VISIBLE);
            }
            DialogManager.dismissBusyDialog(busyDialog);
        }

    }
}
