package com.topanimestream.dialogfragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.topanimestream.App;
import com.topanimestream.R;
import com.topanimestream.beaming.BeamManager;
import com.topanimestream.models.Anime;
import com.topanimestream.models.Episode;
import com.topanimestream.models.Language;
import com.topanimestream.models.OdataRequestInfo;
import com.topanimestream.models.Source;
import com.topanimestream.models.StreamInfo;
import com.topanimestream.models.Subtitle;
import com.topanimestream.parallel.ParallelCallback;
import com.topanimestream.parallel.ParentCallback;
import com.topanimestream.utilities.ODataUtils;
import com.topanimestream.utilities.Utils;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CastOptionsDialogFragment extends DialogFragment implements View.OnClickListener {

    @Bind(R.id.btnCast)
    Button btnCast;

    @Bind(R.id.spinnerLanguages)
    Spinner spinnerLanguages;

    @Bind(R.id.spinnerSubtitles)
    Spinner spinnerSubtitles;

    @Bind(R.id.spinnerQualities)
    Spinner spinnerQualities;

    @Bind(R.id.layCastOptions)
    LinearLayout layCastOptions;

    @Bind(R.id.progressBarLoading)
    ProgressBar progressBarLoading;

    ArrayList<Source> sources;
    ArrayList<Language> languages;
    ArrayList<Subtitle> subtitles;
    Anime anime;
    Episode userCurrentEpisode;
    BeamManager bm;

    public static CastOptionsDialogFragment newInstance(Anime anime, Episode userCurrentEpisode) {
        CastOptionsDialogFragment dialogFragment = new CastOptionsDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable("anime", anime);
        args.putParcelable("episode", userCurrentEpisode);
        dialogFragment.setArguments(args);
        return dialogFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        bm = BeamManager.getInstance(getActivity());
        anime = getArguments().getParcelable("anime");
        userCurrentEpisode = getArguments().getParcelable("episode");

        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_dialog_cast_options, null, false);
        ButterKnife.bind(this, view);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setTitle("Cast options");
        builder.setCancelable(true);


        btnCast.setOnClickListener(this);


        GetSourcesAndSubs();

        return builder.create();
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.btnCast:
                Source sourceToPlay = null;
                String language = spinnerLanguages.getSelectedItem().toString();
                String subtitle = spinnerSubtitles.getSelectedItem().toString();
                String quality = spinnerQualities.getSelectedItem().toString();

                for(Source source:sources)
                {
                    if(source.getLink().getLanguage().getName().equalsIgnoreCase(language) &&
                       source.getQuality().equalsIgnoreCase(quality))
                    {
                        sourceToPlay = source;
                    }
                }

                //TODO build streaminfo
                StreamInfo streamInfo = new StreamInfo();

                //TODO download subtitle
                bm.playVideo();
                break;
        }
    }

    public void RefreshAvailableQualities(Language language){
        String defaultQuality = App.currentUser.getPreferredVideoQuality() + "p";
        ArrayList<Source> goodLanguageSources = new ArrayList<>();
        Source defaultSource = null;
        for(Source source:sources)
        {
            if(source.getLink().getLanguageId() == language.getLanguageId())
            {
                goodLanguageSources.add(source);
            }
        }

        for(Source source:goodLanguageSources)
        {
            if(source.getQuality().equals(defaultQuality))
            {
                defaultSource = source;
                break;
            }
        }

        if(defaultSource == null)
        {
            for(Source source:goodLanguageSources)
            {
                if(source.getQuality().equals("1080p"))
                {
                    defaultSource = source;
                    break;
                }
            }

            if(defaultSource == null)
            {
                for(Source source:goodLanguageSources)
                {
                    if(source.getQuality().equals("720p"))
                    {
                        defaultSource = source;
                        break;
                    }
                }
            }

            if(defaultSource == null)
            {
                for(Source source:goodLanguageSources)
                {
                    if(source.getQuality().equals("360p"))
                    {
                        defaultSource = source;
                        break;
                    }
                }
            }

            if(defaultSource == null)
            {
                //What?! set anything
                defaultSource = sources.get(0);
            }
        }

        int defaultQualityIndex = -1;
        for(int i = 0; i < goodLanguageSources.size(); i++){
            if(goodLanguageSources.get(i).toString().equalsIgnoreCase(defaultQuality))
            {
                defaultQualityIndex = i;
                break;
            }
        }
        ArrayAdapter qualityAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item, goodLanguageSources);
        spinnerQualities.setAdapter(qualityAdapter);
        spinnerQualities.setSelection(defaultQualityIndex);

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
                languages = new ArrayList<>();

                subtitles.add(0, new Subtitle());

                for(Source source:sources)
                {
                    boolean containsLanguage = false;
                    for(Language lang:languages)
                    {
                        if(source.getLink().getLanguageId() == lang.getLanguageId())
                        {
                            containsLanguage = true;
                        }
                    }

                    if(!containsLanguage)
                        languages.add(source.getLink().getLanguage());
                }


                String defaultLanguage = Utils.ToLanguageStringDisplay(App.currentUser.getPreferredAudioLang());
                String defaultSubtitle = Utils.ToLanguageStringDisplay(App.currentUser.getPreferredSubtitleLang());

                int defaultLanguageIndex = -1;
                for(int i = 0; i < languages.size(); i++)
                {
                    if(languages.get(i).toString().equalsIgnoreCase(defaultLanguage)) {
                        defaultLanguageIndex = i;
                        break;
                    }
                }
                final ArrayAdapter languageAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item, languages);
                spinnerLanguages.setAdapter(languageAdapter);
                spinnerLanguages.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                        RefreshAvailableQualities((Language)languageAdapter.getItem(position));
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                        //What to do here?
                    }
                });
                spinnerLanguages.setSelection(defaultLanguageIndex);

                ArrayAdapter subtitlesAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item, subtitles);
                spinnerSubtitles.setAdapter(subtitlesAdapter);

                int defaultSubtitleIndex = -1;
                for(int i = 0; i < subtitles.size(); i++) {
                    if (subtitles.get(i).getSubtitleId() == 0) {
                        if (defaultSubtitle.equalsIgnoreCase("none")) {
                            defaultSubtitleIndex = i;
                            break;
                        }
                    }
                    else if (subtitles.get(i).getLanguage().toString().equalsIgnoreCase(defaultSubtitle)) {
                        defaultSubtitleIndex = i;
                        break;
                    }
                }
                spinnerSubtitles.setSelection(defaultSubtitleIndex);

                progressBarLoading.setVisibility(View.GONE);
                layCastOptions.setVisibility(View.VISIBLE);
                //bm.playVideo(null);
            }
        };

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
                sourceCallback.onSuccess(newSources);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getActivity(), getString(R.string.error_loading_sources), Toast.LENGTH_LONG).show();
            }
        });

        ODataUtils.GetEntityList(getSubsUrl, Subtitle.class, new ODataUtils.EntityCallback<ArrayList<Subtitle>>() {
            @Override
            public void onSuccess(ArrayList<Subtitle> newSubtitles, OdataRequestInfo info) {
                subsCallback.onSuccess(newSubtitles);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getActivity(), getString(R.string.error_loading_subtitles), Toast.LENGTH_LONG).show();
            }
        });
    }
}
