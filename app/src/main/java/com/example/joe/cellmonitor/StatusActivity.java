package com.example.joe.cellmonitor;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;

import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private TextInputLayout mStatus;
    private Button mSavebtn;

    private DatabaseReference myRef;
    private FirebaseUser mCurrentUser;

    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        getSupportActionBar().setTitle("Profile Status");
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back_button_image);

        String status_value = getIntent().getStringExtra("status_value");

        mProgress = new ProgressDialog(StatusActivity.this);


        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = mCurrentUser.getUid();
        myRef = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        mStatus = findViewById(R.id.status_input);
        mSavebtn = findViewById(R.id.saveBtn);

        mStatus.getEditText().setText(status_value);

        mSavebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mProgress.setTitle("Saving Changes");
                mProgress.setMessage("Please wait while saving the changes");
                mProgress.show();
                String status = mStatus.getEditText().getText().toString();
                myRef.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mProgress.dismiss();
                        } else {
                            Toast.makeText(getApplicationContext(), "There was some error", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });

    }
}
