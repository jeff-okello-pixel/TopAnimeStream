package com.topanimestream.utilities;

import android.app.UiModeManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;

import com.topanimestream.App;

public class VersionUtils {

    public static boolean isMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static boolean isLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static boolean isJellyBean() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    public static boolean isAndroidTV() {
        UiModeManager uiModeManager = (UiModeManager) App.getContext().getSystemService(Context.UI_MODE_SERVICE);
        return uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION;
    }

    public static boolean isUsingCorrectBuild() {
        String buidAbi = getBuildAbi();
        if(buidAbi.equalsIgnoreCase("local"))
            return true;

        String deviceAbi;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            deviceAbi = Build.CPU_ABI;
        } else {
            deviceAbi = Build.SUPPORTED_ABIS[0];
        }

        // TODO: if arm64 works remove this
        if(deviceAbi.equalsIgnoreCase("arm64-v8a"))
            deviceAbi = "armeabi-v7a";

        return deviceAbi.equalsIgnoreCase(buidAbi);
    }

    private static String getBuildAbi() {
        PackageManager manager = App.getContext().getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(App.getContext().getPackageName(), 0);
            Integer versionCode = info.versionCode;

            if(info.versionName.contains("local"))
                return "local";

            if(versionCode > 4000000) {
                return "x86";
            } else if(versionCode > 3000000) {
                return "arm64-v8a";
            } else if(versionCode > 2000000) {
                return "armeabi-v7a";
            } else if(versionCode > 1000000) {
                return "armeabi";
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return "unsupported";
    }

}
