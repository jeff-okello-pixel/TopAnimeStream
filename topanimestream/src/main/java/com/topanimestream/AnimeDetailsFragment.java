package com.topanimestream;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.topanimestream.models.Anime;
import com.topanimestream.R;


public class AnimeDetailsFragment extends Fragment {
    private ImageView imgBackdrop;
    private TextView txtTitle;
    private TextView txtDescription;
    private TextView txtGenres;
    private RatingBar rtbRating;

    public AnimeDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_anime_details, container, false);
        imgBackdrop = (ImageView) view.findViewById(R.id.imgBackdrop);
        txtTitle = (TextView) view.findViewById(R.id.txtTitle);
        txtDescription = (TextView) view.findViewById(R.id.txtDescription);
        txtGenres = (TextView) view.findViewById(R.id.txtGenres);
        rtbRating = (RatingBar) view.findViewById(R.id.rtbRating);
        return view;
    }

    public void setAnime(Anime anime) {
        if (anime.getBackdropPath(null) != null)
            App.imageLoader.displayImage(anime.getBackdropPath("500"), imgBackdrop);
        else
            imgBackdrop.setVisibility(View.GONE);

        txtTitle.setText(anime.getName());
        txtDescription.setText(anime.getDescription(getActivity()));
        txtGenres.setText(anime.getGenresFormatted());
        if (anime.getRating() != null)
            rtbRating.setRating((float) Utils.roundToHalf(anime.getRating() != 0 ? anime.getRating() / 2 : anime.getRating()));

    }


}
