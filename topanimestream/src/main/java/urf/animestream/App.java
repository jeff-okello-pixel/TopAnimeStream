package urf.animestream;


import java.util.HashMap;
import java.util.Locale;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;

import urf.animestream.R;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class App extends Application implements NetworkChangeReceiver.NetworkEvent {
    HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();
	public static int networkConnection;
	public static Locale locale;
	public static ImageLoader imageLoader;
	private static Connection connection;
	public static boolean isPro = false;
    public static boolean isGooglePlayVersion = true;
    public static boolean isVkOnly = false;
    public static boolean languageChanged = false;
    public static String accessToken;
    public static boolean isTablet;
	private static Context context;
    public static VideoCastManager mCastMgr = null;
    public static String phoneLanguage;
    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
        GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
        ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a company.
    }
    
    synchronized Tracker getTracker(TrackerName trackerId) {
    	  if (!mTrackers.containsKey(trackerId)) {
	    	  GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
	    	  Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker(R.xml.app_tracker)
	    	  : (trackerId == TrackerName.GLOBAL_TRACKER) ? analytics.newTracker("UA-42435534-3")
	    	  : analytics.newTracker("");
	    	  mTrackers.put(trackerId, t);
    	  }
    	  return mTrackers.get(trackerId);
    }
    public static Context getContext(){
        return context;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        isTablet = Utils.isTablet(getApplicationContext());
        phoneLanguage = Locale.getDefault().getLanguage();
        if(phoneLanguage.equals(Locale.FRENCH.getLanguage()))
            phoneLanguage = "2";
        else if(phoneLanguage.equals("es"))
            phoneLanguage = "4";
        else
            phoneLanguage = "1"; //default is english
        context = this;
        NetworkChangeReceiver.SetEvent(this);
        networkConnection = NetworkUtil.getConnectivityStatus(this);
        
        // Create global configuration and initialize ImageLoader with this configuration
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
        .cacheInMemory(true)
        .build();
        
        ImageLoaderConfiguration imgConfig = new ImageLoaderConfiguration.Builder(getApplicationContext())
        .defaultDisplayImageOptions(defaultOptions)
        .build();

        imageLoader = ImageLoader.getInstance();
        imageLoader.init(imgConfig);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        App.accessToken = prefs.getString("AccessToken","");
        setLocale();


    }
    public void setLocale()
    {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

        Configuration config = getBaseContext().getResources().getConfiguration();
        Configuration configMirror = new Configuration(config);

        String lang = settings.getString("prefLanguage", "1");
        boolean shouldSetLanguage = settings.getBoolean("shouldSetLanguage", true);
        if(!App.isGooglePlayVersion && shouldSetLanguage) {
            settings.edit().remove("prefLanguage").apply();
            lang = Utils.ToLanguageString(phoneLanguage);
            settings.edit().putBoolean("shouldSetLanguage", false).apply();
        }
        else if(!App.isGooglePlayVersion)
            lang = Utils.ToLanguageString(lang);
        else
        {
            //lang = Utils.ToLanguageString(phoneLanguage);
            phoneLanguage = "4";
            lang = "es";
        }
        if (! "".equals(lang) && ! configMirror.locale.getLanguage().equals(lang))
        {
            locale = new Locale(lang);
            Locale.setDefault(locale);
            configMirror.locale = locale;
            getBaseContext().getResources().updateConfiguration(configMirror, getBaseContext().getResources().getDisplayMetrics());
        }
    }
    public static VideoCastManager getCastManager(Context context) {
        if (null == mCastMgr) {
            mCastMgr = VideoCastManager.initialize(context, context.getString(R.string.app_id),
                    null, null);
            mCastMgr.enableFeatures(
                    VideoCastManager.FEATURE_NOTIFICATION |
                            VideoCastManager.FEATURE_LOCKSCREEN |
                            VideoCastManager.FEATURE_DEBUGGING);

        }
        mCastMgr.setContext(context);
        mCastMgr.setStopOnDisconnect(true);
        return mCastMgr;
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
    	Configuration configMirror = new Configuration(newConfig);
        super.onConfigurationChanged(configMirror);
        if (locale != null)
        {
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

    static public boolean IsNetworkConnected()
	{
		if(networkConnection != NetworkUtil.TYPE_NOT_CONNECTED)
		{
			//Connected
			if(connection != null)
				connection.ConnectionChanged(1);
			return true;
		}
		//Not connected
		if(connection != null)
			connection.ConnectionChanged(0);
		return false;
	}
   
    static public void SetEvent(Connection event)
    {
    	connection = event;
    }
	 public interface Connection {
	        public void ConnectionChanged(int connectionType);
	 }
}