package com.topanimestream.views;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.topanimestream.R;
import com.topanimestream.adapters.PreferencesListAdapter;
import com.topanimestream.dialogfragments.ColorPickerDialogFragment;
import com.topanimestream.dialogfragments.NumberPickerDialogFragment;
import com.topanimestream.dialogfragments.StringArraySelectorDialogFragment;
import com.topanimestream.managers.VersionManager;
import com.topanimestream.preferences.PrefItem;
import com.topanimestream.preferences.Prefs;
import com.topanimestream.utilities.PrefUtils;
import com.topanimestream.utilities.ToolbarUtils;
import com.topanimestream.utilities.Utils;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class PreferencesActivity extends AppCompatActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener{

    private List<Object> mPrefItems = new ArrayList<>();
    private LinearLayoutManager mLayoutManager;

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.recyclerView)
    RecyclerView recyclerView;
    @InjectView(R.id.rootLayout)
    ViewGroup rootLayout;

    public static Intent startActivity(Activity activity) {
        Intent intent = new Intent(activity, PreferencesActivity.class);
        activity.startActivity(intent);
        return intent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_Blue);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        ButterKnife.inject(this);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.menu_settings));

        //ToolbarUtils.updateToolbarHeight(this, toolbar);

        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);

        PrefUtils.getPrefs(this).registerOnSharedPreferenceChangeListener(this);

        refreshItems();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        PrefUtils.getPrefs(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    private void refreshItems() {
        mPrefItems = new ArrayList<>();
        mPrefItems.add(getString(R.string.general));

        mPrefItems.add(new PrefItem(this, R.drawable.ic_prefs_language, R.string.app_language, Prefs.LOCALE, "",
                new PrefItem.OnClickListener() {
                    @Override
                    public void onClick(final PrefItem item) {
                        int currentPosition = 0;
                        String currentValue = item.getValue().toString();

                        final String[] languages = getResources().getStringArray(R.array.languages);

                        openListSelectionDialog(item.getTitle(), languages, StringArraySelectorDialogFragment.SINGLE_CHOICE, currentPosition,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int position) {

                                        item.saveValue(Utils.ToLanguageId(languages[position]));

                                        dialog.dismiss();

                                        Utils.restartActivity(PreferencesActivity.this);
                                    }
                                });
                    }
                },
                new PrefItem.SubTitleGenerator() {
                    @Override
                    public String get(PrefItem item) {
                        String langCode = item.getValue().toString();
                        return Utils.ToLanguageStringDisplay(langCode);

                    }
                }));



        mPrefItems.add(getString(R.string.updates));
        mPrefItems.add(new PrefItem(this, R.drawable.ic_prefs_auto_updates, R.string.auto_check_for_updates, Prefs.AUTO_CHECK_UPDATE, true,
                new PrefItem.OnClickListener() {
                    @Override
                    public void onClick(PrefItem item) {
                        item.saveValue(!(boolean) item.getValue());
                    }
                },
                new PrefItem.SubTitleGenerator() {
                    @Override
                    public String get(PrefItem item) {
                        boolean enabled = (boolean) item.getValue();
                        return enabled ? getString(R.string.enabled) : getString(R.string.disabled);
                    }
                }));
        mPrefItems.add(new PrefItem(this, R.drawable.ic_prefs_check_updates, R.string.check_for_updates,Prefs.LAST_CHECK_FOR_UPDATE, 1,
                new PrefItem.OnClickListener() {
                    @Override
                    public void onClick(PrefItem item) {
                        VersionManager.checkUpdate(PreferencesActivity.this, true);
                    }
                },
                new PrefItem.SubTitleGenerator() {
                    @Override
                    public String get(PrefItem item) {
                        long timeStamp = Long.parseLong(PrefUtils.get(PreferencesActivity.this, Prefs.LAST_CHECK_FOR_UPDATE, "0"));
                        Calendar cal = Calendar.getInstance(Locale.getDefault());
                        cal.setTimeInMillis(timeStamp);
                        String time = SimpleDateFormat.getTimeInstance(SimpleDateFormat.MEDIUM, Locale.getDefault()).format(timeStamp);
                        String date = DateFormat.format("dd-MM-yyy", cal).toString();
                        return getString(R.string.last_check) + ": " + date + " " + time;
                    }
                }));

        if (recyclerView.getAdapter() != null && mLayoutManager != null) {
            int position = mLayoutManager.findFirstVisibleItemPosition();
            View v = mLayoutManager.findViewByPosition(position);
            recyclerView.setAdapter(new PreferencesListAdapter(mPrefItems));
            if (v != null) {
                int offset = v.getTop();
                mLayoutManager.scrollToPositionWithOffset(position, offset);
            } else {
                mLayoutManager.scrollToPosition(position);
            }
        } else {
            recyclerView.setAdapter(new PreferencesListAdapter(mPrefItems));
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        refreshItems();
        ToolbarUtils.updateToolbarHeight(this, toolbar);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (isUseChangeablePref(key)) {
            refreshItems();
        }
    }

    private boolean isUseChangeablePref(String key) {
        boolean b = false;
        for (Object item : mPrefItems) {
            if (item instanceof PrefItem) {
                PrefItem pref = (PrefItem) item;
                if (pref.getPrefKey().equals(key))
                    b = true;
            }
        }
        return b;
    }

    private void openListSelectionDialog(String title, String[] items, int mode, int defaultPosition,
                                         DialogInterface.OnClickListener onClickListener) {
        if (mode == StringArraySelectorDialogFragment.NORMAL) {
            StringArraySelectorDialogFragment.show(getSupportFragmentManager(), title, items, defaultPosition, onClickListener);
        } else if (mode == StringArraySelectorDialogFragment.SINGLE_CHOICE) {
            StringArraySelectorDialogFragment.showSingleChoice(getSupportFragmentManager(), title, items, defaultPosition, onClickListener);
        }
    }
}