package com.example.joe.cellmonitor;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.joe.cellmonitor.models.Requests;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestActivity extends AppCompatActivity {

    private RecyclerView myRequestList;
    private DatabaseReference friendRequestRef;
    private FirebaseAuth mAuth;
    String online_user_id;
    private DatabaseReference usersRef;
    private DatabaseReference friendsDatabaseRef;
    private DatabaseReference friendsReqDatabaseRef;
    private DatabaseReference allDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);

        myRequestList = findViewById(R.id.request_list);
        mAuth = FirebaseAuth.getInstance();
        online_user_id = mAuth.getCurrentUser().getUid();
        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("Friend_req").child(online_user_id);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        friendsDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        friendsReqDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        allDatabase = FirebaseDatabase.getInstance().getReference();
        myRequestList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);


        myRequestList.setLayoutManager(linearLayoutManager);


    }



    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Requests> options = new FirebaseRecyclerOptions.Builder<Requests>()
                .setQuery(friendRequestRef, Requests.class)
                .setLifecycleOwner(this)
                .build();

        FirebaseRecyclerAdapter<Requests,RequestViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Requests, RequestViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestViewHolder holder, int position, @NonNull Requests model) {

                final String list_users_id = getRef(position).getKey();
                DatabaseReference get_type_ref = getRef(position).child("request_type").getRef();

                get_type_ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()){
                            String request_type = dataSnapshot.getValue().toString();

                            if (request_type.equals("received")){

                                usersRef.child(list_users_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        final String userName = dataSnapshot.child("name").getValue().toString();
                                        final String userThumb = dataSnapshot.child("thumb_image").getValue().toString();
                                        final String userStatus = dataSnapshot.child("status").getValue().toString();

                                        holder.setUserName(userName);
                                        holder.setThumb_image(userThumb , RequestActivity.this);
                                        holder.setUserStatus(userStatus);

                                        holder.mView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                CharSequence options[] = new CharSequence[]{"Accept Friend Request", "Cancel Friend Request"};

                                                final AlertDialog.Builder builder = new AlertDialog.Builder(RequestActivity.this);

                                                builder.setTitle("Friend Req Options");
                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {

                                                        //Click Event for each item.
                                                        if(i == 0){


                                                            final String currentDate = DateFormat.getDateInstance().format(new Date());

                                                            Map friendsMap = new HashMap();
                                                            friendsMap.put("Friends/" + online_user_id + "/" + list_users_id + "/data" , currentDate);
                                                            friendsMap.put("Friends/" + list_users_id + "/" + online_user_id + "/data" , currentDate);

                                                            friendsMap.put("Friend_req/" + online_user_id + "/" + list_users_id, null);
                                                            friendsMap.put("Friend_req/" + list_users_id + "/" + online_user_id, null);

                                                            allDatabase.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                                                                @Override
                                                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                                    if (databaseError == null){

                                                                        Toast.makeText(RequestActivity.this, "Friend Request Accepted Successfully !", Toast.LENGTH_SHORT).show();
                                                                    }

                                                                }
                                                            });


                                                        }

                                                        if(i == 1){


                                                            friendsReqDatabaseRef.child(online_user_id).child(list_users_id)
                                                                    .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {

                                                                    friendsReqDatabaseRef.child(list_users_id).child(online_user_id)
                                                                            .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            Toast.makeText(RequestActivity.this, "Friend Request Cancelled Successfully !", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    });

                                                                }
                                                            });

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

                            }else if (request_type.equals("sent")){


                                Button req_sent_btn = holder.mView.findViewById(R.id.request_accept_btn);
                                req_sent_btn.setText("Request Sent");

                                holder.mView.findViewById(R.id.request_decline_btn).setVisibility(View.INVISIBLE);

                                usersRef.child(list_users_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        final String userName = dataSnapshot.child("name").getValue().toString();
                                        final String userThumb = dataSnapshot.child("thumb_image").getValue().toString();
                                        final String userStatus = dataSnapshot.child("status").getValue().toString();

                                        holder.setUserName(userName);
                                        holder.setThumb_image(userThumb , RequestActivity.this);
                                        holder.setUserStatus(userStatus);


                                        holder.mView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {

                                                CharSequence options[] = new CharSequence[]{"Cancel Friend Request"};

                                                final AlertDialog.Builder builder = new AlertDialog.Builder(RequestActivity.this);

                                                builder.setTitle("Friend Request Sent");
                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {

                                                        //Click Event for each item.
                                                        if(i == 0){


                                                            friendsReqDatabaseRef.child(online_user_id).child(list_users_id)
                                                                    .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {

                                                                    friendsReqDatabaseRef.child(list_users_id).child(online_user_id)
                                                                            .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            Toast.makeText(RequestActivity.this, "Friend Request Cancelled Successfully !", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    });

                                                                }
                                                            });

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

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });



            }

            @Override
            public RequestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_request_all_users_layout, parent, false);

                return new RequestViewHolder(view);
            }
        };
        myRequestList.setAdapter(firebaseRecyclerAdapter);

    }



    public static class RequestViewHolder extends RecyclerView.ViewHolder{

        View mView;
        

        public RequestViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }

        public void setUserName(String userName) {

            TextView userDisplayName = mView.findViewById(R.id.request_profile_name);
            userDisplayName.setText(userName);

        }

        public void setThumb_image(final String userThumb , final Context ctx) {

            final CircleImageView thumb_image = mView.findViewById(R.id.request_profile_image);
            Picasso.with(ctx).load(userThumb).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.avatar).into(thumb_image, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {

                    Picasso.with(ctx).load(userThumb).placeholder(R.drawable.avatar).into(thumb_image);

                }
            });

        }

        public void setUserStatus(String status) {

            TextView userDisplayStatus = mView.findViewById(R.id.request_profile_status);
            userDisplayStatus.setText(status);

        }
    }
}
