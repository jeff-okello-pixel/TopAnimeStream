package com.aniblitz.managers;
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
import android.widget.TextView;

import com.aniblitz.App;
import com.aniblitz.R;
import com.aniblitz.models.*;

/**
 * Created by marcandre.therrien on 2014-10-30.
 */
public class DialogManager {
    public interface NetworkErrorDialogEvent
    {
        public void onNetworkDialogTryAgain();

        public void onNetworkDialogCancelled();

    }

    public interface GenericDialogEvent
    {
        public void onGenericDialogOk();

    }

    public interface GenericTryAgainDialogEvent
    {
        public void onGenericDialogTryAgain();

        public void onGenericDialogCancelled();

    }

    public interface GenericTwoButtonDialogEvent
    {
        public void onGenericDialogFirstButton();

        public void onGenericDialogSecondButton();

    }
    public static void ShowNoServiceDialog(Context context)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.title_service_unavailable));
        //builder.setIcon(R.drawable.icon);
        builder.setMessage(context.getString(R.string.service_unavailable));
        builder.setPositiveButton(context.getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();

                    }
                });

        ShowDialog(builder);
    }
    public static void ShowChromecastConnectionErrorDialog(Context context)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.title_connect_chromecast))
                .setMessage(context.getString(R.string.message_connect_chromecast))
                .setCancelable(false)
                .setIcon(R.drawable.mr_ic_media_route_off_holo_light)
                .setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        ShowDialog(builder);
    }
    public static void ShowNetworkErrorDialog(final Context context){
        if(!(context instanceof NetworkErrorDialogEvent))
            throw new ClassCastException("Activity must implement NetworkDialogEvent.");

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.error));
        //builder.setIcon(R.drawable.icon);
        builder.setMessage(context.getString(R.string.no_connection));
        builder.setPositiveButton(context.getString(R.string.try_again),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        ((NetworkErrorDialogEvent)context).onNetworkDialogTryAgain();
                    }
                });

        builder.setNeutralButton(context.getString(R.string.network_settings),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        context.startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                    }
                });

        builder.setNegativeButton(context.getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        ((NetworkErrorDialogEvent)context).onNetworkDialogCancelled();
                    }
                });

        ShowDialog(builder);

    }
    private static void ShowDialog(AlertDialog.Builder builder)
    {
        try
        {
            builder.show();
        }catch(Exception e)//leaked error
        {
            e.printStackTrace();
        }
    }

    public static void ShowUpdateDialog(final Context context, final com.aniblitz.models.Package pkg){

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.title_update) + " " + context.getString(R.string.app_name) + " " + String.valueOf(pkg.getVersion()));
        //builder.setIcon(R.drawable.icon);
        builder.setMessage(context.getString(R.string.new_version_available) + "\n\n" + context.getString(R.string.changes) + "\n" + pkg.getChanges());
        builder.setPositiveButton(context.getString(R.string.update_now),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(pkg.getApkUrl()));
                        request.setDescription(context.getString(R.string.new_version_downloading));
                        request.setTitle(context.getString(R.string.title_aniblitz_update));

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                            request.allowScanningByMediaScanner();
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        }
                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, context.getString(R.string.app_name) + "-" + String.valueOf(pkg.getVersion()) + ".apk");


                        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                        manager.enqueue(request);
                        dialog.dismiss();
                        prefs.edit().putBoolean("ShowUpdate", false);

                    }
                }
        );
        builder.setNeutralButton(context.getString(R.string.remind_me_later),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        prefs.edit().putBoolean("ShowUpdate", true);
                    }
                });

        builder.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        prefs.edit().putBoolean("ShowUpdate", false).commit();
                    }
                });

        ShowDialog(builder);

    }
    /*not really usable since you can't have 2 of them in the same activity...*/
    public static void ShowGenericTwoButtonDialog(final Context context, String title, String message, String firstButtonTitle, String secondButtonTitle){
        if(!(context instanceof NetworkErrorDialogEvent))
            throw new ClassCastException("Activity must implement GenericTwoButtonDialogEvent.");

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        //builder.setIcon(R.drawable.icon);
        builder.setMessage(message);
        builder.setPositiveButton(firstButtonTitle,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ((GenericTwoButtonDialogEvent)context).onGenericDialogFirstButton();

                    }
                });

        builder.setNegativeButton(secondButtonTitle,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ((GenericTwoButtonDialogEvent)context).onGenericDialogSecondButton();
                    }
                });

        ShowDialog(builder);

    }
    public static void ShowChromecastNotProErrorDialog(final Context context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.pro_version_needed));
        //builder.setIcon(R.drawable.icon);
        builder.setMessage(context.getString(R.string.need_pro_chromecast));
        builder.setPositiveButton(context.getString(R.string.action_buypro),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.aniblitz.pro")));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=com.aniblitz.pro")));
                        }
                    }
                });
        builder.setNegativeButton(context.getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        ShowDialog(builder);
    }
    public static void ShowUpgradedToProDialog(final Context context){

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.title_aniblitz_upgraded));
        //builder.setIcon(R.drawable.icon);
        builder.setMessage(context.getString(R.string.aniblitz_upgraded_message));
        builder.setPositiveButton(context.getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        ShowDialog(builder);

    }
    public static void ShowBuyProDialog(final Context context){

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.title_buy_aniblitz));
        //builder.setIcon(R.drawable.icon);
        if(App.isGooglePlayVersion && !App.isPro)
        {
            builder.setMessage(context.getString(R.string.aniblitz_has_2_pro));
            builder.setPositiveButton(context.getString(R.string.all_languages),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //TODO redirect to promo/buy app
                        }
                    });
            builder.setNeutralButton(context.getString(R.string.spanish_only),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            try {
                                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.aniblitz.pro")));
                            } catch (android.content.ActivityNotFoundException anfe) {
                                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=com.aniblitz.pro")));
                            }
                        }
                    });
            builder.setNegativeButton(context.getString(R.string.cancel),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
        }
        else
        {
            builder.setMessage(context.getString(R.string.bored_animes_spanish));
            builder.setPositiveButton(context.getString(R.string.buy_full_version),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //TODO redirect to promo/buy app
                        }
                    });
            builder.setNegativeButton(context.getString(R.string.cancel),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
        }

        ShowDialog(builder);

    }
    public static void ShowWelcomeDialog(final Context context){
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.welcome_aniblitz));
        //builder.setIcon(R.drawable.icon);
        builder.setMessage(context.getString(R.string.welcome_dialog_message));
        builder.setPositiveButton(context.getString(R.string.get_full_version),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //TODO go to download/register/promo page

                        prefs.edit().putBoolean("ShowWelcomeDialog", false).apply();
                    }
                });

        builder.setNegativeButton(context.getString(R.string.no_thanks),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        prefs.edit().putBoolean("ShowWelcomeDialog", false).apply();
                    }
                });

        ShowDialog(builder);

    }


    public static void ShowGenericErrorDialog(final Context context, String errorMessage){
        if(!(context instanceof GenericDialogEvent))
            throw new ClassCastException("Activity must implement GenericDialogEvent.");

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.error));
        //builder.setIcon(R.drawable.icon);
        builder.setMessage(errorMessage);
        builder.setPositiveButton(context.getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        ((GenericDialogEvent)context).onGenericDialogOk();
                    }
                });

        ShowDialog(builder);
    }

    public static void ShowGenericTryAgainErrorDialog(final Context context, String errorMessage){
        if(!(context instanceof GenericTryAgainDialogEvent))
            throw new ClassCastException("Activity must implement GenericTryAgainDialogEvent.");

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.error));
        //builder.setIcon(R.drawable.icon);
        builder.setMessage(errorMessage);
        builder.setPositiveButton(context.getString(R.string.try_again),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        ((GenericTryAgainDialogEvent)context).onGenericDialogTryAgain();
                    }
                });
        builder.setNegativeButton(context.getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        ((GenericTryAgainDialogEvent)context).onGenericDialogCancelled();
                    }
                });

        ShowDialog(builder);


    }

    public static Dialog showBusyDialog(String message, Context context) {
        Dialog busyDialog = new Dialog(context, R.style.lightbox_dialog);
        busyDialog.setContentView(R.layout.lightbox_dialog);
        ((TextView)busyDialog.findViewById(R.id.dialogText)).setText(message);
        try{
            busyDialog.show();
        }catch(Exception e){}//leaked error
        return busyDialog;
    }

    public static void dismissBusyDialog(Dialog busyDialog) {
        if (busyDialog != null)
            busyDialog.dismiss();

        busyDialog = null;
    }
}