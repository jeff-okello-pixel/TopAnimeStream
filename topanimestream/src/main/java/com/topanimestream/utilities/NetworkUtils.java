package com.topanimestream.utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.topanimestream.App;

public class NetworkUtils {

	/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
     * is wifi connected
	 * - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */

    /**
     * Get whether or not a wifi connection is currently connected.
     */
    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) return false;
        return connectivityManager.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI &&
                connectivityManager.getActiveNetworkInfo().isConnected();
    }

    /* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
     * is ethernet connected
	 * - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */

    /**
     * Get whether or not an ethernet connection is currently connected.
     */
    public static boolean isEthernetConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) return false;

        return connectivityManager.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_ETHERNET &&
                connectivityManager.getActiveNetworkInfo().isConnected();

    }

	/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	 * is network connected
	 * - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */

    /**
     * Get whether or not any network connection is present (eg. wifi, 3G, etc.).
     */
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) return false;

        return connectivityManager.getActiveNetworkInfo().isConnected();
    }

    /**
     * Get ip address of the Wifi service
     *
     * @return IP
     */
    public static String getWifiIPAddress() {
        WifiManager wifiMgr = (WifiManager) App.getContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();
        return String.format("%d.%d.%d.%d", (ip & 0xff), (ip >> 8 & 0xff), (ip >> 16 & 0xff), (ip >> 24 & 0xff));
    }


}
