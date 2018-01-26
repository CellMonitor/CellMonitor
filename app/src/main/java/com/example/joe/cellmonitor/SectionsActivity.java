package com.example.joe.cellmonitor;


import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class SectionsActivity extends AppCompatActivity {


    private TextInputLayout textInputLayout;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sections);

        textInputLayout = findViewById(R.id.secName);
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Sections");
        mAuth = FirebaseAuth.getInstance();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String sectionName = textInputLayout.getEditText().toString();
                if (!TextUtils.isEmpty(sectionName)){

                    databaseReference.child(mAuth.getCurrentUser().getUid()).child(sectionName);

                }

            }
        });


    }
}