package com.topanimestream.models;

import java.util.ArrayList;

final public class CurrentUser {
    public static int AccountId;
    public static String Username;
    public static String ProfilePic;
    public static String AddedDate;
    public static String LastUpdatedDate;
    public static String About;
    public static boolean IsDisabled;
    public static boolean IsBanned;
    public static String BannedReason;
    public static String LastLoginDate;
    public static String LastActivityDate;
    public static ArrayList<Role> Roles;
    public static void SetCurrentUser(Account account)
    {

        CurrentUser.AccountId = account.getAccountId();
        CurrentUser.Username = account.getUsername();
        CurrentUser.ProfilePic = account.getProfilePic();
        CurrentUser.AddedDate = account.getAddedDate();
        CurrentUser.LastUpdatedDate = account.getLastUpdatedDate();
        CurrentUser.About = account.getAbout();
        CurrentUser.IsDisabled = account.isDisabled();
        CurrentUser.IsBanned = account.isBanned();
        CurrentUser.BannedReason = account.getBannedReason();
        CurrentUser.LastLoginDate = account.getLastLoginDate();
        CurrentUser.LastActivityDate = account.getLastActivityDate();
        CurrentUser.Roles = account.getRoles();
    }
}
