package com.aniblitz;

import java.util.ArrayList;

import com.aniblitz.models.Mirror;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class ProviderActivity extends ActionBarActivity {

	private int animeId;
	private Resources r;
	private SharedPreferences prefs;
	private ArrayList<Mirror> mirrors;
	private ProviderListFragment providerListFragment;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.Theme_Blue);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_providers);
		r = getResources();
		animeId = getIntent().getIntExtra("AnimeId", 0);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setTitle(Html.fromHtml("<font color=#f0f0f0>" + r.getString(R.string.title_providers) + "</font>"));
		Bundle bundle = getIntent().getExtras();
		mirrors = bundle.getParcelableArrayList("Mirrors");
		
		if(mirrors == null)
		{
			Toast.makeText(this, r.getString(R.string.error_loading_providers), Toast.LENGTH_LONG).show();
			finish();
		}
		
		providerListFragment = (ProviderListFragment) getSupportFragmentManager().findFragmentById(R.id.providerListFragment);
		
		providerListFragment.setProviders(mirrors);
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
