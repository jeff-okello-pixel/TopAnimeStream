package com.topanimestream.views.profile;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.MenuItem;
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

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        if (toolbar != null) {
            toolbar.setTitle(getString(R.string.my_friends));
            toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
            setSupportActionBar(toolbar);
        }


    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                AnimationManager.ActivityFinish(this);
                break;
        }
        return true;
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
