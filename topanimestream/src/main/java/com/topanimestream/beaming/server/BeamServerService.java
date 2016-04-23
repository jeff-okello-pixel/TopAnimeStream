package com.topanimestream.beaming.server;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.topanimestream.Constants;
import com.topanimestream.utilities.NetworkUtils;

public class BeamServerService extends Service {

    private static BeamServer sServer;

    /**
     * Start service and server
     *
     * @param intent  Intent used for start
     * @param flags   Flags
     * @param startId Id
     * @return Starting sticky or not?
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sServer = new BeamServer(NetworkUtils.getWifiIPAddress(), Constants.SERVER_PORT);

        return START_STICKY;
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

    /**
     * Destroy service and server running inside
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public static BeamServer getServer() {
        return sServer;
    }
}
