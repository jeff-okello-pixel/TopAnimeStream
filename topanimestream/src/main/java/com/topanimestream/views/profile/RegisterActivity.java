package com.topanimestream.views.profile;

import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.topanimestream.App;
import com.topanimestream.R;
import com.topanimestream.utilities.Utils;
import com.topanimestream.managers.AnimationManager;
import com.topanimestream.managers.DialogManager;
import com.topanimestream.views.TASBaseActivity;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;

import java.util.Locale;

public class RegisterActivity extends TASBaseActivity implements View.OnClickListener {

    private Dialog busyDialog;

    public RegisterActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_register);



    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {

        }
    }

    private class RegisterTask extends AsyncTask<Void, Void, String> {

        private static final String NAMESPACE = "http://tempuri.org/";
        final String SOAP_ACTION = "http://tempuri.org/IAnimeService/";
        private String URL;
        private String method = "Register";
        private String token;

        @Override
        protected void onPreExecute() {
            busyDialog = DialogManager.showBusyDialog(getString(R.string.creating_your_account), RegisterActivity.this);
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
            /*
            request.addProperty("username", username);
            request.addProperty("password", password);
            request.addProperty("application", "Android");*/


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
                    DialogManager.ShowNoServiceDialog(RegisterActivity.this);
                } else {
                    Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_LONG).show();
                }
            } else {

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
