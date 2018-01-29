package com.example.joe.cellmonitor;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;


import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SectionProfileActivity extends AppCompatActivity {

    private DatabaseReference mSectionDatabase;
    private String sectionKey;


    private static final int GALLERY_PICK = 1;

    //layout
    private CircleImageView mDisplayImage;
    private TextView mName;
    private Button mImageBtn, mTrackBtn;
    private RecyclerView membersRecyclerView;
    private DatabaseReference mSectuonsMembers, mUsersDatabase;

    private StorageReference mImageStorage;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_section_profile);

        membersRecyclerView = findViewById(R.id.membersList);
        membersRecyclerView.setHasFixedSize(true);
        membersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mDisplayImage = findViewById(R.id.section_profile_pic);
        mName = findViewById(R.id.section_displayName);
        mImageBtn = findViewById(R.id.section_settings_img_btn);
        mImageStorage = FirebaseStorage.getInstance().getReference();
        sectionKey = getIntent().getStringExtra("section_key");
        mTrackBtn = findViewById(R.id.section_track_all);
        mTrackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SectionProfileActivity.this, MapActivity.class);
                intent.putExtra("sectionKey", sectionKey);
                startActivity(intent);
            }
        });


        mUsersDatabase = FirebaseDatabase.getInstance().getReference("Users");
        mUsersDatabase.keepSynced(true);
        mSectuonsMembers = FirebaseDatabase.getInstance().getReference("User_Section").child(sectionKey);
        mSectuonsMembers.keepSynced(true);
        mSectionDatabase = FirebaseDatabase.getInstance().getReference().child("Sections").child(sectionKey);
        mSectionDatabase.keepSynced(true);

        mSectionDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();


                mName.setText(name);


                if (!image.equals("default")) {


                    Picasso.with(SectionProfileActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.avatar).into(mDisplayImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(SectionProfileActivity.this).load(image).placeholder(R.drawable.avatar).into(mDisplayImage);
                        }
                    });

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

                mProgressDialog = new ProgressDialog(SectionProfileActivity.this);
                mProgressDialog.setTitle("Uploading Image ..");
                mProgressDialog.setMessage("Please wait while upload and process the image :) <3");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                Uri resultUri = result.getUri();


                StorageReference filepath = mImageStorage.child("profile_images").child(sectionKey + ".jpg");
                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            final String download_url = task.getResult().getDownloadUrl().toString();
                            Map update_hashMap = new HashMap();
                            update_hashMap.put("image", download_url);
                            mSectionDatabase.updateChildren(update_hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(SectionProfileActivity.this, "Success Uploading", Toast.LENGTH_SHORT).show();
                                        mProgressDialog.dismiss();
                                    } else {
                                        Toast.makeText(SectionProfileActivity.this, "Something went wrong .. Please try again !", Toast.LENGTH_SHORT).show();
                                        mProgressDialog.dismiss();
                                    }
                                }
                            });

                        } else {
                            Toast.makeText(SectionProfileActivity.this, "Something went wrong .. Please try again !", Toast.LENGTH_SHORT).show();
                            mProgressDialog.dismiss();
                        }

                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, "Error :" + error, Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    protected void onStart() {
        super.onStart();


        FirebaseRecyclerOptions<Users> options = new FirebaseRecyclerOptions.Builder<Users>()
                .setQuery(mSectuonsMembers, Users.class)
                .setLifecycleOwner(this)
                .build();

        FirebaseRecyclerAdapter<Users, membersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, membersViewHolder>(options) {

            @Override
            public membersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_single_layout, parent, false);

                return new membersViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final membersViewHolder holder, int position, @NonNull Users model) {


                final String list_user_id = getRef(position).getKey();
                Log.d("list_user_id : ", list_user_id);

                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d("DataSnapShot2 : ", dataSnapshot.toString());

                        final String userName = dataSnapshot.child("name").getValue().toString();
                        String userThumb = dataSnapshot.child("thumb_image").getValue().toString();
                        String status = dataSnapshot.child("status").getValue().toString();


                        holder.setName(userName);
                        holder.setStatus(status);
                        holder.setUserImage(userThumb, SectionProfileActivity.this);

                        holder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                CharSequence options[] = new CharSequence[]{userName + "'s Profile", "Send message"};

                                final AlertDialog.Builder builder = new AlertDialog.Builder(SectionProfileActivity.this);

                                builder.setTitle("Select Options");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        //Click Event for each item.
                                        if (i == 0) {

                                            Intent profileIntent = new Intent(SectionProfileActivity.this, UserProfileActivity.class);
                                            profileIntent.putExtra("user_id", list_user_id);
                                            profileIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(profileIntent);
                                            finish();

                                        }

                                        if (i == 1) {

                                            Intent chatIntent = new Intent(SectionProfileActivity.this, ChatActivity.class);
                                            chatIntent.putExtra("user_id", list_user_id);
                                            chatIntent.putExtra("user_name", userName);
                                            startActivity(chatIntent);

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
        membersRecyclerView.setAdapter(firebaseRecyclerAdapter);


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

            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);

        }


    }

    public static class membersViewHolder extends RecyclerView.ViewHolder {

        View mView;

        membersViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }


        public void setName(String name) {

            TextView userNameView = mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);

        }

        public void setStatus(String status) {
            TextView userStatusView = mView.findViewById(R.id.user_single_status);
            userStatusView.setText(status);

        }

        void setUserImage(final String thumb_image, final Context ctx) {

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

