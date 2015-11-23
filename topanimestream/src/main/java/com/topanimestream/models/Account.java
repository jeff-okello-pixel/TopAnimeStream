package com.topanimestream.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.topanimestream.App;
import com.topanimestream.R;
import com.topanimestream.utilities.ImageUtils;

import java.util.ArrayList;
import java.util.Date;

public class Account implements Parcelable {
    private int AccountId;
    private String Username;
    private String ProfilePic;
    private Date AddedDate;
    private Date LastUpdatedDate;
    private String About;
    private boolean IsDisabled;
    private boolean IsBanned;
    private String BannedReason;
    private Date LastLoginDate;
    private Date LastActivityDate;
    private ArrayList<Role> Roles;
    private String PreferredVideoQuality;
    private String PreferredAudioLang;
    private String PreferredSubtitleLang;
    private String imageHostPath = App.getContext().getResources().getString(R.string.image_host_path);

    public Account() {
    }

    public Account(Parcel in) {
        AccountId = in.readInt();
        Username = in.readString();
        ProfilePic = in.readString();
        AddedDate = new Date(in.readLong());
        LastUpdatedDate = new Date(in.readLong());
        About = in.readString();
        IsDisabled = in.readByte() != 0;
        IsBanned = in.readByte() != 0;
        BannedReason = in.readString();
        LastLoginDate = new Date(in.readLong());
        LastActivityDate = new Date(in.readLong());
        PreferredVideoQuality = in.readString();
        PreferredAudioLang = in.readString();
        PreferredSubtitleLang = in.readString();
    }

    public ArrayList<Role> getRoles() {
        return Roles;
    }

    public void setRoles(ArrayList<Role> roles) {
        Roles = roles;
    }

    public String getBannedReason() {
        return BannedReason;
    }

    public void setBannedReason(String bannedReason) {
        BannedReason = bannedReason;
    }

    public int getAccountId() {
        return AccountId;
    }

    public void setAccountId(int accountId) {
        AccountId = accountId;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }
    public String getProfilePic()
    {
        return ProfilePic;
    }
    public String getProfilePicResize(ImageUtils.ImageSize size) {

        if (ProfilePic == null)
            return null;

        if (size == null || size.equals(""))
            return imageHostPath + ProfilePic;

        return ImageUtils.resizeImage(imageHostPath + ProfilePic, size);

    }

    public void setProfilePic(String profilePic) {
        ProfilePic = profilePic;
    }

    public Date getAddedDate() {
        return AddedDate;
    }

    public void setAddedDate(Date addedDate) {
        AddedDate = addedDate;
    }

    public Date getLastUpdatedDate() {
        return LastUpdatedDate;
    }

    public void setLastUpdatedDate(Date lastUpdatedDate) {
        LastUpdatedDate = lastUpdatedDate;
    }

    public String getAbout() {
        return About;
    }

    public void setAbout(String about) {
        About = about;
    }

    public boolean isDisabled() {
        return IsDisabled;
    }

    public void setDisabled(boolean isDisabled) {
        IsDisabled = isDisabled;
    }

    public boolean isBanned() {
        return IsBanned;
    }

    public void setBanned(boolean isBanned) {
        IsBanned = isBanned;
    }

    public String getPreferredVideoQuality() {
        return PreferredVideoQuality;
    }

    public void setPreferredVideoQuality(String preferredVideoQuality) {
        PreferredVideoQuality = preferredVideoQuality;
    }

    public String getPreferredAudioLang() {
        return PreferredAudioLang;
    }

    public void setPreferredAudioLang(String preferredAudioLang) {
        PreferredAudioLang = preferredAudioLang;
    }

    public String getPreferredSubtitleLang() {
        return PreferredSubtitleLang;
    }

    public void setPreferredSubtitleLang(String preferredSubtitleLang) {
        PreferredSubtitleLang = preferredSubtitleLang;
    }
    public Date getLastLoginDate() {
        return LastLoginDate;
    }

    public void setLastLoginDate(Date lastLoginDate) {
        LastLoginDate = lastLoginDate;
    }

    public Date getLastActivityDate() {
        return LastActivityDate;
    }

    public void setLastActivityDate(Date lastActivityDate) {
        LastActivityDate = lastActivityDate;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(AccountId);
        dest.writeString(Username);
        dest.writeString(ProfilePic);
        dest.writeLong(AddedDate.getTime());
        dest.writeLong(LastUpdatedDate.getTime());
        dest.writeString(About);
        dest.writeByte((byte) (IsDisabled ? 1 : 0));
        dest.writeByte((byte) (IsBanned ? 1 : 0));
        dest.writeString(BannedReason);
        dest.writeLong(LastLoginDate.getTime());
        dest.writeLong(LastActivityDate.getTime());
        dest.writeString(PreferredVideoQuality);
        dest.writeString(PreferredAudioLang);
        dest.writeString(PreferredSubtitleLang);
    }
    public static final Creator<Account> CREATOR = new Creator<Account>() {
        public Account createFromParcel(Parcel in) {
            return new Account(in);
        }

        public Account[] newArray(int size) {
            return new Account[size];
        }
    };

}
