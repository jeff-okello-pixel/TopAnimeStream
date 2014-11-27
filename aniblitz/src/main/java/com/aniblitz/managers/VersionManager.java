package com.aniblitz.managers;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;

import com.aniblitz.App;
import com.aniblitz.AsyncTaskTools;
import com.aniblitz.R;
import com.aniblitz.Utils;
import com.aniblitz.managers.DialogManager;
import com.aniblitz.models.*;

import org.json.JSONObject;


public class VersionManager {
    private static Dialog busyDialog;
    //Executed everytime the app is opened
    public static void checkUpdate(Context context, boolean showBusyDialog)
    {
        AsyncTaskTools.execute(new CheckUpdateTask(context, showBusyDialog));
    }

    public static class CheckUpdateTask extends AsyncTask<Void, Void, String> {
        private Context context;
        private boolean showBusyDialog;
        com.aniblitz.models.Package pkg;
        public CheckUpdateTask(Context context, boolean showBusyDialog)
        {
            this.context = context;
            this.showBusyDialog = showBusyDialog;
        }

        @Override
        protected void onPreExecute() {
            if(showBusyDialog)
            {
                busyDialog = DialogManager.showBusyDialog(context.getString(R.string.checking_for_updates), context);
            }

        }


        @Override
        protected String doInBackground(Void... params) {
            if(App.IsNetworkConnected())
            {
                try
                {
                    JSONObject jsonPackage = Utils.GetJson(context.getString(R.string.aniblitz_website) + "/apps/android/package.json");
                    pkg = new com.aniblitz.models.Package(jsonPackage);
                }
                catch(Exception e)
                {
                    return null;
                }

            }
            else
            {
                return null;
            }

            return "Success";
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                if (result == null) {
                    //Failed to get the version
                }
                else
                {
                    int versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
                    if(pkg.getVersion() > versionCode)
                    {
                        DialogManager.ShowUpdateDialog(context, pkg);
                    }
                }

            } catch (Exception e)//catch all exception, handle orientation change
            {
                e.printStackTrace();
            }
            if(showBusyDialog)
            {
                DialogManager.dismissBusyDialog(busyDialog);
            }


        }

    }
}
