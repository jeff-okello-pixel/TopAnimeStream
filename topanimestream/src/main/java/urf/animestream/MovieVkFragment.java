package urf.animestream;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import urf.animestream.R;
import urf.animestream.managers.Mp4Manager;
import urf.animestream.models.Anime;
import urf.animestream.models.AnimeSource;
import urf.animestream.models.Mirror;
import urf.animestream.models.Vk;


public class MovieVkFragment extends Fragment implements View.OnClickListener {
    private Button btnDubbed;
    private Button btnSubbed;
    private Anime anime;
    private AnimeSource subbedAnimeSource;
    private AnimeSource dubbedAnimeSource;
    private AlertDialog qualityDialog;

    public static MovieVkFragment newInstance(Anime anime) {
        MovieVkFragment fragment = new MovieVkFragment();
        Bundle args = new Bundle();
        args.putParcelable("Anime", anime);
        fragment.setArguments(args);
        return fragment;
    }
    public MovieVkFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movie_vk, container, false);
        Bundle bundle = getArguments();
        anime = bundle.getParcelable("Anime");
        btnDubbed = (Button) view.findViewById(R.id.btnDubbed);
        btnSubbed = (Button) view.findViewById(R.id.btnSubbed);
        btnDubbed.setOnClickListener(this);
        btnSubbed.setOnClickListener(this);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String language = prefs.getString("prefLanguage", "1");
        for(AnimeSource animeSource: anime.getAnimeSources())
        {
            if(String.valueOf(animeSource.getLanguageId()).equals(language) && animeSource.getVks().size() > 0)
            {
                if(animeSource.isSubbed())
                    subbedAnimeSource = animeSource;
                else
                    dubbedAnimeSource = animeSource;
            }
        }
        if(subbedAnimeSource == null)
            btnSubbed.setVisibility(View.GONE);

        if(dubbedAnimeSource == null)
            btnDubbed.setVisibility(View.GONE);

        return view;
    }


    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.btnDubbed:
                ShowQualityDialog(false);
                break;
            case R.id.btnSubbed:
                ShowQualityDialog(true);
                break;

        }
    }
    
    private void ShowQualityDialog(final boolean isSubbed)
    {
        final CharSequence[] items = new CharSequence[]{"720", "480", "360", "240" };

        final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());
        alertBuilder.setTitle(getString(R.string.choose_quality));
        alertBuilder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                Vk vk = isSubbed ? subbedAnimeSource.getVks().get(0) : dubbedAnimeSource.getVks().get(0);
                Mirror mirror = new Mirror(vk);
                Mp4Manager.getMp4(mirror, getActivity(), anime, null);
            }
        });

        qualityDialog = alertBuilder.create();
        qualityDialog.show();
    }
}
