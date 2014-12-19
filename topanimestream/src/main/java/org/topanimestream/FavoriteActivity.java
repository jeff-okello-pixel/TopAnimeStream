package org.topanimestream;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
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

import org.json.JSONArray;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.ArrayList;
import java.util.Collections;

import org.topanimestream.adapters.AnimeListAdapter;
import org.topanimestream.managers.AnimationManager;
import org.topanimestream.managers.DialogManager;
import org.topanimestream.models.Anime;
import org.topanimestream.models.AnimeSource;
import org.topanimestream.R;

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
          actionBar.setDisplayShowHomeEnabled(false);
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
                    AnimationManager.ActivityFinish(this);
			    	break;
			 }
	    return true;
	  }
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Anime anime = animes.get(position);
			animeId = anime.getAnimeId();
            Intent intent = new Intent(this,AnimeDetailsActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable("Anime", anime);
            intent.putExtras(bundle);
            startActivity(intent);
			
		}
		
		@Override
		protected void onResume() 
		{
		    super.onResume();
		    populateList();
				
			
		}
		private void populateList()
		{
            if(App.isGooglePlayVersion) {
                SQLiteHelper sqlLite = new SQLiteHelper(this);
                animes = sqlLite.getFavorites(prefs.getString("prefLanguage", "1"));
                sqlLite.close();
                listView.setAdapter(new AnimeListAdapter(this, animes));
                if (animes.size() > 0)
                    txtNoFavorite.setVisibility(View.GONE);
                else
                    txtNoFavorite.setVisibility(View.VISIBLE);
            }
            else
            {
                AsyncTaskTools.execute(new GetFavoriteTask());
            }
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
                        if(App.isGooglePlayVersion) {
                            SQLiteHelper sqlLite = new SQLiteHelper(FavoriteActivity.this);
                            sqlLite.removeFavorite(animes.get(position).getAnimeId());
                            populateList();
                            Toast.makeText(FavoriteActivity.this, r.getString(R.string.toast_remove_favorite), Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            AsyncTaskTools.execute(new RemoveFavoriteTask(animes.get(position).getAnimeId()));
                        }
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
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        AnimationManager.ActivityFinish(this);
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
                    busyDialog = DialogManager.showBusyDialog(getString(R.string.deleting_from_favorites), FavoriteActivity.this);
                    URL = getString(R.string.anime_service_path);
                }

                ;

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
                        Toast.makeText(FavoriteActivity.this, error, Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        AsyncTaskTools.execute(new GetFavoriteTask());
                        Toast.makeText(FavoriteActivity.this, r.getString(R.string.toast_remove_favorite), Toast.LENGTH_SHORT).show();
                    }
                }
            }
    private class GetFavoriteTask extends AsyncTask<Void, Void, String> {
        private Dialog busyDialog;
        private String url;
        private String username;
        public GetFavoriteTask() {

        }

        @Override
        protected void onPreExecute() {
            busyDialog = DialogManager.showBusyDialog(getString(R.string.loading_favorites), FavoriteActivity.this);
            animes = new ArrayList<Anime>();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(FavoriteActivity.this);
            username = prefs.getString("Username", null);
            url = new WcfDataServiceUtility(getString(R.string.anime_data_service_path)).getEntity("Accounts").formatJson().expand("Favorites/Anime/AnimeSources,Favorites/Anime/AnimeInformations,Favorites/Anime/Genres").filter("Username%20eq%20%27" + username + "%27").select("Favorites").build();

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
                            return null;
                        }
                    }
                    JSONArray jsonValue = json.getJSONArray("value");
                    JSONArray jsonFavorites = jsonValue.getJSONObject(0).getJSONArray("Favorites");
                    for(int i = 0; i < jsonFavorites.length(); i++)
                    {

                        Anime anime = new Anime(jsonFavorites.getJSONObject(i).getJSONObject("Anime"), FavoriteActivity.this);
                        anime.setOrder(!jsonFavorites.getJSONObject(i).isNull("Order") ? jsonFavorites.getJSONObject(i).getInt("Order") : 0);
                        for(AnimeSource animeSource : anime.getAnimeSources())
                        {
                            if(String.valueOf(animeSource.getLanguageId()).equals(prefs.getString("prefLanguage", "1")))
                            {
                                if(!animes.contains(anime))
                                    animes.add(anime);
                            }
                        }

                    }
                    return null;
                }
                else
                {
                    return getString(R.string.error_loading_favorites);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return getString(R.string.error_loading_favorites);
        }

        @Override
        protected void onPostExecute(String error) {
            DialogManager.dismissBusyDialog(busyDialog);
            if(error != null)
            {
                if(error.equals("401"))
                {
                    Toast.makeText(FavoriteActivity.this, getString(R.string.have_been_logged_out), Toast.LENGTH_LONG).show();
                    startActivity(new Intent(FavoriteActivity.this, LoginActivity.class));
                    finish();
                }
                else {
                    Toast.makeText(FavoriteActivity.this, error, Toast.LENGTH_LONG).show();
                }
            }
            else
            {
                Collections.sort(animes, new Anime());
                listView.setAdapter(new AnimeListAdapter(FavoriteActivity.this, animes));
                if (animes.size() > 0)
                    txtNoFavorite.setVisibility(View.GONE);
                else
                    txtNoFavorite.setVisibility(View.VISIBLE);
            }
        }
    }

}
