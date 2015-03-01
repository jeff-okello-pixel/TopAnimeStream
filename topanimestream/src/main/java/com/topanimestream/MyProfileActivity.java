package com.topanimestream;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nirhart.parallaxscroll.views.ParallaxListView;
import com.topanimestream.managers.AnimationManager;
import com.topanimestream.managers.DialogManager;
import com.topanimestream.models.Item;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;

import java.util.Locale;

public class MyProfileActivity extends ActionBarActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private Dialog busyDialog;
    private SharedPreferences prefs;
    private ParallaxListView listView;
    private ListAdapter adapter;
    public MyProfileActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_Blue);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myprofile);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        listView = (ParallaxListView) findViewById(R.id.listView);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setTitle(Html.fromHtml("<font color=#f0f0f0>" + getString(R.string.my_profile) + "</font>"));

        final Item[] items = {
                new Item(getString(R.string.edit_profile), R.drawable.ic_edit),
                new Item(getString(R.string.menu_favorites), R.drawable.ic_star),
                new Item(getString(R.string.menu_history), R.drawable.ic_history_black),
                new Item(getString(R.string.votes), R.drawable.ic_vote),
                new Item(getString(R.string.reviews), R.drawable.ic_review),
                new Item(getString(R.string.recommendations), R.drawable.ic_recommendation),
                new Item(getString(R.string.friends), R.drawable.ic_friends),
                new Item(getString(R.string.changes), R.drawable.ic_changes)};

        adapter = new ArrayAdapter<Item>(
                this,
                android.R.layout.select_dialog_item,
                android.R.id.text1,
                items){
            public View getView(int position, View convertView, ViewGroup parent) {
                //User super class to create the View
                View v = super.getView(position, convertView, parent);
                v.setBackgroundColor(Color.WHITE);
                TextView tv = (TextView)v.findViewById(android.R.id.text1);
                //Put the image on the TextView
                tv.setCompoundDrawablesWithIntrinsicBounds(items[position].icon, 0, 0, 0);

                //Add margin between image and text (support various screen densities)
                int dp5 = (int) (5 * getResources().getDisplayMetrics().density + 0.8f);
                tv.setCompoundDrawablePadding(dp5);

                return v;
            }
        };
        View view = getLayoutInflater().inflate(R.layout.profile_header, null);

        listView.addParallaxedHeaderView(view);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(this);


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        AnimationManager.ActivityFinish(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

    }
}
