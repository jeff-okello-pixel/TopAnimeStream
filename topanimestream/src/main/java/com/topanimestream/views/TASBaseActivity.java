package com.topanimestream.views;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;
import com.topanimestream.models.Episode;
import com.topanimestream.utilities.SQLiteHelper;

import java.util.ArrayList;

public class TASBaseActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState, int layoutId) {
        setTheme(R.style.Theme_Blue);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anime_details);
        currentUserReview = null;
        layAnimeDetails = (LinearLayout) findViewById(R.id.layAnimeDetails);
        layEpisodes = (LinearLayout) findViewById(R.id.layEpisodes);
        r = getResources();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        db = new SQLiteHelper(this);
        mItems = new ArrayList<String>();
        episodes = new ArrayList<Episode>();

        Bundle bundle = getIntent().getExtras();
        anime = bundle.getParcelable("Anime");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        if (toolbar != null) {
            toolbar.setTitle(getString(R.string.episodes_of) + " " + anime.getName());
            toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
            setSupportActionBar(toolbar);
        }

        if (anime == null || anime.getAnimeId() == 0) {
            Toast.makeText(this, r.getString(R.string.error_loading_anime_details), Toast.LENGTH_LONG).show();
            finish();
        }

        if (savedInstanceState != null) {
            anime = savedInstanceState.getParcelable("anime");

        }


    }




}
