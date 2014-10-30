package com.aniblitz.managers;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.TextView;

import com.aniblitz.R;

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
    public static void ShowChromecastConnectionErrorDialog(Context context)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.title_connect_chromecast))
                .setMessage(context.getString(R.string.message_connect_chromecast))
                .setCancelable(false)
                .setIcon(R.drawable.mr_ic_media_route_off_holo_light)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        builder.show();
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

        builder.show();

    }
    public static void ShowUpdateDialog(final Context context){
        if(!(context instanceof NetworkErrorDialogEvent))
            throw new ClassCastException("Activity must implement GenericTwoButtonDialogEvent.");

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.title_update);
        //builder.setIcon(R.drawable.icon);
        builder.setMessage(R.string.new_version_available);
        builder.setPositiveButton(R.string.update_now,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //TODO download .apk

                    }
                });

        builder.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        builder.show();

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
        builder.show();

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
        builder.show();

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

        builder.show();


    }

    public static Dialog showBusyDialog(String message, Activity act) {
        Dialog busyDialog = new Dialog(act, R.style.lightbox_dialog);
        busyDialog.setContentView(R.layout.lightbox_dialog);
        ((TextView)busyDialog.findViewById(R.id.dialogText)).setText(message);

        busyDialog.show();
        return busyDialog;
    }

    public static void dismissBusyDialog(Dialog busyDialog) {
        if (busyDialog != null)
            busyDialog.dismiss();

        busyDialog = null;
    }
}