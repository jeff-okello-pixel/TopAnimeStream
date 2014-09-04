package com.aniblitz;

import com.aniblitz.models.Episode;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class EpisodeDetailsActivity extends ActionBarActivity {

	private int animeId;
	private Resources r;
	private SharedPreferences prefs;
	private Episode episode;
    private String type;
	private ProviderListFragment providerListFragment;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.Theme_Blue);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_episode_details);
		r = getResources();
		animeId = getIntent().getIntExtra("AnimeId", 0);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		Bundle bundle = getIntent().getExtras();
        episode = (Episode)bundle.getParcelable("Episode");
        type = bundle.getString("Type");
		
		if(episode == null)
		{
			Toast.makeText(this, getString(R.string.error_loading_episode_details), Toast.LENGTH_LONG).show();
			finish();
		}
        String episodeName;
        if(episode.getEpisodeName() != null && !episode.getEpisodeName().equals(""))
            episodeName = episode.getEpisodeName();
        else
            episodeName = getString(R.string.episode) + episode.getEpisodeNumber();

        actionBar.setTitle(Html.fromHtml("<font color=#f0f0f0>" + episodeName + "</font>"));

		providerListFragment = (ProviderListFragment) getSupportFragmentManager().findFragmentById(R.id.providerListFragment);
		
		providerListFragment.setProviders(episode.getMirrors(), type);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		
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
		}

		return true;
	}


}
