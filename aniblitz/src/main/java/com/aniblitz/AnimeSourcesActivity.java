package com.aniblitz;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.aniblitz.models.Anime;
import com.aniblitz.models.Episode;

import com.aniblitz.R;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.os.Build;

public class AnimeSourcesActivity extends ActionBarActivity {

	private int animeId;
	private ListView listViewSources;
	private Resources r;
	public ArrayList<String> mItems;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_anime_episodes);
		r = this.getResources();
		mItems = new ArrayList<String>();
		animeId = getIntent().getIntExtra("AnimeId", 0);
		if(animeId == 0 )
		{
			Toast.makeText(this, r.getString(R.string.error_loading_sources), Toast.LENGTH_LONG).show();
		}
		(new AnimeEpisodesTask()).execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.anime_search, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
private class AnimeEpisodesTask extends AsyncTask<Void, Void, String> {
		
		public AnimeEpisodesTask()
		{

		}
		private final String URL = "http://lanbox.ca/AnimeServices/AnimeDataService.svc/Animes(" + animeId + ")?$format=json&$expand=Episodes";
		
		@Override
	    protected void onPreExecute()
	    {
			
	    };      
	    @Override
	    protected String doInBackground(Void... params)
	    {   
	    	
	    	JSONObject json = Utils.GetJson(URL);
	    	JSONObject mainJson = new JSONObject();
	    	JSONArray episodesArray = new JSONArray();
	    	try {
	    		mainJson = json.getJSONObject("value");
	    		episodesArray = mainJson.getJSONArray("Episodes");
			} catch (Exception e) {
				return null;
			}
	    	for(int i = 0;i<episodesArray.length();i++)
	    	{
	    		JSONObject episodeJson;
				try {
					episodeJson = episodesArray.getJSONObject(i);
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
	    		Toast.makeText(AnimeSourcesActivity.this, r.getString(R.string.error_loading_sources), Toast.LENGTH_LONG).show();
	    		finish();
	    		return;
	    	}
	    }
	
	}

}
