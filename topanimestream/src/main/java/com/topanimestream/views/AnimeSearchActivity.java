package com.topanimestream.views;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import com.topanimestream.utilities.ToolbarUtils;
import com.topanimestream.managers.AnimationManager;
import com.topanimestream.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class AnimeSearchActivity extends AppCompatActivity {
    private AnimeListFragment mFragment;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    @InjectView(R.id.searchview)
    SearchView mSearchview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anime_search);
        ButterKnife.inject(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ToolbarUtils.updateToolbarHeight(this, toolbar);


        mSearchview.onActionViewExpanded();
        mSearchview.setOnQueryTextListener(mSearchListener);



        if (null != savedInstanceState) {
            mFragment = (AnimeListFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
            return;
        }

        //create and add the media fragment
        mFragment =
                AnimeListFragment.newInstance("Search", AnimeListFragment.Mode.SEARCH, "", "");

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, mFragment).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search, menu);
        return true;
    }

    private SearchView.OnQueryTextListener mSearchListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String s) {
            if (null == mFragment) return false;//fragment not added yet.
            mFragment.triggerSearch(s);
            return true;
        }

        @Override
        public boolean onQueryTextChange(String s) {
            if (s.equals("")) {
                onQueryTextSubmit(s);
            }
            return false;
        }
    };
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
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
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
