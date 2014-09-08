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
import android.widget.ListView;
import android.widget.Toast;
import android.preference.PreferenceManager;

public class AnimeDetailsActivity extends ActionBarActivity implements EpisodesContainerFragment.ProviderFragmentCoordinator {
	private ListView listViewEpisodes;
	private ArrayList<Episode> episodes;
	public ArrayList<String> mItems;
	private Dialog busyDialog;
	private Anime anime;

	private MenuItem menuFavorite;
	private Resources r;
	private SharedPreferences prefs;
	private SQLiteHelper db;
    private ProviderListFragment providerListFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.Theme_Blue);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_anime_details);
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
        //is a movie and the activity has no state
        if(anime.isMovie() && savedInstanceState == null)
        {
            AsyncTaskTools.execute(new LoadProvidersTask());
        }
        //is not a movie and the activity has no state
        else if (!anime.isMovie() && savedInstanceState == null) {
        	(new AnimeEpisodesTask()).execute();
        }
        else
        {
            anime = savedInstanceState.getParcelable("anime");

        }

        FragmentManager fm = getSupportFragmentManager();
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
private class AnimeEpisodesTask extends AsyncTask<Void, Void, String> {
		
		public AnimeEpisodesTask()
		{

		}
		private final String URL = "http://lanbox.ca/AnimeServices/AnimeDataService.svc/Episodes()?$filter=AnimeId%20eq%20" + anime.getAnimeId() + "%20and%20Mirrors/any(m:m/AnimeSource/LanguageId%20eq%20" + prefs.getString("prefLanguage", "1") + ")&$expand=Mirrors/AnimeSource,Mirrors/Provider,EpisodeInformations&$format=json";
		
		@Override
	    protected void onPreExecute()
	    {
			busyDialog = Utils.showBusyDialog(getString(R.string.loading_anime_details), AnimeDetailsActivity.this);
	    };      
	    @Override
	    protected String doInBackground(Void... params)
	    {   
	    	
	    	JSONObject json = Utils.GetJson(URL);
	    	JSONArray episodesArray = new JSONArray();

	    	try {
	    		episodesArray = json.getJSONArray("value");
			} catch (Exception e) {
				return null;
			}
	    	for(int i = 0;i<episodesArray.length();i++)
	    	{
	    		JSONObject episodeJson;
				try {
					episodeJson = episodesArray.getJSONObject(i);
					Episode episode = new Episode(episodeJson);
					
					episodes.add(episode);
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
	    		Toast.makeText(AnimeDetailsActivity.this, r.getString(R.string.error_loading_episodes), Toast.LENGTH_LONG).show();
	    		finish();
	    		return;
	    	}
	    	EpisodesLoadedEvent event = (EpisodesLoadedEvent) getSupportFragmentManager().findFragmentById(R.id.episodeListFragment);
	    	event.onEpisodesLoaded(episodes, anime.getName(), anime.getDescription(), anime.getPosterPath(null));
	        Utils.dismissBusyDialog(busyDialog);
	    }
	
	}
	@Override
	public void onEpisodeSelected(Episode episode, String type) {
			Intent intent = new Intent(this, EpisodeDetailsActivity.class);
            intent.putExtra("Episode", episode);
            intent.putExtra("Type", type);
			startActivity(intent);
	}
    public class LoadProvidersTask extends AsyncTask<Void, Void, String> {
        private String URL;
        private ArrayList<Mirror> mirrors;
        private Dialog busyDialog;

        @Override
        protected void onPreExecute()
        {
            busyDialog = Utils.showBusyDialog(getString(R.string.loading_anime_details), AnimeDetailsActivity.this);
            URL = new WcfDataServiceUtility(getString(R.string.anime_service_path)).getTableSpecificRow("AnimeSources",anime.getAnimeSources().get(0).getAnimeSourceId(),false).formatJson().expand("Mirrors/Provider").build();

        };
        @Override
        protected String doInBackground(Void... params)
        {

            JSONObject json = Utils.GetJson(URL);
            JSONArray mirrorArray = new JSONArray();
            try {
                mirrors = new ArrayList<Mirror>();
                mirrorArray = json.getJSONArray("Mirrors");
            } catch (JSONException e) {
                return null;
            }
            for(int i = 0;i<mirrorArray.length();i++)
            {
                try {
                    mirrors.add(new Mirror(mirrorArray.getJSONObject(i)));
                } catch (Exception e) {
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
                Toast.makeText(AnimeDetailsActivity.this, getString(R.string.error_loading_anime_details), Toast.LENGTH_LONG).show();
                return;
            }
            else
            {
                MovieLoadedEvent event = (MovieLoadedEvent) getSupportFragmentManager().findFragmentById(R.id.episodeListFragment);
                event.onMovieLoaded(mirrors);
            }

            Utils.dismissBusyDialog(busyDialog);


        }

    }


}
