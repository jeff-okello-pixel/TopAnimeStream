package com.topanimestream.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

public class AnimeSource implements Parcelable {
    private int AnimeSourceId;
    private int AnimeId;
    private boolean IsSubbed;
    private int LanguageId;
    private String SourceUrl;
    private String AddedDate;
    private ArrayList<Mirror> Mirrors = new ArrayList<Mirror>();
    private ArrayList<Vk> vks = new ArrayList<Vk>();

    public AnimeSource() {
        super();
    }

    public AnimeSource(int animeSourceId, int animeId, boolean isDubbed,
                       boolean isSubbed, int languageId, String sourceUrl, String addedDate) {
        super();
        AnimeSourceId = animeSourceId;
        AnimeId = animeId;
        IsSubbed = isSubbed;
        LanguageId = languageId;
        SourceUrl = sourceUrl;
        AddedDate = addedDate;
    }

    public AnimeSource(Parcel in) {
        Parcelable[] parcelableVkArray = in.readParcelableArray(Vk.class.getClassLoader());
        Vk[] resultVkArray = null;
        if (parcelableVkArray != null) {
            resultVkArray = Arrays.copyOf(parcelableVkArray, parcelableVkArray.length, Vk[].class);
            vks = new ArrayList<Vk>(Arrays.asList(resultVkArray));
        }

        Parcelable[] parcelableMirrorArray = in.readParcelableArray(Mirror.class.getClassLoader());
        Mirror[] resultMirrorArray = null;
        if (parcelableMirrorArray != null) {
            resultMirrorArray = Arrays.copyOf(parcelableMirrorArray, parcelableMirrorArray.length, Mirror[].class);
            Mirrors = new ArrayList<Mirror>(Arrays.asList(resultMirrorArray));
        }

        AnimeSourceId = in.readInt();
        AnimeId = in.readInt();
        IsSubbed = in.readByte() != 0;
        LanguageId = in.readInt();
        SourceUrl = in.readString();
        AddedDate = in.readString();

    }

    public AnimeSource(JSONObject jsonAnimeSource) {
        try {
            Mirrors = new ArrayList<Mirror>();
            vks = new ArrayList<Vk>();

            this.AnimeSourceId = !jsonAnimeSource.isNull("AnimeSourceId") ? jsonAnimeSource.getInt("AnimeSourceId") : 0;
            this.AnimeId = !jsonAnimeSource.isNull("AnimeId") ? jsonAnimeSource.getInt("AnimeId") : 0;
            this.AddedDate = !jsonAnimeSource.isNull("AddedDate") ? jsonAnimeSource.getString("AddedDate") : null;
            this.IsSubbed = !jsonAnimeSource.isNull("IsSubbed") ? jsonAnimeSource.getBoolean("IsSubbed") : false;
            this.LanguageId = !jsonAnimeSource.isNull("LanguageId") ? jsonAnimeSource.getInt("LanguageId") : 0;
            this.SourceUrl = !jsonAnimeSource.isNull("SourceUrl") ? jsonAnimeSource.getString("SourceUrl") : null;
            if (!jsonAnimeSource.isNull("Mirrors")) {
                JSONArray jsonMirrors = jsonAnimeSource.getJSONArray("Mirrors");
                for (int i = 0; i < jsonMirrors.length(); i++) {
                    Mirrors.add(new Mirror(jsonMirrors.getJSONObject(i)));
                }
            }

            if (!jsonAnimeSource.isNull("vks")) {
                JSONArray jsonVks = jsonAnimeSource.getJSONArray("vks");
                for (int i = 0; i < jsonVks.length(); i++) {
                    vks.add(new Vk(jsonVks.getJSONObject(i)));
                }
            }
        } catch (Exception e) {

        }
    }

    public ArrayList<Vk> getVks() {
        return vks;
    }

    public void setVks(ArrayList<Vk> vks) {
        this.vks = vks;
    }

    public ArrayList<Mirror> getMirrors() {
        return Mirrors;
    }

    public void setMirrors(ArrayList<Mirror> mirrors) {
        this.Mirrors = mirrors;
    }

    public int getAnimeSourceId() {
        return AnimeSourceId;
    }

    public void setAnimeSourceId(int animeSourceId) {
        AnimeSourceId = animeSourceId;
    }

    public int getAnimeId() {
        return AnimeId;
    }

    public void setAnimeId(int animeId) {
        AnimeId = animeId;
    }

    public boolean isSubbed() {
        return IsSubbed;
    }

    public void setIsSubbed(boolean isSubbed) {
        IsSubbed = isSubbed;
    }

    public int getLanguageId() {
        return LanguageId;
    }

    public void setLanguageId(int languageId) {
        LanguageId = languageId;
    }

    public String getSourceUrl() {
        return SourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        SourceUrl = sourceUrl;
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
        Parcelable[] parcelableVkArray = new Parcelable[vks.size()];
        dest.writeParcelableArray(vks.toArray(parcelableVkArray), flags);

        Parcelable[] parcelableMirrorArray = new Parcelable[Mirrors.size()];
        dest.writeParcelableArray(Mirrors.toArray(parcelableMirrorArray), flags);

        dest.writeInt(AnimeSourceId);
        dest.writeInt(AnimeId);
        dest.writeByte((byte) (IsSubbed ? 1 : 0));
        dest.writeInt(LanguageId);
        dest.writeString(SourceUrl);
        dest.writeString(AddedDate);
    }

    public static final Creator<AnimeSource> CREATOR = new Creator<AnimeSource>() {
        public AnimeSource createFromParcel(Parcel in) {
            return new AnimeSource(in);
        }

        public AnimeSource[] newArray(int size) {
            return new AnimeSource[size];
        }
    };
}
