package com.topanimestream.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import com.topanimestream.App;

public class Episode implements Parcelable, Comparator<Episode> {
    private int AnimeId;
    private int EpisodeId;
    private String EpisodeNumber;
    private String EpisodeName;
    private String AiredDate;
    private ArrayList<Mirror> Mirrors;
    private ArrayList<Vk> Vks;
    private EpisodeInformations EpisodeInformations;
    private String Screenshot;
    private int Order;
    private String ScreenshotHD;

    public Episode() {
        super();
    }

    public Episode(Parcel in) {
        Parcelable[] parcelableVkArray = in.readParcelableArray(Vk.class.getClassLoader());
        Vk[] resultVkArray = null;
        if (parcelableVkArray != null) {
            resultVkArray = Arrays.copyOf(parcelableVkArray, parcelableVkArray.length, Vk[].class);
            Vks = new ArrayList<Vk>(Arrays.asList(resultVkArray));
        }
        Parcelable[] parcelableMirrorArray = in.readParcelableArray(Mirror.class.getClassLoader());
        Mirror[] resultMirrorArray = null;
        if (parcelableMirrorArray != null) {
            resultMirrorArray = Arrays.copyOf(parcelableMirrorArray, parcelableMirrorArray.length, Mirror[].class);
            Mirrors = new ArrayList<Mirror>(Arrays.asList(resultMirrorArray));
        }
        EpisodeInformations = (EpisodeInformations) in.readParcelable(EpisodeInformations.class.getClassLoader());
        AnimeId = in.readInt();
        EpisodeId = in.readInt();
        EpisodeNumber = in.readString();
        EpisodeName = in.readString();
        AiredDate = in.readString();
        Screenshot = in.readString();
        Order = in.readInt();
    }

    public Episode(JSONObject jsonEpisode, Context context) {
        JSONArray episodeInfoArray = new JSONArray();
        JSONArray episodeMirrors = new JSONArray();
        JSONArray vkArray = new JSONArray();
        try {
            this.setEpisodeNumber(!jsonEpisode.isNull("EpisodeNumber") ? jsonEpisode.getString("EpisodeNumber") : "0");
            this.setEpisodeId(!jsonEpisode.isNull("EpisodeId") ? jsonEpisode.getInt("EpisodeId") : 0);
            this.setAnimeId(!jsonEpisode.isNull("AnimeId") ? jsonEpisode.getInt("AnimeId") : 0);
            this.setAiredDate(!jsonEpisode.isNull("AiredDate") ? jsonEpisode.getString("AiredDate") : null);
            this.setScreenshot(!jsonEpisode.isNull("Screenshot") ? jsonEpisode.getString("Screenshot") : null);
            this.setOrder(!jsonEpisode.isNull("Order") ? jsonEpisode.getInt("Order") : 0);
            episodeMirrors = !jsonEpisode.isNull("Mirrors") ? jsonEpisode.getJSONArray("Mirrors") : null;
            vkArray = !jsonEpisode.isNull("vks") ? jsonEpisode.getJSONArray("vks") : null;
            this.Mirrors = new ArrayList<Mirror>();
            if (episodeMirrors != null) {
                for (int i = 0; i < episodeMirrors.length(); i++) {
                    this.Mirrors.add(new Mirror(episodeMirrors.getJSONObject(i)));
                }
            }
            this.Vks = new ArrayList<Vk>();
            if (vkArray != null) {
                for (int i = 0; i < vkArray.length(); i++) {
                    this.Vks.add(new Vk(vkArray.getJSONObject(i)));
                }
            }
            episodeInfoArray = !jsonEpisode.isNull("EpisodeInformations") ? jsonEpisode.getJSONArray("EpisodeInformations") : null;
            if (episodeInfoArray != null) {
                for (int i = 0; i < episodeInfoArray.length(); i++) {
                    EpisodeInformations episodeInformations = new EpisodeInformations(episodeInfoArray.getJSONObject(i));
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                    if (App.isGooglePlayVersion) {
                        if (String.valueOf(episodeInformations.getLanguageId()).equals(App.phoneLanguage)) {
                            this.EpisodeInformations = episodeInformations;
                            break;
                        }
                    } else {
                        if (String.valueOf(episodeInformations.getLanguageId()).equals(prefs.getString("prefLanguage", "1"))) {
                            this.EpisodeInformations = episodeInformations;
                            break;
                        }
                    }
                }
            }


        } catch (Exception e) {
        }
    }

    public Episode(int animeId, int episodeId, String episodeNumber,
                   String episodeName, String airedDate) {
        super();
        AnimeId = animeId;
        EpisodeId = episodeId;
        EpisodeNumber = episodeNumber;
        EpisodeName = episodeName;
        AiredDate = airedDate;
    }

    public String getScreenshotHD() {
        return ScreenshotHD;
    }

    public void setScreenshotHD(String screenshotHD) {
        ScreenshotHD = screenshotHD;
    }

    public int getOrder() {
        return Order;
    }

    public void setOrder(int order) {
        Order = order;
    }

    public String getScreenshot() {
        return Screenshot;
    }

    public void setScreenshot(String screenshot) {
        Screenshot = screenshot;
    }

    public EpisodeInformations getEpisodeInformations() {
        return EpisodeInformations;
    }

    public void setEpisodeInformations(EpisodeInformations episodeInformations) {
        EpisodeInformations = episodeInformations;
    }

    public ArrayList<Vk> getVks() {
        return Vks;
    }

    public void setVks(ArrayList<Vk> vks) {
        Vks = vks;
    }

    public ArrayList<Mirror> getMirrors() {
        return Mirrors;
    }

    public void setMirrors(ArrayList<Mirror> mirrors) {
        Mirrors = mirrors;
    }

    public int getAnimeId() {
        return AnimeId;
    }

    public void setAnimeId(int animeId) {
        AnimeId = animeId;
    }

    public int getEpisodeId() {
        return EpisodeId;
    }

    public void setEpisodeId(int episodeId) {
        EpisodeId = episodeId;
    }

    public String getEpisodeNumber() {
        return EpisodeNumber;
    }

    public void setEpisodeNumber(String episodeNumber) {
        EpisodeNumber = episodeNumber;
    }

    public String getEpisodeName() {
        return EpisodeName;
    }

    public void setEpisodeName(String episodeName) {
        EpisodeName = episodeName;
    }

    public String getAiredDate() {
        return AiredDate;
    }

    public void setAiredDate(String airedDate) {
        AiredDate = airedDate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (Vks == null)
            Vks = new ArrayList<Vk>();
        if (Mirrors == null)
            Mirrors = new ArrayList<Mirror>();
        Parcelable[] parcelableVkArray = new Parcelable[Vks.size()];
        dest.writeParcelableArray(Vks.toArray(parcelableVkArray), flags);
        Parcelable[] parcelableMirrorArray = new Parcelable[Mirrors.size()];
        dest.writeParcelableArray(Mirrors.toArray(parcelableMirrorArray), flags);
        dest.writeParcelable(EpisodeInformations, flags);
        dest.writeInt(AnimeId);
        dest.writeInt(EpisodeId);
        dest.writeString(EpisodeNumber);
        dest.writeString(EpisodeName);
        dest.writeString(AiredDate);
        dest.writeString(Screenshot);
        dest.writeInt(Order);


    }

    public static final Creator<Episode> CREATOR = new Creator<Episode>() {
        public Episode createFromParcel(Parcel in) {
            return new Episode(in);
        }

        public Episode[] newArray(int size) {
            return new Episode[size];
        }
    };

    @Override
    public int compare(Episode episode, Episode episode2) {
        int val = 0;

        if (episode.getOrder() < episode2.getOrder()) {
            val = -1;
        } else if (episode.getOrder() > episode2.getOrder()) {
            val = 1;
        } else if (episode.getOrder() == episode2.getOrder()) {
            val = 0;
        }
        return val;
    }
}
