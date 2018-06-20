package com.mustapha.tefa.cellmonitor;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;
import com.mustapha.tefa.cellmonitor.models.Users;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ChildrenSearch extends AppCompatActivity {


    private EditText mSearchField;
    private ImageButton mSearchBtn;

    private RecyclerView mResultList;

    private DatabaseReference mUserDatabase,allDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_children_search);

        Toolbar mUsersToolbar = findViewById(R.id.children2_app_bar);
        setSupportActionBar(mUsersToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Children Search ..");

        actionBar.setDisplayHomeAsUpEnabled(true);


        mUserDatabase = FirebaseDatabase.getInstance().getReference("Children");
        allDatabase = FirebaseDatabase.getInstance().getReference();

        mSearchField = findViewById(R.id.search_field);
        mSearchBtn = findViewById(R.id.search_btn);

        mResultList = findViewById(R.id.result_list);
        mResultList.setHasFixedSize(true);
        mResultList.setLayoutManager(new LinearLayoutManager(this));

        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String searchText = mSearchField.getText().toString();

                firebaseUserSearch(searchText);

            }
        });

    }

    private void firebaseUserSearch(String searchText) {

        Toast.makeText(ChildrenSearch.this, "Started Search", Toast.LENGTH_LONG).show();

        Query firebaseSearchQuery = mUserDatabase.orderByChild("child_code").equalTo(searchText);

        FirebaseRecyclerOptions<Users> options = new FirebaseRecyclerOptions.Builder<Users>()
                .setQuery(firebaseSearchQuery, Users.class)
                .setLifecycleOwner(this)
                .build();

        FirebaseRecyclerAdapter<Users, ChildrenViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, ChildrenViewHolder>(options) {
            @NonNull
            @Override
            public ChildrenViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_single_layout, parent, false);

                return new ChildrenViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ChildrenViewHolder holder, int position, @NonNull Users model) {
                final String child_id = getRef(position).getKey();
                final String current_user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

                holder.setDetails(getApplicationContext(), model.getName(), model.getStatus(), model.getImage());
                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ChildrenSearch.this);
                        final EditText input = new EditText(ChildrenSearch.this);
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT);
                        input.setLayoutParams(lp);

                        builder.setMessage("Are you sure this is your Child ? , if yes , Please fill the secure code.")
                                .setCancelable(true)
                                .setView(input)
                                .setPositiveButton("Yes.", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        //do things
                                        String secure_code = input.getText().toString();
                                        if (!TextUtils.isEmpty(secure_code)) {

                                            mUserDatabase.child(child_id).child("Secure_Code").setValue(secure_code);
                                            final String currentDate = DateFormat.getDateInstance().format(new Date());

                                            Map friendsMap = new HashMap();
                                            friendsMap.put("Family/" + current_user_id + "/" + child_id + "/date", currentDate);
                                            friendsMap.put("Family/" + child_id + "/" + current_user_id + "/date", currentDate);


                                            allDatabase.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                                                @Override
                                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                    if (databaseError == null) {

                                                        Toast.makeText(ChildrenSearch.this, "Child Add Successfully !", Toast.LENGTH_SHORT).show();
                                                    }

                                                }
                                            });
                                        } else {
                                            Toast.makeText(ChildrenSearch.this,"Please fill the secure code !",Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                });
            }


        };

        mResultList.setAdapter(firebaseRecyclerAdapter);

    }


    // View Holder Class

    public static class ChildrenViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public ChildrenViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }

        public void setDetails(Context ctx, String userName, String userStatus, String userImage){

            TextView user_name = mView.findViewById(R.id.user_single_name);
            TextView user_status = mView.findViewById(R.id.user_single_status);
            final ImageView user_image = mView.findViewById(R.id.user_single_image);


            user_name.setText(userName);
            user_status.setText(userStatus);

            Picasso.get().load(userImage).placeholder(R.drawable.avatar).into(user_image);


        }




    }

}
