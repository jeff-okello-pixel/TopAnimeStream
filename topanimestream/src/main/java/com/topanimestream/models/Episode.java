package com.topanimestream.models;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import com.topanimestream.App;
import com.topanimestream.preferences.Prefs;
import com.topanimestream.utilities.PrefUtils;

public class Episode implements Parcelable, Comparator<Episode> {
    private ArrayList<Link> Links;
    private ArrayList<EpisodeInformations> EpisodeInformations;
    private ArrayList<Subtitle> Subtitles;

    private int EpisodeId;
    private String EpisodeNumber;
    private int AnimeId;
    private Date AiredDate;
    private String Screenshot;
    private String ScreenshotHD;
    private int Order;
    private String Thumbnail;
    private boolean IsAvailable;
    private Date AvailableDate;
    private String EpisodeName;

    public Episode() {
        super();
    }

    public Episode(Parcel in) {
        Parcelable[] parcelableLinkArray = in.readParcelableArray(Link.class.getClassLoader());
        Link[] resultLinkArray = null;
        if (parcelableLinkArray != null) {
            resultLinkArray = Arrays.copyOf(parcelableLinkArray, parcelableLinkArray.length, Link[].class);
            Links = new ArrayList<Link>(Arrays.asList(resultLinkArray));
        }

        Parcelable[] parcelableEpisodeInfoArray = in.readParcelableArray(Link.class.getClassLoader());
        EpisodeInformations[] resultEpisodeInfoArray = null;
        if (parcelableEpisodeInfoArray != null) {
            resultEpisodeInfoArray = Arrays.copyOf(parcelableEpisodeInfoArray, parcelableEpisodeInfoArray.length, EpisodeInformations[].class);
            EpisodeInformations = new ArrayList<EpisodeInformations>(Arrays.asList(resultEpisodeInfoArray));
        }

        Parcelable[] parcelableSubtitleArray = in.readParcelableArray(Subtitle.class.getClassLoader());
        Subtitle[] resultSubtitleArray = null;
        if (parcelableSubtitleArray != null) {
            resultSubtitleArray = Arrays.copyOf(parcelableSubtitleArray, parcelableSubtitleArray.length, Subtitle[].class);
            Subtitles = new ArrayList<Subtitle>(Arrays.asList(resultSubtitleArray));
        }

        EpisodeId = in.readInt();
        EpisodeNumber = in.readString();
        AnimeId = in.readInt();
        long AiredDateTime = in.readLong();
        if(AiredDateTime != 0)
            AiredDate = new Date(AiredDateTime); //better performance than serializing it.
        else
            AiredDate = null;
        Screenshot = in.readString();
        ScreenshotHD = in.readString();
        Order = in.readInt();
        Thumbnail = in.readString();
        IsAvailable = in.readByte() != 0;
        long AvailableDateTime = in.readLong();
        if(AvailableDateTime != 0)
            AvailableDate = new Date(AvailableDateTime); //better performance than serializing it.
        else
            AvailableDate = null;
        EpisodeName = in.readString();
    }

    public String getScreenshotHD() {
        return ScreenshotHD;
    }

    public ArrayList<Link> getLinks() {
        return Links;
    }

    public void setLinks(ArrayList<Link> links) {
        Links = links;
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
        if(this.EpisodeInformations == null || this.EpisodeInformations.size() < 1)
            return null;

        for(EpisodeInformations info: this.EpisodeInformations)
        {
            if (String.valueOf(info.getLanguageId()).equals(PrefUtils.get(App.getContext(), Prefs.LOCALE, "1"))) {
                return info;
            }
        }

        return null;
    }

    public void setEpisodeInformations(ArrayList<EpisodeInformations> episodeInformations) {
        EpisodeInformations = episodeInformations;
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

    public Date getAiredDate() {
        return AiredDate;
    }

    public void setAiredDate(Date airedDate) {
        AiredDate = airedDate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        if(Links == null)
            Links = new ArrayList<Link>();

        if(EpisodeInformations == null)
            EpisodeInformations = new ArrayList<EpisodeInformations>();

        if(Subtitles == null)
            Subtitles = new ArrayList<Subtitle>();

        Parcelable[] parcelableLinkArray = new Parcelable[Links.size()];
        dest.writeParcelableArray(Links.toArray(parcelableLinkArray), flags);
        Parcelable[] parcelableEpisodeInfoArray = new Parcelable[EpisodeInformations.size()];
        dest.writeParcelableArray(EpisodeInformations.toArray(parcelableEpisodeInfoArray), flags);
        Parcelable[] parcelableSubtitleArray = new Parcelable[Subtitles.size()];
        dest.writeParcelableArray(Subtitles.toArray(parcelableSubtitleArray), flags);

        dest.writeInt(EpisodeId);
        dest.writeString(EpisodeNumber);
        dest.writeInt(AnimeId);
        dest.writeLong(AiredDate != null ? AiredDate.getTime() : 0);
        dest.writeString(Screenshot);
        dest.writeString(ScreenshotHD);
        dest.writeInt(Order);
        dest.writeString(Thumbnail);
        dest.writeByte((byte) (IsAvailable ? 1 : 0));
        dest.writeLong(AvailableDate != null ? AvailableDate.getTime() : 0);
        dest.writeString(EpisodeName);

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
