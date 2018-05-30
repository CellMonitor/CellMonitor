package com.youssif.joe.weapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.youssif.joe.weapp.models.Friends;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.youssif.joe.weapp.GetTimeAgo.getTimeAgo;

public class ChildrenActivity extends AppCompatActivity {

    private RecyclerView mChildrenList;
    private FirebaseAuth mAuth;
    private DatabaseReference mFamilyDatabase , mChildrenDatabase;
    private List<Address> addresses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_children);
        Toolbar mUsersToolbar = findViewById(R.id.children_app_bar);
        setSupportActionBar(mUsersToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Children ❤");

        actionBar.setDisplayHomeAsUpEnabled(true);

        mChildrenList = findViewById(R.id.childrenList);
        mAuth = FirebaseAuth.getInstance();

        String mCurrent_user_id = mAuth.getCurrentUser().getUid();

        mFamilyDatabase = FirebaseDatabase.getInstance().getReference().child("Family").child(mCurrent_user_id);
        mFamilyDatabase.keepSynced(true);

        mChildrenDatabase = FirebaseDatabase.getInstance().getReference().child("Children");
        mChildrenDatabase.keepSynced(true);


        mChildrenList.setHasFixedSize(true);
        mChildrenList.setLayoutManager(new LinearLayoutManager(this));












        FloatingActionButton fab =  findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChildrenActivity.this,ChildrenSearch.class);
                startActivity(intent);
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();




        FirebaseRecyclerOptions<Friends> options = new FirebaseRecyclerOptions.Builder<Friends>()
                .setQuery(mFamilyDatabase, Friends.class)
                .setLifecycleOwner(this)
                .build();

        FirebaseRecyclerAdapter<Friends, MyChildrenViewHolder> friendsRecyclerViewAdapter = new FirebaseRecyclerAdapter<Friends, MyChildrenViewHolder>(options) {

            @Override
            public MyChildrenViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_single_layout, parent, false);

                return new MyChildrenViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final MyChildrenViewHolder myChildrenViewHolder, int position, @NonNull Friends friends) {


                final String list_user_id = getRef(position).getKey();
                Log.d("list_user_id : ", list_user_id);

                mChildrenDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d("DataSnapShot2 : " , dataSnapshot.toString());

                        final String userName = dataSnapshot.child("name").getValue().toString();
                        String userThumb = dataSnapshot.child("thumb_image").getValue().toString();
                        if (dataSnapshot.hasChild("Location")) {
                            String location = dataSnapshot.child("Location").getValue().toString();
                            long locationTime = (long) dataSnapshot.child("Location_Time").getValue();

                            String[] separated = location.split(",");
                            String latiPos = separated[0].trim();
                            String longiPos = separated[1].trim();

                            double latitude = Double.parseDouble(latiPos);
                            double longitude = Double.parseDouble(longiPos);


                            Geocoder geocoder;

                            geocoder = new Geocoder(ChildrenActivity.this, Locale.getDefault());

                            try {
                                addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                            } catch (IOException e) {
                                e.printStackTrace();
                            }


                            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                            //String city = addresses.get(0).getLocality();
                            //String state = addresses.get(0).getAdminArea();
                            //String country = addresses.get(0).getCountryName();
                            //String postalCode = addresses.get(0).getPostalCode();
                            //String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL

                            String lastSeenTime = getTimeAgo(locationTime,ChildrenActivity.this);

                            myChildrenViewHolder.setStatus(address + "\n" + lastSeenTime);
                        }
                        myChildrenViewHolder.setName(userName);

                        myChildrenViewHolder.setUserImage(userThumb, ChildrenActivity.this);

                        myChildrenViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                CharSequence options[] = new CharSequence[]{"Track " + userName + " On Map!"};

                                final AlertDialog.Builder builder = new AlertDialog.Builder(ChildrenActivity.this);

                                builder.setTitle("Select Options");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        //Click Event for each item.
                                        if (i == 0) {



                                        }


                                    }
                                });

                                builder.show();

                            }
                        });


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }


        };
        mChildrenList.setAdapter(friendsRecyclerViewAdapter);




    }

    public static class MyChildrenViewHolder extends RecyclerView.ViewHolder {

        View mView;

        MyChildrenViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }



        public void setName(String name) {

            TextView userNameView = mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);

        }

        public void setStatus(String status){
            TextView userStatusView =  mView.findViewById(R.id.user_single_status);
            userStatusView.setText(status);

        }

        void setUserImage(final String thumb_image, final Context ctx) {

            final CircleImageView userImageView = mView.findViewById(R.id.user_single_image);
            Picasso.get().load(thumb_image).placeholder(R.drawable.avatar).into(userImageView);


        }




    }

}
