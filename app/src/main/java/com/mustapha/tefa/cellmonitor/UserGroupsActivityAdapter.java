package com.mustapha.tefa.cellmonitor;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;



public class UserGroupsActivityAdapter extends RecyclerView.Adapter<UserGroupsActivityAdapter.ViewHolder> {

    private List<String> mSectionsList;
    private Context ctx;
    private String mFriendID;
    private FirebaseAuth mAuth;

    UserGroupsActivityAdapter(List<String> mUsersList, Context ctx , String mFriendID) {

        this.mSectionsList = mUsersList;
        this.ctx = ctx;
        this.mFriendID = mFriendID;

    }



    @NonNull
    @Override
    public UserGroupsActivityAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_single_layout ,parent, false);

        return new UserGroupsActivityAdapter.ViewHolder(v);

    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView status;
        CircleImageView profileImage;
        TextView displayName;
        ImageView messageImage;
        TextView messageTime;

        ViewHolder(View view) {
            super(view);

            messageTime = view.findViewById(R.id.time_text_layout);
            status = view.findViewById(R.id.user_single_status);
            profileImage = view.findViewById(R.id.user_single_image);
            displayName = view.findViewById(R.id.user_single_name);
            messageImage = view.findViewById(R.id.message_image_layout);

        }
    }

    @Override
    public void onBindViewHolder(@NonNull final UserGroupsActivityAdapter.ViewHolder viewHolder, int i) {

        final String section_id = mSectionsList.get(i);

        mAuth = FirebaseAuth.getInstance();


        DatabaseReference mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Sections").child(section_id);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                final DatabaseReference mUserSectionsDatabase2 = FirebaseDatabase.getInstance().getReference("User_Section");
                String name = dataSnapshot.child("name").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                long status = (long) dataSnapshot.child("CreationTime").getValue();


                @SuppressLint("SimpleDateFormat") SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd'\n'hh:mm a");
                String time = sfd.format(new Date(status));

                viewHolder.displayName.setText(name);
                viewHolder.status.setText(time);

                Picasso.get().load(image).placeholder(R.drawable.group_avatar).into(viewHolder.profileImage);

                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final ProgressDialog progressDialog = new ProgressDialog(ctx);
                        progressDialog.setMessage("Please wait while Adding your friend to the group ..");
                        progressDialog.show();


                        Log.d("JohnSectionKey :",section_id);
                        Log.d("JohnFriendID :" , mFriendID);
                        Log.d("JohnCurrentUid :",mAuth.getCurrentUser().getUid());

                        final Map userSectionMap = new HashMap();
                        userSectionMap.put("AddedTime", ServerValue.TIMESTAMP);
                        userSectionMap.put("AddedBy",mAuth.getCurrentUser().getUid());
                        mUserSectionsDatabase2.child(section_id).child(mFriendID).updateChildren(userSectionMap).addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {

                                if (task.isSuccessful()){

                                    mUserSectionsDatabase2.child(mFriendID).child(section_id).child("AddedTime").setValue(ServerValue.TIMESTAMP);
                                    mUserSectionsDatabase2.child(mFriendID).child(section_id).child("AddedBy").setValue(mAuth.getCurrentUser().getUid());

                                    progressDialog.dismiss();
                                    Toast.makeText(ctx, "Your friend has been added successfully !", Toast.LENGTH_SHORT).show();

                                } else {
                                    progressDialog.dismiss();
                                    Toast.makeText(ctx, "Something went wrong .. Please try again !", Toast.LENGTH_SHORT).show();

                                }

                            }
                        });

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    @Override
    public int getItemCount() {
        return mSectionsList.size();
    }














}
