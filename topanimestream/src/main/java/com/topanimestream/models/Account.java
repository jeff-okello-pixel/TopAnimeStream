package com.topanimestream.models;

import java.util.ArrayList;

public class Account {
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

    public String getProfilePic() {
        return ProfilePic;
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


}
