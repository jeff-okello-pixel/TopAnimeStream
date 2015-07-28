package com.topanimestream.views.profile;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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
import com.topanimestream.views.TASBaseActivity;

import butterknife.Bind;

public class MyWatchlistActivity extends TASBaseActivity implements OnItemClickListener {

    private ArrayList<Anime> animes;

    @Bind(R.id.txtNoWatch)
    TextView txtNoWatch;

    @Bind(R.id.listViewHistory)
    ListView listViewHistory;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_history);

        toolbar.setTitle(getString(R.string.title_history));
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        setSupportActionBar(toolbar);

        listViewHistory.setOnItemClickListener(this);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        Anime anime = animes.get(position);
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
        animes = sqlLite.GetHistory();
        sqlLite.close();
        Collections.reverse(animes);
        listViewHistory.setAdapter(new HistoryListAdapter(this, animes));
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
