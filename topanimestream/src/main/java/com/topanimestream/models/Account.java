package com.topanimestream.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.topanimestream.App;
import com.topanimestream.R;

import java.util.ArrayList;

public class Account implements Parcelable {
    private int AccountId;
    private String Username;
    private String ProfilePic;
    private String AddedDate;
    private String LastUpdatedDate;
    private String About;
    private boolean IsDisabled;
    private boolean IsBanned;
    private String BannedReason;
    private String LastLoginDate;
    private String LastActivityDate;
    private ArrayList<Role> Roles;
    private String imageHostPath = App.getContext().getResources().getString(R.string.image_host_path);
    public Account() {
    }

    public Account(int accountId, String username, String profilePic, String addedDate, String lastUpdatedDate, String about, boolean isDisabled, boolean isBanned, String bannedReason, String lastLoginDate, String lastActivityDate) {
        AccountId = accountId;
        Username = username;
        ProfilePic = profilePic;
        AddedDate = addedDate;
        LastUpdatedDate = lastUpdatedDate;
        About = about;
        IsDisabled = isDisabled;
        IsBanned = isBanned;
        BannedReason = bannedReason;
        LastLoginDate = lastLoginDate;
        LastActivityDate = lastActivityDate;
    }
    public Account(Parcel in) {
        AccountId = in.readInt();
        Username = in.readString();
        ProfilePic = in.readString();
        AddedDate = in.readString();
        LastUpdatedDate = in.readString();
        About = in.readString();
        IsDisabled = in.readByte() != 0;
        IsBanned = in.readByte() != 0;
        BannedReason = in.readString();
        LastLoginDate = in.readString();
        LastActivityDate = in.readString();
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
    public String getProfilePicResize(String size) {

        if (ProfilePic == null)
            return null;

        if (size == null || size.equals(""))
            return imageHostPath + ProfilePic;

        String imageName = ProfilePic.substring(ProfilePic.lastIndexOf("/") + 1);
        imageName = "w" + size + "_" + imageName;
        String fullProfilePicPath = imageHostPath + ProfilePic.substring(0, ProfilePic.lastIndexOf("/") + 1) + imageName;
        return fullProfilePicPath;
    }

    public void setProfilePic(String profilePic) {
        ProfilePic = profilePic;
    }

    public String getAddedDate() {
        return AddedDate;
    }

    public void setAddedDate(String addedDate) {
        AddedDate = addedDate;
    }

    public String getLastUpdatedDate() {
        return LastUpdatedDate;
    }

    public void setLastUpdatedDate(String lastUpdatedDate) {
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

    public String getLastLoginDate() {
        return LastLoginDate;
    }

    public void setLastLoginDate(String lastLoginDate) {
        LastLoginDate = lastLoginDate;
    }

    public String getLastActivityDate() {
        return LastActivityDate;
    }

    public void setLastActivityDate(String lastActivityDate) {
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
        dest.writeString(AddedDate);
        dest.writeString(LastUpdatedDate);
        dest.writeString(About);
        dest.writeByte((byte) (IsDisabled ? 1 : 0));
        dest.writeByte((byte) (IsBanned ? 1 : 0));
        dest.writeString(BannedReason);
        dest.writeString(LastLoginDate);
        dest.writeString(LastActivityDate);

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
