package com.aniblitz;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.aniblitz.interfaces.EpisodesLoadedEvent;
import com.aniblitz.interfaces.MovieLoadedEvent;
import com.aniblitz.models.Anime;
import com.aniblitz.models.Episode;
import com.aniblitz.models.Mirror;


import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.text.Html;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.preference.PreferenceManager;

public class AnimeDetailsActivity extends ActionBarActivity implements EpisodesContainerFragment.ProviderFragmentCoordinator{
	private ListView listViewEpisodes;
	private ArrayList<Episode> episodes;
	public ArrayList<String> mItems;
	private Dialog busyDialog;
	private Anime anime;
    private LinearLayout layAnimeDetails;
	private MenuItem menuFavorite;
	private Resources r;
	private SharedPreferences prefs;
	private SQLiteHelper db;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.Theme_Blue);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_anime_details);
        layAnimeDetails = (LinearLayout) findViewById(R.id.layAnimeDetails);
		r = getResources();
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		db = new SQLiteHelper(this);
		mItems = new ArrayList<String>();
		episodes = new ArrayList<Episode>();

		Bundle bundle = getIntent().getExtras();
		anime = bundle.getParcelable("Anime");
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setTitle(Html.fromHtml("<font color=#f0f0f0>" + getString(R.string.episodes_of) + anime.getName() + "</font>"));

		if(anime == null || anime.getAnimeId() == 0)
		{
			Toast.makeText(this, r.getString(R.string.error_loading_episodes), Toast.LENGTH_LONG).show();
			finish();
		}

        //is not a movie and the activity has no state
        if (!anime.isMovie() && savedInstanceState == null) {

        }
        else
        {
            anime = savedInstanceState.getParcelable("anime");

        }

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(layAnimeDetails.getId(),EpisodesContainerFragment.newInstance(anime.isMovie() ? "providers" : "episodes", anime));
        ft.commit();
        AnimeDetailsFragment animeDetailsFragment = (AnimeDetailsFragment)fm.findFragmentById(R.id.animeDetailsFragment);
        if(animeDetailsFragment != null)
            animeDetailsFragment.setAnime(anime);

	}
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("anime", anime);
        
        super.onSaveInstanceState(outState);
    }
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.episodes, menu);
		menuFavorite = menu.findItem(R.id.action_favorite);
		
		if(db.isFavorite(anime.getAnimeId(), prefs.getString("prefLanguage", "1")))
			menuFavorite.setIcon(R.drawable.ic_favorite);
		else
			menuFavorite.setIcon(R.drawable.ic_not_favorite);
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId())
		{
			case R.id.action_settings:
			break;
			case android.R.id.home:
				finish();
			break;
			case R.id.action_favorite:
				if(db.isFavorite(anime.getAnimeId(), prefs.getString("prefLanguage", "1")))
				{
					menuFavorite.setIcon(R.drawable.ic_not_favorite);
					db.removeFavorite(anime.getAnimeId());
					Toast.makeText(this, r.getString(R.string.toast_remove_favorite), Toast.LENGTH_SHORT).show();
				}
				else
				{
					menuFavorite.setIcon(R.drawable.ic_favorite);
					db.addFavorite(anime.getAnimeId(), anime.getName(), anime.getPosterPath(null), anime.getGenresFormatted(), anime.getDescription(), Integer.valueOf(prefs.getString("prefLanguage", "1")));
					Toast.makeText(this, r.getString(R.string.toast_add_favorite), Toast.LENGTH_SHORT).show();
				}
			break;
		}

		return true;
	}



	@Override
	public void onEpisodeSelected(Episode episode, String type) {
			Intent intent = new Intent(this, EpisodeDetailsActivity.class);
            intent.putExtra("Episode", episode);
            intent.putExtra("Type", type);
			startActivity(intent);
	}



}
