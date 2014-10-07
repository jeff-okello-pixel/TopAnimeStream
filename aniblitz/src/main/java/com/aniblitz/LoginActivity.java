package com.aniblitz;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.aniblitz.models.Episode;
import com.aniblitz.models.Mirror;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.ArrayList;


public class LoginActivity extends ActionBarActivity implements View.OnClickListener {
    private Button btnLogin;
    private Button btnRegister;
    private EditText txtUserName;
    private EditText txtPassword;
    private LinearLayout layContent;
    private Dialog busyDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_Blue);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setTitle(Html.fromHtml("<font color=#f0f0f0>" + getString(R.string.login) + "</font>"));
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        txtUserName = (EditText) findViewById(R.id.txtUsername);
        txtPassword = (EditText) findViewById(R.id.txtPassword);
        layContent = (LinearLayout) findViewById(R.id.layContent);
        btnLogin.setOnClickListener(this);
        btnRegister.setOnClickListener(this);
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
        switch (view.getId()) {
            case R.id.btnLogin:
                if (txtUserName.getText().toString().equals("")) {
                    txtUserName.setError(getString(R.string.error_username_empty));
                    AnimationManager.Shake(layContent);
                    return;
                }

                if (txtPassword.getText().toString().equals("")) {
                    txtPassword.setError(getString(R.string.error_password_empty));
                    AnimationManager.Shake(layContent);
                    return;
                }

                AsyncTaskTools.execute(new LoginTask(txtUserName.getText().toString(), txtPassword.getText().toString()));
                break;
        }
    }

    private class LoginTask extends AsyncTask<Void, Void, String> {
        private String userName;
        private String password;

        public LoginTask(String userName, String password) {
            this.userName = userName;
            this.password = password;
        }
        private static final String NAMESPACE = "http://tempuri.org/";
        final String SOAP_ACTION = "http://tempuri.org/IAnimeService/";
        private String URL;
        private String method = "Login";
        private String token;
        @Override
        protected void onPreExecute() {
            busyDialog = Utils.showBusyDialog(getString(R.string.logging), LoginActivity.this);
            URL = "http://lanbox.ca/AnimeServices/AnimeService.svc";
        }

        ;

        @Override
        protected String doInBackground(Void... params) {
            if(!App.IsNetworkConnected())
            {
                return getString(R.string.error_internet_connection);
            }
            SoapObject request = new SoapObject(NAMESPACE, method);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            request.addProperty("username", userName);
            request.addProperty("password", password);


            envelope .dotNet = true;
            envelope.setOutputSoapObject(request);
            HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
            SoapPrimitive result = null;
            try
            {
                androidHttpTransport.call(SOAP_ACTION + method, envelope);
                result = (SoapPrimitive)envelope.getResponse();
                token = result.toString();
                return null;
            }
            catch (Exception e)
            {
                if(e instanceof SoapFault)
                {
                    return e.getMessage();
                }

                e.printStackTrace();
            }
            return getString(R.string.error_login);
        }

        @Override
        protected void onPostExecute(String error) {
                if(error != null)
                {
                    Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
                }
                else
                {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                    prefs.edit().putString("AccessToken", token).commit();
                    App.accessToken = token;
                    Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_LONG).show();
                }
                Utils.dismissBusyDialog(busyDialog);



        }
    }
}
