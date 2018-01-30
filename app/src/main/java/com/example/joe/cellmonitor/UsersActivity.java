package com.example.joe.cellmonitor;


import android.app.ProgressDialog;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.example.joe.cellmonitor.models.Users;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;



import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity  {

    private DatabaseReference databaseReference,mUserDatabase;
    private FirebaseAuth mAuth;

    ProgressDialog progressDialog;

    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.keepSynced(true);
        mUserDatabase = FirebaseDatabase.getInstance().getReference("Friends").child(mAuth.getCurrentUser().getUid());

        recyclerView =  findViewById(R.id.users_list);

        recyclerView.setHasFixedSize(true);

        recyclerView.setLayoutManager(new LinearLayoutManager(UsersActivity.this));






    }

    @Override
    protected void onStart() {
        super.onStart();

        progressDialog = new ProgressDialog(UsersActivity.this);

        progressDialog.setMessage("Loading Data from Firebase Database");

        progressDialog.show();


        FirebaseRecyclerOptions<Users> options = new FirebaseRecyclerOptions.Builder<Users>()
                .setQuery(databaseReference,Users.class)
                .setLifecycleOwner(this)
                .build();
        FirebaseRecyclerAdapter<Users,ViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, ViewHolder>(options) {

            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_single_layout, parent, false);

                return new ViewHolder(view);
            }
            @Override
            protected void onBindViewHolder(@NonNull final ViewHolder holder, int position, @NonNull final Users model) {

                final String user_id = getRef(position).getKey();
                if (!(user_id.equals(mAuth.getCurrentUser().getUid()))) {
                    mUserDatabase.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (!(dataSnapshot.hasChild(user_id))){
                                databaseReference.child(user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {


                                        final String name = dataSnapshot.child("name").getValue().toString();
                                        String status = dataSnapshot.child("status").getValue().toString();
                                        String userImage = dataSnapshot.child("thumb_image").getValue().toString();


                                        holder.setDisplayName(name);
                                        holder.setUserStatus(status);
                                        holder.setUserImage(userImage, getApplicationContext());
                                        progressDialog.dismiss();


                                        holder.mView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                Intent intent = new Intent(UsersActivity.this, UserProfileActivity.class);
                                                intent.putExtra("user_id", user_id);
                                                startActivity(intent);
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            } else {
                                holder.removeViewsinCard();
                                progressDialog.dismiss();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });



                } else {

                    holder.removeViewsinCard();
                    progressDialog.dismiss();




                }

            }


        };
        recyclerView.setAdapter(firebaseRecyclerAdapter);


        DatabaseReference mUserRef = FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        mUserRef.child("online").setValue(true);
        Boolean mLocationPermissionGranted;
        final String FINE_LOCATION = android.Manifest.permission.ACCESS_FINE_LOCATION;
        final String COURSE_LOCATION = android.Manifest.permission.ACCESS_COARSE_LOCATION;
        final int LOCATION_PERMISSION_REQUEST_CODE = 1234;


        String[] permissions = {android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
                //initMap();


                FusedLocationProviderClient mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

                try {
                    if (mLocationPermissionGranted) {
                        final Task location = mFusedLocationProviderClient.getLastLocation();


                        location.addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                if (task.isSuccessful() && task.getResult() != null) {
                                    Location currentLocation = (Location) task.getResult();
                                    Double latitude = currentLocation.getLatitude();
                                    Double longitude = currentLocation.getLongitude();
                                    FirebaseAuth mAuth = FirebaseAuth.getInstance();
                                    DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Users").child(mAuth.getCurrentUser().getUid());
                                    myRef.child("Location").setValue(latitude + "," + longitude);
                                    myRef.child("Location_Time").setValue(ServerValue.TIMESTAMP);



                                }
                            }
                        });
                    }

                } catch (SecurityException e) {
                    Toast.makeText(this, "getDeviceLocation: SecurityException: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                }


            } else {
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }


    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        View mView;

        ViewHolder(View itemView) {

            super(itemView);

            mView = itemView;


        }

        void removeViewsinCard(){
            CardView cardView = mView.findViewById(R.id.cardView);
            cardView.removeAllViewsInLayout();
            cardView.setMinimumHeight(0);
            cardView.setMinimumWidth(0);
            cardView.setVisibility(View.GONE);
        }

        void setDisplayName(String name){
            TextView userNameView = mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);

        }
        void setUserStatus(String status){
            TextView userStatusView = mView.findViewById(R.id.user_single_status);
            userStatusView.setText(status);

        }
        void setUserImage(final String thumb_image, final Context ctx){
            final CircleImageView userImageView = mView.findViewById(R.id.user_single_image);

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
