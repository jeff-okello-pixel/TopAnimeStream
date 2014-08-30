package com.aniblitz;

import java.util.Locale;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.text.Html;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ListView;

public class Settings extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener
{
	private App app;
	private boolean mStopOnExit;
	
    protected void onCreate(final Bundle savedInstanceState)
    {
    	setTheme(R.style.Theme_Blue);
        super.onCreate(savedInstanceState);
        app = (App) this.getApplication();
        
        this.addPreferencesFromResource(R.xml.settings);
      
        setTitle(Html.fromHtml("<font color=#f0f0f0>" + getString(R.string.action_settings) + "</font>"));

		  
        //getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        ListView lv = (ListView) findViewById(android.R.id.list);
        if(lv != null)
        {
        	ViewGroup parent = (ViewGroup)lv.getParent();
        	if(parent != null)
        	{
        	    parent.setPadding(0, 0, 0, 0);
        	}
        }
        SetSummary();
    }
    
	@Override
	  public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
		    case android.R.id.home:
		    	finish();
		    	break; 
	    }
	    return true;
	}
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //get the new value from Intent data
        if (requestCode == 1){
            if (resultCode == RESULT_OK) {

            }
     }

    }
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		SetSummary();
    }

    public void SetSummary()
    {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	Preference prefLanguage = findPreference("prefLanguage");
    	String languageId = prefs.getString("prefLanguage", "1");
    	String language = "English";
    	if(languageId.equals("2"))
    		language = "Français";
    	else if(languageId.equals("4"))
    		language = "Español";
    	prefLanguage.setSummary(language);
    }

	@Override
	public boolean onPreferenceClick(Preference preference) {

		return false;
	}

}