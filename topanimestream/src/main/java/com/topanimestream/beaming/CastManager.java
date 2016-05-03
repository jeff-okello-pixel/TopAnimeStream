package com.topanimestream.beaming;

import android.content.Context;
import android.widget.Toast;

import com.topanimestream.App;
import com.topanimestream.R;
import com.topanimestream.models.Anime;
import com.topanimestream.models.Episode;
import com.topanimestream.models.OdataRequestInfo;
import com.topanimestream.models.Source;
import com.topanimestream.models.Subtitle;
import com.topanimestream.parallel.ParallelCallback;
import com.topanimestream.parallel.ParentCallback;
import com.topanimestream.utilities.ODataUtils;
import com.topanimestream.utilities.Utils;

import java.util.ArrayList;

public class CastManager {

    static ArrayList<Source> sources;
    static ArrayList<Subtitle> subtitles;

    public static void StartCasting(final BeamManager bm, Anime anime, Episode userCurrentEpisode, final Context context)
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

        String getSourcesUrl;
        String getSubsUrl;

        if(!anime.isMovie()) {
            getSourcesUrl = context.getString(R.string.odata_path) + "GetSources(animeId=" + anime.getAnimeId() + ",episodeId=" + userCurrentEpisode.getEpisodeId() + ")?$expand=Link($expand=Language)";
            getSubsUrl = context.getString(R.string.odata_path) + "Subtitles?$filter=AnimeId%20eq%20" + anime.getAnimeId() + "%20and%20EpisodeId%20eq%20" + userCurrentEpisode.getEpisodeId() + "&$expand=Language";
        }
        else {
            getSourcesUrl = context.getString(R.string.odata_path) + "GetSources(animeId=" + anime.getAnimeId() + ",episodeId=null)?$expand=Link($expand=Language)";
            getSubsUrl = context.getString(R.string.odata_path) + "Subtitles?$filter=AnimeId%20eq%20" + anime.getAnimeId() + "&$expand=Language";
        }

        ODataUtils.GetEntityList(getSourcesUrl, Source.class, new ODataUtils.EntityCallback<ArrayList<Source>>() {
            @Override
            public void onSuccess(ArrayList<Source> newSources, OdataRequestInfo info) {
                sourceCallback.onSuccess(sources);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(context, context.getString(R.string.error_loading_sources), Toast.LENGTH_LONG).show();
            }
        });

        ODataUtils.GetEntityList(getSubsUrl, Subtitle.class, new ODataUtils.EntityCallback<ArrayList<Subtitle>>() {
            @Override
            public void onSuccess(ArrayList<Subtitle> newSubtitles, OdataRequestInfo info) {
                subsCallback.onSuccess(subtitles);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(context, context.getString(R.string.error_loading_subtitles), Toast.LENGTH_LONG).show();
            }
        });
    }
}
