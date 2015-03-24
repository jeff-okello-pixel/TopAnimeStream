package com.topanimestream.views.profile;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

import com.google.gson.Gson;
import com.topanimestream.App;
import com.topanimestream.utilities.AsyncTaskTools;
import com.topanimestream.custom.TextureViewVideo;
import com.topanimestream.utilities.Utils;
import com.topanimestream.utilities.WcfDataServiceUtility;
import com.topanimestream.managers.AnimationManager;
import com.topanimestream.managers.DialogManager;
import com.topanimestream.R;
import com.topanimestream.models.Account;
import com.topanimestream.models.CurrentUser;
import com.topanimestream.views.MainActivity;

public class LoginActivity extends ActionBarActivity implements View.OnClickListener {
    private Button btnLogin;
    private Button btnRegister;
    private Button btnPasswordRecovery;
    private TextView txtTitle;
    private EditText txtUserName;
    private EditText txtPassword;
    private Dialog busyDialog;
    private Boolean shouldCloseOnly;//Used to start the mainactivity or not
    private SharedPreferences prefs;
    private VideoView videoView;
    private Button btnBottomLogin;
    private Button btnCancel;
    private TextureViewVideo mTextureVideoView;
    private MediaPlayer mMediaPlayer;
    private LinearLayout layLogin;

    public LoginActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_Blue);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_awesome_login);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Intent intent = getIntent();
        shouldCloseOnly = intent.getBooleanExtra("ShouldCloseOnly", false);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setTitle(Html.fromHtml("<font color=#f0f0f0>" + getString(R.string.login) + "</font>"));
        actionBar.hide();
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnPasswordRecovery = (Button) findViewById(R.id.btnPasswordRecovery);
        layLogin = (LinearLayout) findViewById(R.id.layLogin);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnCancel = (Button) findViewById(R.id.btnCancel);
        txtUserName = (EditText) findViewById(R.id.txtUsername);
        txtPassword = (EditText) findViewById(R.id.txtPassword);
        txtTitle = (TextView) findViewById(R.id.txtTitle);
        btnBottomLogin = (Button) findViewById(R.id.btnBottomLogin);
        Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/toony_loons.ttf");
        txtTitle.setTypeface(typeFace);
        videoView = (VideoView) findViewById(R.id.videoView);
        videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.loginbackground));
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });
        videoView.start();

        btnLogin.setOnClickListener(this);
        btnPasswordRecovery.setOnClickListener(this);
        btnRegister.setOnClickListener(this);
        btnBottomLogin.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        String lang = prefs.getString("prefLanguage", "");
        if (lang.equals("1"))
            lang = "en";
        else if (lang.equals("2"))
            lang = "fr";
        else if (lang.equals("4"))
            lang = "es";
        else
            lang = "en";
        switch (view.getId()) {
            case R.id.btnLogin:
                if (txtUserName.getText().toString().equals("")) {
                    txtUserName.setError(getString(R.string.error_username_empty));
                    AnimationManager.Shake(layLogin);
                    return;
                }

                if (txtPassword.getText().toString().equals("")) {
                    txtPassword.setError(getString(R.string.error_password_empty));
                    AnimationManager.Shake(layLogin);
                    return;
                }

                AsyncTaskTools.execute(new LoginTask(txtUserName.getText().toString(), txtPassword.getText().toString()));
                break;
            case R.id.btnCancel:
                btnBottomLogin.setVisibility(View.VISIBLE);
                btnPasswordRecovery.setVisibility(View.GONE);
                Animation animFadeOut = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_out);
                layLogin.setAnimation(animFadeOut);
                layLogin.setVisibility(View.INVISIBLE);
                break;
            case R.id.btnBottomLogin:
                btnBottomLogin.setVisibility(View.GONE);
                btnPasswordRecovery.setVisibility(View.VISIBLE);
                Animation animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in);
                layLogin.setAnimation(animFadeIn);
                layLogin.setVisibility(View.VISIBLE);
                break;
            case R.id.btnPasswordRecovery:
                String passwordRecoveryUrl = getString(R.string.topanimestream_website);
                passwordRecoveryUrl += lang + "/forgot-password";
                Intent intentPasswordRecovery = new Intent(Intent.ACTION_VIEW);
                intentPasswordRecovery.setData(Uri.parse(passwordRecoveryUrl));
                startActivity(intentPasswordRecovery);
                break;
            case R.id.btnRegister:
                String registerUrl = getString(R.string.topanimestream_website);
                registerUrl += lang + "/register";
                Intent intentRegister = new Intent(Intent.ACTION_VIEW);
                intentRegister.setData(Uri.parse(registerUrl));
                startActivity(intentRegister);
                break;
        }
    }

    private class LoginTask extends AsyncTask<Void, Void, String> {
        private String username;
        private String password;

        public LoginTask(String userName, String password) {
            this.username = userName;
            this.password = password;
        }

        private static final String NAMESPACE = "http://tempuri.org/";
        final String SOAP_ACTION = "http://tempuri.org/IAnimeService/";
        private String URL;
        private String method = "Login";
        private String token;

        @Override
        protected void onPreExecute() {
            busyDialog = DialogManager.showBusyDialog(getString(R.string.logging), LoginActivity.this);
            URL = getString(R.string.anime_service_path);
        }


        @Override
        protected String doInBackground(Void... params) {
            if (!App.IsNetworkConnected()) {
                return getString(R.string.error_internet_connection);
            }

            if (!Utils.IsServiceAvailable()) {
                return getString(R.string.service_unavailable);
            }
            SoapObject request = new SoapObject(NAMESPACE, method);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);

            envelope.headerOut = new Element[1];
            Element lang = new Element().createElement("", "Lang");
            lang.addChild(Node.TEXT, Locale.getDefault().getLanguage());
            envelope.headerOut[0] = lang;
            request.addProperty("username", username);
            request.addProperty("password", password);
            request.addProperty("application", "Android");


            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);

            HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
            //androidHttpTransport.debug = true;
            SoapPrimitive result = null;
            try {
                androidHttpTransport.call(SOAP_ACTION + method, envelope);
                //String requestDump = androidHttpTransport.requestDump.toString();
                result = (SoapPrimitive) envelope.getResponse();
                token = result.toString();
                return null;
            } catch (Exception e) {
                if (e instanceof SoapFault) {
                    return e.getMessage();
                }

                e.printStackTrace();
            }
            return getString(R.string.error_login);
        }

        @Override
        protected void onPostExecute(String error) {

            if (error != null) {
                if (error.equals(getString(R.string.service_unavailable))) {
                    DialogManager.ShowNoServiceDialog(LoginActivity.this);
                } else {
                    Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
                }
            } else {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                prefs.edit().putString("AccessToken", token).apply();
                prefs.edit().putString("Username", username).apply();
                App.accessToken = token;
                AsyncTaskTools.execute(new AccountTask(LoginActivity.this, username));
            }
        }
    }

    public class AccountTask extends AsyncTask<Void, Void, String> {
        private Context context;
        private String username;
        private ArrayList<Integer> premiumRoles = new ArrayList<Integer>(Arrays.asList(1, 2, 3, 6));

        public AccountTask(Context context, String username) {
            this.context = context;
            this.username = username;
        }

        @Override
        protected void onPreExecute() {
        }


        @Override
        protected String doInBackground(Void... params) {
            if (App.IsNetworkConnected()) {
                try {
                    JSONObject jsonAccount = Utils.GetJson(new WcfDataServiceUtility(context.getString(R.string.anime_data_service_path)).getEntity("Accounts").filter("Username%20eq%20%27" + username + "%27").expand("Roles").formatJson().build());

                    Gson gson = new Gson();
                    Account account = gson.fromJson(jsonAccount.getJSONArray("value").getJSONObject(0).toString(), Account.class);
                    //Set global current user
                    CurrentUser.SetCurrentUser(account);
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
                DialogManager.dismissBusyDialog(busyDialog);
                if (result == null) {
                    //Failed to get the role
                    Toast.makeText(LoginActivity.this, getString(R.string.error_login), Toast.LENGTH_LONG).show();
                } else {
                    if (!Collections.disjoint(CurrentUser.Roles, premiumRoles)) {
                        //is premium
                        prefs.edit().putBoolean("IsPro", true).apply();
                        App.isPro = true;
                    } else {
                        prefs.edit().putBoolean("IsPro", false).apply();
                        App.isPro = false;
                    }

                    Toast.makeText(LoginActivity.this, getString(R.string.login_successful), Toast.LENGTH_LONG).show();
                    if (!shouldCloseOnly) {
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        AnimationManager.ActivityStart(LoginActivity.this);
                    }
                    finish();
                }

            } catch (Exception e)//catch all exception, handle orientation change
            {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        AnimationManager.ActivityFinish(this);
    }
}
