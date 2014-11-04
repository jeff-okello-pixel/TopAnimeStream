package com.aniblitz;

import java.util.ArrayList;

import com.aniblitz.managers.AnimationManager;
import com.aniblitz.managers.Mp4Manager;
import com.aniblitz.models.Anime;
import com.aniblitz.models.AnimeInformation;
import com.aniblitz.models.Episode;
import com.aniblitz.models.Mirror;
import com.aniblitz.models.Vk;
import com.fwwjt.pacjz173199.AdView;
import com.google.android.gms.cast.ApplicationMetadata;
import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.google.sample.castcompanionlibrary.cast.callbacks.VideoCastConsumerImpl;
import com.google.sample.castcompanionlibrary.widgets.MiniController;


import android.app.AlertDialog;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.text.Html;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
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
    private EpisodesContainerFragment episodeContainerFragment;
    private MovieVkFragment movieVkFragment;
    private AlertDialog qualityDialog;
    private VideoCastConsumerImpl mCastConsumer;
    private MenuItem mediaRouteMenuItem;
    private LinearLayout layEpisodes;
    private MiniController mMini;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.Theme_Blue);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_anime_details);
        layAnimeDetails = (LinearLayout) findViewById(R.id.layAnimeDetails);
        layEpisodes = (LinearLayout) findViewById(R.id.layEpisodes);
		r = getResources();
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		db = new SQLiteHelper(this);
		mItems = new ArrayList<String>();
		episodes = new ArrayList<Episode>();

		Bundle bundle = getIntent().getExtras();
		anime = bundle.getParcelable("Anime");
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setTitle(Html.fromHtml("<font color=#f0f0f0>" + getString(R.string.episodes_of) + " " + anime.getName() + "</font>"));

		if(anime == null || anime.getAnimeId() == 0)
		{
			Toast.makeText(this, r.getString(R.string.error_loading_episodes), Toast.LENGTH_LONG).show();
			finish();
		}

        if(savedInstanceState != null)
        {
            anime = savedInstanceState.getParcelable("anime");

        }
        if(App.isPro) {

            AdView adView = (AdView)findViewById(R.id.adView);
            ((ViewGroup)adView.getParent()).removeView(adView);
            VideoCastManager.checkGooglePlaySevices(this);

            App.getCastManager(this);

            // -- Adding MiniController
            mMini = (MiniController) findViewById(R.id.miniController);
            App.mCastMgr.addMiniController(mMini);

            mCastConsumer = new VideoCastConsumerImpl();
            App.mCastMgr.reconnectSessionIfPossible(this, false);
        }

        String language = prefs.getString("prefLanguage", "1");
        FragmentManager fm = getSupportFragmentManager();

        episodeContainerFragment = (EpisodesContainerFragment)fm.findFragmentByTag("episodeContainerFragment");
            if(episodeContainerFragment == null) {
                FragmentTransaction ft = fm.beginTransaction();
                ft.add(layEpisodes.getId(), EpisodesContainerFragment.newInstance(anime), "episodeContainerFragment");
                ft.commit();
        }


        AnimeDetailsFragment animeDetailsFragment = (AnimeDetailsFragment)fm.findFragmentById(R.id.animeDetailsFragment);
        if(animeDetailsFragment != null)
            animeDetailsFragment.setAnime(anime);


	}

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        AnimationManager.ActivityFinish(this);
    }
    @Override
    protected void onResume() {
        if(App.isPro)
        {
            App.getCastManager(this);
            if (null != App.mCastMgr) {
                App.mCastMgr.addVideoCastConsumer(mCastConsumer);
                App.mCastMgr.incrementUiCounter();
            }
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        if(App.isPro)
        {
            App.mCastMgr.decrementUiCounter();
            App.mCastMgr.removeVideoCastConsumer(mCastConsumer);
        }
        super.onPause();
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("anime", anime);
        
        super.onSaveInstanceState(outState);
    }
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.episodes, menu);
        if(App.isPro)
        {
            mediaRouteMenuItem = App.mCastMgr.addMediaRouterButton(menu, R.id.media_route_menu_item);
        }
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
                AnimationManager.ActivityFinish(this);
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
                    AnimeInformation info = anime.getAnimeInformation(this);
					db.addFavorite(anime.getAnimeId(), anime.getName(), anime.getRelativePosterPath(null), anime.getGenresFormatted(), info.getOverview() != null && !info.getOverview().equals("") ? info.getOverview() : info.getDescription(),String.valueOf(anime.getRating()), anime.getRelativeBackdropPath(null), Integer.valueOf(prefs.getString("prefLanguage", "1")));
					Toast.makeText(this, r.getString(R.string.toast_add_favorite), Toast.LENGTH_SHORT).show();
				}
			break;
		}

		return true;
	}



	@Override
	public void onEpisodeSelected(final Episode episode, String type) {
        /*
        if(episode.getVks().size() > 0)
        {
            boolean isSubbed = type.equals("Subbed") ? true : false;
            String language = prefs.getString("prefLanguage", "1");
            for(final Vk vk: episode.getVks())
            {
                if(vk.getAnimeSource().isSubbed() == isSubbed && String.valueOf(vk.getAnimeSource().getLanguageId()).equals(language))
                {
                    Mirror mirror = new Mirror(vk);
                    Mp4Manager.getMp4(mirror, AnimeDetailsActivity.this, anime, episode);

                    break;
                }

            }
        }
        else if(!App.isVkOnly)
        {*/
            Intent intent = new Intent(this, EpisodeDetailsActivity.class);
            intent.putExtra("Episode", episode);
            intent.putExtra("Type", type);
            //bug when episode + anime in the same bundle... still don't know why
            Bundle hackBundle = new Bundle();
            hackBundle.putParcelable("Anime", anime);
            intent.putExtra("hackBundle", hackBundle);
            startActivity(intent);
        //}
	}



}
