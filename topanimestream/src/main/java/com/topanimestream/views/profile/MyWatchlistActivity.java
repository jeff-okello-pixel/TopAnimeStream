package com.topanimestream.views.profile;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

import com.topanimestream.utilities.SQLiteHelper;
import com.topanimestream.adapters.HistoryListAdapter;
import com.topanimestream.managers.AnimationManager;
import com.topanimestream.models.Anime;
import com.topanimestream.R;
import com.topanimestream.views.AnimeDetailsActivity;

public class MyWatchlistActivity extends ActionBarActivity implements OnItemClickListener {
    private ListView listView;
    private ArrayList<Anime> animes;
    private int animeId;
    private TextView txtNoWatch;
    private Resources r;
    private AlertDialog alertProviders;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_Blue);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        r = getResources();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        txtNoWatch = (TextView) findViewById(R.id.txtNoWatch);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(Html.fromHtml("<font color=#f0f0f0>" + getString(R.string.title_history) + "</font>"));
        listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(this);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                AnimationManager.ActivityFinish(this);
                break;
        }
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        Anime anime = animes.get(position);
        animeId = anime.getAnimeId();
        Intent intent = new Intent(this, AnimeDetailsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("Anime", anime);
        intent.putExtras(bundle);
        startActivity(intent);

    }

    @Override
    protected void onResume() {
        super.onResume();
        populateList();


    }

    private void populateList() {
        SQLiteHelper sqlLite = new SQLiteHelper(this);
        animes = sqlLite.GetHistory(prefs.getString("prefLanguage", "1"));
        sqlLite.close();
        Collections.reverse(animes);
        listView.setAdapter(new HistoryListAdapter(this, animes));
        if (animes.size() > 0)
            txtNoWatch.setVisibility(View.GONE);
        else
            txtNoWatch.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        AnimationManager.ActivityFinish(this);
    }

}
