package com.topanimestream.views;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Toast;

import com.topanimestream.App;
import com.topanimestream.R;
import com.topanimestream.adapters.PreferencesListAdapter;
import com.topanimestream.dialogfragments.ColorPickerDialogFragment;
import com.topanimestream.dialogfragments.NumberPickerDialogFragment;
import com.topanimestream.dialogfragments.StringArraySelectorDialogFragment;
import com.topanimestream.managers.DialogManager;
import com.topanimestream.managers.VersionManager;
import com.topanimestream.preferences.PrefItem;
import com.topanimestream.preferences.Prefs;
import com.topanimestream.utilities.PrefUtils;
import com.topanimestream.utilities.ToolbarUtils;
import com.topanimestream.utilities.Utils;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;

public class PreferencesActivity extends TASBaseActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener{

    private List<Object> mPrefItems = new ArrayList<>();
    private LinearLayoutManager mLayoutManager;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.recyclerView)
    RecyclerView recyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_preferences);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.menu_settings));

        ToolbarUtils.updateToolbarHeight(this, toolbar);

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
        mPrefItems.add(new PrefItem(this, R.drawable.ic_prefs_language, R.string.app_language, Prefs.LOCALE, "",
                new PrefItem.OnClickListener() {
                    @Override
                    public void onClick(final PrefItem item) {
                        int currentPosition = 0;
                        String currentValue = Utils.ToLanguageStringDisplay(String.valueOf(item.getValue()));

                        final String[] languages = getResources().getStringArray(R.array.languages);
                        currentPosition = Arrays.asList(languages).indexOf(currentValue);
                        DialogManager.OpenListSelectionDialog(item.getTitle(), languages, StringArraySelectorDialogFragment.SINGLE_CHOICE, currentPosition, PreferencesActivity.this,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int position) {

                                        item.saveValue(Utils.ToLanguageId(languages[position]));

                                        dialog.dismiss();
                                        App.setLocale();
                                        App.languageChanged = true;
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
                }));*/


        mPrefItems.add(getString(R.string.videos));
        mPrefItems.add(new PrefItem(this, R.drawable.ic_hdtv, R.string.default_quality, Prefs.DEFAULT_VIDEO_QUALITY, "1080p",
                new PrefItem.OnClickListener() {
                    @Override
                    public void onClick(final PrefItem item) {
                        int currentPosition = 0;
                        String currentValue = item.getValue().toString();

                        final String[] qualities = getResources().getStringArray(R.array.qualitiesArray);
                        currentPosition = Arrays.asList(qualities).indexOf(currentValue);
                        DialogManager.OpenListSelectionDialog(item.getTitle(), qualities, StringArraySelectorDialogFragment.SINGLE_CHOICE, currentPosition,PreferencesActivity.this,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int position) {

                                        item.saveValue(qualities[position]);
                                        dialog.dismiss();
                                    }
                                });
                    }
                },
                new PrefItem.SubTitleGenerator() {
                    @Override
                    public String get(PrefItem item) {
                        return item.getValue().toString();

                    }
                }));
        mPrefItems.add(new PrefItem(this, R.drawable.ic_language_black, R.string.default_spoken_language, Prefs.DEFAULT_VIDEO_LANGUAGE, "3",
                new PrefItem.OnClickListener() {
                    @Override
                    public void onClick(final PrefItem item) {
                        int currentPosition = 0;
                        String currentLanguageId = item.getValue().toString();

                        if(currentLanguageId.equals("1"))
                            currentPosition = 0;
                        else if(currentLanguageId.equals("3"))
                            currentPosition = 1;

                        final String[] languages = getResources().getStringArray(R.array.videoLanguagesArray);

                        DialogManager.OpenListSelectionDialog(item.getTitle(), languages, StringArraySelectorDialogFragment.SINGLE_CHOICE, currentPosition,PreferencesActivity.this,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int position) {

                                        item.saveValue(getResources().getStringArray(R.array.videoLanguagesArrayValues)[position]);
                                        Toast.makeText(PreferencesActivity.this,getString(R.string.if_default_language_not_available), Toast.LENGTH_LONG).show();
                                        dialog.dismiss();
                                    }
                                });
                    }
                },
                new PrefItem.SubTitleGenerator() {
                    @Override
                    public String get(PrefItem item) {
                        String langCode = item.getValue().toString();
                        if(langCode.equals("1"))
                            return getString(R.string.language_english);
                        else if(langCode.equals("3"))
                            return getString(R.string.language_japanese);

                        return "";


                    }
                }));
        mPrefItems.add(new PrefItem(this, R.drawable.ic_subtitles_black, R.string.default_subtitle_language, Prefs.DEFAULT_VIDEO_SUBTITLE_LANGUAGE, "1",
                new PrefItem.OnClickListener() {
                    @Override
                    public void onClick(final PrefItem item) {
                        int currentPosition = 0;
                        String currentLanguageId = item.getValue().toString();
                        if(currentLanguageId.equals("0"))
                            currentPosition = 0;
                        else if(currentLanguageId.equals("1"))
                            currentPosition = 1;
                        else if(currentLanguageId.equals("3"))
                            currentPosition = 2;
                        String[] menuLanguages = new String[3];
                        menuLanguages[0] = getString(R.string.none);

                        String[] languages = getResources().getStringArray(R.array.videoLanguagesArray);
                        menuLanguages[1] = languages[0]; //English
                        menuLanguages[2] = languages[1]; //Japanese
                        DialogManager.OpenListSelectionDialog(item.getTitle(), menuLanguages, StringArraySelectorDialogFragment.SINGLE_CHOICE, currentPosition,PreferencesActivity.this,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int position) {
                                        if(position != 0) {
                                            item.saveValue(getResources().getStringArray(R.array.videoLanguagesArrayValues)[position - 1]);
                                            Toast.makeText(PreferencesActivity.this, getString(R.string.if_default_subtitle_not_available), Toast.LENGTH_LONG).show();
                                        }
                                        else
                                            item.saveValue("0");


                                        dialog.dismiss();
                                    }
                                });
                    }
                },
                new PrefItem.SubTitleGenerator() {
                    @Override
                    public String get(PrefItem item) {
                        String langCode = item.getValue().toString();
                        if(langCode.equals("0"))
                            return getString(R.string.none);
                        else if(langCode.equals("1"))
                            return getString(R.string.language_english);
                        else if(langCode.equals("3"))
                            return getString(R.string.language_japanese);

                        return "";


                    }
                }));

        mPrefItems.add(getString(R.string.subtitles_style));
        mPrefItems.add(new PrefItem(this, R.drawable.ic_color, R.string.color, Prefs.SUBTITLE_COLOR, Color.WHITE,
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
        mPrefItems.add(new PrefItem(this, R.drawable.ic_subtitles_size, R.string.size, Prefs.SUBTITLE_SIZE, 16,
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
        mPrefItems.add(new PrefItem(this, R.drawable.ic_subtitle_outline_color, R.string.outline_color, Prefs.SUBTITLE_STROKE_COLOR, Color.BLACK,
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
        mPrefItems.add(new PrefItem(this, R.drawable.ic_subtitle_outline_size, R.string.outline_width, Prefs.SUBTITLE_STROKE_WIDTH, 2,
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


}