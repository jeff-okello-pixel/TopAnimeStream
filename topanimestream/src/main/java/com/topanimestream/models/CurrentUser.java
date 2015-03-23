package com.topanimestream.models;

import com.topanimestream.App;
import com.topanimestream.R;

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
    private static String imageHostPath = App.getContext().getResources().getString(R.string.image_host_path);
    private static Role userRole = null;
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

    public static String GetProfilePicResize(String size)
    {

        if (ProfilePic == null)
            return null;

        if (size == null || size.equals(""))
            return imageHostPath + ProfilePic;

        String imageName = ProfilePic.substring(ProfilePic.lastIndexOf("/") + 1);
        imageName = "w" + size + "_" + imageName;
        String fullProfilePicPath = imageHostPath + ProfilePic.substring(0, ProfilePic.lastIndexOf("/") + 1) + imageName;
        return fullProfilePicPath;
    }

    public static Role GetRole()
    {
        if(userRole == null) {
            for (Role role : CurrentUser.Roles) {
                if (userRole == null || role.getRankOrder() < userRole.getRankOrder())
                    userRole = role;
            }
        }

        return userRole;
    }


}
