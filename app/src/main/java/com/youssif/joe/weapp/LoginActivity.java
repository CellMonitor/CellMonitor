package com.youssif.joe.weapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;


import java.util.HashMap;



public class LoginActivity extends AppCompatActivity {

    private CallbackManager callbackManager;
    private static final int RC_SIGN_IN = 1;
    private static final String TAG = "GoogleActivity";
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private Intent HomeActivityIntent;
    private FirebaseUser current_user;
    private ProgressDialog mProgressDialog;
    private ProgressDialog mLoginProgress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //To remove action bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);


        mLoginProgress = new ProgressDialog(this);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading .. Please wait :)");
        mProgressDialog.setMessage("Please wait while logging in .");
        mProgressDialog.setCanceledOnTouchOutside(false);

        callbackManager = CallbackManager.Factory.create();
        mAuth = FirebaseAuth.getInstance();


        final EditText mEmail = findViewById(R.id.userName);
        final EditText mPassword = findViewById(R.id.password);
        Button loginBtn = findViewById(R.id.signinButton);


        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();

                if(!TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)) {

                    mLoginProgress.setTitle("Loading .. Please wait :)");
                    mLoginProgress.setMessage("Please wait while logging in .");
                    mLoginProgress.setCanceledOnTouchOutside(false);
                    mLoginProgress.show();

                    mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()){
                                Intent mainIntent = new Intent(LoginActivity.this, HomeActivity.class);
                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(mainIntent);
                                finish();
                                mLoginProgress.dismiss();
                            } else {
                                Toast.makeText(LoginActivity.this, "Something went wrong .. Please try again !", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });


        LoginButton loginButton = findViewById(R.id.login_button);
        Button mGoogleBtn = findViewById(R.id.googleBtn);


        HomeActivityIntent = new Intent(LoginActivity.this, HomeActivity.class);
        HomeActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        HomeActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        HomeActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);


        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(LoginActivity.this, gso);


        mGoogleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });


        // Callback registration
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code

                mProgressDialog.show();
                handleFacebookAccessToken(loginResult.getAccessToken());

            }

            @Override
            public void onCancel() {
                // App code
                Toast.makeText(LoginActivity.this, "Login Cancelled", Toast.LENGTH_LONG).show();
            }


            @Override
            public void onError(FacebookException exception) {
                // App code
                Toast.makeText(LoginActivity.this, exception.toString(), Toast.LENGTH_LONG  ).show();
            }
        });
    }




    private void handleFacebookAccessToken(final AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //Toast.makeText(LoginActivity.this, "Logged into firebase", Toast.LENGTH_LONG).show();
                            saveData();
                            startActivity(HomeActivityIntent);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            mProgressDialog.dismiss();
                            //updateUI(null);
                        }

                        // ...
                    }
                });
    }


    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                mProgressDialog.show();
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                Toast.makeText(this, "Failed to Log in using Google account", Toast.LENGTH_SHORT).show();
                // ...
            }
        }

    }
    public void childLoginClicked(View view){

        Intent intent = new Intent(LoginActivity.this,ChildLoginActivity.class);
        startActivity(intent);



    }
    public void signUpClicked(View view){

        Intent registerIntent = new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(registerIntent);

    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            //Toast.makeText(LoginActivity.this, "Authentication success.",
                              //      Toast.LENGTH_SHORT).show();
                            saveData();
                            startActivity(HomeActivityIntent);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            mProgressDialog.dismiss();
                            // updateUI(null);
                        }

                        // ...
                    }
                });

    }

    private void saveData() {

        current_user = FirebaseAuth.getInstance().getCurrentUser();
        assert current_user != null;
        final String uid = current_user.getUid();
        final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
        final DatabaseReference users = myRef.child("Users");
        users.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("datasnapshot : ",dataSnapshot.toString());
                if (dataSnapshot.child(uid).child("status").exists()){

                    String deviceToken = FirebaseInstanceId.getInstance().getToken();
                    users.child(uid).child("device_token").setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //Toast.makeText(LoginActivity.this,"Welcome back friend",Toast.LENGTH_SHORT).show();
                        }
                    });
                }else {
                    String deviceToken = FirebaseInstanceId.getInstance().getToken();
                    DatabaseReference usersData = users.child(uid);
                    HashMap<String, String> userMap = new HashMap<>();
                    userMap.put("name", current_user.getDisplayName());
                    userMap.put("status", "Hey there ! .. I am using WeApp Tracking App.");
                    userMap.put("image", current_user.getPhotoUrl().toString());
                    userMap.put("thumb_image", "default");
                    userMap.put("device_token", deviceToken);
                    usersData.setValue(userMap);


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        mProgressDialog.dismiss();


    }


}
