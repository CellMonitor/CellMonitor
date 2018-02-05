package com.youssif.joe.weapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class ProfileActivity extends AppCompatActivity {

    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;


    private static final int GALLERY_PICK = 1;

    //layout
    private CircleImageView mDisplayImage;
    private TextView mName;
    private TextView mStatus;

    private StorageReference mImageStorage;

    private ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mDisplayImage = findViewById(R.id.profile_pic);
        mName = findViewById(R.id.displayName);
        mStatus = findViewById(R.id.status);
        Button mStatusBtn = findViewById(R.id.settings_status_btn);
        Button mImageBtn = findViewById(R.id.settings_img_btn);
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mImageStorage = FirebaseStorage.getInstance().getReference();


        String current_uid = mCurrentUser.getUid();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
        mUserDatabase.keepSynced(true);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();


                mName.setText(name);
                mStatus.setText(status);

                if (!image.equals("default")) {

                    //Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.avatar).into(mDisplayImage);
                    Picasso.with(ProfileActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.avatar).into(mDisplayImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.avatar).into(mDisplayImage);
                        }
                    });

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String status_value = mStatus.getText().toString();
                Intent intent = new Intent(ProfileActivity.this, StatusActivity.class);
                intent.putExtra("status_value", status_value);
                startActivity(intent);
            }
        });
        mImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent, "Select Image"), GALLERY_PICK);

            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();


            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .start(this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mProgressDialog = new ProgressDialog(ProfileActivity.this);
                mProgressDialog.setTitle("Uploading Image ..");
                mProgressDialog.setMessage("Please wait while upload and process the image :) <3");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                Uri resultUri = result.getUri();
                final File thumb_filePath = new File(resultUri.getPath());
                String current_user_id = mCurrentUser.getUid();


                Bitmap thumb_bitmap = null;
                try {
                    thumb_bitmap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(75)
                            .compressToBitmap(thumb_filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
                final byte[] thumb_byte = baos.toByteArray();


                final StorageReference thumb_filepath = mImageStorage.child("profile_images").child("thumbs").child(current_user_id +".jpg" );
                StorageReference filepath = mImageStorage.child("profile_images").child(current_user_id + ".jpg");
                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            final String download_url = task.getResult().getDownloadUrl().toString();

                            UploadTask uploadTask = thumb_filepath.putBytes(thumb_byte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                                    String thumb_downloadUrl = thumb_task.getResult().getDownloadUrl().toString();

                                    if (thumb_task.isSuccessful()){

                                        Map update_hashMap = new HashMap();
                                        update_hashMap.put("image",download_url);
                                        update_hashMap.put("thumb_image",thumb_downloadUrl);

                                        mUserDatabase.updateChildren(update_hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(ProfileActivity.this,"Success Uploading", Toast.LENGTH_SHORT).show();
                                                    mProgressDialog.dismiss();
                                                }
                                            }
                                        });

                                    }
                                    else {
                                        Toast.makeText(ProfileActivity.this,"Error uploading thumbnail",Toast.LENGTH_SHORT).show();
                                        mProgressDialog.dismiss();

                                    }

                                }
                            });


                        } else {
                            mProgressDialog.dismiss();
                        }

                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.e("upload image error :", error.toString());
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();


        if (FirebaseAuth.getInstance().getCurrentUser() != null) {


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


        } else {

            Intent intent = new Intent(this,LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);

        }






    }




}




