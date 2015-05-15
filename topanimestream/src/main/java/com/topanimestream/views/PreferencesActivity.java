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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

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
        /*
        mPrefItems.add(new PrefItem(this, R.drawable.ic_prefs_app_language, R.string.i18n_language, Prefs.LOCALE, "",
                new PrefItem.OnClickListener() {
                    @Override
                    public void onClick(final PrefItem item) {
                        int currentPosition = 0;
                        String currentValue = item.getValue().toString();

                        final String[] languages = getResources().getStringArray(R.array.languages);
                        Arrays.sort(languages);

                        String[] items = new String[languages.length];
                        for (int i = 0; i < languages.length; i++) {
                            Locale locale = LocaleUtils.toLocale(languages[i]);
                            items[i + 1] = locale.getDisplayName(locale);
                            if (languages[i].equals(currentValue)) {
                                currentPosition = i + 1;
                            }
                        }

                        openListSelectionDialog(item.getTitle(), items, StringArraySelectorDialogFragment.SINGLE_CHOICE, currentPosition,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int position) {
                                        if (position == 0) {
                                            item.clearValue();
                                        } else {
                                            item.saveValue(languages[position - 1]);
                                        }

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
                        if (langCode.isEmpty())
                            return getString(R.string.device_language);

                        Locale locale = LocaleUtils.toLocale(langCode);
                        return locale.getDisplayName(locale);

                    }
                }));

        mPrefItems.add(getResources().getString(R.string.subtitles));

        mPrefItems.add(new PrefItem(this, R.drawable.ic_prefs_subtitle_color, R.string.subtitle_color, Prefs.SUBTITLE_COLOR, Color.WHITE,
                new PrefItem.OnClickListener() {
                    @Override
                    public void onClick(final PrefItem item) {
                        Bundle args = new Bundle();
                        args.putString(NumberPickerDialogFragment.TITLE, item.getTitle());
                        args.putInt(NumberPickerDialogFragment.DEFAULT_VALUE, (int) item.getValue());

                        ColorPickerDialogFragment dialogFragment = new ColorPickerDialogFragment();
                        dialogFragment.setArguments(args);
                        dialogFragment.setOnResultListener(new ColorPickerDialogFragment.ResultListener() {
                            @Override
                            public void onNewValue(int value) {
                                item.saveValue(value);
                            }
                        });
                        dialogFragment.show(getFragmentManager(), "pref_fragment");
                    }
                },
                new PrefItem.SubTitleGenerator() {
                    @Override
                    public String get(PrefItem item) {
                        return String.format("#%06X", 0xFFFFFF & (int) item.getValue());
                    }
                }));
        mPrefItems.add(new PrefItem(this, R.drawable.ic_prefs_subtitle_size, R.string.subtitle_size, Prefs.SUBTITLE_SIZE, 16,
                new PrefItem.OnClickListener() {
                    @Override
                    public void onClick(final PrefItem item) {
                        Bundle args = new Bundle();
                        args.putString(NumberPickerDialogFragment.TITLE, item.getTitle());
                        args.putInt(NumberPickerDialogFragment.MAX_VALUE, 60);
                        args.putInt(NumberPickerDialogFragment.MIN_VALUE, 10);
                        args.putInt(NumberPickerDialogFragment.DEFAULT_VALUE, (int) item.getValue());

                        NumberPickerDialogFragment dialogFragment = new NumberPickerDialogFragment();
                        dialogFragment.setArguments(args);
                        dialogFragment.setOnResultListener(new NumberPickerDialogFragment.ResultListener() {
                            @Override
                            public void onNewValue(int value) {
                                item.saveValue(value);
                            }
                        });
                        dialogFragment.show(getFragmentManager(), "pref_fragment");
                    }
                },
                new PrefItem.SubTitleGenerator() {
                    @Override
                    public String get(PrefItem item) {
                        return Integer.toString((int) item.getValue());
                    }
                }));
        mPrefItems.add(new PrefItem(this, R.drawable.ic_prefs_subtitle_stroke_color, R.string.subtitle_stroke_color, Prefs.SUBTITLE_STROKE_COLOR, Color.BLACK,
                new PrefItem.OnClickListener() {
                    @Override
                    public void onClick(final PrefItem item) {
                        Bundle args = new Bundle();
                        args.putString(NumberPickerDialogFragment.TITLE, item.getTitle());
                        args.putInt(NumberPickerDialogFragment.DEFAULT_VALUE, (int) item.getValue());

                        ColorPickerDialogFragment dialogFragment = new ColorPickerDialogFragment();
                        dialogFragment.setArguments(args);
                        dialogFragment.setOnResultListener(new ColorPickerDialogFragment.ResultListener() {
                            @Override
                            public void onNewValue(int value) {
                                item.saveValue(value);
                            }
                        });
                        dialogFragment.show(getFragmentManager(), "pref_fragment");
                    }
                },
                new PrefItem.SubTitleGenerator() {
                    @Override
                    public String get(PrefItem item) {
                        return String.format("#%06X", 0xFFFFFF & (int) item.getValue());
                    }
                }));
        mPrefItems.add(new PrefItem(this, R.drawable.ic_prefs_subtitle_stroke_width, R.string.subtitle_stroke_width, Prefs.SUBTITLE_STROKE_WIDTH, 2,
                new PrefItem.OnClickListener() {
                    @Override
                    public void onClick(final PrefItem item) {
                        Bundle args = new Bundle();
                        args.putString(NumberPickerDialogFragment.TITLE, item.getTitle());
                        args.putInt(NumberPickerDialogFragment.MAX_VALUE, 5);
                        args.putInt(NumberPickerDialogFragment.MIN_VALUE, 0);
                        args.putInt(NumberPickerDialogFragment.DEFAULT_VALUE, (int) item.getValue());

                        NumberPickerDialogFragment dialogFragment = new NumberPickerDialogFragment();
                        dialogFragment.setArguments(args);
                        dialogFragment.setOnResultListener(new NumberPickerDialogFragment.ResultListener() {
                            @Override
                            public void onNewValue(int value) {
                                item.saveValue(value);
                            }
                        });
                        dialogFragment.show(getFragmentManager(), "pref_fragment");
                    }
                },
                new PrefItem.SubTitleGenerator() {
                    @Override
                    public String get(PrefItem item) {
                        return Integer.toString((int) item.getValue());
                    }
                }));
        mPrefItems.add(new PrefItem(this, R.drawable.ic_prefs_subtitle_lang, R.string.subtitle_language, Prefs.SUBTITLE_DEFAULT, "",
                new PrefItem.OnClickListener() {
                    @Override
                    public void onClick(final PrefItem item) {
                        int currentPosition = 0;
                        String currentValue = item.getValue().toString();

                        final String[] languages = getResources().getStringArray(R.array.subtitle_languages);
                        String[] items = new String[languages.length + 1];
                        items[0] = getString(R.string.no_default_set);
                        for (int i = 0; i < languages.length; i++) {
                            Locale locale = LocaleUtils.toLocale(languages[i]);
                            items[i + 1] = locale.getDisplayName(locale);
                            if (languages[i].equals(currentValue)) {
                                currentPosition = i + 1;
                            }
                        }

                        openListSelectionDialog(item.getTitle(), items, StringArraySelectorDialogFragment.SINGLE_CHOICE, currentPosition,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int position) {
                                        if (position == 0) {
                                            item.clearValue();
                                        } else {
                                            item.saveValue(languages[position - 1]);
                                        }
                                        dialog.dismiss();
                                    }
                                });
                    }
                },
                new PrefItem.SubTitleGenerator() {
                    @Override
                    public String get(PrefItem item) {
                        String langCode = item.getValue().toString();
                        if (langCode.isEmpty())
                            return getString(R.string.no_default_set);

                        Locale locale = LocaleUtils.toLocale(langCode);
                        return locale.getDisplayName(locale);
                    }
                }));
        mPrefItems.add(getResources().getString(R.string.advanced));
        mPrefItems.add(new PrefItem(this, R.drawable.ic_prefs_remove_cache, R.string.remove_cache, Prefs.REMOVE_CACHE, true,
                new PrefItem.OnClickListener() {
                    @Override
                    public void onClick(PrefItem item) {
                        PrefUtils.save(PreferencesActivity.this, Prefs.REMOVE_CACHE, !(boolean) item.getValue());
                    }
                },
                new PrefItem.SubTitleGenerator() {
                    @Override
                    public String get(PrefItem item) {
                        boolean enabled = (boolean) item.getValue();
                        return enabled ? getString(R.string.enabled) : getString(R.string.disabled);
                    }
                }));



        mPrefItems.add(getResources().getString(R.string.updates));
        mPrefItems.add(new PrefItem(this, R.drawable.ic_prefs_auto_update, R.string.auto_updates, Prefs.AUTOMATIC_UPDATES, true,
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
        mPrefItems.add(new PrefItem(this, R.drawable.ic_prefs_check_update, R.string.check_for_updates, PopcornUpdater.LAST_UPDATE_CHECK, 1,
                new PrefItem.OnClickListener() {
                    @Override
                    public void onClick(PrefItem item) {
                        PopcornUpdater.getInstance(PreferencesActivity.this).checkUpdatesManually();
                    }
                },
                new PrefItem.SubTitleGenerator() {
                    @Override
                    public String get(PrefItem item) {
                        long timeStamp = Long.parseLong(PrefUtils.get(PreferencesActivity.this, PopcornUpdater.LAST_UPDATE_CHECK, "0"));
                        Calendar cal = Calendar.getInstance(Locale.getDefault());
                        cal.setTimeInMillis(timeStamp);
                        String time = SimpleDateFormat.getTimeInstance(SimpleDateFormat.MEDIUM, Locale.getDefault()).format(timeStamp);
                        String date = DateFormat.format("dd-MM-yyy", cal).toString();
                        return getString(R.string.last_check) + ": " + date + " " + time;
                    }
                }));*/

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