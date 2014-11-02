package com.aniblitz.managers;

import android.content.Context;
import android.os.AsyncTask;

import com.aniblitz.App;
import com.aniblitz.AsyncTaskTools;
import com.aniblitz.managers.DialogManager;

public class VersionManager {
    //Executed everytime the app is opened
    public static void checkUpdate(Context context)
    {
        AsyncTaskTools.execute(new CheckUpdateTask(context));
    }

    public static class CheckUpdateTask extends AsyncTask<Void, Void, String> {
        private Context context;

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
               //TODO get json
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
                    String versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
                    int versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
                    //TODO check if current version < server version then show dialog
                    DialogManager.ShowUpdateDialog(context);
                }

            } catch (Exception e)//catch all exception, handle orientation change
            {
                e.printStackTrace();
            }


        }

    }
}
