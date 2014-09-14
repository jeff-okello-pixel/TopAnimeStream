package com.aniblitz;

import com.aniblitz.models.Episode;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class EpisodeDetailsActivity extends ActionBarActivity {

	private int animeId;
	private Resources r;
	private SharedPreferences prefs;
	private Episode episode;
    private ImageView imgScreenshot;
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
        imgScreenshot = (ImageView)findViewById(R.id.imgScreenshot);
        String episodeName;
        if(episode.getEpisodeName() != null && !episode.getEpisodeName().equals(""))
            episodeName = episode.getEpisodeName();
        else
            episodeName = getString(R.string.episode) + episode.getEpisodeNumber();

        actionBar.setTitle(Html.fromHtml("<font color=#f0f0f0>" + episodeName + "</font>"));
        if(episode.getScreenshot() != null && !episode.getScreenshot().equals(""))
            App.imageLoader.displayImage(getString(R.string.image_host_path) + episode.getScreenshot(),imgScreenshot);
        else
            imgScreenshot.setVisibility(View.GONE);

        FragmentManager fm = getSupportFragmentManager();
        providerListFragment = (ProviderListFragment)fm.findFragmentByTag("providerFragment");
        if(providerListFragment == null) {

            FragmentTransaction ft = fm.beginTransaction();
            providerListFragment = ProviderListFragment.newInstance(-1, episode.getMirrors(), type);
            ft.add(R.id.layEpisodeDetails, providerListFragment, "providerFragment");
            ft.commit();
        }
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId())
		{
			case android.R.id.home:
				finish();
			break;
		}

		return true;
	}


}
