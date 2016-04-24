package com.topanimestream.views;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import java.util.ArrayList;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.topanimestream.App;
import com.topanimestream.beaming.BeamManager;
import com.topanimestream.models.Favorite;
import com.topanimestream.models.OdataRequestInfo;
import com.topanimestream.models.Source;
import com.topanimestream.models.Subtitle;
import com.topanimestream.models.WatchedVideo;
import com.topanimestream.parallel.ParallelCallback;
import com.topanimestream.parallel.ParentCallback;
import com.topanimestream.utilities.ImageUtils;
import com.topanimestream.utilities.ODataUtils;
import com.topanimestream.utilities.ToolbarUtils;
import com.topanimestream.managers.AnimationManager;
import com.topanimestream.models.Anime;
import com.topanimestream.models.Episode;
import com.topanimestream.R;
import com.topanimestream.utilities.Utils;

import butterknife.Bind;

public class AnimeDetailsActivity extends TASBaseActivity implements EpisodeListFragment.EpisodeListCallback, View.OnClickListener {
    private Anime anime;
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
    MenuItem menuItemAddFavorite;
    MenuItem menuItemRemoveFavorite;
    Episode userCurrentEpisode;
    ArrayList<Source> sources = new ArrayList();
    ArrayList<Subtitle> subtitles = new ArrayList();
    boolean sourceAndSubsLoadIndicator;
    BeamManager bm;

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

        if(anime.isAvailable())
            fabPlay.setOnClickListener(this);
        else {
            CoordinatorLayout.LayoutParams p = (CoordinatorLayout.LayoutParams) fabPlay.getLayoutParams();
            p.setAnchorId(View.NO_ID);
            fabPlay.setLayoutParams(p);
            fabPlay.setVisibility(View.GONE);
        }

        FragmentManager fm = getSupportFragmentManager();
        fragmentEpisodesList = (EpisodeListFragment) fm.findFragmentByTag("frag_episodes");

        if(fragmentEpisodesList == null) {
            FragmentTransaction trans = fm.beginTransaction();
            trans.add(episodefragContainer.getId(), EpisodeListFragment.newInstance(anime), "frag_episodes");
            trans.commit();
            fm.executePendingTransactions();
            fragmentEpisodesList = (EpisodeListFragment) fm.findFragmentByTag("frag_episodes");
        }

        fragmentEpisodesList.setEpisodeListCallback(this);

        Configuration configuration = getResources().getConfiguration();
        int screenWidthDp = configuration.screenHeightDp;

        final float scale = getResources().getDisplayMetrics().density;
        int heightScreenPixels = (int) (screenWidthDp * scale + 0.5f);

        ViewGroup.LayoutParams params = appbar.getLayoutParams();
        params.height = (int)Math.round((heightScreenPixels / 5) * 2.5);
        appbar.setLayoutParams(params);

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

        ODataUtils.GetEntity(getString(R.string.odata_path) + "MyInstantWatch(animeId=" + anime.getAnimeId() + ",episodeId=null)?$expand=Episode", WatchedVideo.class, new ODataUtils.EntityCallback<WatchedVideo>() {
            @Override
            public void onSuccess(WatchedVideo watchedVideo, OdataRequestInfo info) {
                if(watchedVideo != null && watchedVideo.getEpisode() != null)
                {
                    userCurrentEpisode = watchedVideo.getEpisode();
                }
            }

            @Override
            public void onFailure(Exception e) {

            }
        });


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
        getMenuInflater().inflate(R.menu.anime_details, menu);
        menuItemAddFavorite = menu.findItem(R.id.action_addfavorite);
        menuItemRemoveFavorite = menu.findItem(R.id.action_removefavorite);
        CheckIsFavorite();
        return true;
    }

    public void CheckIsFavorite()
    {
        ODataUtils.GetEntityList(getString(R.string.odata_path) + "MyFavorites?$filter=AnimeId%20eq%20" + anime.getAnimeId(), Favorite.class, new ODataUtils.EntityCallback<ArrayList<Favorite>>() {
            @Override
            public void onSuccess(ArrayList<Favorite> favorites, OdataRequestInfo info) {
                if(favorites.size() > 0) {
                    menuItemAddFavorite.setVisible(false);
                    menuItemRemoveFavorite.setVisible(true);
                }
                else{
                    menuItemAddFavorite.setVisible(true);
                    menuItemRemoveFavorite.setVisible(false);
                }
            }

            @Override
            public void onFailure(Exception e) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_addfavorite:
                menuItemAddFavorite.setVisible(false);
                menuItemRemoveFavorite.setVisible(true);

                String jsonBodyAdd = "{animeId:" + anime.getAnimeId() + "}";
                ODataUtils.Post(getString(R.string.odata_path) + "Favorites/AddToMyFavorites", jsonBodyAdd, new ODataUtils.Callback() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onFailure(Exception e) {
                        AnimeDetailsActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(AnimeDetailsActivity.this, getString(R.string.error_adding_favorite), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });
                App.shouldUpdateFavorites = true;
                break;
            case R.id.action_removefavorite:
                menuItemAddFavorite.setVisible(true);
                menuItemRemoveFavorite.setVisible(false);
                String jsonBodyRemove = "{animeId:" + anime.getAnimeId() + "}";
                ODataUtils.Post(getString(R.string.odata_path) + "Favorites/RemoveFromMyFavorites", jsonBodyRemove, new ODataUtils.Callback() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onFailure(Exception e) {
                        AnimeDetailsActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(AnimeDetailsActivity.this, getString(R.string.error_removing_favorite), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });
                App.shouldUpdateFavorites = true;
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
        intent.putExtra("skip", fragmentEpisodesList.currentSkip);
        intent.putExtra("limit", fragmentEpisodesList.currentLimit);
        startActivityForResult(intent, MainActivity.UpdateWatchCode);
    }

    @Override
    public void EpisodesLoaded(ArrayList<Episode> episodes) {
        if (episodes != null && episodes.size() > 0) {
            if(userCurrentEpisode == null)
                userCurrentEpisode = episodes.get(0);
            anime.setEpisodes(episodes);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == MainActivity.UpdateWatchCode)
        {
            if(!anime.isMovie()) {
                if (data != null) {
                    WatchedVideo watchedVideo = data.getParcelableExtra("watchedvideo");
                    if (watchedVideo != null)
                        userCurrentEpisode = watchedVideo.getEpisode();
                }
            }
            setResult(MainActivity.UpdateWatchCode, data);
        }
    }

    @Override
    public void onClick(View view) {
        bm = BeamManager.getInstance(AnimeDetailsActivity.this);
        if(bm.isConnected()) {
            GetSourcesAndSubs();
        }
        else {
            Intent intent = new Intent(AnimeDetailsActivity.this, VideoPlayerActivity.class);
            intent.putExtra("anime", anime);
            intent.putExtra("episodeToPlay", userCurrentEpisode);
            intent.putExtra("skip", fragmentEpisodesList.currentSkip);
            intent.putExtra("limit", fragmentEpisodesList.currentLimit);
            startActivityForResult(intent, MainActivity.UpdateWatchCode);
        }
    }

    public void GetSourcesAndSubs()
    {
        final ParallelCallback<ArrayList<Source>> sourceCallback = new ParallelCallback();
        final ParallelCallback<ArrayList<Subtitle>> subsCallback = new ParallelCallback();
        new ParentCallback(sourceCallback, subsCallback) {
            @Override
            protected void handleSuccess() {
                sources = sourceCallback.getData();
                subtitles = subsCallback.getData();

                String defaultLanguageId = Utils.ToLanguageId(App.currentUser.getPreferredAudioLang());
                String defaultQuality = App.currentUser.getPreferredVideoQuality() + "p";
                String defaultSubtitle = Utils.ToLanguageId(App.currentUser.getPreferredSubtitleLang());

                bm.playVideo(null);
            }
        };
        sourceAndSubsLoadIndicator = false;
        String getSourcesUrl;
        String getSubsUrl;

        if(!anime.isMovie()) {
            getSourcesUrl = getString(R.string.odata_path) + "GetSources(animeId=" + anime.getAnimeId() + ",episodeId=" + userCurrentEpisode.getEpisodeId() + ")?$expand=Link($expand=Language)";
            getSubsUrl = getString(R.string.odata_path) + "Subtitles?$filter=AnimeId%20eq%20" + anime.getAnimeId() + "%20and%20EpisodeId%20eq%20" + userCurrentEpisode.getEpisodeId() + "&$expand=Language";
        }
        else {
            getSourcesUrl = getString(R.string.odata_path) + "GetSources(animeId=" + anime.getAnimeId() + ",episodeId=null)?$expand=Link($expand=Language)";
            getSubsUrl = getString(R.string.odata_path) + "Subtitles?$filter=AnimeId%20eq%20" + anime.getAnimeId() + "&$expand=Language";
        }

        ODataUtils.GetEntityList(getSourcesUrl, Source.class, new ODataUtils.EntityCallback<ArrayList<Source>>() {
            @Override
            public void onSuccess(ArrayList<Source> newSources, OdataRequestInfo info) {
                sourceCallback.onSuccess(sources);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AnimeDetailsActivity.this, getString(R.string.error_loading_sources), Toast.LENGTH_LONG).show();
            }
        });

        ODataUtils.GetEntityList(getSubsUrl, Subtitle.class, new ODataUtils.EntityCallback<ArrayList<Subtitle>>() {
            @Override
            public void onSuccess(ArrayList<Subtitle> newSubtitles, OdataRequestInfo info) {
                subsCallback.onSuccess(subtitles);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AnimeDetailsActivity.this, getString(R.string.error_loading_subtitles), Toast.LENGTH_LONG).show();
            }
        });
    }



}
