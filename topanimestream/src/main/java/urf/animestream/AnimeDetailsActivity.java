package urf.animestream;

import java.util.ArrayList;

import urf.animestream.R;
import urf.animestream.managers.AnimationManager;
import urf.animestream.managers.DialogManager;
import urf.animestream.models.Anime;
import urf.animestream.models.AnimeInformation;
import urf.animestream.models.AnimeSource;
import urf.animestream.models.Episode;
import com.fwwjt.pacjz173199.AdView;
import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.google.sample.castcompanionlibrary.cast.callbacks.VideoCastConsumerImpl;
import com.google.sample.castcompanionlibrary.widgets.MiniController;


import android.app.AlertDialog;
import android.os.AsyncTask;
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

import org.json.JSONArray;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

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
        actionBar.setDisplayShowHomeEnabled(false);
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
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        AnimationManager.ActivityFinish(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("anime", anime);
        
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.episodes, menu);
        if(App.isPro)
        {
            mediaRouteMenuItem = App.mCastMgr.addMediaRouterButton(menu, R.id.media_route_menu_item);
        }
		menuFavorite = menu.findItem(R.id.action_favorite);

        if(App.isGooglePlayVersion) {
            if (db.isFavorite(anime.getAnimeId(), prefs.getString("prefLanguage", "1")))
                menuFavorite.setIcon(R.drawable.ic_favorite);
            else
                menuFavorite.setIcon(R.drawable.ic_not_favorite);
        }
        else
        {
            AsyncTaskTools.execute(new IsFavoriteTask());
        }
		
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
               if(App.isGooglePlayVersion)
               {
                   //4 because it is spanish only
                    if(db.isFavorite(anime.getAnimeId(), "4")) {
                        menuFavorite.setIcon(R.drawable.ic_not_favorite);
                        db.removeFavorite(anime.getAnimeId());
                        Toast.makeText(this, r.getString(R.string.toast_remove_favorite), Toast.LENGTH_SHORT).show();
                    }else
                    {
                        menuFavorite.setIcon(R.drawable.ic_favorite);
                        AnimeInformation info = anime.getAnimeInformation(this);
                        String description = "";
                        if(info != null)
                        {
                            description = info.getOverview() != null && !info.getOverview().equals("") ? info.getOverview() : info.getDescription();
                        }
                        db.addFavorite(anime.getAnimeId(), anime.getName(), anime.getRelativePosterPath(null), anime.getGenresFormatted(), description, String.valueOf(anime.getRating()), anime.getRelativeBackdropPath(null), 4);
                        Toast.makeText(this, r.getString(R.string.toast_add_favorite), Toast.LENGTH_SHORT).show();
                    }
                }
				else
				{
                    if(menuFavorite.getIcon().getConstantState().equals(getResources().getDrawable(R.drawable.ic_favorite).getConstantState())) {
                        AsyncTaskTools.execute(new RemoveFavoriteTask(anime.getAnimeId()));
                    }
                    else
                    {
                        AsyncTaskTools.execute(new AddFavoriteTask(anime.getAnimeId()));
                    }
				}
			break;
		}

		return true;
	}
    private class RemoveFavoriteTask extends AsyncTask<Void, Void, String> {
        private Dialog busyDialog;
        private int animeId;
        public RemoveFavoriteTask(int animeId) {
            this.animeId = animeId;
        }
        private static final String NAMESPACE = "http://tempuri.org/";
        final String SOAP_ACTION = "http://tempuri.org/IAnimeService/";
        private String URL;
        private String method = "RemoveFromFavorite";

        @Override
        protected void onPreExecute() {
            busyDialog = DialogManager.showBusyDialog(getString(R.string.deleting_from_favorites), AnimeDetailsActivity.this);
            URL = getString(R.string.anime_service_path);
        }

        @Override
        protected String doInBackground(Void... params) {
            if(!App.IsNetworkConnected())
            {
                return getString(R.string.error_internet_connection);
            }
            SoapObject request = new SoapObject(NAMESPACE, method);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            request.addProperty("animeId", animeId);
            envelope = Utils.addAuthentication(envelope);
            envelope .dotNet = true;
            envelope.setOutputSoapObject(request);
            HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
            SoapPrimitive result = null;
            try
            {
                androidHttpTransport.call(SOAP_ACTION + method, envelope);
                result = (SoapPrimitive)envelope.getResponse();
                return null;
            }
            catch (Exception e)
            {
                if(e instanceof SoapFault)
                {
                    return e.getMessage();
                }

                e.printStackTrace();
            }
            return getString(R.string.error_remove_favorite);
        }

        @Override
        protected void onPostExecute(String error) {
            Utils.dismissBusyDialog(busyDialog);
            if(error != null)
            {
                Toast.makeText(AnimeDetailsActivity.this, error, Toast.LENGTH_LONG).show();
            }
            else
            {
                menuFavorite.setIcon(R.drawable.ic_not_favorite);
                Toast.makeText(AnimeDetailsActivity.this, r.getString(R.string.toast_remove_favorite), Toast.LENGTH_SHORT).show();
            }
        }
    }
    private class AddFavoriteTask extends AsyncTask<Void, Void, String> {
        private Dialog busyDialog;
        private int animeId;
        public AddFavoriteTask(int animeId) {
            this.animeId = animeId;
        }
        private static final String NAMESPACE = "http://tempuri.org/";
        final String SOAP_ACTION = "http://tempuri.org/IAnimeService/";
        private String URL;
        private String method = "AddToFavorite";

        @Override
        protected void onPreExecute() {
            busyDialog = DialogManager.showBusyDialog(getString(R.string.adding_to_favorites), AnimeDetailsActivity.this);
            URL = getString(R.string.anime_service_path);
        }

        @Override
        protected String doInBackground(Void... params) {
            if(!App.IsNetworkConnected())
            {
                return getString(R.string.error_internet_connection);
            }
            SoapObject request = new SoapObject(NAMESPACE, method);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            request.addProperty("animeId", animeId);
            envelope = Utils.addAuthentication(envelope);
            envelope .dotNet = true;
            envelope.setOutputSoapObject(request);
            HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
            SoapPrimitive result = null;
            try
            {
                androidHttpTransport.call(SOAP_ACTION + method, envelope);
                result = (SoapPrimitive)envelope.getResponse();
                return null;
            }
            catch (Exception e)
            {
                if(e instanceof SoapFault)
                {
                    return e.getMessage();
                }

                e.printStackTrace();
            }
            return getString(R.string.error_remove_favorite);
        }

        @Override
        protected void onPostExecute(String error) {
            Utils.dismissBusyDialog(busyDialog);
            if(error != null)
            {
                Toast.makeText(AnimeDetailsActivity.this, error, Toast.LENGTH_LONG).show();
            }
            else
            {
                menuFavorite.setIcon(R.drawable.ic_favorite);
                Toast.makeText(AnimeDetailsActivity.this, r.getString(R.string.toast_add_favorite), Toast.LENGTH_SHORT).show();
            }
        }
    }
    private class IsFavoriteTask extends AsyncTask<Void, Void, String> {
        private String url;
        private String username;
        private boolean isFavorite = false;
        public IsFavoriteTask() {

        }

        @Override
        protected void onPreExecute() {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AnimeDetailsActivity.this);
            username = prefs.getString("Username", null);
            url = new WcfDataServiceUtility(getString(R.string.anime_data_service_path)).getEntity("Accounts").formatJson().expand("Favorites/Anime/AnimeSources").filter("Username%20eq%20%27" + username + "%27").select("Favorites").build();

        }

        @Override
        protected String doInBackground(Void... params) {
            if(!App.IsNetworkConnected())
            {
                return getString(R.string.error_internet_connection);
            }

            try
            {
                if(username != null)
                {
                    JSONObject json = Utils.GetJson(url);
                    if(!json.isNull("error"))
                    {
                        try {
                            int error = json.getInt("error");
                            if(error == 401)
                            {
                                return "401";
                            }
                        } catch (Exception e) {
                            return getString(R.string.error_loading_anime_details);
                        }
                    }
                    JSONArray jsonValue = json.getJSONArray("value");
                    JSONArray jsonFavorites = jsonValue.getJSONObject(0).getJSONArray("Favorites");
                    for(int i = 0; i < jsonFavorites.length(); i++)
                    {
                        Anime animeFavorite = new Anime(jsonFavorites.getJSONObject(i).getJSONObject("Anime"), AnimeDetailsActivity.this);
                        for(AnimeSource animeSource : animeFavorite.getAnimeSources())
                        {
                            if(String.valueOf(animeSource.getLanguageId()).equals(prefs.getString("prefLanguage", "1")) && animeFavorite.getAnimeId() == anime.getAnimeId())
                            {
                                isFavorite = true;
                            }
                        }

                    }
                    return null;
                }
                else
                {
                    return getString(R.string.error_loading_anime_details);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return getString(R.string.error_loading_anime_details);
        }

        @Override
        protected void onPostExecute(String error) {
            if(error != null)
            {
                if(error.equals("401"))
                {
                    Toast.makeText(AnimeDetailsActivity.this, getString(R.string.have_been_logged_out), Toast.LENGTH_LONG).show();
                    AnimeDetailsActivity.this.startActivity(new Intent(AnimeDetailsActivity.this, LoginActivity.class));
                    AnimeDetailsActivity.this.finish();
                }
                else
                {
                    Toast.makeText(AnimeDetailsActivity.this, error, Toast.LENGTH_LONG).show();
                    menuFavorite.setIcon(R.drawable.ic_not_favorite);
                }
            }
            else
            {
                if(isFavorite)
                    menuFavorite.setIcon(R.drawable.ic_favorite);
                else
                    menuFavorite.setIcon(R.drawable.ic_not_favorite);
            }
        }
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
            if(!App.isTablet)
                AnimationManager.ActivityStart(this);
        //}
	}



}
