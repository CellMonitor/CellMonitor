package com.example.joe.cellmonitor;


import android.app.ProgressDialog;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.example.joe.cellmonitor.models.Users;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;



import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity  {

    public DatabaseReference databaseReference;

    ProgressDialog progressDialog;

    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

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


        Query firebaseSearchQuery = databaseReference.orderByChild("name");
        FirebaseRecyclerOptions<Users> options = new FirebaseRecyclerOptions.Builder<Users>()
                .setQuery(firebaseSearchQuery,Users.class)
                .setLifecycleOwner(this)
                .build();
        FirebaseRecyclerAdapter<Users,ViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, ViewHolder>(options) {

            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_single_layout, parent, false);

                return new ViewHolder(view);
            }
            @Override
            protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Users model) {

                holder.setDisplayName(model.getName());
                holder.setUserStatus(model.getStatus());
                holder.setUserImage(model.getThumb_image(),getApplicationContext());
                progressDialog.dismiss();
                final String user_id = getRef(position).getKey();

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(UsersActivity.this,UserProfileActivity.class);
                        intent.putExtra("user_id",user_id);
                        startActivity(intent);
                    }
                });

            }


        };
        recyclerView.setAdapter(firebaseRecyclerAdapter);


    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        View mView;

        ViewHolder(View itemView) {

            super(itemView);

            mView = itemView;


        }

        void setDisplayName(String name){
            TextView userNameView = mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);

        }
        void setUserStatus(String status){
            TextView userStatusView = mView.findViewById(R.id.user_single_status);
            userStatusView.setText(status);

        }
        void setUserImage(String thumb_image, Context ctx){
            CircleImageView userImageView = mView.findViewById(R.id.user_single_image);
            Picasso.with(ctx).load(thumb_image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.avatar).into(userImageView);
        }


    }
}
