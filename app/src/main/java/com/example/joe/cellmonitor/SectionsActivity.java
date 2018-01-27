package com.example.joe.cellmonitor;


import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;


public class SectionsActivity extends AppCompatActivity {


    private EditText secName;
    private DatabaseReference sectionReference,userSectionRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sections);

        secName = findViewById(R.id.secName);
        sectionReference = FirebaseDatabase.getInstance().getReference().child("Sections");
        userSectionRef = FirebaseDatabase.getInstance().getReference().child("User_Section");
        mAuth = FirebaseAuth.getInstance();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String sectionName = secName.getText().toString();
                if (!TextUtils.isEmpty(sectionName)){

                    DatabaseReference user_push_ref = sectionReference.push();

                    final String sectionKey = user_push_ref.getKey();

                    final Map sectionMap = new HashMap();
                    sectionMap.put("name", sectionName);
                    sectionMap.put("CreationTime", ServerValue.TIMESTAMP);
                    sectionMap.put("image", "default");
                    sectionReference.child(sectionKey).updateChildren(sectionMap).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()){
                                userSectionRef.child(mAuth.getCurrentUser().getUid()).child(sectionKey).child("AddedTime").setValue(ServerValue.TIMESTAMP);
                                Toast.makeText(SectionsActivity.this, "Section has been created :)", Toast.LENGTH_SHORT).show();
                                finish();

                            } else {
                                Toast.makeText(SectionsActivity.this, "Something went wrong !", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                } else {
                    Toast.makeText(SectionsActivity.this, "Please Fill the information !", Toast.LENGTH_SHORT).show();
                }

            }
        });


    }
}