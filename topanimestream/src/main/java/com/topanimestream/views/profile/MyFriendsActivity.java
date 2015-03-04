package com.topanimestream.views.profile;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.view.View;
import android.widget.ListView;

import com.topanimestream.R;
import com.topanimestream.managers.AnimationManager;

public class MyFriendsActivity extends ActionBarActivity implements View.OnClickListener {
    private ListView listViewMyFriends;
    public MyFriendsActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_Blue);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_friends);
        listViewMyFriends = (ListView) findViewById(R.id.listViewMyFriends);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setTitle(Html.fromHtml("<font color=#f0f0f0>" + getString(R.string.my_friends) + "</font>"));


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

}
