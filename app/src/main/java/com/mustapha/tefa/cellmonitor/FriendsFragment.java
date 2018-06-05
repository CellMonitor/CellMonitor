package com.mustapha.tefa.cellmonitor;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mustapha.tefa.cellmonitor.models.Friends;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    private RecyclerView mFriendsList;

    private FirebaseAuth mAuth;
    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mUsersDatabase;

    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mMainView = inflater.inflate(R.layout.fragment_friends, container, false);

        mAuth = FirebaseAuth.getInstance();


        FloatingActionButton fab = mMainView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), UsersActivity.class);
                startActivity(intent);

            }
        });

        mFriendsList = mMainView.findViewById(R.id.friends_list);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null){
            Intent intent = new Intent(getContext(),LoginActivity.class);
            startActivity(intent);
        }else {
            String mCurrent_user_id = mAuth.getCurrentUser().getUid();

            mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_user_id);
            mFriendsDatabase.keepSynced(true);
            mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
            mUsersDatabase.keepSynced(true);

        }

        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));


        return mMainView;

    }


    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();



        if (currentUser == null) {


            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);


        } else {

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
                    Log.d("list_user_id : ", list_user_id);

                    mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Log.d("DataSnapShot2 : " , dataSnapshot.toString());

                            final String userName = dataSnapshot.child("name").getValue().toString();
                            String userThumb = dataSnapshot.child("thumb_image").getValue().toString();
                            String status = dataSnapshot.child("status").getValue().toString();

                            if (dataSnapshot.hasChild("online")) {

                                String userOnline = dataSnapshot.child("online").getValue().toString();
                                friendsViewHolder.setUserOnline(userOnline);

                            }


                            friendsViewHolder.setName(userName);
                            friendsViewHolder.setStatus(status);
                            friendsViewHolder.setUserImage(userThumb, getContext());

                            friendsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    CharSequence options[] = new CharSequence[]{userName + "'s Profile", "Send message" , "Add to Group"};

                                    final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                                    builder.setTitle("Select Options");
                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                            //Click Event for each item.
                                            if (i == 0) {

                                                Intent profileIntent = new Intent(getContext(), UserProfileActivity.class);
                                                profileIntent.putExtra("user_id", list_user_id);
                                                startActivity(profileIntent);

                                            }

                                            if (i == 1) {

                                                Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                                chatIntent.putExtra("user_id", list_user_id);
                                                chatIntent.putExtra("user_name", userName);
                                                startActivity(chatIntent);

                                            }
                                            if (i == 2) {

                                                Intent groupsIntent = new Intent(getContext(),UserGroupsActivity.class);
                                                groupsIntent.putExtra("user_id",list_user_id);
                                                startActivity(groupsIntent);

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
            Picasso.get().load(thumb_image).placeholder(R.drawable.avatar).into(userImageView);


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
