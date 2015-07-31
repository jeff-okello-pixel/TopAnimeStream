package com.topanimestream.views;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.topanimestream.App;
import com.topanimestream.preferences.Prefs;
import com.topanimestream.utilities.AsyncTaskTools;
import com.topanimestream.R;
import com.topanimestream.utilities.PrefUtils;
import com.topanimestream.utilities.Utils;
import com.topanimestream.managers.DialogManager;
import com.topanimestream.models.Review;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import butterknife.Bind;


public class ManageReviewActivity extends TASBaseActivity implements View.OnTouchListener, RatingBar.OnRatingBarChangeListener, View.OnClickListener {

    private int animeId;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.lblArtRating)
    TextView lblArtRating;
    @Bind(R.id.lblCharacterRating)
    TextView lblCharacterRating;
    @Bind(R.id.lblStoryRating)
    TextView lblStoryRating;
    @Bind(R.id.lblSoundRating)
    TextView lblSoundRating;
    @Bind(R.id.lblEnjoymentRating)
    TextView lblEnjoymentRating;
    @Bind(R.id.lblOverallRating)
    TextView lblOverallRating;

    @Bind(R.id.rtbArtRating)
    RatingBar rtbArtRating;
    @Bind(R.id.rtbCharacterRating)
    RatingBar rtbCharacterRating;
    @Bind(R.id.rtbStoryRating)
    RatingBar rtbStoryRating;
    @Bind(R.id.rtbSoundRating)
    RatingBar rtbSoundRating;
    @Bind(R.id.rtbEnjoymentRating)
    RatingBar rtbEnjoymentRating;

    @Bind(R.id.btnSave)
    Button btnSave;
    @Bind(R.id.btnCancel)
    Button btnCancel;
    @Bind(R.id.btnDelete)
    Button btnDelete;

    @Bind(R.id.txtYourReview)
    EditText txtYourReview;

    private Review currentUserReview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_manage_review);

        btnCancel.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        btnDelete.setOnClickListener(this);

        rtbArtRating.setOnTouchListener(this);
        rtbCharacterRating.setOnTouchListener(this);
        rtbStoryRating.setOnTouchListener(this);
        rtbSoundRating.setOnTouchListener(this);
        rtbEnjoymentRating.setOnTouchListener(this);

        rtbArtRating.setOnRatingBarChangeListener(this);
        rtbCharacterRating.setOnRatingBarChangeListener(this);
        rtbStoryRating.setOnRatingBarChangeListener(this);
        rtbSoundRating.setOnRatingBarChangeListener(this);
        rtbEnjoymentRating.setOnRatingBarChangeListener(this);

        if (toolbar != null) {
            toolbar.setTitle(getString(R.string.add_review));
            toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
            setSupportActionBar(toolbar);
        }
        Intent intent = getIntent();
        animeId = intent.getExtras().getInt("animeId");
        currentUserReview = intent.getExtras().getParcelable("currentUserReview");
        if(currentUserReview != null)
        {
            rtbArtRating.setRating(currentUserReview.getArtRating() / 2);
            rtbCharacterRating.setRating(currentUserReview.getCharacterRating() / 2);
            rtbStoryRating.setRating(currentUserReview.getStoryRating() / 2);
            rtbSoundRating.setRating(currentUserReview.getSoundRating() / 2);
            rtbEnjoymentRating.setRating(currentUserReview.getEnjoymentRating() / 2);
            txtYourReview.setText(currentUserReview.getValue());
            btnDelete.setVisibility(View.VISIBLE);
            toolbar.setTitle(getString(R.string.edit_your_review));

        }

    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        RatingBar ratingBar = (RatingBar)view;
        switch(ratingBar.getId())
        {
            case R.id.rtbArtRating:
                lblArtRating.setText(Utils.floatToStringWithoutDot(rtbArtRating.getRating()) + "/5");
                break;
            case R.id.rtbCharacterRating:
                lblCharacterRating.setText(Utils.floatToStringWithoutDot(rtbCharacterRating.getRating()) + "/5");
                break;
            case R.id.rtbStoryRating:
                lblStoryRating.setText(Utils.floatToStringWithoutDot(rtbStoryRating.getRating()) + "/5");
                break;
            case R.id.rtbSoundRating:
                lblSoundRating.setText(Utils.floatToStringWithoutDot(rtbSoundRating.getRating()) + "/5");
                break;
            case R.id.rtbEnjoymentRating:
                lblEnjoymentRating.setText(Utils.floatToStringWithoutDot(rtbEnjoymentRating.getRating()) + "/5");
                break;
        }

        SetOverallRating();

        return false;
    }
    private void SetOverallRating()
    {
        float overallRating =   (rtbArtRating.getRating() +
                                rtbCharacterRating.getRating() +
                                rtbEnjoymentRating.getRating() +
                                rtbSoundRating.getRating() +
                                rtbStoryRating.getRating()) / 5;
        lblOverallRating.setText(Utils.floatToStringWithoutDot(overallRating) + "/5");
    }
    @Override
    public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
        switch(ratingBar.getId())
        {
            case R.id.rtbArtRating:
                lblArtRating.setText(Utils.floatToStringWithoutDot(rtbArtRating.getRating()) + "/5");
                break;
            case R.id.rtbCharacterRating:
                lblCharacterRating.setText(Utils.floatToStringWithoutDot(rtbCharacterRating.getRating()) + "/5");
                break;
            case R.id.rtbStoryRating:
                lblStoryRating.setText(Utils.floatToStringWithoutDot(rtbStoryRating.getRating()) + "/5");
                break;
            case R.id.rtbSoundRating:
                lblSoundRating.setText(Utils.floatToStringWithoutDot(rtbSoundRating.getRating()) + "/5");
                break;
            case R.id.rtbEnjoymentRating:
                lblEnjoymentRating.setText(Utils.floatToStringWithoutDot(rtbEnjoymentRating.getRating()) + "/5");
                break;
        }
        SetOverallRating();
    }

    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.btnSave:
                AsyncTaskTools.execute(new AddReviewTask());
                break;
            case R.id.btnDelete:
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                AsyncTaskTools.execute(new RemoveReviewTask());
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                dialog.dismiss();
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(ManageReviewActivity.this);
                builder.setMessage(getString(R.string.really_sure_delete_review)).setPositiveButton(getString(R.string.yes), dialogClickListener)
                        .setNegativeButton(getString(R.string.no), dialogClickListener).show();

                break;
            case R.id.btnCancel:
                finish();
                break;

        }
    }
    private class RemoveReviewTask extends AsyncTask<Void, Void, String> {
        private Dialog busyDialog;

        private static final String NAMESPACE = "http://tempuri.org/";
        final String SOAP_ACTION = "http://tempuri.org/IAnimeService/";
        private String URL;
        private String method = "RemoveReview";
        @Override
        protected void onPreExecute() {
            busyDialog = DialogManager.showBusyDialog(getString(R.string.deleting_review), ManageReviewActivity.this);
            URL = getString(R.string.anime_service_path);
        }

        @Override
        protected String doInBackground(Void... params) {
            if (!App.IsNetworkConnected()) {
                return getString(R.string.error_internet_connection);
            }
            SoapObject request = new SoapObject(NAMESPACE, method);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            request.addProperty("reviewId", currentUserReview.getReviewId());
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
            return getString(R.string.error_remove_favorite);
        }

        @Override
        protected void onPostExecute(String error) {
            DialogManager.dismissBusyDialog(busyDialog);
            if (error != null) {
                Toast.makeText(ManageReviewActivity.this, error, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(ManageReviewActivity.this, getString(R.string.review_deleted), Toast.LENGTH_LONG).show();
                //Starting the previous Intent
                Intent previousActivity = new Intent(getApplicationContext(), ReviewsActivity.class);
                previousActivity.putExtra("reviewId", currentUserReview.getReviewId());
                setResult(0, previousActivity);
                finish();
            }
        }
    }
    private class AddReviewTask extends AsyncTask<Void, Void, String> {
        private Dialog busyDialog;

        private static final String NAMESPACE = "http://tempuri.org/";
        final String SOAP_ACTION = "http://tempuri.org/IAnimeService/";
        private String URL;
        private String method = "Review";
        private int reviewId;
        @Override
        protected void onPreExecute() {
            if(currentUserReview != null) {
                busyDialog = DialogManager.showBusyDialog(getString(R.string.updating_review), ManageReviewActivity.this);
            }else
            {
                busyDialog = DialogManager.showBusyDialog(getString(R.string.adding_your_review), ManageReviewActivity.this);
            }
            URL = getString(R.string.anime_service_path);
        }

        @Override
        protected String doInBackground(Void... params) {
            if (!App.IsNetworkConnected()) {
                return getString(R.string.error_internet_connection);
            }
            SoapObject request = new SoapObject(NAMESPACE, method);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            request.addProperty("animeId", animeId);
            request.addProperty("languageId", Integer.valueOf(PrefUtils.get(ManageReviewActivity.this, Prefs.LOCALE, "1")));
            request.addProperty("review", txtYourReview.getText().toString());
            request.addProperty("storyRating", Math.round(rtbStoryRating.getRating() * 2));
            request.addProperty("artRating", Math.round(rtbArtRating.getRating() * 2));
            request.addProperty("characterRating", Math.round(rtbCharacterRating.getRating() * 2));
            request.addProperty("enjoymentRating", Math.round(rtbEnjoymentRating.getRating() * 2));
            request.addProperty("soundRating", Math.round(rtbSoundRating.getRating() * 2));
            envelope = Utils.addAuthentication(envelope);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);
            HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
            SoapPrimitive result = null;
            try {
                androidHttpTransport.call(SOAP_ACTION + method, envelope);
                result = (SoapPrimitive) envelope.getResponse();
                reviewId = Integer.valueOf(result.toString());
                return null;
            } catch (Exception e) {
                if (e instanceof SoapFault) {
                    return e.getMessage();
                }

                e.printStackTrace();
            }
            return getString(R.string.error_remove_favorite);
        }

        @Override
        protected void onPostExecute(String error) {
            DialogManager.dismissBusyDialog(busyDialog);
            if (error != null) {
                Toast.makeText(ManageReviewActivity.this, error, Toast.LENGTH_LONG).show();
            } else {
                if(currentUserReview != null){
                    Toast.makeText(ManageReviewActivity.this, getString(R.string.review_updated), Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(ManageReviewActivity.this, getString(R.string.review_created), Toast.LENGTH_SHORT).show();
                }
                //Starting the previous Intent
                Intent previousActivity = new Intent(getApplicationContext(), ReviewsActivity.class);
                previousActivity.putExtra("reviewId", reviewId);
                setResult(0, previousActivity);
                finish();
            }
        }
    }
}
