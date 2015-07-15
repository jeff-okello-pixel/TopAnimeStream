package com.topanimestream.views;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.topanimestream.App;
import com.topanimestream.utilities.Utils;
import com.topanimestream.models.Anime;
import com.topanimestream.R;

import butterknife.Bind;
import butterknife.ButterKnife;


public class AnimeDetailsFragment extends Fragment {

    @Bind(R.id.imgBackdrop)
    ImageView imgBackdrop;

    @Bind(R.id.txtTitle)
    TextView txtTitle;

    @Bind(R.id.txtDescription)
    TextView txtDescription;

    @Bind(R.id.txtGenres)
    TextView txtGenres;

    @Bind(R.id.rtbRating)
    RatingBar rtbRating;

    @Bind(R.id.scrollView2)
    ScrollView scrollView;



    public AnimeDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    public static AnimeDetailsFragment newInstance(Anime anime) {
        AnimeDetailsFragment ttFrag = new AnimeDetailsFragment();
        Bundle args = new Bundle();
        args.putParcelable("anime", anime);
        ttFrag.setArguments(args);
        return ttFrag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_anime_details, container, false);
        ButterKnife.bind(this, view);

        if(scrollView != null)
        {
            scrollView.setSaveEnabled(false);
        }
        setAnime((Anime)getArguments().getParcelable("anime"));
        return view;
    }

    public void setAnime(Anime anime) {
        if (anime.getBackdropPath() != null)
            App.imageLoader.displayImage(Utils.resizeImage(getString(R.string.image_host_path) + anime.getBackdropPath(), App.ImageSize.w500.getValue()), imgBackdrop);
        else
            imgBackdrop.setVisibility(View.GONE);

        txtTitle.setText(anime.getName());
        txtDescription.setText(anime.getDescription(getActivity()));
        txtGenres.setText(anime.getGenresFormatted());
        if (anime.getRating() != null)
            rtbRating.setRating((float) Utils.roundToHalf(anime.getRating() != 0 ? anime.getRating() / 2 : anime.getRating()));

    }


}
