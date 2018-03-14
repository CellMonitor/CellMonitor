package com.youssif.joe.weapp;


import android.content.Context;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivityAdapter extends RecyclerView.Adapter<UsersActivityAdapter.ViewHolder>{


    private List<String> mUsersList;
    private Context ctx;

    UsersActivityAdapter(List<String> mUsersList, Context ctx) {

        this.mUsersList = mUsersList;
        this.ctx = ctx;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_single_layout ,parent, false);

        return new ViewHolder(v);

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
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {

        final String user_id = mUsersList.get(i);




        DatabaseReference mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                String name = dataSnapshot.child("name").getValue().toString();
                final String image = dataSnapshot.child("thumb_image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();

                viewHolder.displayName.setText(name);
                viewHolder.status.setText(status);

                Picasso.get().load(image).placeholder(R.drawable.avatar).into(viewHolder.profileImage);

                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(ctx, UserProfileActivity.class);
                        intent.putExtra("user_id", user_id);
                        ctx.startActivity(intent);

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
        return mUsersList.size();
    }






}