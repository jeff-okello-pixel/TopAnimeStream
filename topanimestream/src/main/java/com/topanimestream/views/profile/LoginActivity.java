package com.topanimestream.views.profile;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.gson.Gson;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.topanimestream.App;
import com.topanimestream.models.OdataErrorMessage;
import com.topanimestream.preferences.Prefs;
import com.topanimestream.utilities.AsyncTaskTools;
import com.topanimestream.utilities.PrefUtils;
import com.topanimestream.utilities.Utils;
import com.topanimestream.managers.AnimationManager;
import com.topanimestream.managers.DialogManager;
import com.topanimestream.R;
import com.topanimestream.models.CurrentUser;
import com.topanimestream.views.MainActivity;
import com.topanimestream.views.TASBaseActivity;
import butterknife.Bind;
public class LoginActivity extends TASBaseActivity implements View.OnClickListener {
    @Bind(R.id.btnLogin)
    Button btnLogin;

    @Bind(R.id.btnRegister)
    Button btnRegister;

    @Bind(R.id.btnPasswordRecovery)
    Button btnPasswordRecovery;

    @Bind(R.id.txtTitle)
    TextView txtTitle;

    @Bind(R.id.txtUsername)
    EditText txtUserName;

    @Bind(R.id.txtPassword)
    EditText txtPassword;

    @Bind(R.id.videoView)
    VideoView videoView;

    @Bind(R.id.btnBottomLogin)
    Button btnBottomLogin;

    @Bind(R.id.btnCancel)
    Button btnCancel;

    @Bind(R.id.layLogin)
    LinearLayout layLogin;

    @Bind(R.id.coordinatorLayout)
    CoordinatorLayout coordinatorLayout;

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

        String token = PrefUtils.get(this, Prefs.ACCESS_TOKEN, null);
        if(token != null)
            AsyncTaskTools.execute(new ValidTokenTask(token));
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

        @Override
        protected void onPreExecute() {
            busyDialog = DialogManager.showBusyDialog(getString(R.string.logging), LoginActivity.this);
        }

        @Override
        protected String doInBackground(Void... params) {
            if (!App.IsNetworkConnected()) {
                return getString(R.string.error_internet_connection);
            }

            OkHttpClient client = App.getHttpClient();

            RequestBody formBody = new FormEncodingBuilder()
                    .add("Username", username)
                    .add("Password", password)
                    .add("Application", "Android")
                    .build();

            Request request = new Request.Builder()
                    .url(getString(R.string.api_path) + "login")
                    .post(formBody)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                Gson gson = new Gson();
                if(response.isSuccessful()) {
                    App.currentUser = gson.fromJson(response.body().string(), CurrentUser.class);

                    //We need to put the token string before the token for the header.
                    App.accessToken = "Token " + App.currentUser.getToken();

                    return null;
                }
                else
                {
                    OdataErrorMessage errorMessage = gson.fromJson(response.body().string(), OdataErrorMessage.class);
                    if(errorMessage.getMessage() != null &&  !errorMessage.getMessage().equals(""))
                    {
                        return errorMessage.getMessage();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return getString(R.string.error_while_login);
        }

        @Override
        protected void onPostExecute(String error) {

            if (error != null) {
                Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
            } else {
                PrefUtils.save(LoginActivity.this, Prefs.ACCESS_TOKEN, App.currentUser.getToken());
                PrefUtils.save(LoginActivity.this, Prefs.USERNAME, username);
                if (!shouldCloseOnly) {
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    AnimationManager.ActivityStart(LoginActivity.this);
                }
                finish();
            }

            DialogManager.dismissBusyDialog(busyDialog);
        }
    }
    private class ValidTokenTask extends AsyncTask<Void, Void, String> {
        String token;
        boolean isValidToken = false;
        public ValidTokenTask(String token)
        {
            this.token = token;
        }
        @Override
        protected void onPreExecute() {
            busyDialog = DialogManager.showBusyDialog(getString(R.string.logging), LoginActivity.this);
        }

        @Override
        protected String doInBackground(Void... params) {
            if (!App.IsNetworkConnected()) {
                return getString(R.string.error_internet_connection);
            }

            OkHttpClient client = App.getHttpClient();

            final MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType,'"' + token + '"');

            Request request = new Request.Builder()
                    .url(getString(R.string.api_path) + "ValidateToken")
                    .post(body)
                    .build();

            try {
                Gson gson = new Gson();
                Response response = client.newCall(request).execute();
                if(response.isSuccessful())
                {
                    App.currentUser = gson.fromJson(response.body().string(), CurrentUser.class);

                    //We need to put the token string before the token for the header.
                    App.accessToken = "Token " + App.currentUser.getToken();

                    isValidToken = true;
                    return null;
                }
                else
                {
                    OdataErrorMessage errorMessage = gson.fromJson(response.body().string(), OdataErrorMessage.class);
                    if(errorMessage.getMessage() != null &&  !errorMessage.getMessage().equals(""))
                    {
                        if(errorMessage.getMessage().equals("Token invalid."))
                        {
                            return null; //simply show the login screen
                        }
                        else
                        {
                            return errorMessage.getMessage();
                        }

                    }
                }

            } catch (Exception e) {

                e.printStackTrace();
            }
            return getString(R.string.error_login);
        }

        @Override
        protected void onPostExecute(String error) {
            try {
                DialogManager.dismissBusyDialog(busyDialog);
            } catch (Exception e) {
            }

            if (error != null) {
                Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
            } else {
                if(isValidToken) {
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                }
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
