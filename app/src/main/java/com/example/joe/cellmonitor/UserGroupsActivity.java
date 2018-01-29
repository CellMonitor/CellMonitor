package com.example.joe.cellmonitor;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.joe.cellmonitor.models.Sections;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserGroupsActivity extends AppCompatActivity {

    private Toolbar mGroupsToolbar;
    private RecyclerView recyclerView;
    private DatabaseReference mUserSectionsDatabase,mSectionDatabase;
    private FirebaseAuth mAuth;
    private String mFriendID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_groups);
        mGroupsToolbar = findViewById(R.id.app_bar_layout);
        setSupportActionBar(mGroupsToolbar);

        getSupportActionBar().setTitle("Select Group ..");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mAuth = FirebaseAuth.getInstance();
        mSectionDatabase = FirebaseDatabase.getInstance().getReference("Sections");
        mUserSectionsDatabase = FirebaseDatabase.getInstance().getReference("User_Section").child(mAuth.getCurrentUser().getUid());


        mFriendID = getIntent().getStringExtra("user_id");
        setSupportActionBar(mGroupsToolbar);
        recyclerView = findViewById(R.id.groupsList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));





    }


    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Sections> options = new FirebaseRecyclerOptions.Builder<Sections>()
                .setQuery(mUserSectionsDatabase ,Sections.class)
                .setLifecycleOwner(this)
                .build();

        FirebaseRecyclerAdapter<Sections,sectionViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Sections, sectionViewHolder>(options) {
            @Override
            public sectionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_single_layout, parent, false);

                return new sectionViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final sectionViewHolder holder, int position, @NonNull Sections model) {

                final String sectionKeys = getRef(position).getKey();
                Log.d("Sections_Keys :" , sectionKeys);


                mSectionDatabase.child(sectionKeys).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d("DataSnapShot : " , dataSnapshot.toString());
                        final String sectionName = dataSnapshot.child("name").getValue().toString();
                        String sectionImage = dataSnapshot.child("image").getValue().toString();
                        long sectionCreationDate = (long) dataSnapshot.child("CreationTime").getValue();




                        holder.setName(sectionName);
                        holder.setTimeStamp(sectionCreationDate);
                        holder.setSectionImage(sectionImage,UserGroupsActivity.this);

                        holder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {



                            }
                        });


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });





            }


        };
        recyclerView.setAdapter(firebaseRecyclerAdapter);




    }

    private static class sectionViewHolder extends RecyclerView.ViewHolder{
        View mView;

        sectionViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }
        public void setName(String name) {

            TextView userNameView = mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);

        }



        public void setTimeStamp(long timeStamp) {

            SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd'\n'hh:mm a");
            String time = sfd.format(new Date(timeStamp));
            TextView userStatusView = mView.findViewById(R.id.user_single_status);
            userStatusView.setText(time);

        }

        void setSectionImage(final String thumb_image, final Context ctx) {

            final CircleImageView userImageView = mView.findViewById(R.id.user_single_image);
            Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.avatar).into(userImageView);
            Picasso.with(ctx).load(thumb_image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.avatar).into(userImageView, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {

                    Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.avatar).into(userImageView);

                }
            });

        }

    }

}
