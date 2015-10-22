package com.topanimestream;


import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.File;
import java.util.Locale;

import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.squareup.okhttp.OkHttpClient;
import com.topanimestream.preferences.Prefs;
import com.topanimestream.utilities.NetworkChangeReceiver;
import com.topanimestream.utilities.NetworkUtil;
import com.topanimestream.utilities.PixelUtils;
import com.topanimestream.utilities.PrefUtils;
import com.topanimestream.utilities.StorageUtils;
import com.topanimestream.utilities.Utils;

public class App extends Application implements NetworkChangeReceiver.NetworkEvent {
    public static int networkConnection;
    public static Locale locale;
    public static ImageLoader imageLoader;
    private static Connection connection;
    public static boolean languageChanged = false;
    public static String accessToken;
    public static boolean isTablet;
    private static Context context;
    public static String phoneLanguage;
    public static int sdkVersion;
    public static String currentLanguageId;
    private static OkHttpClient sHttpClient;
    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sdkVersion = android.os.Build.VERSION.SDK_INT;
        isTablet = PixelUtils.isTablet(getApplicationContext());
        phoneLanguage = Locale.getDefault().getLanguage();
        if (phoneLanguage.equals(Locale.FRENCH.getLanguage()))
            phoneLanguage = "2";
        else if (phoneLanguage.equals("es"))
            phoneLanguage = "4";
        else
            phoneLanguage = "1"; //default is english
        context = this;
        NetworkChangeReceiver.SetEvent(this);
        networkConnection = NetworkUtil.getConnectivityStatus(this);

        // Create global configuration and initialize ImageLoader with this configuration
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .displayer(new FadeInBitmapDisplayer(300))
                .build();

        ImageLoaderConfiguration imgConfig = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .defaultDisplayImageOptions(defaultOptions)
                .threadPoolSize(10)
                .build();

        imageLoader = ImageLoader.getInstance();
        imageLoader.init(imgConfig);

        App.accessToken = PrefUtils.get(getApplicationContext(), Prefs.ACCESS_TOKEN, "");
        setLocale();


    }

    public static void setLocale() {
        Configuration config = getContext().getResources().getConfiguration();
        Configuration configMirror = new Configuration(config);

        String lang = PrefUtils.get(getContext(), Prefs.LOCALE, "1");
        currentLanguageId = lang;
        boolean shouldSetLanguage = PrefUtils.get(getContext(), Prefs.SHOULD_SET_LANGUAGE, true);
        if (shouldSetLanguage) {
            PrefUtils.remove(getContext(), Prefs.LOCALE);
            lang = Utils.ToLanguageString(phoneLanguage);
            PrefUtils.save(getContext(), Prefs.SHOULD_SET_LANGUAGE, false);
        } else {
            lang = Utils.ToLanguageString(lang);
        }

        if (!"".equals(lang) && !configMirror.locale.getLanguage().equals(lang)) {
            locale = new Locale(lang);
            Locale.setDefault(locale);
            configMirror.locale = locale;
            getContext().getResources().updateConfiguration(configMirror, getContext().getResources().getDisplayMetrics());
        }
    }

    public static OkHttpClient getHttpClient() {
        if (sHttpClient == null) {
            sHttpClient = new OkHttpClient();

            int cacheSize = 10 * 1024 * 1024;
            File cacheLocation = new File(PrefUtils.get(getContext(), Prefs.STORAGE_LOCATION, StorageUtils.getIdealCacheDirectory(getContext()).toString()));
            cacheLocation.mkdirs();
            com.squareup.okhttp.Cache cache = null;
            try {
                cache = new com.squareup.okhttp.Cache(cacheLocation, cacheSize);
            } catch (Exception e) {
                e.printStackTrace();
            }
            sHttpClient.setCache(cache);
        }
        return sHttpClient;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Configuration configMirror = new Configuration(newConfig);
        super.onConfigurationChanged(configMirror);
        if (locale != null) {
            configMirror.locale = locale;
            Locale.setDefault(locale);
            getBaseContext().getResources().updateConfiguration(configMirror, getBaseContext().getResources().getDisplayMetrics());
        }
    }

    @Override
    public void NotConnected() {
        networkConnection = NetworkUtil.TYPE_NOT_CONNECTED;
        IsNetworkConnected();
    }

    @Override
    public void InternetConnected(int type) {
        networkConnection = type;
        IsNetworkConnected();
    }

    static public boolean IsNetworkConnected() {
        if (networkConnection != NetworkUtil.TYPE_NOT_CONNECTED) {
            //Connected
            if (connection != null)
                connection.ConnectionChanged(1);
            return true;
        }
        //Not connected
        if (connection != null)
            connection.ConnectionChanged(0);
        return false;
    }

    static public void SetEvent(Connection event) {
        connection = event;
    }

    public interface Connection {
        public void ConnectionChanged(int connectionType);
    }
}