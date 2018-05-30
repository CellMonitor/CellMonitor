package com.youssif.joe.weapp;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.UUID;


public class HomeActivity extends AppCompatActivity {


    private static final String TAG = "HomeActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mAuth = FirebaseAuth.getInstance();

        getSupportActionBar().setTitle("WeApp ❤");


        ViewPager mViewPager = findViewById(R.id.main_tabPager);
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(1);
        TabLayout mTabLayout = findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);

        if (mAuth.getCurrentUser() != null){

            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

        }






    }

    public boolean isServicesOK() {
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(HomeActivity.this);

        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(HomeActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_profile) {
            Intent profileIntent = new Intent(HomeActivity.this, ProfileActivity.class);
            startActivity(profileIntent);
        } else if (id == R.id.action_map) {
            if (isServicesOK()) {

                Intent intent = new Intent(HomeActivity.this, MapActivity.class);
                startActivity(intent);

            }
        } else if (id == R.id.action_logOut) {

            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
            mAuth.signOut();
            LoginManager.getInstance().logOut();
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
        } else if (id == R.id.action_children) {

            Intent childIntent = new Intent(HomeActivity.this,ChildrenActivity.class);
            startActivity(childIntent);

        }else if (id == R.id.action_requests) {

            Intent requestIntent = new Intent(HomeActivity.this,RequestActivity.class);
            startActivity(requestIntent);

        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();



        if (currentUser == null) {


            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            finish();

        } else if (FirebaseAuth.getInstance().getCurrentUser() != null && !FirebaseAuth.getInstance().getCurrentUser().getProviders().contains("phone") )  {

            mUserRef.child("online").setValue(true);
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

        } else if (FirebaseAuth.getInstance().getCurrentUser() != null && FirebaseAuth.getInstance().getCurrentUser().getProviders().contains("phone"))  {

            Intent childIntent = new Intent(HomeActivity.this, ChildHomeActivity.class);
            childIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            childIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            childIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(childIntent);
            finish();



        }
    }


}
