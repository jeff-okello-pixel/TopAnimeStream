package com.topanimestream.views.profile;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
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

import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;
import com.topanimestream.App;
import com.topanimestream.utilities.AsyncTaskTools;
import com.topanimestream.utilities.SQLiteHelper;
import com.topanimestream.utilities.Utils;
import com.topanimestream.utilities.WcfDataServiceUtility;
import com.topanimestream.adapters.AnimeListAdapter;
import com.topanimestream.managers.AnimationManager;
import com.topanimestream.managers.DialogManager;
import com.topanimestream.models.Anime;
import com.topanimestream.R;
import com.topanimestream.models.CurrentUser;
import com.topanimestream.views.AnimeDetailsActivity;

public class MyFavoritesActivity extends ActionBarActivity implements OnItemClickListener {
    private DragSortListView listView;
    private ArrayList<Anime> animes;
    private int animeId;
    private TextView txtNoFavorite;
    private Resources r;
    private AlertDialog alertProviders;
    private SharedPreferences prefs;
    private AnimeListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_Blue);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);
        r = getResources();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        txtNoFavorite = (TextView) findViewById(R.id.txtNoFavorite);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        if (toolbar != null) {
            toolbar.setTitle(getString(R.string.title_favorites));
            toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
            setSupportActionBar(toolbar);
        }

        listView = (DragSortListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(this);
        listView.setDropListener(onDrop);
        listView.setRemoveListener(onRemove);
        DragSortController controller = new DragSortController(listView);
        //controller.setClickRemoveId(R.id.);
        controller.setRemoveEnabled(true);
        controller.setSortEnabled(true);
        controller.setDragHandleId(R.id.layAnime);
        controller.setBackgroundColor(getResources().getColor(R.color.blueTab));
        controller.setDragInitMode(DragSortController.ON_LONG_PRESS);

        listView.setFloatViewManager(controller);
        listView.setOnTouchListener(controller);
        listView.setDragScrollProfile(ssProfile);
        listView.setDragEnabled(true);

    }
    private DragSortListView.DragScrollProfile ssProfile =
            new DragSortListView.DragScrollProfile() {
                @Override
                public float getSpeed(float w, long t) {
                    if (w > 0.8f) {
                        // Traverse all views in a millisecond
                        return ((float) adapter.getCount()) / 0.001f;
                    } else {
                        return 6.0f * w;
                    }
                }
            };
    private DragSortListView.RemoveListener onRemove = new DragSortListView.RemoveListener() {
        @Override
        public void remove(int which) {
            Anime anime = animes.get(which);
            animes.remove(anime);
            adapter.notifyDataSetChanged();
            if (App.isGooglePlayVersion) {
                SQLiteHelper sqlLite = new SQLiteHelper(MyFavoritesActivity.this);
                sqlLite.removeFavorite(anime.getAnimeId());
                populateList();
                Toast.makeText(MyFavoritesActivity.this, r.getString(R.string.toast_remove_favorite), Toast.LENGTH_SHORT).show();
            } else {
                AsyncTaskTools.execute(new RemoveFavoriteTask(anime.getAnimeId()));
            }

            if (animes.size() > 0)
                txtNoFavorite.setVisibility(View.GONE);
            else
                txtNoFavorite.setVisibility(View.VISIBLE);
        }
    };
    private DragSortListView.DropListener onDrop =
            new DragSortListView.DropListener() {
                @Override
                public void drop(int from, int to) {
                    if (from != to) {
                        Anime item = adapter.getItem(from);
                        animes.remove(item);
                        animes.add(to, item);
                        adapter.notifyDataSetChanged();
                        AsyncTaskTools.execute(new ChangeFavoriteOrderTask(item.getAnimeId(), to + 1));
                    }
                }
            };

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
        Intent intent = new Intent(this, AnimeDetailsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("Anime", anime);
        intent.putExtras(bundle);
        startActivity(intent);

    }

    @Override
    protected void onResume() {
        super.onResume();
        populateList();


    }

    private void populateList() {
        if (App.isGooglePlayVersion) {
            SQLiteHelper sqlLite = new SQLiteHelper(this);
            animes = sqlLite.getFavorites(prefs.getString("prefLanguage", "1"));
            sqlLite.close();
            listView.setAdapter(new AnimeListAdapter(this, animes));
        } else {
            AsyncTaskTools.execute(new GetFavoriteTask());
        }
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
            busyDialog = DialogManager.showBusyDialog(getString(R.string.deleting_from_favorites), MyFavoritesActivity.this);
            URL = getString(R.string.anime_service_path);
        }

        @Override
        protected String doInBackground(Void... params) {
            if (!App.IsNetworkConnected()) {
                return getString(R.string.error_internet_connection);
            }
            SoapObject request = new SoapObject(NAMESPACE, method);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            request.addProperty("animeId", animeId);
            envelope = Utils.addAuthentication(envelope);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);
            HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
            SoapPrimitive result = null;
            try {
                androidHttpTransport.call(SOAP_ACTION + method, envelope);
                result = (SoapPrimitive) envelope.getResponse();
                return null;
            } catch (Exception e) {
                if (e instanceof SoapFault) {
                    return e.getMessage();
                }

                e.printStackTrace();
            }
            return getString(R.string.error_remove_favorite);
        }

        @Override
        protected void onPostExecute(String error) {
            DialogManager.dismissBusyDialog(busyDialog);
            if (error != null) {
                Toast.makeText(MyFavoritesActivity.this, error, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MyFavoritesActivity.this, r.getString(R.string.toast_remove_favorite), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class GetFavoriteTask extends AsyncTask<Void, Void, String> {
        private Dialog busyDialog;
        private String url;

        public GetFavoriteTask() {

        }

        @Override
        protected void onPreExecute() {
            busyDialog = DialogManager.showBusyDialog(getString(R.string.loading_favorites), MyFavoritesActivity.this);
            animes = new ArrayList<Anime>();
            url = new WcfDataServiceUtility(getString(R.string.anime_data_service_path)).getEntity("Favorites").formatJson().expand("Anime/AnimeSources,Anime/AnimeInformations,Anime/Genres").filter("AccountId%20eq%20" + CurrentUser.AccountId).build();

        }

        @Override
        protected String doInBackground(Void... params) {
            if (!App.IsNetworkConnected()) {
                return getString(R.string.error_internet_connection);
            }

            try {
                JSONObject json = Utils.GetJson(url);
                if (!json.isNull("error")) {
                    try {
                        int error = json.getInt("error");
                        if (error == 401) {
                            return "401";
                        }
                    } catch (Exception e) {
                        return null;
                    }
                }
                JSONArray jsonFavorites = json.getJSONArray("value");
                for (int i = 0; i < jsonFavorites.length(); i++) {

                    Anime anime = new Anime(jsonFavorites.getJSONObject(i).getJSONObject("Anime"), MyFavoritesActivity.this);
                    anime.setOrder(!jsonFavorites.getJSONObject(i).isNull("Order") ? jsonFavorites.getJSONObject(i).getInt("Order") : 0);
                    animes.add(anime);

                }
                return null;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return getString(R.string.error_loading_favorites);
        }

        @Override
        protected void onPostExecute(String error) {
            DialogManager.dismissBusyDialog(busyDialog);
            if (error != null) {
                if (error.equals("401")) {
                    Toast.makeText(MyFavoritesActivity.this, getString(R.string.have_been_logged_out), Toast.LENGTH_LONG).show();
                    startActivity(new Intent(MyFavoritesActivity.this, LoginActivity.class));
                    finish();
                } else {
                    Toast.makeText(MyFavoritesActivity.this, error, Toast.LENGTH_LONG).show();
                    AsyncTaskTools.execute(new GetFavoriteTask());
                }
            } else {
                Collections.sort(animes, new Anime());
                adapter = new AnimeListAdapter(MyFavoritesActivity.this, animes);
                listView.setAdapter(adapter);
                if (animes.size() > 0)
                    txtNoFavorite.setVisibility(View.GONE);
                else
                    txtNoFavorite.setVisibility(View.VISIBLE);
            }
        }
    }

    private class ChangeFavoriteOrderTask extends AsyncTask<Void, Void, String> {
        private Dialog busyDialog;
        private static final String NAMESPACE = "http://tempuri.org/";
        final String SOAP_ACTION = "http://tempuri.org/IAnimeService/";
        private String URL;
        private String method = "ChangeFavoriteOrder";
        private int order;
        private int animeId;

        public ChangeFavoriteOrderTask(int animeId, int order) {
            this.animeId = animeId;
            this.order = order;
        }

        @Override
        protected void onPreExecute() {
            busyDialog = DialogManager.showBusyDialog(getString(R.string.changing_favorite_order), MyFavoritesActivity.this);
            URL = getString(R.string.anime_service_path);
        }

        @Override
        protected String doInBackground(Void... params) {
            if (!App.IsNetworkConnected()) {
                return getString(R.string.error_internet_connection);
            }
            SoapObject request = new SoapObject(NAMESPACE, method);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            request.addProperty("animeId", animeId);
            request.addProperty("order", order);
            envelope = Utils.addAuthentication(envelope);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);
            HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
            SoapPrimitive result = null;
            try {
                androidHttpTransport.call(SOAP_ACTION + method, envelope);
                result = (SoapPrimitive) envelope.getResponse();
                return null;
            } catch (Exception e) {
                if (e instanceof SoapFault) {
                    return e.getMessage();
                }

                e.printStackTrace();
            }
            return getString(R.string.error_changing_order);
        }

        @Override
        protected void onPostExecute(String error) {
            DialogManager.dismissBusyDialog(busyDialog);
            if (error != null) {
                if (error.equals("401")) {
                    Toast.makeText(MyFavoritesActivity.this, getString(R.string.have_been_logged_out), Toast.LENGTH_LONG).show();
                    startActivity(new Intent(MyFavoritesActivity.this, LoginActivity.class));
                    finish();
                } else {
                    Toast.makeText(MyFavoritesActivity.this, error, Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(MyFavoritesActivity.this, getString(R.string.order_changed), Toast.LENGTH_LONG).show();
            }
        }
    }

}
