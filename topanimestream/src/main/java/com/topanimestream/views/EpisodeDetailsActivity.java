package com.topanimestream.views;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.topanimestream.App;
import com.topanimestream.managers.AnimationManager;
import com.topanimestream.models.Anime;
import com.topanimestream.models.Episode;
import com.topanimestream.models.EpisodeInformations;
import com.topanimestream.R;
import com.topanimestream.utilities.PixelUtils;

import butterknife.Bind;
import butterknife.ButterKnife;

public class EpisodeDetailsActivity extends TASBaseActivity {
    private Episode episode;
    private String type;
    private ProviderListFragment providerListFragment;
    private Anime anime;

    @Bind(R.id.txtEpisodeName)
    TextView txtEpisodeName;

    @Bind(R.id.txtDescription)
    TextView txtDescription;

    @Bind(R.id.imgScreenshot)
    ImageView imgScreenshot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //TODO test this
        if (App.isTablet) {
            setTheme(R.style.PopupTheme);
            ButterKnife.bind(this);
        }
        else {
            super.onCreate(savedInstanceState, R.layout.activity_episode_details);
        }
        //To show activity as dialog and dim the background, you need to declare android:theme="@style/PopupTheme" on for the chosen activity on the manifest


        if (App.isTablet) {
            requestWindowFeature(Window.FEATURE_ACTION_BAR);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND,
                    WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

            float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
            float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.height = 850; //fixed height
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                params.width = Math.round(dpWidth / 2); //fixed width
            else
                params.width = Math.round(dpWidth - (dpWidth / 8)); //fixed width
            params.alpha = 1.0f;
            params.dimAmount = 0.5f;
            getWindow().setAttributes((WindowManager.LayoutParams) params);
        }

        Bundle bundle = getIntent().getExtras();
        episode = bundle.getParcelable("Episode");
        Bundle hackBundle = bundle.getBundle("hackBundle");
        anime = hackBundle.getParcelable("Anime");
        type = bundle.getString("Type");

        if (episode == null) {
            Toast.makeText(this, getString(R.string.error_loading_episode_details), Toast.LENGTH_LONG).show();
            finish();
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
            //toolbar.setBackgroundDrawable(getResources().getDrawable(R.drawable.ab_solid_blue));
            toolbar.setTitle(getString(R.string.episode) + " " + episode.getEpisodeNumber());
            setSupportActionBar(toolbar);
        }


        String episodeName = "";
        EpisodeInformations episodeInfo = episode.getEpisodeInformations();
        if (episodeInfo != null) {
            if (episodeInfo.getEpisodeName() != null && !episodeInfo.getEpisodeName().equals("")) {
                episodeName = episodeInfo.getEpisodeName();
            } else {
                episodeName = getString(R.string.episode) + " " + episode.getEpisodeNumber();
            }

            if (episodeInfo.getSummary() != null && !episodeInfo.getSummary().equals("")) {
                txtDescription.setText(episodeInfo.getSummary());
            } else if (episodeInfo.getDescription() != null && !episodeInfo.getDescription().equals("")) {
                txtDescription.setText(episodeInfo.getDescription());
            }
        }
        txtEpisodeName.setText(episodeName);
        if (episode.getScreenshot() != null && !episode.getScreenshot().equals(""))
            App.imageLoader.displayImage(getString(R.string.image_host_path) + episode.getScreenshot(), imgScreenshot);
        else
            imgScreenshot.setVisibility(View.GONE);

        FragmentManager fm = getSupportFragmentManager();
        providerListFragment = (ProviderListFragment) fm.findFragmentByTag("providerFragment");
        if (providerListFragment == null) {

            FragmentTransaction ft = fm.beginTransaction();
            providerListFragment = ProviderListFragment.newInstance(-1, episode, type, anime);
            ft.add(R.id.layEpisodeDetails, providerListFragment, "providerFragment");
            ft.commit();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //TODO test this
            case android.R.id.home:
                if (PixelUtils.isTablet(this)) {
                    finish();
                }
                else
                {
                    super.onOptionsItemSelected(item);
                }
                break;
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        if (!PixelUtils.isTablet(this))
            AnimationManager.ActivityFinish(this);
    }
}
