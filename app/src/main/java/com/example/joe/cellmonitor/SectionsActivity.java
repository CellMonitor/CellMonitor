package com.example.joe.cellmonitor;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.joe.cellmonitor.models.Friends;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;


import de.hdodenhof.circleimageview.CircleImageView;

public class SectionsActivity extends AppCompatActivity {

    private RecyclerView mFriendsList;
    private FirebaseAuth mAuth;
    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mUsersDatabase;
    private ImageButton mSearchBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sections);

        mAuth = FirebaseAuth.getInstance();
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

            }
        });


        mSearchBtn = (ImageButton) findViewById(R.id.search_btn);
        final EditText editText = findViewById(R.id.searchEditText);

        mFriendsList = findViewById(R.id.recyclerFriends);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null){
            Intent intent = new Intent(SectionsActivity.this,LoginActivity.class);
            startActivity(intent);
        }else {
            String mCurrent_user_id = mAuth.getCurrentUser().getUid();

            mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_user_id);
            mFriendsDatabase.keepSynced(true);
            mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
            mUsersDatabase.keepSynced(true);

        }

        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(this));

        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                 String searchText = editText.getText().toString();

                firebaseUserSearch(searchText);

            }
        });


    }

    private void firebaseUserSearch(String searchText) {

        Query firebaseSearchQuery = mUsersDatabase.orderByChild("name").startAt(searchText).endAt(searchText + "\uf8ff");

        FirebaseRecyclerOptions<Friends> options = new FirebaseRecyclerOptions.Builder<Friends>()
                .setQuery(firebaseSearchQuery, Friends.class)
                .setLifecycleOwner(this)
                .build();

        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> friendsRecyclerViewAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(options) {

            @Override
            public FriendsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_single_layout, parent, false);

                return new FriendsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder friendsViewHolder, int position, @NonNull Friends friends) {


                final String list_user_id = getRef(position).getKey();

                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String userName = dataSnapshot.child("name").getValue().toString();
                        String userThumb = dataSnapshot.child("thumb_image").getValue().toString();
                        String status = dataSnapshot.child("status").getValue().toString();

                        if (dataSnapshot.hasChild("online")) {

                            String userOnline = dataSnapshot.child("online").getValue().toString();
                            friendsViewHolder.setUserOnline(userOnline);

                        }


                        friendsViewHolder.setName(userName);
                        friendsViewHolder.setStatus(status);
                        friendsViewHolder.setUserImage(userThumb, SectionsActivity.this);

                        friendsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                CharSequence options[] = new CharSequence[]{userName + "'s Profile", "Send message"};

                                final AlertDialog.Builder builder = new AlertDialog.Builder(SectionsActivity.this);

                                builder.setTitle("Select Options");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        //Click Event for each item.
                                        if (i == 0) {

                                            Intent profileIntent = new Intent(SectionsActivity.this, UserProfileActivity.class);
                                            profileIntent.putExtra("user_id", list_user_id);
                                            startActivity(profileIntent);

                                        }

                                        if (i == 1) {

                                            Intent chatIntent = new Intent(SectionsActivity.this, ChatActivity.class);
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
        mFriendsList.setAdapter(friendsRecyclerViewAdapter);
    }


    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();



        if (currentUser == null) {


            Intent intent = new Intent(SectionsActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);


        } else {

            
            //Query firebaseSearchQuery = mUsersDatabase.orderByChild("name").startAt(searchText).endAt(searchText + "\uf8ff");

            FirebaseRecyclerOptions<Friends> options = new FirebaseRecyclerOptions.Builder<Friends>()
                    .setQuery(mFriendsDatabase, Friends.class)
                    .setLifecycleOwner(this)
                    .build();

            FirebaseRecyclerAdapter<Friends, FriendsViewHolder> friendsRecyclerViewAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(options) {

                @Override
                public FriendsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_single_layout, parent, false);

                    return new FriendsViewHolder(view);
                }

                @Override
                protected void onBindViewHolder(@NonNull final FriendsViewHolder friendsViewHolder, int position, @NonNull Friends friends) {


                    final String list_user_id = getRef(position).getKey();

                    mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            final String userName = dataSnapshot.child("name").getValue().toString();
                            String userThumb = dataSnapshot.child("thumb_image").getValue().toString();
                            String status = dataSnapshot.child("status").getValue().toString();

                            if (dataSnapshot.hasChild("online")) {

                                String userOnline = dataSnapshot.child("online").getValue().toString();
                                friendsViewHolder.setUserOnline(userOnline);

                            }


                            friendsViewHolder.setName(userName);
                            friendsViewHolder.setStatus(status);
                            friendsViewHolder.setUserImage(userThumb, SectionsActivity.this);

                            friendsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    CharSequence options[] = new CharSequence[]{userName + "'s Profile", "Send message"};

                                    final AlertDialog.Builder builder = new AlertDialog.Builder(SectionsActivity.this);

                                    builder.setTitle("Select Options");
                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                            //Click Event for each item.
                                            if (i == 0) {

                                                Intent profileIntent = new Intent(SectionsActivity.this, UserProfileActivity.class);
                                                profileIntent.putExtra("user_id", list_user_id);
                                                startActivity(profileIntent);

                                            }

                                            if (i == 1) {

                                                Intent chatIntent = new Intent(SectionsActivity.this, ChatActivity.class);
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
            mFriendsList.setAdapter(friendsRecyclerViewAdapter);


        }


    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        FriendsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }



        public void setName(String name) {

            TextView userNameView = mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);

        }

        public void setStatus(String status){
            TextView userStatusView =  mView.findViewById(R.id.user_single_status);
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
        void setUserOnline(String online_status) {

            ImageView userOnlineView = mView.findViewById(R.id.user_single_online_icon);

            if(online_status.equals("true")){

                userOnlineView.setVisibility(View.VISIBLE);

            } else {

                userOnlineView.setVisibility(View.INVISIBLE);

            }

        }



    }
}
