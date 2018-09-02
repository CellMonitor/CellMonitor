package com.youssif.joe.weapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class UserGroupsActivity extends AppCompatActivity {

    private final List<String> groups = new ArrayList<>();
    private UserGroupsActivityAdapter mAdapter;
    private RecyclerView recyclerView;
    private DatabaseReference mUserSectionsDatabase2;
    private FirebaseAuth mAuth;
    private String mFriendID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_groups);
        Toolbar mGroupsToolbar = findViewById(R.id.app_bar_layout);
        setSupportActionBar(mGroupsToolbar);

        getSupportActionBar().setTitle("Select Group ..");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mAuth = FirebaseAuth.getInstance();
        mUserSectionsDatabase2 = FirebaseDatabase.getInstance().getReference("User_Section");



        mFriendID = getIntent().getStringExtra("user_id");

        mAdapter = new UserGroupsActivityAdapter(groups,UserGroupsActivity.this, mFriendID);


        setSupportActionBar(mGroupsToolbar);
        recyclerView = findViewById(R.id.groupsList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));





    }


    @Override
    protected void onStart() {
        super.onStart();


        mUserSectionsDatabase2.child(mAuth.getCurrentUser().getUid()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                mUserSectionsDatabase2.child(dataSnapshot.getKey()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!(dataSnapshot.hasChild(mFriendID))){
                            groups.add(dataSnapshot.getKey());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });






        recyclerView.setAdapter(mAdapter);




    }



}
