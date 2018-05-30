package com.youssif.joe.weapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Handler;
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
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class ChildHomeActivity extends AppCompatActivity {

    private DatabaseReference mChildRef;
    private FirebaseUser mCurrentUser;


    private static final int GALLERY_PICK = 1;

    //layout
    private CircleImageView mDisplayImage;
    private TextView mName,mChildCode;

    private StorageReference mImageStorage;

    private ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_home);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mChildRef = FirebaseDatabase.getInstance().getReference().child("Children").child(mAuth.getCurrentUser().getUid());
        mChildRef.keepSynced(true);

        mDisplayImage = findViewById(R.id.profile_pic);
        mChildCode = findViewById(R.id.child_code);
        mName = findViewById(R.id.displayName);
        Button mImageBtn = findViewById(R.id.settings_img_btn);
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mImageStorage = FirebaseStorage.getInstance().getReference();

        mChildRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String code = dataSnapshot.child("child_code").getValue().toString();


                mChildCode.setText(code);
                mName.setText(name);


                if (!image.equals("default")) {

                    //Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.avatar).into(mDisplayImage);
                    Picasso.get().load(image)
                            .placeholder(R.drawable.avatar).into(mDisplayImage);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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

                mProgressDialog = new ProgressDialog(ChildHomeActivity.this);
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

                                        mChildRef.updateChildren(update_hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(ChildHomeActivity.this,"Success Uploading", Toast.LENGTH_SHORT).show();
                                                    mProgressDialog.dismiss();
                                                }
                                            }
                                        });

                                    }
                                    else {
                                        Toast.makeText(ChildHomeActivity.this,"Error uploading thumbnail",Toast.LENGTH_SHORT).show();
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

        generateLocation();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                generateLocation();
                handler.postDelayed(this, 120000); //now is every 2 minutes
            }
        }, 120000); //Every 120000 ms (2 minutes)

    }

    @Override
    protected void onRestart() {
        super.onRestart();

        generateLocation();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                generateLocation();
                handler.postDelayed(this, 120000); //now is every 2 minutes
            }
        }, 120000); //Every 120000 ms (2 minutes)


    }

    @Override
    protected void onPause() {
        super.onPause();

        generateLocation();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                generateLocation();
                handler.postDelayed(this, 120000); //now is every 2 minutes
            }
        }, 120000); //Every 120000 ms (2 minutes)

    }

    @Override
    protected void onStop() {
        super.onStop();

        generateLocation();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                generateLocation();
                handler.postDelayed(this, 120000); //now is every 2 minutes
            }
        }, 120000); //Every 120000 ms (2 minutes)


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        generateLocation();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                generateLocation();
                handler.postDelayed(this, 120000); //now is every 2 minutes
            }
        }, 120000); //Every 120000 ms (2 minutes)


    }


    private void generateLocation(){

        if (FirebaseAuth.getInstance().getCurrentUser() != null && FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() != null)  {

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
                                        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Children").child(mAuth.getCurrentUser().getUid());
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

    }

}
