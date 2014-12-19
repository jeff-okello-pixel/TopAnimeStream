package org.topanimestream;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import org.topanimestream.managers.AnimationManager;
import org.topanimestream.managers.VersionManager;
import org.topanimestream.R;

public class Settings extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener
{
	private App app;
	private boolean mStopOnExit;
    private AlertDialog dialog;
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
        PreferenceCategory miscCategory = (PreferenceCategory) findPreference("prefCategoryMisc");
        Preference prefAutoCheckUpdates = (Preference)findPreference("prefAutoCheckUpdates");
        Preference prefManuallyCheckUpdates = (Preference)findPreference("prefManuallyCheckUpdates");
        prefManuallyCheckUpdates.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference pref) {
                VersionManager.checkUpdate(Settings.this, true);
                return true;
            }
        });

        if(App.isGooglePlayVersion)
        {
            miscCategory.removePreference(prefManuallyCheckUpdates);
            miscCategory.removePreference(prefAutoCheckUpdates);
        }
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
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Settings.this);
        if(key.equals("prefLanguage") && !App.isGooglePlayVersion) {
            app.setLocale();
            App.languageChanged = true;
            Utils.restartActivity(this);
        }
        else if(key.equals("prefLanguage") && App.isGooglePlayVersion && !prefs.getString("prefLanguage", "0").equals("4"))
        {


            AlertDialog.Builder builder = new AlertDialog.Builder(Settings.this);

            builder.setTitle(getString(R.string.not_available));
            builder.setMessage(getString(R.string.download_full_version));
            builder.setPositiveButton(getString(R.string.goto_topanimestream), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    String language = Utils.ToLanguageString(App.phoneLanguage);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(getString(R.string.topanimestream_website) + language + "/" + "android/"));
                    startActivity(intent);
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialog.cancel();
                }
            });
            try {
                dialog = builder.show();
            }catch(Exception e)
            {
                Toast.makeText(Settings.this,getString(R.string.download_full_version), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
            prefs.edit().putString("prefLanguage", "4").commit();

        }
		SetSummary();
    }

    public void SetSummary()
    {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	Preference prefLanguage = findPreference("prefLanguage");
    	String languageId = prefs.getString("prefLanguage", "1");
    	String language = getString(R.string.language_english);
    	if(languageId.equals("2"))
    		language = getString(R.string.language_french);
    	else if(languageId.equals("4"))
    		language = getString(R.string.language_spanish);
    	prefLanguage.setSummary(language);

        Preference prefPlayInternal = findPreference("prefPlayInternal");
        String player = prefs.getString("prefPlayInternal", "undefined");
        String[] summaries = getResources().getStringArray(R.array.players);
        String summary;
        if(player.equals("true"))
            summary = summaries[0];
        else if(player.equals("false"))
            summary = summaries[1];
        else
            summary = summaries[2];

        prefPlayInternal.setSummary(summary);

    }

	@Override
	public boolean onPreferenceClick(Preference preference) {

        return false;
	}

    @Override
    protected void onPause()
    {
        super.onPause();
        AnimationManager.ActivityFinish(this);
    }

}