package com.topanimestream.views;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.topanimestream.App;
import com.topanimestream.R;
import com.topanimestream.adapters.PlayerEpisodesAdapter;
import com.topanimestream.models.Episode;
import com.topanimestream.models.Language;
import com.topanimestream.models.Source;
import com.topanimestream.models.Subtitle;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Locale;

public class VideoControllerView extends FrameLayout implements View.OnTouchListener {
    private static final String TAG = "VideoControllerView";
    
    private VideoControllerCallback mCallback;
    private Context             mContext;
    private RelativeLayout mAnchor;
    private View                mRoot;
    private SeekBar             mProgress;
    private TextView            mEndTime, mCurrentTime;
    private boolean             mShowing;
    private boolean             mDragging;
    private static final int    sDefaultTimeout = 3000;
    private static final int    FADE_OUT = 1;
    private static final int    SHOW_PROGRESS = 2;
    private boolean             mUseFastForward;
    private boolean             mFromXml;
    private boolean             mListenersSet;
    private OnClickListener mNextListener, mPrevListener;
    StringBuilder               mFormatBuilder;
    Formatter                   mFormatter;
    private ImageButton         mPauseButton;
    private ImageButton         mFfwdButton;
    private ImageButton         mRewButton;
    private ImageButton         mNextButton;
    private ImageButton         mPrevButton;
    private Handler             mHandler = new MessageHandler(this);
    public  Boolean             mCanTouchAgain = true;
    private LinearLayout        layBottom;
    private Toolbar             toolbar;
    private DrawerLayout        mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private boolean             drawerIsOpened;
    private ListView            leftDrawerEpisodes;
    private Float               layBottomAlpha = 1.0f;// used for older devices
    private ArrayList<Episode>  episodes;
    private Episode             currentEpisode;
    private ArrayList<Subtitle> currentVideoSubtitles = new ArrayList<Subtitle>();
    private ArrayList<Source>   currentVideoSources = new ArrayList<Source>();
    private ArrayList<Language> currentVideoLanguages = new ArrayList<Language>();
    private Language            currentSelectedLanguage;
    private MenuItem            menuSubtitles;
    private MenuItem            menuLanguage;
    private MenuItem            menuSettings;

    public VideoControllerView(Context context, boolean useFastForward, ArrayList<Episode> episodes, Episode currentEpisode) {
        super(context);
        mContext = context;
        mUseFastForward = useFastForward;
        this.episodes = episodes;
        this.currentEpisode = currentEpisode;
        this.currentSelectedLanguage = new Language(3, "Japanese", "ja");
        Log.i(TAG, TAG);
    }

    @Override
    public void onFinishInflate() {
        if (mRoot != null)
            initControllerView(mRoot);
    }
    
    public void setMediaPlayer(VideoControllerCallback player) {
        mCallback = player;
        updatePausePlay();
    }

    /**
     * Set the view that acts as the anchor for the control view.
     * This can for example be a VideoView, or your Activity's main view.
     * @param view The view to which to anchor the controller when it is visible.
     */
    public void setAnchorView(RelativeLayout view) {
        mAnchor = view;

        LayoutParams frameParams = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );

        removeAllViews();
        View v = makeControllerView();
        addView(v, frameParams);
    }

    /**
     * Create the view that holds the widgets that control playback.
     * Derived classes can override this to create their own.
     * @return The controller view.
     * @hide This doesn't work as advertised
     */
    protected View makeControllerView() {
        if(mRoot == null) {
            LayoutInflater inflate = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mRoot = inflate.inflate(R.layout.media_controller, null);

            initControllerView(mRoot);
        }

        return mRoot;
    }
    private void initControllerView(View v) {
        layBottom = (LinearLayout) v.findViewById(R.id.layBottom);
        if(layBottom != null)
            layBottom.setOnTouchListener(this);


        toolbar = (Toolbar) v.findViewById(R.id.toolbar);
        if(toolbar != null) {
            toolbar.inflateMenu(R.menu.media_controller);
            menuSubtitles = toolbar.getMenu().findItem(R.id.menuSubtitles);
            menuLanguage = toolbar.getMenu().findItem(R.id.menuLanguage);
            menuSettings = toolbar.getMenu().findItem(R.id.menuSettings);

            mDrawerLayout = (DrawerLayout) v.findViewById(R.id.drawer_layout);
            leftDrawerEpisodes = (ListView) v.findViewById(R.id.leftDrawerEpisodes);

            if (mDrawerLayout != null) {
                if(episodes != null && episodes.size() > 0) {
                    if(leftDrawerEpisodes != null) {
                        leftDrawerEpisodes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                Episode selectedEpisode = episodes.get(position);
                                if(currentEpisode != null && currentEpisode.getEpisodeId() != selectedEpisode.getEpisodeId()) {
                                    currentEpisode = selectedEpisode;
                                    currentVideoSubtitles = null;
                                    currentVideoSources = null;
                                    //Hide all toolbar menuitem until the new selected episode loads.
                                    menuSubtitles.setVisible(false);
                                    menuLanguage.setVisible(false);
                                    menuSettings.setVisible(false);
                                    mCallback.EpisodeSelected(selectedEpisode);
                                }

                                mDrawerLayout.closeDrawers();
                            }
                        });

                        PlayerEpisodesAdapter adapter = new PlayerEpisodesAdapter(mContext, episodes);
                        leftDrawerEpisodes.setAdapter(adapter);
                        for(int i = 0; i < adapter.getCount(); i++)
                        {
                            if(adapter.getItem(i).getEpisodeId() == currentEpisode.getEpisodeId())
                            {
                                leftDrawerEpisodes.setItemChecked(i , true);
                                leftDrawerEpisodes.setSelection(adapter.getItemPosition(adapter.getItem(i)));
                                break;
                            }
                        }

                    }
                    mDrawerLayout.setDrawerListener(new DrawerListener());
                    mDrawerLayout.setScrimColor(getResources().getColor(android.R.color.transparent));
                    //mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

                    mDrawerToggle = new ActionBarDrawerToggle(
                            (Activity) mContext, mDrawerLayout, toolbar, 0, 0);

                    mDrawerToggle.syncState();
                }
                else
                {
                    leftDrawerEpisodes.setVisibility(View.GONE);
                }
            }

            toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    switch(menuItem.getItemId())
                    {
                        case R.id.menuSubtitles:
                            ShowSubtitleMenu();
                            break;
                        case R.id.menuLanguage:
                            ShowLanguageMenu();
                            break;
                        case R.id.menuSettings:
                            ShowQualityMenu();
                            break;

                    }
                    show(sDefaultTimeout);
                    return true;
                }
            });
            toolbar.setOnTouchListener(this);
        }

        mPauseButton = (ImageButton) v.findViewById(R.id.pause);
        if (mPauseButton != null) {
            mPauseButton.requestFocus();
            mPauseButton.setOnClickListener(mPauseListener);
        }

        mFfwdButton = (ImageButton) v.findViewById(R.id.ffwd);
        if (mFfwdButton != null) {
            mFfwdButton.setOnClickListener(mFfwdListener);
            if (!mFromXml) {
                mFfwdButton.setVisibility(mUseFastForward ? View.VISIBLE : View.GONE);
            }
        }

        mRewButton = (ImageButton) v.findViewById(R.id.rew);
        if (mRewButton != null) {
            mRewButton.setOnClickListener(mRewListener);
            if (!mFromXml) {
                mRewButton.setVisibility(mUseFastForward ? View.VISIBLE : View.GONE);
            }
        }

        // By default these are hidden. They will be enabled when setPrevNextListeners() is called 
        mNextButton = (ImageButton) v.findViewById(R.id.next);
        if (mNextButton != null && !mFromXml && !mListenersSet) {
            mNextButton.setVisibility(View.GONE);
        }
        mPrevButton = (ImageButton) v.findViewById(R.id.prev);
        if (mPrevButton != null && !mFromXml && !mListenersSet) {
            mPrevButton.setVisibility(View.GONE);
        }

        mProgress = (SeekBar) v.findViewById(R.id.mediacontroller_progress);
        if (mProgress != null) {
            if (mProgress instanceof SeekBar) {
                SeekBar seeker = (SeekBar) mProgress;
                seeker.setOnSeekBarChangeListener(mSeekListener);
            }
            mProgress.setMax(1000);
        }

        mEndTime = (TextView) v.findViewById(R.id.time);
        mCurrentTime = (TextView) v.findViewById(R.id.time_current);
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());

        installPrevNextListeners();
    }
    //called from the activity
    public void ShowMenuItems(boolean showSubtitles)
    {
        menuSubtitles.setVisible(showSubtitles);
        menuLanguage.setVisible(true);
        menuSettings.setVisible(true);
    }
    public void SetSubtitles(ArrayList<Subtitle> subs)
    {
        currentVideoSubtitles = subs;
    }
    public void SetSources(ArrayList<Source> sources)
    {
        currentVideoSources = sources;

        for(Source source:currentVideoSources)
        {
            boolean containsLanguage = false;
            for(Language lang:currentVideoLanguages)
            {
                if(source.getLink().getLanguageId() == lang.getLanguageId())
                {
                    containsLanguage = true;
                }
            }

            if(!containsLanguage)
                currentVideoLanguages.add(source.getLink().getLanguage());
        }

    }
    private void ShowLanguageMenu()
    {


        Language[] languageArray = new Language[currentVideoLanguages.size()];
        languageArray = currentVideoLanguages.toArray(languageArray);
        final Language[] finalLanguageArray = languageArray;
        ListAdapter adapter = new ArrayAdapter<Language>(
                mContext,
                android.R.layout.select_dialog_singlechoice,
                android.R.id.text1,
                languageArray){
            public View getView(int position, View convertView, ViewGroup parent) {
                //User super class to create the View
                View v = super.getView(position, convertView, parent);
                TextView tv = (TextView)v.findViewById(android.R.id.text1);

                Language language = finalLanguageArray[position];
                tv.setText(language.getName());
                return v;
            }
        };
        String currentLanguage = mCallback.GetCurrentLanguageId();
        int currentPosition = 0;
        for(int i = 0; i < currentVideoLanguages.size(); i++)
        {
            if(currentVideoLanguages.get(i).getLanguageId() == Integer.valueOf(currentLanguage))
            {
                currentPosition = i;
            }
        }
        new AlertDialog.Builder(mContext)
                .setTitle(mContext.getString(R.string.choose_option))
                .setSingleChoiceItems(adapter, currentPosition, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int position) {
                        Language selectedLanguage = finalLanguageArray[position];
                        currentSelectedLanguage = selectedLanguage;
                        mCallback.LanguageSelected(selectedLanguage);
                        dialog.dismiss();
                    }
                }).show();
    }
    private void ShowQualityMenu()
    {
        ArrayList<Source> sourcesOfCurrentLanguage = new ArrayList<Source>();
        for(Source source:currentVideoSources)
        {
            if(source.getLink().getLanguageId() == currentSelectedLanguage.getLanguageId())
            {
                sourcesOfCurrentLanguage.add(source);
            }
        }
        Source[] sourceArray = new Source[sourcesOfCurrentLanguage.size()];
        final Source[] finalSourceArray = sourcesOfCurrentLanguage.toArray(sourceArray);
        ListAdapter adapter = new ArrayAdapter<Source>(
                mContext,
                android.R.layout.select_dialog_singlechoice,
                android.R.id.text1,
                finalSourceArray){
            public View getView(int position, View convertView, ViewGroup parent) {
                //User super class to create the View
                View v = super.getView(position, convertView, parent);
                TextView tv = (TextView)v.findViewById(android.R.id.text1);

                Source source = finalSourceArray[position];
                tv.setText(source.getQuality());
                return v;
            }
        };
        int currentPosition = 0;
        for(int i = 0; i < sourcesOfCurrentLanguage.size(); i++)
        {
            if(mCallback.GetCurrentQuality().equals(sourcesOfCurrentLanguage.get(i).getQuality()))
            {
                currentPosition = i;
            }
        }

        new AlertDialog.Builder(mContext)
                .setTitle(mContext.getString(R.string.choose_option))
                .setSingleChoiceItems(adapter, currentPosition, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int position) {
                        Source selectedSource = finalSourceArray[position];
                        mCallback.QualitySelected(selectedSource.getQuality());
                        dialog.dismiss();
                    }
                }).show();
    }
    private void ShowSubtitleMenu()
    {
        if(currentVideoSubtitles.size() > 0) {
            //add None option
            ArrayList<Subtitle> tempSubList = currentVideoSubtitles;
            if (tempSubList.get(0).getSubtitleId() != 0)
                tempSubList.add(0, new Subtitle());
            Subtitle[] subtitleArray = new Subtitle[currentVideoSubtitles.size()];
            final Subtitle[] finalSubtitleArray = currentVideoSubtitles.toArray(subtitleArray);
            ListAdapter adapter = new ArrayAdapter<Subtitle>(
                    mContext,
                    android.R.layout.select_dialog_singlechoice,
                    android.R.id.text1,
                    finalSubtitleArray) {
                public View getView(int position, View convertView, ViewGroup parent) {
                    //User super class to create the View
                    View v = super.getView(position, convertView, parent);
                    TextView tv = (TextView) v.findViewById(android.R.id.text1);
                    if (position == 0) {

                        //Put the image on the TextView
                        tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.flag_none, 0, 0, 0);

                        //Add margin between image and text (support various screen densities)
                        int dp5 = (int) (5 * getResources().getDisplayMetrics().density + 0.5f);
                        tv.setCompoundDrawablePadding(dp5);

                        tv.setText(mContext.getString(R.string.none));
                    } else {
                        Subtitle sub = finalSubtitleArray[position];
                        //Put the image on the TextView
                        tv.setCompoundDrawablesWithIntrinsicBounds(sub.getLanguage().getFlagDrawable(), 0, 0, 0);

                        //Add margin between image and text (support various screen densities)
                        int dp5 = (int) (5 * getResources().getDisplayMetrics().density + 0.5f);
                        tv.setCompoundDrawablePadding(dp5);

                        tv.setText(sub.getLanguage().getName() + (!sub.getSpecification().equals("") ? "(" + sub.getSpecification() + ")" : ""));
                    }
                    return v;
                }
            };

            new AlertDialog.Builder(mContext)
                    .setTitle(mContext.getString(R.string.choose_option))
                    .setSingleChoiceItems(adapter, mCallback.GetCurrentSubtitlePosition(), new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, int position) {
                            Subtitle selectedSub = finalSubtitleArray[position];
                            if (position != mCallback.GetCurrentSubtitlePosition()) {
                                mCallback.SubtitleSelected(selectedSub);
                            } else {
                                Toast.makeText(mContext, mContext.getString(R.string.already_have) + selectedSub.getLanguage().getName() + selectedSub.getSpecification(), Toast.LENGTH_LONG).show();
                            }
                            dialog.dismiss();
                        }
                    }).show();
        }
    }
    private class DrawerListener implements DrawerLayout.DrawerListener {
        @Override
        public void onDrawerOpened(View drawerView) {
            show(0);
            drawerIsOpened = true;
            mDrawerToggle.onDrawerOpened(drawerView);

        }

        @Override
        public void onDrawerClosed(View drawerView) {
            show(1000);//3000 is too long, user might press on the screen for nothing
            drawerIsOpened = false;
            mDrawerToggle.onDrawerClosed(drawerView);

        }

        @SuppressLint("NewApi")
        @Override
        public void onDrawerSlide(View drawerView, float slideOffset) {
            if(App.sdkVersion < android.os.Build.VERSION_CODES.JELLY_BEAN) {

                AlphaAnimation alpha = new AlphaAnimation(layBottomAlpha, 1 - slideOffset);
                alpha.setDuration(0); // Make animation instant
                alpha.setFillAfter(true); // Tell it to persist after the animation ends
                layBottom.startAnimation(alpha);
                layBottomAlpha = 1 - slideOffset;
            }
            else
            {
                layBottom.setAlpha(1 - slideOffset);
            }

            mDrawerToggle.onDrawerSlide(drawerView, slideOffset);
        }

        @Override
        public void onDrawerStateChanged(int newState) {

            mDrawerToggle.onDrawerStateChanged(newState);

        }
    }
    /**
     * Show the controller on screen. It will go away
     * automatically after 3 seconds of inactivity.
     */
    public void show() {
        show(sDefaultTimeout);
    }

    /**
     * Disable pause or seek buttons if the stream cannot be paused or seeked.
     * This requires the control interface to be a MediaPlayerControlExt
     */
    private void disableUnsupportedButtons() {
        if (mCallback == null) {
            return;
        }
        
        try {
            if (mPauseButton != null && !mCallback.canPause()) {
                mPauseButton.setEnabled(false);
            }
            if (mRewButton != null && !mCallback.canSeekBackward()) {
                mRewButton.setEnabled(false);
            }
            if (mFfwdButton != null && !mCallback.canSeekForward()) {
                mFfwdButton.setEnabled(false);
            }
        } catch (IncompatibleClassChangeError ex) {
            // We were given an old version of the interface, that doesn't have
            // the canPause/canSeekXYZ methods. This is OK, it just means we
            // assume the media can be paused and seeked, and so we don't disable
            // the buttons.
        }
    }
    
    /**
     * Show the controller on screen. It will go away
     * automatically after 'timeout' milliseconds of inactivity.
     * @param timeout The timeout in milliseconds. Use 0 to show
     * the controller until hide() is called.
     */
    public void show(int timeout) {

        if (!mShowing && mAnchor != null) {
            mCanTouchAgain = false;
            setProgress();
            if (mPauseButton != null) {
                mPauseButton.requestFocus();
            }
            disableUnsupportedButtons();
            try {
                mAnchor.addView(this);
            }catch(Exception e)
            {
                return;
            }

            Animation animation = AnimationUtils.loadAnimation(App.getContext(), R.anim.abc_fade_in);
            animation.setStartOffset(0);
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    mCanTouchAgain = true;
                }
            }, animation.getDuration() + 50);
            this.startAnimation(animation);
            mShowing = true;
        }
        updatePausePlay();
        
        // cause the progress bar to be updated even if mShowing
        // was already true.  This happens, for example, if we're
        // paused with the progress bar showing the user hits play.
        mHandler.sendEmptyMessage(SHOW_PROGRESS);

        Message msg = mHandler.obtainMessage(FADE_OUT);
        if (timeout != 0) {
            mHandler.removeMessages(FADE_OUT);
            mHandler.sendMessageDelayed(msg, timeout);
        }
        else
            mHandler.removeMessages(FADE_OUT);
    }
    
    public boolean isShowing() {
        return mShowing;
    }

    /**
     * Remove the controller from the screen.
     */
    public void hide() {
        mCanTouchAgain = false;
        if (mAnchor == null) {
            return;
        }

        try {
            Animation animation = AnimationUtils.loadAnimation(App.getContext(), R.anim.abc_fade_out);
            animation.setStartOffset(0);
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    mAnchor.removeView(VideoControllerView.this);
                }
            }, animation.getDuration());
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    mCanTouchAgain = true;
                }
            }, animation.getDuration() + 50);
            this.startAnimation(animation);
            mHandler.removeMessages(SHOW_PROGRESS);
        } catch (IllegalArgumentException ex) {
            Log.w("MediaController", "already removed");
        }
        mShowing = false;
    }

    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours   = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    private int setProgress() {
        if (mCallback == null || mDragging) {
            return 0;
        }
        
        int position = mCallback.getCurrentPosition();
        int duration = mCallback.getDuration();
        if (mProgress != null) {
            if (duration > 0) {
                // use long to avoid overflow
                long pos = 1000L * position / duration;
                mProgress.setProgress( (int) pos);
            }
            int percent = mCallback.getBufferPercentage();
            mProgress.setSecondaryProgress(percent * 10);
        }

        if (mEndTime != null)
            mEndTime.setText(stringForTime(duration));
        if (mCurrentTime != null)
            mCurrentTime.setText(stringForTime(position));

        return position;
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        show(sDefaultTimeout);
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        if (mCallback == null) {
            return true;
        }

        int keyCode = event.getKeyCode();
        final boolean uniqueDown = event.getRepeatCount() == 0
                && event.getAction() == KeyEvent.ACTION_DOWN;
        if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                || keyCode == KeyEvent.KEYCODE_SPACE) {
            if (uniqueDown) {
                doPauseResume();
                show(sDefaultTimeout);
                if (mPauseButton != null) {
                    mPauseButton.requestFocus();
                }
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
            if (uniqueDown && !mCallback.isPlaying()) {
                mCallback.start();
                updatePausePlay();
                show(sDefaultTimeout);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
            if (uniqueDown && mCallback.isPlaying()) {
                mCallback.pause();
                updatePausePlay();
                show(sDefaultTimeout);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
                || keyCode == KeyEvent.KEYCODE_VOLUME_UP
                || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE) {
            // don't show the controls for volume adjustment
            return super.dispatchKeyEvent(event);
        } else if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU) {
            if (uniqueDown) {
                hide();
            }
            return true;
        }

        show(sDefaultTimeout);

        return super.dispatchKeyEvent(event);
    }

    private OnClickListener mPauseListener = new OnClickListener() {
        public void onClick(View v) {
            doPauseResume();
            show(sDefaultTimeout);
        }
    };

    private OnClickListener mFullscreenListener = new OnClickListener() {
        public void onClick(View v) {
            doToggleFullscreen();
            show(sDefaultTimeout);
        }
    };

    public void updatePausePlay() {
        if (mRoot == null || mPauseButton == null || mCallback == null) {
            return;
        }

        if (mCallback.isPlaying()) {
            mPauseButton.setImageResource(R.drawable.ic_media_pause);
        } else {
            mPauseButton.setImageResource(R.drawable.ic_media_play);
        }
    }

    private void doPauseResume() {
        if (mCallback == null) {
            return;
        }
        
        if (mCallback.isPlaying()) {
            mCallback.pause();
        } else {
            mCallback.start();
        }
        updatePausePlay();
    }

    private void doToggleFullscreen() {
        if (mCallback == null) {
            return;
        }
        
        mCallback.toggleFullScreen();
    }

    // There are two scenarios that can trigger the seekbar listener to trigger:
    //
    // The first is the user using the touchpad to adjust the posititon of the
    // seekbar's thumb. In this case onStartTrackingTouch is called followed by
    // a number of onProgressChanged notifications, concluded by onStopTrackingTouch.
    // We're setting the field "mDragging" to true for the duration of the dragging
    // session to avoid jumps in the position in case of ongoing playback.
    //
    // The second scenario involves the user operating the scroll ball, in this
    // case there WON'T BE onStartTrackingTouch/onStopTrackingTouch notifications,
    // we will simply apply the updated position without suspending regular updates.
    private OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {
        public void onStartTrackingTouch(SeekBar bar) {
            if(mCanTouchAgain) {
                show(3600000);

                mDragging = true;

                // By removing these pending progress messages we make sure
                // that a) we won't update the progress while the user adjusts
                // the seekbar and b) once the user is done dragging the thumb
                // we will post one of these messages to the queue again and
                // this ensures that there will be exactly one message queued up.
                mHandler.removeMessages(SHOW_PROGRESS);
            }
        }

        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            if (mCallback == null) {
                return;
            }
            
            if (!fromuser) {
                // We're not interested in programmatically generated changes to
                // the progress bar's position.
                return;
            }

            long duration = mCallback.getDuration();
            long newposition = (duration * progress) / 1000L;
            mCallback.seekTo((int) newposition);
            if (mCurrentTime != null)
                mCurrentTime.setText(stringForTime( (int) newposition));
        }

        public void onStopTrackingTouch(SeekBar bar) {
            mDragging = false;
            setProgress();
            updatePausePlay();
            show(sDefaultTimeout);

            // Ensure that progress is properly updated in the future,
            // the call to show() does not guarantee this because it is a
            // no-op if we are already showing.
            mHandler.sendEmptyMessage(SHOW_PROGRESS);
        }
    };

    @Override
    public void setEnabled(boolean enabled) {
        if (mPauseButton != null) {
            mPauseButton.setEnabled(enabled);
        }
        if (mFfwdButton != null) {
            mFfwdButton.setEnabled(enabled);
        }
        if (mRewButton != null) {
            mRewButton.setEnabled(enabled);
        }
        if (mNextButton != null) {
            mNextButton.setEnabled(enabled && mNextListener != null);
        }
        if (mPrevButton != null) {
            mPrevButton.setEnabled(enabled && mPrevListener != null);
        }
        if (mProgress != null) {
            mProgress.setEnabled(enabled);
        }
        disableUnsupportedButtons();
        super.setEnabled(enabled);
    }

    private OnClickListener mRewListener = new OnClickListener() {
        public void onClick(View v) {
            if (mCallback == null) {
                return;
            }
            
            int pos = mCallback.getCurrentPosition();
            pos -= 5000; // milliseconds
            mCallback.seekTo(pos);
            setProgress();

            show(sDefaultTimeout);
        }
    };

    private OnClickListener mFfwdListener = new OnClickListener() {
        public void onClick(View v) {
            if (mCallback == null) {
                return;
            }
            
            int pos = mCallback.getCurrentPosition();
            pos += 15000; // milliseconds
            mCallback.seekTo(pos);
            setProgress();

            show(sDefaultTimeout);
        }
    };
    private void installPrevNextListeners() {
        if (mNextButton != null) {
            mNextButton.setOnClickListener(mNextListener);
            mNextButton.setEnabled(mNextListener != null);
        }

        if (mPrevButton != null) {
            mPrevButton.setOnClickListener(mPrevListener);
            mPrevButton.setEnabled(mPrevListener != null);
        }
    }

    public void setPrevNextListeners(OnClickListener next, OnClickListener prev) {
        mNextListener = next;
        mPrevListener = prev;
        mListenersSet = true;

        if (mRoot != null) {
            installPrevNextListeners();
            
            if (mNextButton != null && !mFromXml) {
                mNextButton.setVisibility(View.VISIBLE);
            }
            if (mPrevButton != null && !mFromXml) {
                mPrevButton.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if(mCanTouchAgain) {
            if(view.getId() != R.id.drawer_layout && view.getId() != R.id.leftDrawerEpisodes)
            {
                show(sDefaultTimeout);
            }
        }

        return true;
    }

    public interface VideoControllerCallback {
        void    start();
        void    pause();
        int     getDuration();
        int     getCurrentPosition();
        void    seekTo(int pos);
        boolean isPlaying();
        int     getBufferPercentage();
        boolean canPause();
        boolean canSeekBackward();
        boolean canSeekForward();
        boolean isFullScreen();
        void    toggleFullScreen();
        int     GetCurrentSubtitlePosition();
        String  GetCurrentLanguageId();
        String  GetCurrentQuality();
        void    SubtitleSelected(Subtitle subtitle);
        void    EpisodeSelected(Episode episode);
        void    QualitySelected(String quality);
        void    LanguageSelected(Language language);
    }
    
    private static class MessageHandler extends Handler {
        private final WeakReference<VideoControllerView> mView; 

        MessageHandler(VideoControllerView view) {
            mView = new WeakReference<VideoControllerView>(view);
        }
        @Override
        public void handleMessage(Message msg) {
            VideoControllerView view = mView.get();
            if (view == null || view.mCallback == null) {
                return;
            }
            
            int pos;
            switch (msg.what) {
                case FADE_OUT:
                    view.hide();
                    break;
                case SHOW_PROGRESS:
                    pos = view.setProgress();
                    if (!view.mDragging && view.mShowing && view.mCallback.isPlaying()) {
                        msg = obtainMessage(SHOW_PROGRESS);
                        sendMessageDelayed(msg, 1000 - (pos % 1000));
                    }
                    break;
            }
        }
    }
}