package com.topanimestream.views.profile;

import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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

import com.google.gson.Gson;
import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;
import com.topanimestream.App;
import com.topanimestream.adapters.FavoriteListAdapter;
import com.topanimestream.models.Favorite;
import com.topanimestream.models.OdataRequestInfo;
import com.topanimestream.utilities.AsyncTaskTools;
import com.topanimestream.utilities.ODataUtils;
import com.topanimestream.utilities.Utils;
import com.topanimestream.utilities.WcfDataServiceUtility;
import com.topanimestream.managers.AnimationManager;
import com.topanimestream.managers.DialogManager;
import com.topanimestream.models.Anime;
import com.topanimestream.R;
import com.topanimestream.views.AnimeDetailsActivity;
import com.topanimestream.views.TASBaseActivity;

import butterknife.Bind;

public class MyFavoritesActivity extends TASBaseActivity implements OnItemClickListener {
    private ArrayList<Favorite> favorites;
    private FavoriteListAdapter adapter;

    @Bind(R.id.txtNoFavorite)
    TextView txtNoFavorite;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.listViewFavorites)
    DragSortListView listViewFavorites;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_favorite);

        toolbar.setTitle(getString(R.string.title_favorites));
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        setSupportActionBar(toolbar);

        listViewFavorites.setOnItemClickListener(this);
        listViewFavorites.setDropListener(onDrop);
        listViewFavorites.setRemoveListener(onRemove);

        DragSortController controller = new DragSortController(listViewFavorites);
        //controller.setClickRemoveId(R.id.);
        controller.setRemoveEnabled(true);
        controller.setSortEnabled(true);
        controller.setDragHandleId(R.id.layAnime);
        controller.setBackgroundColor(getResources().getColor(R.color.blueTab));
        controller.setDragInitMode(DragSortController.ON_LONG_PRESS);

        listViewFavorites.setFloatViewManager(controller);
        listViewFavorites.setOnTouchListener(controller);
        listViewFavorites.setDragScrollProfile(ssProfile);
        listViewFavorites.setDragEnabled(true);

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
            Favorite favorite = favorites.get(which);
            favorites.remove(favorite);
            adapter.notifyDataSetChanged();

            AsyncTaskTools.execute(new RemoveFavoriteTask(favorite.getAnime().getAnimeId()));

            if (favorites.size() > 0)
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
                        Favorite item = adapter.getItem(from);
                        favorites.remove(item);
                        favorites.add(to, item);
                        adapter.notifyDataSetChanged();
                        AsyncTaskTools.execute(new ChangeFavoriteOrderTask(item.getAnime().getAnimeId(), to + 1));
                    }
                }
            };


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        Anime anime = favorites.get(position).getAnime();
        Intent intent = new Intent(this, AnimeDetailsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("Anime", anime);
        intent.putExtras(bundle);
        startActivity(intent);

    }

    @Override
    protected void onResume() {
        super.onResume();
        AsyncTaskTools.execute(new GetFavoriteTask());

        ODataUtils.GetEntityList(getString(R.string.odata_path) + "MyFavorites?$expand=Anime&$orderby=Order", Favorite.class, new ODataUtils.Callback<ArrayList<Favorite>>() {
            @Override
            public void onSuccess(ArrayList<Favorite> entity, OdataRequestInfo info) {
                adapter = new FavoriteListAdapter(MyFavoritesActivity.this, favorites);
                listViewFavorites.setAdapter(adapter);
                if (favorites.size() > 0)
                    txtNoFavorite.setVisibility(View.GONE);
                else
                    txtNoFavorite.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(Exception e) {

            }
        });
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
            URL = getString(R.string.odata_path);
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
                Toast.makeText(MyFavoritesActivity.this, getString(R.string.toast_remove_favorite), Toast.LENGTH_SHORT).show();
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
            favorites = new ArrayList<Favorite>();
            url = new WcfDataServiceUtility(getString(R.string.odata_path)).getEntity("Favorites").formatJson().expand("Anime/AnimeSources,Anime/AnimeInformations,Anime/Genres").filter("AccountId%20eq%20" + App.currentUser.getAccountId()).build();

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

                    Gson gson = new Gson();
                    favorites.add(gson.fromJson(jsonFavorites.getJSONObject(i).getJSONObject("Anime").toString(), Favorite.class));

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
                /*
                Collections.sort(animes, new Anime());
                adapter = new AnimeListAdapter(MyFavoritesActivity.this, animes);
                listViewFavorites.setAdapter(adapter);
                if (animes.size() > 0)
                    txtNoFavorite.setVisibility(View.GONE);
                else
                    txtNoFavorite.setVisibility(View.VISIBLE);*/
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
            URL = getString(R.string.odata_path);
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
