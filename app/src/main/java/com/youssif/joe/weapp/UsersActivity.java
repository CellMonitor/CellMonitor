package com.youssif.joe.weapp;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.youssif.joe.weapp.models.Users;

import java.util.ArrayList;
import java.util.List;


public class UsersActivity extends AppCompatActivity {

    private DatabaseReference databaseReference, mUserDatabase;
    private FirebaseAuth mAuth;
    private UsersActivityAdapter mAdapter;
    private final List<String> peaple = new ArrayList<>();
    private EditText mSearchField;

    private RecyclerView mResultList, recyclerView;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);


        Toolbar mUsersToolbar = findViewById(R.id.users_app_bar);
        setSupportActionBar(mUsersToolbar);


        mSearchField = findViewById(R.id.search_field);
        ImageButton mSearchBtn = findViewById(R.id.search_btn);

        mResultList = findViewById(R.id.result_list);
        mResultList.setHasFixedSize(true);
        mResultList.setLayoutManager(new LinearLayoutManager(this));

        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                hideSoftKeyboard();
                String searchText = mSearchField.getText().toString();
                if (!TextUtils.isEmpty(searchText)) {
                    firebaseUserSearch(searchText);
                }
            }
        });

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Add new Friends ..");

        actionBar.setDisplayHomeAsUpEnabled(true);
        mAdapter = new UsersActivityAdapter(peaple,UsersActivity.this);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.keepSynced(true);
        mUserDatabase = FirebaseDatabase.getInstance().getReference("Friends").child(mAuth.getCurrentUser().getUid());

        recyclerView = findViewById(R.id.users_list);

        recyclerView.setHasFixedSize(true);

        recyclerView.setLayoutManager(new LinearLayoutManager(UsersActivity.this));


    }

    private void hideSoftKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            assert imm != null;
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void firebaseUserSearch(String searchText) {

        Query firebaseSearchQuery = databaseReference.orderByChild("name").startAt(searchText).endAt(searchText + "\uf8ff");
        FirebaseRecyclerOptions<Users> options = new FirebaseRecyclerOptions.Builder<Users>()
                .setQuery(firebaseSearchQuery, Users.class)
                .setLifecycleOwner(this)
                .build();

        FirebaseRecyclerAdapter<Users, ViewHolder> mAdapter = new FirebaseRecyclerAdapter<Users, ViewHolder>(options) {
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_single_layout, parent, false);

                return new ViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Users model) {

                final String user_id = getRef(position).getKey();
                holder.setDetails(UsersActivity.this, model.getName(), model.getStatus(), model.getImage());

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(UsersActivity.this, UserProfileActivity.class);
                        intent.putExtra("user_id", user_id);
                        startActivity(intent);
                    }
                });
            }


        };
        mResultList.setAdapter(mAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();

        progressDialog = new ProgressDialog(UsersActivity.this);

        progressDialog.setMessage("Loading Data.. Please wait :)");

        progressDialog.show();

        recyclerView.setAdapter(mAdapter);

        final ArrayList<String> frineds = new ArrayList<>();


        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Log.d("snaapshot", snapshot.getKey());
                    frineds.add(snapshot.getKey());
                    frineds.add(mAuth.getCurrentUser().getUid());
                }

                databaseReference.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        if (!frineds.contains(dataSnapshot.getKey())) {
                            Log.d("snaapshot1", dataSnapshot.getKey());

                            peaple.add(dataSnapshot.getKey());
                        }
                        progressDialog.dismiss();
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

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


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

        void setDetails(final Context ctx, String userName, String userStatus, final String userImage) {

            TextView user_name = mView.findViewById(R.id.user_single_name);
            TextView user_status = mView.findViewById(R.id.user_single_status);
            final ImageView user_image = mView.findViewById(R.id.user_single_image);


            user_name.setText(userName);
            user_status.setText(userStatus);

            Picasso.get().load(userImage).placeholder(R.drawable.avatar).into(user_image);


        }




    }


}
