package com.topanimestream.utilities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.SearchRecentSuggestions;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.conn.util.InetAddressUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipException;

import com.topanimestream.App;
import com.topanimestream.R;

public class Utils {

    public static boolean isNumeric(String s) {
        return s.matches("[-+]?\\d*\\.?\\d+");
    }


    public static int getJsonCount = 0;

    public static String unGunzip(String str) {
        String s1 = null;

        try {
            byte b[] = str.getBytes();
            InputStream bais = new ByteArrayInputStream(b);
            GZIPInputStream gs = new GZIPInputStream(bais);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int numBytesRead = 0;
            byte[] tempBytes = new byte[6000];
            try {
                while ((numBytesRead = gs.read(tempBytes, 0, tempBytes.length)) != -1) {
                    baos.write(tempBytes, 0, numBytesRead);
                }

                s1 = new String(baos.toByteArray());
                s1 = baos.toString();
            } catch (ZipException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return s1;
    }
    public static String floatToStringWithoutDot(float d) {
        int i = (int) d;
        return d == i ? String.valueOf(i) : String.valueOf(d);
    }
    public static void createLoginDialog(final Activity act) {
        Utils.lockScreen(act);
        final Dialog loginDialog = new Dialog(act);
        loginDialog.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                Utils.unlockScreen(act);
            }
        });
        loginDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        loginDialog.setContentView(R.layout.login_dialog);
        loginDialog.setTitle(act.getResources().getString(R.string.login));

        Button btnLogin = (Button) loginDialog.findViewById(R.id.btnLogin);
        Button btnRegister = (Button) loginDialog.findViewById(R.id.btnRegister);
        final EditText username = (EditText) loginDialog.findViewById(R.id.txtUsername);
        final EditText password = (EditText) loginDialog.findViewById(R.id.txtPassword);
        final TextView lblError = (TextView) loginDialog.findViewById(R.id.lblError);
        // if button is clicked, close the custom dialog
        btnLogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!username.getText().toString().equals("") && !password.getText().toString().equals("")) {
                    lblError.setVisibility(View.GONE);

                } else {
                    lblError.setVisibility(View.VISIBLE);
                    lblError.setText(act.getString(R.string.invalid_username_password));
                }
            }
        });
        btnRegister.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        loginDialog.show();
    }

    public static boolean isProInstalled(Context context) {
        PackageManager manager = context.getPackageManager();
        if (manager.checkSignatures(context.getPackageName(), "com.topanimestream.pro")
                == PackageManager.SIGNATURE_MATCH) {
            //Pro key installed, and signatures match
            return true;
        }
        return false;
    }


    public static String getStringResourceByName(String aString) {
        try {
            Context context = App.getContext();
            String packageName = context.getPackageName();
            int resId = context.getResources().getIdentifier(aString, "string", packageName);
            return context.getString(resId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return aString;
    }

    private static boolean downloadFile(String url, File outputFile) {
        //return true if successful, false otherwise
        try {
            URL u = new URL(url);
            URLConnection conn = u.openConnection();
            int contentLength = conn.getContentLength();
            InputStream inputStream = conn.getInputStream();
            OutputStream output = new FileOutputStream(outputFile);
            byte[] buffer = new byte[8 * 1024]; // Or whatever
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) > 0) {
                output.write(buffer, 0, bytesRead);
            }
            output.flush();
            output.close();
            inputStream.close();

        } catch (Exception e) {
            return false; // swallow a 404
        }
        return true;
    }

    public static boolean isGZipped(InputStream in) {
        if (!in.markSupported()) {
            in = new BufferedInputStream(in);
        }
        in.mark(2);
        int magic = 0;
        try {
            magic = in.read() & 0xff | ((in.read() << 8) & 0xff00);
            in.reset();
        } catch (IOException e) {
            e.printStackTrace(System.err);
            return false;
        }
        return magic == GZIPInputStream.GZIP_MAGIC;
    }

    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress().toUpperCase();
                        boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 port suffix
                                return delim < 0 ? sAddr : sAddr.substring(0, delim);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
        } // for now eat exceptions
        return "";
    }

    public static String formatHHMMSS(long secondsCount) {
        //Calculate the seconds to display:
        int seconds = (int) (secondsCount % 60);
        secondsCount -= seconds;
        //Calculate the minutes:
        long minutesCount = secondsCount / 60;
        long minutes = minutesCount % 60;
        minutesCount -= minutes;
        //Calculate the hours:
        long hoursCount = minutesCount / 60;
        long hours = minutesCount % 60;
        hoursCount -= hours;

        long daysCount = hoursCount / 24;

        if (daysCount > 99) {
            return "∞";
        }
        //Build the String
        return "" + daysCount + "d " + hoursCount + "h " + minutes + "m " + seconds + "s ";
    }

    public static String RemoveFolderInvalidChars(String line) {
        String ReservedChars = "|\\?*<\":>+[]/'";
        for (char c : ReservedChars.toCharArray()) {
            line = line.replace(c, ' ');
        }
        return line;
    }

    public static void CopyStream(InputStream is, OutputStream os) {
        final int buffer_size = 1024;
        try {
            byte[] bytes = new byte[buffer_size];
            for (; ; ) {
                int count = is.read(bytes, 0, buffer_size);
                if (count == -1)
                    break;
                os.write(bytes, 0, count);
            }
        } catch (Exception ex) {
        }
    }

    public static double roundToHalf(double x) {
        return (Math.ceil(x * 2) / 2);
    }

    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = listView.getPaddingTop() + listView.getPaddingBottom();
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            if (listItem instanceof ViewGroup) {
                listItem.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            }
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    public static void setViewMargins(View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }

    public static String JsonStringToDateString(String jsonDate) {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        c.setTimeInMillis(Long.valueOf(jsonDate.replace("/Date(", "").replace(")/", "")));
        String month = "";
        if (String.valueOf(c.get(Calendar.MONTH) + 1).length() > 1) {
            month = String.valueOf(c.get(Calendar.MONTH) + 1);
        } else {
            month = "0" + String.valueOf(c.get(Calendar.MONTH) + 1);
        }
        String day = "";
        if (String.valueOf(c.get(Calendar.DATE)).length() > 1) {
            day = String.valueOf(c.get(Calendar.DATE));
        } else {
            day = "0" + String.valueOf(c.get(Calendar.DATE));
        }
        return String.valueOf(c.get(Calendar.YEAR)) + "/" + month + "/" + day;
    }

    public static SoapSerializationEnvelope addAuthentication(SoapSerializationEnvelope envelope) {
        envelope.headerOut = new Element[1];
        Element lang = new Element().createElement("", "Authentication");
        lang.addChild(Node.TEXT, App.accessToken);
        envelope.headerOut[0] = lang;
        return envelope;
    }

    public static boolean IsServiceAvailable() {

        try {
            URL animeServiceUrl = new URL(App.getContext().getString(R.string.anime_service_path_no_dash));
            HttpURLConnection animeServiceConnection = (HttpURLConnection) animeServiceUrl.openConnection();
            animeServiceConnection.setRequestMethod("GET");
            animeServiceConnection.connect();
            int code = animeServiceConnection.getResponseCode();
            if (code != 503 && code != 504 && code != 404) {
                URL animeDataServiceUrl = new URL(App.getContext().getString(R.string.anime_data_service_path_no_dash));
                HttpURLConnection animeDataServiceConnection = (HttpURLConnection) animeDataServiceUrl.openConnection();
                animeDataServiceConnection.setRequestMethod("GET");
                animeDataServiceConnection.connect();
                code = animeDataServiceConnection.getResponseCode();
                if (code != 503 && code != 504 && code != 404) {
                    return true;
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;

    }

    public static String ToLanguageId(String language) {
        language = language.toLowerCase();
        if (language.equals("en"))
            return "1";
        else if (language.equals("fr"))
            return "2";
        else if (language.equals("es"))
            return "4";


        return null;
    }

    public static String ToLanguageString(String id) {
        if (id.equals("1"))
            return "en";
        else if (id.equals("2"))
            return "fr";
        else if (id.equals("4"))
            return "es";


        return null;
    }

    public static JSONObject GetJson(String urlString) {
        BufferedReader reader = null;
        URLConnection conn = null;
        byte[] buf = new byte[4096];
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            URL url = new URL(urlString);
            conn = url.openConnection();
            conn.setConnectTimeout(6000);
            if (App.accessToken != null && !App.accessToken.equals(""))
                conn.setRequestProperty("Authentication", App.accessToken);
            else
                conn.setRequestProperty("Authentication", App.getContext().getString(R.string.urc));
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            StringBuffer buffer = new StringBuffer();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1)
                buffer.append(chars, 0, read);

            // String json = unGunzip(buffer.toString());
            String json = buffer.toString();
            getJsonCount = 0;
            if (json == null)
                return null;
            return new JSONObject(json);
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            //lets try again
            if (getJsonCount < 1) {
                getJsonCount++;
                Utils.GetJson(urlString);
            } else {
                return null;
            }

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            try {
                int respCode = ((HttpURLConnection) conn).getResponseCode();
                InputStream es = ((HttpURLConnection) conn).getErrorStream();
                int ret = 0;
                // read the response body
                while ((ret = es.read(buf)) > 0) {
                    os.write(buf, 0, ret);
                }
                // close the errorstream
                es.close();
                if (respCode == 401) {
                    try {
                        return new JSONObject("{'error':401}");
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                }
                return null;
            } catch (IOException ex) {

            }
        } finally {
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException e) {
                }
        }
        return null;
    }

    public static class ReportMirror extends AsyncTask<Void, Void, String> {
        private static final String NAMESPACE = "http://tempuri.org/";
        final String SOAP_ACTION = "http://tempuri.org/IAnimeService/";
        private String URL;
        private String method = "ReportMirror";
        private int mirrorId;
        private Context context;

        public ReportMirror(int mirrorId, Context context) {
            this.mirrorId = mirrorId;
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            URL = App.getContext().getString(R.string.anime_service_path);
        }

        @Override
        protected String doInBackground(Void... params) {
            if (!App.IsNetworkConnected()) {
                return App.getContext().getString(R.string.error_internet_connection);
            }
            SoapObject request = new SoapObject(NAMESPACE, method);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            request.addProperty("mirrorId", mirrorId);
            envelope = Utils.addAuthentication(envelope);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);
            HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
            SoapPrimitive result = null;
            try {
                androidHttpTransport.call(SOAP_ACTION + method, envelope);
                result = (SoapPrimitive) envelope.getResponse();
                return null;
            } catch (Exception e) {
                if (e instanceof SoapFault) {
                    return e.getMessage();
                }

                e.printStackTrace();
            }
            return "error";
        }

        @Override
        protected void onPostExecute(String error) {
            if (error != null) {
                //dont show any error because it is a behind the scene report
            } else {
                Toast.makeText(context, context.getString(R.string.report_sent), Toast.LENGTH_LONG).show();
            }
        }
    }

    public static String getStringFromFile(String filePath) throws Exception {
        File fl = new File(filePath);
        FileInputStream fin = new FileInputStream(fl);
        String ret = convertStreamToString(fin);
        //Make sure you close all streams.
        fin.close();
        return ret;
    }

    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }


    public static void watchYoutubeVideo(String id, Activity act) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
            act.startActivity(intent);
        } catch (Exception ex) {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://www.youtube.com/watch?v=" + id));
            act.startActivity(intent);
        }
    }

    public static String parseSearch(String key, String search) {
        search = search.replaceAll("[^\\w\\s_]", " ").replace("_", " ");
        ArrayList<String> terms = new ArrayList<String>();
        for (String str : search.trim().replace("-", " ").split("[ ]", -1)) {
            if (!String.format("%b", str).replaceAll("false", "").equals("")) {
                terms.add("+" + str.trim() + "*");
            }
        }
        return String.format(new String(new char[terms.toArray().length]).replace("\0", "%s" + " ").replaceFirst("(.*)" + " " + "$", "$1"), terms.toArray()).replace("+", "+" + key + ":");
    }

    public static void restartActivity(Activity act) {
        if (Build.VERSION.SDK_INT >= 11) {
            act.overridePendingTransition(0, 0);
            act.recreate();
            act.overridePendingTransition(0, 0);
        } else {
            Intent intent = act.getIntent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            act.finish();
            act.overridePendingTransition(0, 0);

            act.startActivity(intent);
            act.overridePendingTransition(0, 0);
        }
    }

    public static void SaveRecentSearch(Activity act, String query) {
        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(act,
                SuggestionProvider.AUTHORITY, SuggestionProvider.MODE);
        suggestions.saveRecentQuery(query, null);
    }
    public static String checkDataServiceErrors(JSONObject json, String errorMessage)
    {
        try {
            if (!json.isNull("error")) {

                int error = json.getInt("error");
                if (error == 401) {
                    return "401";
                }
            }
        }
        catch (Exception e) {
            return errorMessage;
        }

        return null;
    }
    public static int GenreNameToId(String genreName) {
        if (genreName.equals(App.getContext().getString(R.string.action)))
            return 1034;
        else if (genreName.equals(App.getContext().getString(R.string.adventure)))
            return 1035;
        else if (genreName.equals(App.getContext().getString(R.string.drama)))
            return 1036;
        else if (genreName.equals(App.getContext().getString(R.string.fantasy)))
            return 1037;
        else if (genreName.equals(App.getContext().getString(R.string.sciencefiction)))
            return 1038;
        else if (genreName.equals(App.getContext().getString(R.string.comedy)))
            return 1039;
        else if (genreName.equals(App.getContext().getString(R.string.mystery)))
            return 1040;
        else if (genreName.equals(App.getContext().getString(R.string.romance)))
            return 1041;
        else if (genreName.equals(App.getContext().getString(R.string.horror)))
            return 1042;
        else if (genreName.equals(App.getContext().getString(R.string.supernatural)))
            return 1043;
        else if (genreName.equals(App.getContext().getString(R.string.magic)))
            return 1044;
        else if (genreName.equals(App.getContext().getString(R.string.sliceoflife)))
            return 1045;
        else if (genreName.equals(App.getContext().getString(R.string.tournament)))
            return 1046;
        else if (genreName.equals(App.getContext().getString(R.string.psychological)))
            return 1047;
        else if (genreName.equals(App.getContext().getString(R.string.thriller)))
            return 1048;
        else if (genreName.equals(App.getContext().getString(R.string.animation)))
            return 1049;
        else if (genreName.equals(App.getContext().getString(R.string.children)))
            return 1050;
        else if (genreName.equals(App.getContext().getString(R.string.music)))
            return 1051;
        else if (genreName.equals(App.getContext().getString(R.string.school)))
            return 1052;
        else if (genreName.equals(App.getContext().getString(R.string.sports)))
            return 1053;
        else if (genreName.equals(App.getContext().getString(R.string.erotica)))
            return 1054;

        return 0;
    }

    public static void lockScreen(Activity act) {
        int orientation = act.getRequestedOrientation();
        int rotation = ((WindowManager) act.getSystemService(
                Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                break;
            case Surface.ROTATION_90:
                orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                break;
            case Surface.ROTATION_180:
                orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                break;
            default:
                orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                break;
        }

        act.setRequestedOrientation(orientation);
    }

    public static void unlockScreen(Activity act) {
        act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }


}
