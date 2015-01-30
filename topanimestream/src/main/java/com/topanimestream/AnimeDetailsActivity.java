package com.topanimestream;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fwwjt.pacjz173199.AdView;
import com.google.gson.Gson;
import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.google.sample.castcompanionlibrary.cast.callbacks.VideoCastConsumerImpl;
import com.google.sample.castcompanionlibrary.widgets.MiniController;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.net.URL;
import java.util.ArrayList;

import com.topanimestream.managers.AnimationManager;
import com.topanimestream.managers.DialogManager;
import com.topanimestream.models.Anime;
import com.topanimestream.models.AnimeInformation;
import com.topanimestream.models.AnimeSource;
import com.topanimestream.models.CurrentUser;
import com.topanimestream.models.Episode;
import com.topanimestream.R;
import com.topanimestream.models.Item;
import com.topanimestream.models.Recommendation;
import com.topanimestream.models.Review;
import com.topanimestream.models.Vote;

public class AnimeDetailsActivity extends ActionBarActivity implements EpisodesContainerFragment.ProviderFragmentCoordinator {
    private ListView listViewEpisodes;
    private ArrayList<Episode> episodes;
    public ArrayList<String> mItems;
    private Dialog busyDialog;
    private Anime anime;
    private LinearLayout layAnimeDetails;
    private MenuItem menuMoreOptions;
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
    private boolean isFavorite = false;
    private Vote currentUserVote;
    private Review currentUserReview;
    private Recommendation currentUserRecommendation;
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

        if (anime == null || anime.getAnimeId() == 0) {
            Toast.makeText(this, r.getString(R.string.error_loading_anime_details), Toast.LENGTH_LONG).show();
            finish();
        }

        if (savedInstanceState != null) {
            anime = savedInstanceState.getParcelable("anime");

        }
        if (App.isPro) {

            AdView adView = (AdView) findViewById(R.id.adView);
            ((ViewGroup) adView.getParent()).removeView(adView);
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

        episodeContainerFragment = (EpisodesContainerFragment) fm.findFragmentByTag("episodeContainerFragment");
        if (episodeContainerFragment == null) {
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(layEpisodes.getId(), EpisodesContainerFragment.newInstance(anime), "episodeContainerFragment");
            ft.commit();
        }


        AnimeDetailsFragment animeDetailsFragment = (AnimeDetailsFragment) fm.findFragmentById(R.id.animeDetailsFragment);
        if (animeDetailsFragment != null)
            animeDetailsFragment.setAnime(anime);


        AsyncTaskTools.execute(new AnimeDetailsTask());


    }


    @Override
    protected void onResume() {
        if (App.isPro) {
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
        if (App.isPro) {
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
        if (App.isPro) {
            mediaRouteMenuItem = App.mCastMgr.addMediaRouterButton(menu, R.id.media_route_menu_item);
        }
        menuMoreOptions = menu.findItem(R.id.action_moreoptions);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                break;
            case android.R.id.home:
                finish();
                AnimationManager.ActivityFinish(this);
                break;
            case R.id.action_moreoptions:
                final Item[] items = {
                        new Item(isFavorite ? getString(R.string.remove_favorite) : getString(R.string.action_favorite), isFavorite ? R.drawable.ic_action_star : R.drawable.ic_action_star_empty),
                        new Item(getString(R.string.add_vote), R.drawable.ic_vote),
                        new Item(getString(R.string.reviews), R.drawable.ic_review),
                        new Item(getString(R.string.recommendations), R.drawable.ic_recommendation)
                };

                ListAdapter adapter = new ArrayAdapter<Item>(
                        this,
                        android.R.layout.select_dialog_item,
                        android.R.id.text1,
                        items){
                    public View getView(int position, View convertView, ViewGroup parent) {
                        //User super class to create the View
                        View v = super.getView(position, convertView, parent);
                        TextView tv = (TextView)v.findViewById(android.R.id.text1);

                        //Put the image on the TextView
                        tv.setCompoundDrawablesWithIntrinsicBounds(items[position].icon, 0, 0, 0);

                        //Add margin between image and text (support various screen densities)
                        int dp5 = (int) (5 * getResources().getDisplayMetrics().density + 0.5f);
                        tv.setCompoundDrawablePadding(dp5);

                        return v;
                    }
                };


                new AlertDialog.Builder(AnimeDetailsActivity.this)
                        .setTitle(getString(R.string.choose_option))
                        .setAdapter(adapter, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                String selectedItem = items[item].toString();
                                if (selectedItem.equals(getString(R.string.action_favorite)))
                                {
                                    AsyncTaskTools.execute(new AddFavoriteTask(anime.getAnimeId()));
                                }
                                else if (selectedItem.equals(getString(R.string.remove_favorite)))
                                {
                                    AsyncTaskTools.execute(new RemoveFavoriteTask(anime.getAnimeId()));
                                }
                                else if (selectedItem.equals(getString(R.string.add_vote)))
                                {
                                    final Dialog dialogVote = new Dialog(AnimeDetailsActivity.this);
                                    dialogVote.setContentView(R.layout.vote_dialog);

                                    RatingBar rtbCurrentRating = (RatingBar) dialogVote.findViewById(R.id.rtbCurrentRating);
                                    final RatingBar rtbUserRating = (RatingBar) dialogVote.findViewById(R.id.rtbUserRating);
                                    Button btnCancel = (Button) dialogVote.findViewById(R.id.btnCancel);
                                    Button btnRemoveVote = (Button) dialogVote.findViewById(R.id.btnRemoveVote);
                                    Button btnSave = (Button) dialogVote.findViewById(R.id.btnSave);

                                    if (anime.getRating() != null)
                                        rtbCurrentRating.setRating((float) Utils.roundToHalf(anime.getRating() != 0 ? anime.getRating() / 2 : anime.getRating()));

                                    if(currentUserVote != null)
                                        rtbUserRating.setRating(currentUserVote.getValue() / 2);

                                    btnCancel.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            dialogVote.dismiss();
                                        }
                                    });

                                    btnRemoveVote.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            AsyncTaskTools.execute(new RemoveVoteTask());
                                        }
                                    });

                                    btnSave.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            AsyncTaskTools.execute(new VoteTask((int)(rtbUserRating.getRating() * 2)));
                                        }
                                    });

                                    dialogVote.show();
                                }
                                else if (selectedItem.equals(getString(R.string.reviews)))
                                {

                                }
                                else if (selectedItem.equals(getString(R.string.recommendations)))
                                {

                                }
                            }
                        }).show();

                break;
        }

        return true;
    }
    private class RemoveVoteTask extends AsyncTask<Void, Void, String> {
        private Dialog busyDialog;
        @Override
        protected void onPreExecute() {
            busyDialog = Utils.showBusyDialog(getString(R.string.removing_vote), AnimeDetailsActivity.this);

        }

        @Override
        protected String doInBackground(Void... params) {
            if (!App.IsNetworkConnected()) {
                return getString(R.string.error_internet_connection);
            }
            SoapObject request = new SoapObject(NAMESPACE, method);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            request.addProperty("animeId", anime.getAnimeId());
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

            return "Success";
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                if (result == null) {
                    Toast.makeText(AnimeDetailsActivity.this, getString(R.string.error_removing_vote), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(AnimeDetailsActivity.this, getString(R.string.vote_removed), Toast.LENGTH_LONG).show();
                }

                Utils.dismissBusyDialog(busyDialog);
            } catch (Exception e)//catch all exception, handle orientation change
            {
                e.printStackTrace();
            }


        }

    }
    private class VoteTask extends AsyncTask<Void, Void, String> {
        private Dialog busyDialog;
        private int userVote;
        public VoteTask(int UserVote)
        {
            userVote = UserVote;
        }
        @Override
        protected void onPreExecute() {
            busyDialog = Utils.showBusyDialog("Adding your vote...", AnimeDetailsActivity.this);

        }

        @Override
        protected String doInBackground(Void... params) {
            if (!App.IsNetworkConnected()) {
                return getString(R.string.error_internet_connection);
            }
            SoapObject request = new SoapObject(NAMESPACE, method);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            request.addProperty("animeId", anime.getAnimeId());
            request.addProperty("value", userVote);
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

            return "Success";
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                if (result == null) {
                    Toast.makeText(AnimeDetailsActivity.this, getString(R.string.error_adding_vote), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(AnimeDetailsActivity.this, getString(R.string.vote_added), Toast.LENGTH_LONG).show();
                }

                Utils.dismissBusyDialog(busyDialog);
            } catch (Exception e)//catch all exception, handle orientation change
            {
                e.printStackTrace();
            }


        }

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
            Utils.dismissBusyDialog(busyDialog);
            if (error != null) {
                Toast.makeText(AnimeDetailsActivity.this, error, Toast.LENGTH_LONG).show();
            } else {
                isFavorite = false;
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
            Utils.dismissBusyDialog(busyDialog);
            if (error != null) {
                Toast.makeText(AnimeDetailsActivity.this, error, Toast.LENGTH_LONG).show();
            } else {
                isFavorite = true;
                Toast.makeText(AnimeDetailsActivity.this, r.getString(R.string.toast_add_favorite), Toast.LENGTH_SHORT).show();
            }
        }
    }
    private class AnimeDetailsTask extends AsyncTask<Void, Void, String> {
        private String isFavoriteUrl;
        private String userReviewUrl;
        private String userRecommendationUrl;
        private String userVoteUrl;
        private Dialog busyDialog;
        private String checkServiceErrors(JSONObject json)
        {
            try {
                if (!json.isNull("error")) {

                        int error = json.getInt("error");
                        if (error == 401) {
                            return "401";
                        }
                    }
                }
            catch (Exception e) {
                return getString(R.string.error_loading_anime_details);
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            busyDialog = Utils.showBusyDialog(getString(R.string.loading_anime_details), AnimeDetailsActivity.this);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AnimeDetailsActivity.this);
            isFavoriteUrl = new WcfDataServiceUtility(getString(R.string.anime_data_service_path)).getEntity("Favorites").formatJson().filter("AccountId%20eq%20%27" + CurrentUser.AccountId + "%27%20and%20AnimeId%20eq%20" + anime.getAnimeId()).build();
            userVoteUrl = new WcfDataServiceUtility(getString(R.string.anime_data_service_path)).getEntity("Votes").formatJson().filter("AccountId%20eq%20" + CurrentUser.AccountId + "%20and%20AnimeId%20eq%20" + anime.getAnimeId()).build();
            userRecommendationUrl = new WcfDataServiceUtility(getString(R.string.anime_data_service_path)).getEntity("Recommendations").formatJson().filter("AccountId%20eq%20" + CurrentUser.AccountId + "%20and%20AnimeId%20eq%20" + anime.getAnimeId()).build();
            userReviewUrl = new WcfDataServiceUtility(getString(R.string.anime_data_service_path)).getEntity("Reviews").formatJson().filter("AccountId%20eq%20" + CurrentUser.AccountId + "%20and%20AnimeId%20eq%20" + anime.getAnimeId()).build();
        }

        @Override
        protected String doInBackground(Void... params) {
            if (!App.IsNetworkConnected()) {
                return getString(R.string.error_internet_connection);
            }

            try {

                JSONObject jsonFavorite = Utils.GetJson(isFavoriteUrl);
                String errors = checkServiceErrors(jsonFavorite);
                if(errors != null)
                    return errors;

                if(jsonFavorite.getJSONArray("value").length() > 0)
                    isFavorite = true;
                else
                    isFavorite = false;

                Gson gson = new Gson();
                JSONObject jsonVote = Utils.GetJson(userVoteUrl);
                errors = checkServiceErrors(jsonFavorite);
                if(errors != null)
                    return errors;
                if(jsonVote.getJSONArray("value").length() > 0)
                {
                    currentUserVote = gson.fromJson(jsonVote.getJSONArray("value").getJSONObject(0).toString(), Vote.class);
                }

                JSONObject jsonReview = Utils.GetJson(userReviewUrl);
                errors = checkServiceErrors(jsonReview);
                if(errors != null)
                    return errors;
                if(jsonReview.getJSONArray("value").length() > 0)
                {
                    currentUserReview = gson.fromJson(jsonReview.getJSONArray("value").getJSONObject(0).toString(), Review.class);
                }

                JSONObject jsonRecommendation = Utils.GetJson(userRecommendationUrl);
                errors = checkServiceErrors(jsonRecommendation);
                if(errors != null)
                    return errors;
                if(jsonRecommendation.getJSONArray("value").length() > 0)
                {
                    currentUserRecommendation = gson.fromJson(jsonRecommendation.getJSONArray("value").getJSONObject(0).toString(), Recommendation.class);
                }

                return null;

            } catch (Exception e) {
                e.printStackTrace();
            }
            return getString(R.string.error_loading_anime_details);
        }

        @Override
        protected void onPostExecute(String error) {
            if (error != null) {
                if (error.equals("401")) {
                    Toast.makeText(AnimeDetailsActivity.this, getString(R.string.have_been_logged_out), Toast.LENGTH_LONG).show();
                    AnimeDetailsActivity.this.startActivity(new Intent(AnimeDetailsActivity.this, LoginActivity.class));
                    AnimeDetailsActivity.this.finish();
                } else {
                    Toast.makeText(AnimeDetailsActivity.this, error, Toast.LENGTH_LONG).show();
                    isFavorite = false;
                }
            }
            Utils.dismissBusyDialog(busyDialog);
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
        if (!App.isTablet)
            AnimationManager.ActivityStart(this);
        //}
    }


}
