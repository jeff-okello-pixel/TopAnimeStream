package urf.animestream;

import urf.animestream.R;
import urf.animestream.managers.AnimationManager;
import urf.animestream.models.Anime;
import urf.animestream.models.Episode;
import urf.animestream.models.EpisodeInformations;
import com.fwwjt.pacjz173199.AdView;
import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.google.sample.castcompanionlibrary.cast.callbacks.VideoCastConsumerImpl;
import com.google.sample.castcompanionlibrary.widgets.MiniController;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class EpisodeDetailsActivity extends ActionBarActivity {

	private Resources r;
	private SharedPreferences prefs;
	private Episode episode;
    private ImageView imgScreenshot;
    private String type;
	private ProviderListFragment providerListFragment;
    private MiniController mMini;
    private Anime anime;
    private VideoCastConsumerImpl mCastConsumer;
    private MenuItem mediaRouteMenuItem;
    private TextView txtEpisodeName;
    private TextView txtDescription;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
        if(App.isTablet)
        {
            setTheme(R.style.PopupTheme);
        }
        else
        {
            setTheme(R.style.Theme_Blue);
        }
		super.onCreate(savedInstanceState);
        //To show activity as dialog and dim the background, you need to declare android:theme="@style/PopupTheme" on for the chosen activity on the manifest


        if(App.isTablet) {
            requestWindowFeature(Window.FEATURE_ACTION_BAR);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND,
                    WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

            float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
            float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.height = 850; //fixed height
            if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                params.width = Math.round(dpWidth / 2); //fixed width
            else
                params.width = Math.round(dpWidth - (dpWidth / 8)); //fixed width
            params.alpha = 1.0f;
            params.dimAmount = 0.5f;
            getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
        }
        setContentView(R.layout.activity_episode_details);
		r = getResources();

		Bundle bundle = getIntent().getExtras();
        episode = bundle.getParcelable("Episode");
        Bundle hackBundle = bundle.getBundle("hackBundle");
        anime = hackBundle.getParcelable("Anime");
        type = bundle.getString("Type");
		
		if(episode == null)
		{
			Toast.makeText(this, getString(R.string.error_loading_episode_details), Toast.LENGTH_LONG).show();
			finish();
		}
        imgScreenshot = (ImageView)findViewById(R.id.imgScreenshot);
        txtEpisodeName = (TextView)findViewById(R.id.txtEpisodeName);
        txtDescription = (TextView)findViewById(R.id.txtDescription);
        String episodeName = "";
        EpisodeInformations episodeInfo = episode.getEpisodeInformations();
        if(episodeInfo != null) {
            if (episodeInfo.getEpisodeName() != null && !episodeInfo.getEpisodeName().equals("")) {
                episodeName = episodeInfo.getEpisodeName();
            }
            else{
                episodeName = getString(R.string.episode) + " " + episode.getEpisodeNumber();
            }

            if(episodeInfo.getSummary() != null && !episodeInfo.getSummary().equals(""))
            {
                txtDescription.setText(episodeInfo.getSummary());
            }
            else if(episodeInfo.getDescription() != null && !episodeInfo.getDescription().equals(""))
            {
                txtDescription.setText(episodeInfo.getDescription());
            }
        }
        txtEpisodeName.setText(episodeName);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {

            actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.ab_solid_blue));
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(Html.fromHtml("<font color=#f0f0f0>" + getString(R.string.episode) + " " + episode.getEpisodeNumber() + "</font>"));
        }
        if(episode.getScreenshot() != null && !episode.getScreenshot().equals(""))
            App.imageLoader.displayImage(getString(R.string.image_host_path) + episode.getScreenshot(),imgScreenshot);
        else
            imgScreenshot.setVisibility(View.GONE);

        FragmentManager fm = getSupportFragmentManager();
        providerListFragment = (ProviderListFragment)fm.findFragmentByTag("providerFragment");
        if(providerListFragment == null) {

            FragmentTransaction ft = fm.beginTransaction();
            providerListFragment = ProviderListFragment.newInstance(-1, episode, type, anime);
            ft.add(R.id.layEpisodeDetails, providerListFragment, "providerFragment");
            ft.commit();
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
	}
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.cast, menu);
        if(App.isPro)
        {
            mediaRouteMenuItem = App.mCastMgr.addMediaRouterButton(menu, R.id.media_route_menu_item);
        }
        return true;
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
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId())
		{
			case android.R.id.home:
                finish();
                if(!App.isTablet)
                    AnimationManager.ActivityFinish(this);
			break;
		}

		return true;
	}

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        if(!App.isTablet)
            AnimationManager.ActivityFinish(this);
    }
}
