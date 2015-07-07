package com.topanimestream.models;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.topanimestream.App;
import com.topanimestream.R;

public class Language implements Parcelable {
    private int LanguageId;
    private String Name;
    private String ISO639;

    public Language() {
        super();
    }

    public Language(int languageId, String name, String iSO639) {
        super();
        LanguageId = languageId;
        Name = name;
        ISO639 = iSO639;
    }
    public Language(Parcel in) {
        LanguageId = in.readInt();
        Name = in.readString();
        ISO639 = in.readString();
    }
    public int getFlagDrawable()
    {
        if(getISO639().equals("en"))
            return R.drawable.flag_gb;
        else if(getISO639().equals("fr"))
            return R.drawable.flag_fr;
        else if(getISO639().equals("es"))
            return R.drawable.flag_es;
        else if(getISO639().equals("ja"))
            return R.drawable.flag_jp;
        else if(getISO639().equals("ru"))
            return R.drawable.flag_ru;
        else if(getISO639().equals("ar"))
            return R.drawable.flag_eg;
        else if(getISO639().equals("zh"))
            return R.drawable.flag_cn;
        else if(getISO639().equals("ko"))
            return R.drawable.flag_kr;
        else if(getISO639().equals("sv"))
            return R.drawable.flag_se;
        else if(getISO639().equals("pl"))
            return R.drawable.flag_pl;
        else if(getISO639().equals("ca"))
            return R.drawable.flag_ad;
        else if(getISO639().equals("ro"))
            return R.drawable.flag_ro;
        else if(getISO639().equals("id"))
            return R.drawable.flag_id;
        else if(getISO639().equals("de"))
            return R.drawable.flag_de;
        else if(getISO639().equals("pt"))
            return R.drawable.flag_pt;
        else if(getISO639().equals("da"))
            return R.drawable.flag_dk;
        else if(getISO639().equals("fi"))
            return R.drawable.flag_fi;
        else if(getISO639().equals("vi"))
            return R.drawable.flag_vn;
        else if(getISO639().equals("it"))
            return R.drawable.flag_it;
        else if(getISO639().equals("dv"))
            return R.drawable.flag_nl;


        return 0;
    }
    public int getLanguageId() {
        return LanguageId;
    }

    public void setLanguageId(int languageId) {
        LanguageId = languageId;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getISO639() {
        return ISO639;
    }

    public void setISO639(String iSO639) {
        ISO639 = iSO639;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(LanguageId);
        dest.writeString(Name);
        dest.writeString(ISO639);
    }

    public static final Creator<Language> CREATOR = new Creator<Language>() {
        public Language createFromParcel(Parcel in) {
            return new Language(in);
        }

        public Language[] newArray(int size) {
            return new Language[size];
        }
    };
}
