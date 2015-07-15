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
import java.util.Locale;

import com.google.gson.Gson;
import com.topanimestream.App;
import com.topanimestream.preferences.Prefs;
import com.topanimestream.utilities.AsyncTaskTools;
import com.topanimestream.utilities.PrefUtils;
import com.topanimestream.utilities.Utils;
import com.topanimestream.utilities.WcfDataServiceUtility;
import com.topanimestream.managers.AnimationManager;
import com.topanimestream.managers.DialogManager;
import com.topanimestream.R;
import com.topanimestream.models.Account;
import com.topanimestream.models.CurrentUser;
import com.topanimestream.views.MainActivity;
import com.topanimestream.views.TASBaseActivity;

import butterknife.Bind;

public class LoginActivity extends TASBaseActivity implements View.OnClickListener {
    @Bind(R.id.btnLogin)
    private Button btnLogin;

    @Bind(R.id.btnRegister)
    private Button btnRegister;

    @Bind(R.id.btnPasswordRecovery)
    private Button btnPasswordRecovery;

    @Bind(R.id.txtTitle)
    private TextView txtTitle;

    @Bind(R.id.txtUsername)
    private EditText txtUserName;

    @Bind(R.id.txtPassword)
    private EditText txtPassword;

    @Bind(R.id.videoView)
    private VideoView videoView;

    @Bind(R.id.btnBottomLogin)
    private Button btnBottomLogin;

    @Bind(R.id.btnCancel)
    private Button btnCancel;

    @Bind(R.id.layLogin)
    private LinearLayout layLogin;

    private Dialog busyDialog;
    private Boolean shouldCloseOnly;//Used to start the mainactivity or not

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_awesome_login);


        shouldCloseOnly = getIntent().getBooleanExtra("ShouldCloseOnly", false);

        Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/toony_loons.ttf");
        txtTitle.setTypeface(typeFace);

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
    protected void onResume() {
        super.onResume();
        if(videoView != null)
            videoView.start();
    }

    @Override
    public void onClick(View view) {
        String lang = PrefUtils.get(this, Prefs.LOCALE, "1");
        lang = Utils.ToLanguageString(lang);

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
