package com.topanimestream.managers;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import org.json.JSONObject;

import com.topanimestream.App;
import com.topanimestream.preferences.Prefs;
import com.topanimestream.utilities.AsyncTaskTools;
import com.topanimestream.utilities.PrefUtils;
import com.topanimestream.utilities.Utils;
import com.topanimestream.models.Package;
import com.topanimestream.R;


public class VersionManager {
    private static Dialog busyDialog;

    //Executed everytime the app is opened
    public static void checkUpdate(Context context, boolean showBusyDialog) {
        AsyncTaskTools.execute(new CheckUpdateTask(context, showBusyDialog));
        Long currentTime = System.currentTimeMillis();
        PrefUtils.save(context, Prefs.LAST_CHECK_FOR_UPDATE, currentTime);
    }

    public static class CheckUpdateTask extends AsyncTask<Void, Void, String> {
        private Context context;
        private boolean showBusyDialog;
        Package pkg;

        public CheckUpdateTask(Context context, boolean showBusyDialog) {
            this.context = context;
            this.showBusyDialog = showBusyDialog;
        }

        @Override
        protected void onPreExecute() {
            if (showBusyDialog) {
                busyDialog = DialogManager.showBusyDialog(context.getString(R.string.checking_for_updates), context);
            }

        }


        @Override
        protected String doInBackground(Void... params) {
            if (App.IsNetworkConnected()) {
                try {
                    JSONObject jsonPackage = Utils.GetJson(context.getString(R.string.topanimestream_website) + "/apps/android/package.json");
                    pkg = new Package(jsonPackage);
                } catch (Exception e) {
                    return null;
                }

            } else {
                return null;
            }

            return "Success";
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                if (result == null) {
                    //Failed to get the version
                } else {
                    int versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
                    if(pkg.getVersion() != PrefUtils.get(context, Prefs.LAST_UPDATE_CHECK_VERSION, 0)) {
                        PrefUtils.save(context, Prefs.SHOW_UPDATE, true);
                        PrefUtils.save(context, Prefs.LAST_UPDATE_CHECK_VERSION, pkg.getVersion());
                    }

                    if (pkg.getVersion() > versionCode && PrefUtils.get(context, Prefs.SHOW_UPDATE, false) == true) {
                        DialogManager.ShowUpdateDialog(context, pkg);
                    } else if (showBusyDialog)//means the user requested for it
                    {
                        Toast.makeText(context, context.getString(R.string.latest_version_installed), Toast.LENGTH_LONG).show();
                    }

                }

            } catch (Exception e)//catch all exception, handle orientation change
            {
                e.printStackTrace();
            }
            if (showBusyDialog) {
                DialogManager.dismissBusyDialog(busyDialog);
            }


        }

    }


}
