package com.aniblitz.managers;

import android.content.Context;
import android.os.AsyncTask;

import com.aniblitz.App;
import com.aniblitz.AsyncTaskTools;
import com.aniblitz.R;
import com.aniblitz.Utils;
import com.aniblitz.managers.DialogManager;
import com.aniblitz.models.*;

import org.json.JSONObject;

import io.vov.vitamio.utils.Log;

public class VersionManager {
    //Executed everytime the app is opened
    public static void checkUpdate(Context context)
    {
        AsyncTaskTools.execute(new CheckUpdateTask(context));
    }

    public static class CheckUpdateTask extends AsyncTask<Void, Void, String> {
        private Context context;
        com.aniblitz.models.Package pkg;
        public CheckUpdateTask(Context context)
        {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {


        }


        @Override
        protected String doInBackground(Void... params) {
            if(App.IsNetworkConnected())
            {
                JSONObject jsonPackage = Utils.GetJson(context.getString(R.string.aniblitz_website) + "/apps/android/package.json");
                pkg = new com.aniblitz.models.Package(jsonPackage);
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


        }

    }
}
