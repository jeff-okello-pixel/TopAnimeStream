package com.aniblitz;

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
		        	event.WifiConnected();
		        	break;
		        case 2:
		        	event.MobileConnected();
		        	break;
		        case 3:
		        	event.EthernetConnected();
		        	break;
	        }
    	}
    }
    static public void SetEvent(NetworkEvent event)
    {
    	NetworkChangeReceiver.event = event;
    }
	 public interface NetworkEvent {
	        public void WifiConnected();
	        public void MobileConnected();
	        public void EthernetConnected();
	        public void NotConnected();
	        
	 }
}
