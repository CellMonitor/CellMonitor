package com.example.joe.cellmonitor;


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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.joe.cellmonitor.models.Friends;
import com.example.joe.cellmonitor.models.Sections;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class SectionsFragment extends Fragment {


    private FirebaseAuth mAuth;
    private List<Sections> mSectionList;
    private DatabaseReference mUserSectionDatabase,mSectionsDatabase ;
    private RecyclerView recyclerView;
    String currentUserID ;


    public SectionsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();

        View mMainView = inflater.inflate(R.layout.fragment_sections, container, false);


        currentUserID = mAuth.getCurrentUser().getUid();
        mUserSectionDatabase = FirebaseDatabase.getInstance().getReference("User_Section").child(currentUserID);
        mSectionsDatabase = FirebaseDatabase.getInstance().getReference("Sections");
        recyclerView = mMainView.findViewById(R.id.secList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        FloatingActionButton fab = mMainView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SectionsActivity.class);
                startActivity(intent);

            }
        });


        // Inflate the layout for this fragment
        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();
        final FirebaseUser currentUser = mAuth.getCurrentUser();


        if (currentUser == null) {


            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);


        }



        FirebaseRecyclerOptions<Sections> options = new FirebaseRecyclerOptions.Builder<Sections>()
                .setQuery(mUserSectionDatabase, Sections.class)
                .setLifecycleOwner(this)
                .build();

        FirebaseRecyclerAdapter<Sections, SectionsViewHolder> sectionsRecyclerViewAdapter = new FirebaseRecyclerAdapter<Sections, SectionsViewHolder>(options) {

            @Override
            public SectionsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_single_layout, parent, false);

                return new SectionsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final SectionsViewHolder sectionsViewHolder, int position, @NonNull Sections sections) {

                final String sectionKey = getRef(position).getKey();

                mSectionsDatabase.child(sectionKey).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot != null) {
                            final String sectionName = dataSnapshot.child("name").getValue().toString();
                            String sectionImage = dataSnapshot.child("image").getValue().toString();
                            long sectionCreationDate = (long) dataSnapshot.child("CreationTime").getValue();


                            sectionsViewHolder.setName(sectionName);
                            sectionsViewHolder.setTimeStamp(sectionCreationDate);
                            sectionsViewHolder.setSectionImage(sectionImage, getContext());

                        /*
                        friendsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                CharSequence options[] = new CharSequence[]{userName + "'s Profile", "Send message"};

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

                                    }
                                });

                                builder.show();

                            }
                        });
                        */


                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }


        };
        recyclerView.setAdapter(sectionsRecyclerViewAdapter);







    }

    public static class SectionsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        SectionsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }


        public void setName(String name) {

            TextView userNameView = mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);

        }

        public void setTimeStamp(long timeStamp) {

            SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd'\n'hh:mm a");
            String time = sfd.format(new Date(timeStamp));
            TextView userStatusView = mView.findViewById(R.id.user_single_status);
            userStatusView.setText(time);

        }

        void setSectionImage(final String thumb_image, final Context ctx) {

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
