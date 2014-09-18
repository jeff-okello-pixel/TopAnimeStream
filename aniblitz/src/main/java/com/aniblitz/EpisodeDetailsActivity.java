package com.aniblitz;

import com.aniblitz.models.Episode;
import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.common.images.WebImage;
import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.google.sample.castcompanionlibrary.cast.callbacks.VideoCastConsumerImpl;
import com.google.sample.castcompanionlibrary.widgets.MiniController;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class EpisodeDetailsActivity extends ActionBarActivity {

	private int animeId;
	private Resources r;
	private SharedPreferences prefs;
	private Episode episode;
    private ImageView imgScreenshot;
    private String type;
	private ProviderListFragment providerListFragment;
    private MiniController mMini;
    private VideoCastConsumerImpl mCastConsumer;
    private MenuItem mediaRouteMenuItem;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.Theme_Blue);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_episode_details);
		r = getResources();
		animeId = getIntent().getIntExtra("AnimeId", 0);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		Bundle bundle = getIntent().getExtras();
        episode = (Episode)bundle.getParcelable("Episode");
        type = bundle.getString("Type");
		
		if(episode == null)
		{
			Toast.makeText(this, getString(R.string.error_loading_episode_details), Toast.LENGTH_LONG).show();
			finish();
		}
        imgScreenshot = (ImageView)findViewById(R.id.imgScreenshot);
        String episodeName;
        if(episode.getEpisodeName() != null && !episode.getEpisodeName().equals(""))
            episodeName = episode.getEpisodeName();
        else
            episodeName = getString(R.string.episode) + episode.getEpisodeNumber();

        actionBar.setTitle(Html.fromHtml("<font color=#f0f0f0>" + episodeName + "</font>"));
        if(episode.getScreenshot() != null && !episode.getScreenshot().equals(""))
            App.imageLoader.displayImage(getString(R.string.image_host_path) + episode.getScreenshot(),imgScreenshot);
        else
            imgScreenshot.setVisibility(View.GONE);

        FragmentManager fm = getSupportFragmentManager();
        providerListFragment = (ProviderListFragment)fm.findFragmentByTag("providerFragment");
        if(providerListFragment == null) {

            FragmentTransaction ft = fm.beginTransaction();
            providerListFragment = ProviderListFragment.newInstance(-1, episode.getMirrors(), type);
            ft.add(R.id.layEpisodeDetails, providerListFragment, "providerFragment");
            ft.commit();
        }
        if(App.isPro) {
            VideoCastManager.checkGooglePlaySevices(this);

            App.getCastManager(this);

            // -- Adding MiniController
            mMini = (MiniController) findViewById(R.id.miniController);
            App.mCastMgr.addMiniController(mMini);

            mCastConsumer = new VideoCastConsumerImpl() {
                @Override
                public void onApplicationConnected(ApplicationMetadata appMetadata,
                                                   String sessionId, boolean wasLaunched) {

                }

                @Override
                public void onApplicationDisconnected(int errorCode) {

                }

                @Override
                public void onDisconnected() {

                }

                @Override
                public void onRemoteMediaPlayerMetadataUpdated() {

                }

                @Override
                public void onFailed(int resourceId, int statusCode) {

                }

                @Override
                public void onConnectionSuspended(int cause) {

                }

                @Override
                public void onConnectivityRecovered() {

                }
            };
            App.mCastMgr.reconnectSessionIfPossible(this, false);
        }
	}
    private static MediaInfo buildMediaInfo(String title,
                                            String subTitle, String studio, String url, String imgUrl, String bigImageUrl) {
        MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);

        movieMetadata.putString(MediaMetadata.KEY_SUBTITLE, subTitle);
        movieMetadata.putString(MediaMetadata.KEY_TITLE, title);
        movieMetadata.putString(MediaMetadata.KEY_STUDIO, studio);
        movieMetadata.addImage(new WebImage(Uri.parse(imgUrl)));
        movieMetadata.addImage(new WebImage(Uri.parse(bigImageUrl)));

        return new MediaInfo.Builder(url)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType("video/mp4")
                .setMetadata(movieMetadata)
                .build();
    }
    private void loadRemoteMedia(int position, boolean autoPlay, MediaInfo mediaInfo) {
        App.mCastMgr.startCastControllerActivity(this, mediaInfo, position, autoPlay);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.cast, menu);
        if(App.isPro)
        {
            mediaRouteMenuItem = App.mCastMgr.addMediaRouterButton(menu, R.id.media_route_menu_item);
        }
        return true;
    }  @Override
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
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId())
		{
			case android.R.id.home:
				finish();
			break;
		}

		return true;
	}


}
