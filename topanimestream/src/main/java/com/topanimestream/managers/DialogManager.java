package com.topanimestream.managers;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.topanimestream.App;
import com.topanimestream.dialogfragments.StringArraySelectorDialogFragment;
import com.topanimestream.preferences.Prefs;
import com.topanimestream.utilities.PrefUtils;
import com.topanimestream.utilities.Utils;
import com.topanimestream.R;

public class DialogManager {
    public interface NetworkErrorDialogEvent {
        public void onNetworkDialogTryAgain();

        public void onNetworkDialogCancelled();

    }

    public interface GenericDialogEvent {
        public void onGenericDialogOk();

    }

    public interface GenericTryAgainDialogEvent {
        public void onGenericDialogTryAgain();

        public void onGenericDialogCancelled();

    }

    public interface GenericTwoButtonDialogEvent {
        public void onGenericDialogFirstButton();

        public void onGenericDialogSecondButton();

    }

    public static void ShowNoServiceDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.title_service_unavailable));
        //builder.setIcon(R.drawable.icon);
        builder.setMessage(context.getString(R.string.service_unavailable));
        builder.setPositiveButton(context.getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();

                    }
                }
        );

        ShowDialog(builder);
    }


    private static void ShowDialog(AlertDialog.Builder builder) {
        try {
            builder.show();
        } catch (Exception e)//leaked error
        {
            e.printStackTrace();
        }
    }

    public static void ShowChoosePlayerDialog(final Context context, final String mp4Url, final int mirrorId) {
        View checkBoxView = View.inflate(context, R.layout.dialog_choose_player, null);
        final CheckBox chkAlwaysThis = (CheckBox) checkBoxView.findViewById(R.id.chkAlwaysThis);
        chkAlwaysThis.setText(context.getString(R.string.checkbox_player));

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(checkBoxView);
        builder.setTitle(context.getString(R.string.title_choose_player));
        //builder.setIcon(R.drawable.icon);
        builder.setMessage(context.getString(R.string.choose_player_description));

        builder.setPositiveButton(context.getString(R.string.title_internal_player),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (chkAlwaysThis.isChecked()) {
                            PrefUtils.save(context, Prefs.PLAY_INTERNAL, "true");
                        }
                        Mp4Manager.PlayInternalVideo(context, mp4Url, mirrorId);
                    }
                }
        );

        builder.setNegativeButton(context.getString(R.string.title_external_app),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (chkAlwaysThis.isChecked()) {
                            PrefUtils.save(context, Prefs.PLAY_INTERNAL, "false");
                        }
                        Mp4Manager.PlayExternalVideo(context, mp4Url);
                    }
                }
        );


        ShowDialog(builder);
    }

    public static void ShowUpdateDialog(final Context context, final com.topanimestream.models.Package pkg) {

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.title_update) + " " + context.getString(R.string.app_name) + " " + String.valueOf(pkg.getVersion()));
        //builder.setIcon(R.drawable.icon);
        builder.setMessage(context.getString(R.string.new_version_available) + "\n\n" + context.getString(R.string.changes) + "\n" + pkg.getChanges());
        builder.setPositiveButton(context.getString(R.string.update_now),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        /*
                        Intent intentDownload = new Intent(Intent.ACTION_VIEW);
                        intentDownload.setData(Uri.parse(pkg.getApkUrl()));
                        context.startActivity(intentDownload);*/

                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(pkg.getApkUrl()));
                        request.setDescription(context.getString(R.string.new_version_downloading));
                        request.setTitle(context.getString(R.string.title_topanimestream_update));

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                            request.allowScanningByMediaScanner();
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        }
                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, context.getString(R.string.app_name) + "-" + String.valueOf(pkg.getVersion()) + ".apk");


                        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                        manager.enqueue(request);
                        dialog.dismiss();
                        PrefUtils.save(context, Prefs.SHOW_UPDATE, false);

                    }
                }
        );
        builder.setNeutralButton(context.getString(R.string.remind_me_later),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        PrefUtils.save(context, Prefs.SHOW_UPDATE, true);
                    }
                }
        );

        builder.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        PrefUtils.save(context, Prefs.SHOW_UPDATE, false);
                    }
                }
        );

        ShowDialog(builder);

    }
    public static void OpenListSelectionDialog(String title, String[] items, int mode, int defaultPosition, AppCompatActivity act,
                                         DialogInterface.OnClickListener onClickListener) {
        if (mode == StringArraySelectorDialogFragment.NORMAL) {
            StringArraySelectorDialogFragment.show(act.getSupportFragmentManager(), title, items, defaultPosition, onClickListener);
        } else if (mode == StringArraySelectorDialogFragment.SINGLE_CHOICE) {
            StringArraySelectorDialogFragment.showSingleChoice(act.getSupportFragmentManager(), title, items, defaultPosition, onClickListener);
        }
    }


    public static Dialog showBusyDialog(String message, Context context) {
        Dialog busyDialog = new Dialog(context, R.style.lightbox_dialog);
        busyDialog.setContentView(R.layout.lightbox_dialog);
        ((TextView) busyDialog.findViewById(R.id.dialogText)).setText(message);
        try {
            busyDialog.show();
        } catch (Exception e) {
        }//leaked error
        return busyDialog;
    }

    public static void dismissBusyDialog(Dialog busyDialog) {
        try {
            if (busyDialog != null)
                busyDialog.dismiss();
        } catch (Exception e) {
        }//leaked error
        busyDialog = null;
    }
}