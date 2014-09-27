package com.aniblitz;



import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.aniblitz.models.Anime;


public class AnimeDetailsFragment extends Fragment {
    private ImageView imgBackdrop;
    private TextView txtTitle;
    private TextView txtDescription;
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
        return view;
    }

    public void setAnime(Anime anime)
    {
        if(anime.getBackdropPath(null) != null)
            App.imageLoader.displayImage(anime.getBackdropPath("500"), imgBackdrop);
        else
            imgBackdrop.setVisibility(View.GONE);

        txtTitle.setText(anime.getName());
        txtDescription.setText(anime.getDescription(getActivity()));
    }


}
