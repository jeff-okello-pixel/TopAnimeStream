package com.aniblitz;

import java.util.ArrayList;
import com.aniblitz.adapters.AnimeListAdapter;
import com.aniblitz.models.Anime;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FavoriteActivity extends ActionBarActivity implements OnItemClickListener, OnItemLongClickListener {
	private ListView listView;
	private ArrayList<Anime> animes;
	private int animeId;
	private TextView txtNoFavorite;
	private Resources r;
	private AlertDialog alertProviders;
	private SharedPreferences prefs;
	  @Override
	  protected void onCreate(Bundle savedInstanceState) {
		  setTheme(R.style.Theme_Blue);
		  super.onCreate(savedInstanceState);
		  setContentView(R.layout.activity_favorite);
		  r = getResources();
		  prefs = PreferenceManager.getDefaultSharedPreferences(this);
		  txtNoFavorite = (TextView)findViewById(R.id.txtNoFavorite);
		  ActionBar actionBar = getSupportActionBar();
		  actionBar.setDisplayHomeAsUpEnabled(true);
		  actionBar.setTitle(Html.fromHtml("<font color=#f0f0f0>" + getString(R.string.title_favorites) + "</font>"));
		  listView = (ListView)findViewById(R.id.listView);
		  listView.setOnItemClickListener(this);
		  listView.setOnItemLongClickListener(this);

	  }
	  @Override
	  public boolean onOptionsItemSelected(MenuItem item) {
			 switch (item.getItemId()) {
			    case android.R.id.home:
			    	finish();
			    	break;
			 }
	    return true;
	  }
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Anime anime = animes.get(position);
			animeId = anime.getAnimeId();
			if(!anime.isMovie())
			{
				Intent intent = new Intent(this,EpisodesActivity.class);
				Bundle bundle = new Bundle();
				bundle.putParcelable("Anime", anime);
				intent.putExtras(bundle);
				startActivity(intent);
			}
			else
			{
				(new Utils.MirrorsTask(this,animeId)).execute();
			}
			
		}
		
		@Override
		protected void onResume() 
		{
		    super.onResume();
		    populateList();
				
			
		}
		private void populateList()
		{
			SQLiteHelper sqlLite = new SQLiteHelper(this);
			animes = sqlLite.getFavorites(prefs.getString("prefLanguage", "1"));
			listView.setAdapter(new AnimeListAdapter(this,animes));
			if(animes.size() > 0)
				txtNoFavorite.setVisibility(View.GONE);
			else
				txtNoFavorite.setVisibility(View.VISIBLE);
		}
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				final int position, long id) {
			final CharSequence[] items = {r.getString(R.string.remove_favorite),r.getString(R.string.cancel)};
		 	final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
		 	alertBuilder.setTitle(r.getString(R.string.title_favorites));
		 	alertBuilder.setItems(items, new DialogInterface.OnClickListener() {
		 	    public void onClick(DialogInterface dialog, int item) {
		 	    	if(item == 0)
		 	    	{
		 	    		SQLiteHelper sqlLite = new SQLiteHelper(FavoriteActivity.this);
		 	    		sqlLite.removeFavorite(animes.get(position).getAnimeId());
		 	    		populateList();
		 	    		Toast.makeText(FavoriteActivity.this, r.getString(R.string.toast_remove_favorite), Toast.LENGTH_SHORT).show();
		 	    	}
		 	    	else
		 	    	{
		 	    		alertProviders.dismiss();
		 	    	}
		 	    		
		 	    		
		 	    }
		 	});
		 	alertProviders = alertBuilder.create();
		 	alertProviders.show();
			return true;
		}

}
