package com.topanimestream.views;

import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import com.topanimestream.utilities.ToolbarUtils;
import com.topanimestream.R;

import butterknife.Bind;

public class AnimeSearchActivity extends TASBaseActivity {
    private AnimeListFragment mFragment;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.searchview)
    SearchView mSearchview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_anime_search);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ToolbarUtils.updateToolbarHeight(this, toolbar);

        mSearchview.onActionViewExpanded();
        mSearchview.setOnQueryTextListener(mSearchListener);

        if (null != savedInstanceState) {
            mFragment = (AnimeListFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
            return;
        }

        mFragment =
                AnimeListFragment.newInstance("Search", AnimeListFragment.Mode.SEARCH, "", "");

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, mFragment).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
