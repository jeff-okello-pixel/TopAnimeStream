package com.topanimestream.views;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import java.util.ArrayList;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.topanimestream.App;
import com.topanimestream.custom.DrawGradient;
import com.topanimestream.models.OdataRequestInfo;
import com.topanimestream.utilities.AsyncTaskTools;
import com.topanimestream.utilities.ImageUtils;
import com.topanimestream.utilities.ODataUtils;
import com.topanimestream.utilities.ToolbarUtils;
import com.topanimestream.utilities.Utils;
import com.topanimestream.utilities.WcfDataServiceUtility;
import com.topanimestream.managers.AnimationManager;
import com.topanimestream.managers.DialogManager;
import com.topanimestream.models.Anime;
import com.topanimestream.models.Episode;
import com.topanimestream.R;
import com.topanimestream.models.Item;
import com.topanimestream.models.Recommendation;
import com.topanimestream.models.Review;
import com.topanimestream.models.Vote;
import com.topanimestream.views.profile.LoginActivity;

import butterknife.Bind;

public class AnimeDetailsActivity extends TASBaseActivity implements EpisodeListFragment.EpisodeListCallback, View.OnClickListener {
    private Anime anime;
    private Vote currentUserVote;
    public static Review currentUserReview;
    private Recommendation currentUserRecommendation;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.imgBackdrop)
    ImageView imgBackdrop;

    @Bind(R.id.appbar)
    AppBarLayout appbar;

    @Bind(R.id.progressBackdrop)
    ProgressBar progressBackdrop;

    @Bind(R.id.fabPlay)
    FloatingActionButton fabPlay;

    @Bind(R.id.episodefragContainer)
    FrameLayout episodefragContainer;

    EpisodeListFragment fragmentEpisodesList;

    private Target target = new Target() {
        @Override
        public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
            progressBackdrop.setVisibility(View.GONE);
            Palette.Builder builder = new Palette.Builder(bitmap);
            //24 for images with people face, http://developer.android.com/reference/android/support/v7/graphics/Palette.Builder.html
            builder.maximumColorCount(24);
            builder.generate(new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(Palette palette) {
                    imgBackdrop.setImageDrawable(new BitmapDrawable(getResources(), bitmap));

                    Palette.Swatch vibrant = palette.getVibrantSwatch();
                    if (vibrant != null) {
                        fabPlay.setBackgroundTintList(ColorStateList.valueOf(vibrant.getRgb()));
                        return;
                    }

                    Palette.Swatch darkMuted = palette.getDarkMutedSwatch();
                    if (darkMuted != null) {
                        fabPlay.setBackgroundTintList(ColorStateList.valueOf(darkMuted.getRgb()));
                        return;
                    }
                }
            });
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            progressBackdrop.setVisibility(View.GONE);
            fabPlay.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(AnimeDetailsActivity.this, R.color.dark_green)));
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_anime_details);

        Bundle bundle = getIntent().getExtras();
        anime = bundle.getParcelable("Anime");

        fabPlay.setOnClickListener(this);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction trans = fm.beginTransaction();
        trans.add(episodefragContainer.getId(), EpisodeListFragment.newInstance(anime), "frag_episodes");
        trans.commit();
        fm.executePendingTransactions();


        fragmentEpisodesList = (EpisodeListFragment) fm.findFragmentByTag("frag_episodes");
        fragmentEpisodesList.setEpisodeListCallback(this);

        Configuration configuration = getResources().getConfiguration();
        int screenWidthDp = configuration.screenHeightDp;

        final float scale = getResources().getDisplayMetrics().density;
        int heightScreenPixels = (int) (screenWidthDp * scale + 0.5f);

        ViewGroup.LayoutParams params = appbar.getLayoutParams();
        params.height = (int)Math.round((heightScreenPixels / 5) * 2.5);
        appbar.setLayoutParams(params);


        currentUserReview = null;

        toolbar.setTitle(anime.getName());
        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ToolbarUtils.updateToolbarHeight(this, toolbar);

        if (anime == null || anime.getAnimeId() == 0) {
            Toast.makeText(this, getString(R.string.error_loading_anime_details), Toast.LENGTH_LONG).show();
            finish();
        }

        if (savedInstanceState != null) {
            anime = savedInstanceState.getParcelable("anime");
        }
        Picasso.Builder builder = new Picasso.Builder(this);
        builder.listener(new Picasso.Listener() {
            @Override
            public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                exception.printStackTrace();
            }
        });
        builder.build()
                .load(ImageUtils.resizeImage(App.getContext().getString(R.string.image_host_path) + anime.getBackdropPath(), 600))
                .into(target);


    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
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
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.episodes, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_moreoptions:
                final Item[] items = {
                        new Item(anime.isFavorite() ? getString(R.string.remove_favorite) : getString(R.string.action_favorite), anime.isFavorite() ? R.drawable.ic_action_star : R.drawable.ic_action_star_empty),
                        new Item(getString(R.string.add_vote), R.drawable.ic_vote),
                        new Item(getString(R.string.reviews), R.drawable.ic_review)
                        //new Item(getString(R.string.recommendations), R.drawable.ic_recommendation)
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
                            public void onClick(final DialogInterface dialog, int item) {
                                String selectedItem = items[item].toString();
                                if (selectedItem.equals(getString(R.string.action_favorite))) {
                                    AsyncTaskTools.execute(new AddFavoriteTask(anime.getAnimeId()));
                                } else if (selectedItem.equals(getString(R.string.remove_favorite))) {
                                    AsyncTaskTools.execute(new RemoveFavoriteTask(anime.getAnimeId()));
                                } else if (selectedItem.equals(getString(R.string.add_vote))) {
                                    final Dialog dialogVote = new Dialog(AnimeDetailsActivity.this);
                                    dialogVote.setContentView(R.layout.vote_dialog);
                                    dialogVote.setTitle(getString(R.string.vote));
                                    RatingBar rtbCurrentRating = (RatingBar) dialogVote.findViewById(R.id.rtbCurrentRating);
                                    final RatingBar rtbUserRating = (RatingBar) dialogVote.findViewById(R.id.rtbUserRating);
                                    Button btnCancel = (Button) dialogVote.findViewById(R.id.btnCancel);
                                    Button btnDelete = (Button) dialogVote.findViewById(R.id.btnDelete);
                                    Button btnSave = (Button) dialogVote.findViewById(R.id.btnSave);
                                    TextView lblVoteCount = (TextView) dialogVote.findViewById(R.id.lblVoteCount);

                                    lblVoteCount.setText("(" + anime.getVoteCount() + " " + getString(R.string.votes) + ")");

                                    if (anime.getRating() != null)
                                        rtbCurrentRating.setRating((float) Utils.roundToHalf(anime.getRating() != 0 ? anime.getRating() / 2 : anime.getRating()));

                                    if (currentUserVote != null) {
                                        rtbUserRating.setRating(currentUserVote.getValue() / 2);
                                        btnDelete.setVisibility(View.VISIBLE);
                                    } else
                                        btnDelete.setVisibility(View.GONE);

                                    btnCancel.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            dialogVote.dismiss();
                                        }
                                    });

                                    btnDelete.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    switch (which){
                                                        case DialogInterface.BUTTON_POSITIVE:
                                                            AsyncTaskTools.execute(new RemoveVoteTask());
                                                            dialog.dismiss();
                                                            dialogVote.dismiss();

                                                            break;

                                                        case DialogInterface.BUTTON_NEGATIVE:
                                                            dialog.dismiss();
                                                            dialogVote.dismiss();
                                                            break;
                                                    }
                                                }
                                            };

                                            AlertDialog.Builder builder = new AlertDialog.Builder(AnimeDetailsActivity.this);
                                            builder.setMessage(getString(R.string.really_sure_delete_vote)).setPositiveButton(getString(R.string.yes), dialogClickListener)
                                                    .setNegativeButton(getString(R.string.no), dialogClickListener).show();

                                            dialog.dismiss();
                                        }
                                    });

                                    btnSave.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            AsyncTaskTools.execute(new VoteTask((int) (rtbUserRating.getRating() * 2)));
                                            dialogVote.dismiss();
                                        }
                                    });

                                    dialogVote.show();
                                } else if (selectedItem.equals(getString(R.string.reviews))) {
                                    Intent intent = new Intent(AnimeDetailsActivity.this, ReviewsActivity.class);
                                    intent.putExtra("currentUserReview", currentUserReview);
                                    intent.putExtra("animeId", anime.getAnimeId());
                                    startActivity(intent);

                                } else if (selectedItem.equals(getString(R.string.recommendations))) {

                                }
                            }
                        }).show();

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void OnEpisodeSelected(Episode episodeToPlay, ArrayList<Episode> episodes) {
        anime.setEpisodes(episodes);

        Intent intent = new Intent(AnimeDetailsActivity.this, VideoPlayerActivity.class);
        intent.putExtra("anime", anime);
        intent.putExtra("episodeToPlay", episodeToPlay);
        startActivityForResult(intent, MainActivity.UpdateWatchCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if(requestCode == MainActivity.UpdateWatchCode)
        {
            setResult(MainActivity.UpdateWatchCode, intent);
        }
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(AnimeDetailsActivity.this, VideoPlayerActivity.class);
        intent.putExtra("anime", anime);
        //TODO implement watchlist and get episode from there
        intent.putExtra("episodeToPlay", new Episode());
        startActivityForResult(intent, MainActivity.UpdateWatchCode);
    }

    private class RemoveVoteTask extends AsyncTask<Void, Void, String> {
        private Dialog busyDialog;
        private static final String NAMESPACE = "http://tempuri.org/";
        final String SOAP_ACTION = "http://tempuri.org/IAnimeService/";
        private String method = "RemoveVote";
        private String URL;

        @Override
        protected void onPreExecute() {
            busyDialog = DialogManager.showBusyDialog(getString(R.string.removing_vote), AnimeDetailsActivity.this);
            URL = getString(R.string.odata_path);
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
                return "success";
            } catch (Exception e) {

                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                if (result == null) {
                    Toast.makeText(AnimeDetailsActivity.this, getString(R.string.error_removing_vote), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(AnimeDetailsActivity.this, getString(R.string.vote_removed), Toast.LENGTH_LONG).show();
                    currentUserVote = null;
                    AsyncTaskTools.execute(new AnimeDetailsTask(true));
                }


            } catch (Exception e)//catch all exception, handle orientation change
            {
                e.printStackTrace();
            }
            DialogManager.dismissBusyDialog(busyDialog);

        }

    }
    private class VoteTask extends AsyncTask<Void, Void, String> {
        private Dialog busyDialog;
        private int userVote;
        public VoteTask(int UserVote)
        {
            userVote = UserVote;
        }
        private String URL;
        private static final String NAMESPACE = "http://tempuri.org/";
        final String SOAP_ACTION = "http://tempuri.org/IAnimeService/";
        private String method = "Vote";
        @Override
        protected void onPreExecute() {
            busyDialog = DialogManager.showBusyDialog(getString(R.string.adding_vote), AnimeDetailsActivity.this);
            URL = getString(R.string.odata_path);
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
                return "success";
            } catch (Exception e) {

                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                if (result == null) {
                    Toast.makeText(AnimeDetailsActivity.this, getString(R.string.error_adding_vote), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(AnimeDetailsActivity.this, getString(R.string.vote_added), Toast.LENGTH_LONG).show();
                    AsyncTaskTools.execute(new AnimeDetailsTask(true));
                }

            } catch (Exception e)//catch all exception, handle orientation change
            {
                e.printStackTrace();
            }
            DialogManager.dismissBusyDialog(busyDialog);

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
                Toast.makeText(AnimeDetailsActivity.this, error, Toast.LENGTH_LONG).show();
            } else {
                anime.setIsFavorite(false);
                Toast.makeText(AnimeDetailsActivity.this, getString(R.string.toast_remove_favorite), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(AnimeDetailsActivity.this, error, Toast.LENGTH_LONG).show();
            } else {
                anime.setIsFavorite(true);
                Toast.makeText(AnimeDetailsActivity.this, getString(R.string.toast_add_favorite), Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void GetAnimeDetails()
    {
        ODataUtils.GetEntity(getString(R.string.odata_path) + "Animes(" + anime.getAnimeId() + ")?$expand=AnimeSources,Genres,AnimeInformations,Status", Anime.class, new ODataUtils.Callback<Anime>() {
            @Override
            public void onSuccess(Anime anime, OdataRequestInfo info) {

            }

            @Override
            public void onFailure(Exception e) {

            }
        });
    }
    private class AnimeDetailsTask extends AsyncTask<Void, Void, String> {
        private String userReviewUrl;
        private String userRecommendationUrl;
        private String userVoteUrl;
        private String animeDetailUrl;
        private Dialog busyDialog;
        private boolean isReload;
        public AnimeDetailsTask(boolean IsReload)
        {
            this.isReload = IsReload;

        }


        @Override
        protected void onPreExecute() {
            busyDialog = DialogManager.showBusyDialog(getString(R.string.loading_anime_details), AnimeDetailsActivity.this);
            userVoteUrl = new WcfDataServiceUtility(getString(R.string.odata_path)).getEntity("Votes").formatJson().filter("AccountId%20eq%20" + App.currentUser.getAccountId() + "%20and%20AnimeId%20eq%20" + anime.getAnimeId()).build();
            userRecommendationUrl = new WcfDataServiceUtility(getString(R.string.odata_path)).getEntity("Recommendations").formatJson().filter("AccountId%20eq%20" + App.currentUser.getAccountId() + "%20and%20AnimeId%20eq%20" + anime.getAnimeId()).build();
            userReviewUrl = new WcfDataServiceUtility(getString(R.string.odata_path)).getEntity("Reviews").formatJson().filter("AccountId%20eq%20" + App.currentUser.getAccountId() + "%20and%20AnimeId%20eq%20" + anime.getAnimeId()).expand("Account").build();
            animeDetailUrl = new WcfDataServiceUtility(getString(R.string.odata_path)).getEntitySpecificRow("Animes", anime.getAnimeId(), false).expand("AnimeSources,Genres,AnimeInformations,Status").formatJson().build();
        }

        @Override
        protected String doInBackground(Void... params) {
            if (!App.IsNetworkConnected()) {
                return getString(R.string.error_internet_connection);
            }

            try {

                Gson gson = new Gson();
                JSONObject jsonVote = Utils.GetJson(userVoteUrl);
                String errors = Utils.checkDataServiceErrors(jsonVote, getString(R.string.error_loading_anime_details));
                if(errors != null)
                    return errors;
                if(jsonVote.getJSONArray("value").length() > 0)
                {
                    currentUserVote = gson.fromJson(jsonVote.getJSONArray("value").getJSONObject(0).toString(), Vote.class);
                }

                JSONObject jsonReview = Utils.GetJson(userReviewUrl);
                errors = Utils.checkDataServiceErrors(jsonReview, getString(R.string.error_loading_anime_details));
                if(errors != null)
                    return errors;
                if(jsonReview.getJSONArray("value").length() > 0)
                {
                    currentUserReview = gson.fromJson(jsonReview.getJSONArray("value").getJSONObject(0).toString(), Review.class);
                }

                JSONObject jsonRecommendation = Utils.GetJson(userRecommendationUrl);
                errors = Utils.checkDataServiceErrors(jsonRecommendation, getString(R.string.error_loading_anime_details));
                if(errors != null)
                    return errors;
                if(jsonRecommendation.getJSONArray("value").length() > 0)
                {
                    currentUserRecommendation = gson.fromJson(jsonRecommendation.getJSONArray("value").getJSONObject(0).toString(), Recommendation.class);
                }
                if(isReload) {
                    JSONObject jsonAnime = Utils.GetJson(animeDetailUrl);
                    errors = Utils.checkDataServiceErrors(jsonAnime, getString(R.string.error_loading_anime_details));;
                    if (errors != null)
                        return errors;
                    anime = gson.fromJson(jsonAnime.toString(), Anime.class);
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
                }
            }
            else
            {
                if(isReload)
                {
                    /*
                    if(!anime.isMovie()) {
                        if (animeDetailsFragment != null)
                            animeDetailsFragment.setAnime(anime);
                    }
                    else
                    {
                        if (animeDetailsMovieFragment != null)
                            animeDetailsMovieFragment.setAnime(anime);
                    }*/
                }
            }
            DialogManager.dismissBusyDialog(busyDialog);
        }
    }


}
