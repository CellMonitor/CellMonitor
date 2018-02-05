package com.youssif.joe.weapp;


import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
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


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private RecyclerView mConvList;

    private DatabaseReference mConvDatabase;
    private DatabaseReference mMessageDatabase;
    private DatabaseReference mUsersDatabase;

    private FirebaseAuth mAuth;


    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View mMainView = inflater.inflate(R.layout.fragment_chats, container, false);

        mConvList = mMainView.findViewById(R.id.conv_list);
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null){
            Intent intent = new Intent(getContext(),LoginActivity.class);
            startActivity(intent);
        }else{
            String mCurrent_user_id = mAuth.getCurrentUser().getUid();

            mConvDatabase = FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurrent_user_id);

            mConvDatabase.keepSynced(true);
            mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
            mMessageDatabase = FirebaseDatabase.getInstance().getReference().child("messages").child(mCurrent_user_id);
            mUsersDatabase.keepSynced(true);
        }


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mConvList.setHasFixedSize(true);
        mConvList.setLayoutManager(linearLayoutManager);


        // Inflate the layout for this fragment
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


        }

        else {

            Query conversationQuery = mConvDatabase.orderByChild("timestamp");


            FirebaseRecyclerOptions<Conv> options = new FirebaseRecyclerOptions.Builder<Conv>()
                    .setQuery(conversationQuery, Conv.class)
                    .setLifecycleOwner(this)
                    .build();
            FirebaseRecyclerAdapter<Conv, ConvViewHolder> firebaseConvAdapter = new FirebaseRecyclerAdapter<Conv, ConvViewHolder>(options) {

                @Override
                public ConvViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_single_layout, parent, false);

                    return new ConvViewHolder(view);
                }


                @Override
                protected void onBindViewHolder(@NonNull final ConvViewHolder convViewHolder, int position, @NonNull final Conv conv) {
                    final String list_user_id = getRef(position).getKey();

                    Query lastMessageQuery = mMessageDatabase.child(list_user_id).limitToLast(1);

                    lastMessageQuery.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                            if (dataSnapshot.hasChild("message")) {
                                String data = dataSnapshot.child("message").getValue().toString();
                                convViewHolder.setMessage(data, conv.isSeen());
                            } else {
                                convViewHolder.setMessage("",conv.isSeen());
                            }
                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                    mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            final String userName = dataSnapshot.child("name").getValue().toString();
                            String userThumb = dataSnapshot.child("thumb_image").getValue().toString();

                            if (dataSnapshot.hasChild("online")) {

                                String userOnline = dataSnapshot.child("online").getValue().toString();
                                convViewHolder.setUserOnline(userOnline);

                            }

                            convViewHolder.setName(userName);
                            convViewHolder.setUserImage(userThumb, getContext());

                            convViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {


                                    Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                    chatIntent.putExtra("user_id", list_user_id);
                                    chatIntent.putExtra("user_name", userName);
                                    startActivity(chatIntent);

                                }
                            });


                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
            };

            mConvList.setAdapter(firebaseConvAdapter);
        }

    }

    public static class ConvViewHolder extends RecyclerView.ViewHolder {

        View mView;

        ConvViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }

        public void setMessage(String message, boolean isSeen){

            TextView userStatusView = (TextView) mView.findViewById(R.id.user_single_status);

            if (message!=null) {

                if (!message.startsWith("https://firebasestorage.googleapis.com/v0/b/cellmonitor-644a8.appspot.com/o/message_images")) {
                    userStatusView.setText(message);

                    if (!isSeen) {
                        userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.BOLD);
                    } else {
                        userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.NORMAL);
                    }
                } else {
                    userStatusView.setText("Photo");
                }
            } else {
                userStatusView.setText("");
            }

        }

        public void setName(String name){

            TextView userNameView = mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);

        }

        void setUserImage(final String thumb_image, final Context ctx){

            final CircleImageView userImageView = mView.findViewById(R.id.user_single_image);
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