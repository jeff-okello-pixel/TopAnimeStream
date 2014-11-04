package com.aniblitz;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AppRater {
    private final static String APP_TITLE = "Aniblitz";
    private final static String APP_PNAME = "com.aniblitz";
    static AlertDialog alertRate;
    private final static int DAYS_UNTIL_PROMPT = 3;
    private final static int LAUNCHES_UNTIL_PROMPT = 3;

    public static void app_launched(Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences("apprater", 0);
        if (prefs.getBoolean("dontshowagain", false)) { return ; }

        SharedPreferences.Editor editor = prefs.edit();

        // Increment launch counter
        long launch_count = prefs.getLong("launch_count", 0) + 1;
        editor.putLong("launch_count", launch_count);

        // Wait at least n days before opening
        if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
            showRateDialog(mContext, editor);
        }

        editor.commit();
    }

    public static void showRateDialog(final Context mContext, final SharedPreferences.Editor editor) {

        CharSequence[] items = new CharSequence[]{ mContext.getString(R.string.rate_aniblitz), mContext.getString(R.string.remind_me_later), mContext.getString(R.string.no_thanks)};

        final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mContext);
        alertBuilder.setTitle(mContext.getString(R.string.rate_aniblitz));
        alertBuilder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                switch(item)
                {
                    case 0:
                        mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_PNAME)));
                        if (editor != null) {
                            editor.putBoolean("dontshowagain", true);
                            editor.commit();
                        }
                        alertRate.dismiss();
                        break;
                    case 1:
                        alertRate.dismiss();
                        break;
                    case 2:
                        if (editor != null) {
                            editor.putBoolean("dontshowagain", true);
                            editor.commit();
                        }
                        break;
                }
            }
        });

        alertRate = alertBuilder.create();
        try {
            //leaked error
            alertRate.show();
        }catch(Exception e){}
    }
}