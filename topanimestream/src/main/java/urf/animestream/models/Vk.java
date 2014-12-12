package urf.animestream.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

public class Vk implements Parcelable {
    private int VkId;
    private int AnimeSourceId;
    private String Quality;
    private String Source;
    private int EpisodeId;
    private String AddedDate;
    private AnimeSource AnimeSource;

    public Vk() {
    }
    public Vk(JSONObject jsonVk)
    {
        try {
            if(!jsonVk.isNull("AnimeSource"))
                this.AnimeSource = new AnimeSource(jsonVk.getJSONObject("AnimeSource"));
            this.VkId = !jsonVk.isNull("Id") ? jsonVk.getInt("Id") : 0;
            this.AnimeSourceId = !jsonVk.isNull("AnimeSourceId") ? jsonVk.getInt("AnimeSourceId") : 0;
            this.Quality = !jsonVk.isNull("Quality") ? jsonVk.getString("Quality") : null;
            this.Source = !jsonVk.isNull("Source") ? jsonVk.getString("Source") : null;
            this.EpisodeId = !jsonVk.isNull("EpisodeId") ? jsonVk.getInt("EpisodeId") : 0;
            this.AddedDate = !jsonVk.isNull("AddedDate") ? jsonVk.getString("AddedDate") : null;

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public Vk(Parcel in) {
        AnimeSource = (AnimeSource) in.readParcelable(AnimeSource.class.getClassLoader());
        VkId = in.readInt();
        AnimeSourceId = in.readInt();
        Quality = in.readString();
        Source = in.readString();
        EpisodeId = in.readInt();
        AddedDate = in.readString();
    }
    public Vk(AnimeSource animeSource, int vkId, int animeSourceId, String quality, String source, int episodeId, String addedDate) {
        AnimeSource = animeSource;
        VkId = vkId;
        AnimeSourceId = animeSourceId;
        Quality = quality;
        Source = source;
        EpisodeId = episodeId;
        AddedDate = addedDate;
    }

    public AnimeSource getAnimeSource() {
        return AnimeSource;
    }

    public void setAnimeSource(AnimeSource animeSource) {
        AnimeSource = animeSource;
    }

    public int getVkId() {
        return VkId;
    }

    public void setVkId(int vkId) {
        VkId = vkId;
    }

    public int getAnimeSourceId() {
        return AnimeSourceId;
    }

    public void setAnimeSourceId(int animeSourceId) {
        AnimeSourceId = animeSourceId;
    }

    public String getQuality() {
        return Quality;
    }

    public void setQuality(String quality) {
        Quality = quality;
    }

    public String getSource() {
        return Source;
    }

    public void setSource(String source) {
        Source = source;
    }

    public int getEpisodeId() {
        return EpisodeId;
    }

    public void setEpisodeId(int episodeId) {
        EpisodeId = episodeId;
    }

    public String getAddedDate() {
        return AddedDate;
    }

    public void setAddedDate(String addedDate) {
        AddedDate = addedDate;
    }

    @Override
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(AnimeSource, flags);
        dest.writeInt(VkId);
        dest.writeInt(AnimeSourceId);
        dest.writeString(Quality);
        dest.writeString(Source);
        dest.writeInt(EpisodeId);
        dest.writeString(AddedDate);



    }

    public static final Parcelable.Creator<Vk> CREATOR = new Parcelable.Creator<Vk>()
    {
        public Vk createFromParcel(Parcel in)
        {
            return new Vk(in);
        }
        public Vk[] newArray(int size)
        {
            return new Vk[size];
        }
    };
}
