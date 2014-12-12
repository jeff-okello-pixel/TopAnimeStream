package urf.animestream;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NetworkChangeReceiver extends BroadcastReceiver {
	static NetworkEvent event = null;
    @Override
    public void onReceive(final Context context, final Intent intent) {
    	if(event!=null)
    	{
	        int status = NetworkUtil.getConnectivityStatus(context);
	        switch(status)
	        {
		        case 0:
		        	event.NotConnected();
		        	break;
		        case 1:
		        case 2:
		        case 3:
		        	event.InternetConnected(status);
		        	break;
	        }
    	}
    }
    static public void SetEvent(NetworkEvent event)
    {
    	NetworkChangeReceiver.event = event;
    }
	 public interface NetworkEvent {
	        public void NotConnected();
            public void InternetConnected(int type);
	        
	 }
}
