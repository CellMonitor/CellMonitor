package com.example.joe.cellmonitor;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

public class UserGroupsActivity extends AppCompatActivity {

    private Toolbar mChatToolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_groups);

        mChatToolbar = findViewById(R.id.app_bar_layout);
        setSupportActionBar(mChatToolbar);



        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
    }
}
