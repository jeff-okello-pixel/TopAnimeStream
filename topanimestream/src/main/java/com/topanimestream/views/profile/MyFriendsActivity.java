package com.topanimestream.views.profile;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;

import com.topanimestream.R;
import com.topanimestream.managers.AnimationManager;
import com.topanimestream.views.TASBaseActivity;

import butterknife.Bind;

public class MyFriendsActivity extends TASBaseActivity implements View.OnClickListener {

    @Bind(R.id.listViewMyFriends)
    ListView listViewMyFriends;

    @Bind(R.id.toolbar)
    Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_my_friends);

        toolbar.setTitle(getString(R.string.my_friends));
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        setSupportActionBar(toolbar);


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
