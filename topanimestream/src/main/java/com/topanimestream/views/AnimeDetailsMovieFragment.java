package com.topanimestream.views;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.topanimestream.App;
import com.topanimestream.R;
import com.topanimestream.models.Anime;
import com.topanimestream.utilities.ImageUtils;
import com.topanimestream.utilities.Utils;


public class AnimeDetailsMovieFragment extends Fragment implements View.OnClickListener {
    private ImageView imgPoster;
    private TextView txtTitle;
    private TextView txtDescription;
    private TextView txtGenres;
    private RatingBar rtbRating;
    private ScrollView scrollView;
    private RelativeLayout layPlay;
    public AnimeDetailsMovieFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    public static AnimeDetailsMovieFragment newInstance(Anime anime) {
        AnimeDetailsMovieFragment ttFrag = new AnimeDetailsMovieFragment();
        Bundle args = new Bundle();
        args.putParcelable("anime", anime);
        ttFrag.setArguments(args);
        return ttFrag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_anime_details_movie, container, false);

        imgPoster = (ImageView) view.findViewById(R.id.imgPoster);
        txtTitle = (TextView) view.findViewById(R.id.txtTitle);
        txtDescription = (TextView) view.findViewById(R.id.txtDescription);
        txtGenres = (TextView) view.findViewById(R.id.txtGenres);
        rtbRating = (RatingBar) view.findViewById(R.id.rtbRating);
        scrollView = (ScrollView) view.findViewById(R.id.scrollView);
        layPlay = (RelativeLayout) view.findViewById(R.id.layPlay);
        layPlay.setOnClickListener(this);
        if(scrollView != null)
        {
            scrollView.setSaveEnabled(false);
        }
        setAnime((Anime)getArguments().getParcelable("anime"));
        return view;
    }

    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.layPlay:
                ((AnimeDetailsMovieCallback)getActivity()).OnMoviePlayClick();
                break;

        }
    }

    public interface AnimeDetailsMovieCallback {
        void OnMoviePlayClick();
    }
    public void setAnime(Anime anime) {
        App.imageLoader.displayImage(ImageUtils.resizeImage(getString(R.string.image_host_path) + anime.getPosterPath(), ImageUtils.ImageSize.w500.getValue()), imgPoster);
        txtTitle.setText(anime.getName());
        txtDescription.setText(anime.getDescription(getActivity()));
        txtGenres.setText(anime.getGenresFormatted());
        if (anime.getRating() != null)
            rtbRating.setRating((float) Utils.roundToHalf(anime.getRating() != 0 ? anime.getRating() / 2 : anime.getRating()));

    }


}
