package com.topanimestream.views;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.topanimestream.R;
import com.topanimestream.models.Episode;
import com.topanimestream.utilities.SQLiteHelper;

import java.util.ArrayList;

public class TASBaseActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState, int layoutId) {
        setTheme(R.style.Theme_Blue);
        super.onCreate(savedInstanceState);
        setContentView(layoutId);

    }




}
