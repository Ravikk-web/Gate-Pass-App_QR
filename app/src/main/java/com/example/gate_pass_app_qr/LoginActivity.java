package com.example.gate_pass_app_qr;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private EditText editTextLoginEmail, editTextLoginPwd;
    private ProgressBar progressBar;
    private FirebaseAuth authProfile;
    private DatabaseReference mDatabase;
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Objects.requireNonNull(getSupportActionBar()).hide();

        editTextLoginEmail = findViewById(R.id.loginEmail);
        editTextLoginPwd = findViewById(R.id.loginPwd);
        progressBar = findViewById(R.id.progressBar);

        authProfile = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        Button buttonForgotPassword = findViewById(R.id.forgot_password);

        //reset password
        buttonForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this, "You can now reset your password.", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
            }
        });

        ImageView imageViewShowHidePwd = findViewById(R.id.imageViewshow_hide_pwd);

        imageViewShowHidePwd.setImageResource((R.drawable.ic_hide_pwd));
        imageViewShowHidePwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editTextLoginPwd.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())){
                    //If password is Visible we will hide it
                    editTextLoginPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    //change The icon
                    imageViewShowHidePwd.setImageResource(R.drawable.ic_hide_pwd);
                }else{
                    editTextLoginPwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    imageViewShowHidePwd.setImageResource(R.drawable.ic_show_pwd);
                }
            }
        });

        //Login User
        Button buttonLogin = findViewById(R.id.loginButton);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String textEmail = editTextLoginEmail.getText().toString();
                String textPwd = editTextLoginPwd.getText().toString();

                if (TextUtils.isEmpty(textEmail)){
                    Toast.makeText(LoginActivity.this, "Please Enter Your Email.", Toast.LENGTH_SHORT).show();
                    editTextLoginEmail.setError("Email is Required");
                    editTextLoginEmail.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(textEmail).matches()) {
                    Toast.makeText(LoginActivity.this, "Please re-enter Your Email.", Toast.LENGTH_SHORT).show();
                    editTextLoginEmail.setError("Valid Email not Found");
                    editTextLoginEmail.requestFocus();

                } else if (TextUtils.isEmpty(textPwd)){
                    Toast.makeText(LoginActivity.this, "Please Enter Your Password.", Toast.LENGTH_SHORT).show();
                    editTextLoginEmail.setError("Come on, password is Required");
                    editTextLoginPwd.requestFocus();
                }else {
                    progressBar.setVisibility(View.VISIBLE);
                    loginUser(textEmail, textPwd);
                }
            }
        });

    }
    private void loginUser(String email, String pwd) {
        progressBar.setVisibility(View.VISIBLE);
        authProfile.signInWithEmailAndPassword(email,pwd).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){

                    //Get the instance of the current user
                    FirebaseUser firebaseUser = authProfile.getCurrentUser();

                    //Check if the User has verified the email or not
                    if(firebaseUser.isEmailVerified()){
                        Toast.makeText(LoginActivity.this, "User Login Successfull.", Toast.LENGTH_SHORT).show();
                        //Open Cheeck User's Role
                        checkUserRole();

                    }
                    else {
                        Toast.makeText(LoginActivity.this, "User not Verified. Please Verify first. A verification email has been send.", Toast.LENGTH_SHORT).show();
                        firebaseUser.sendEmailVerification();
                        authProfile.signOut(); //sign out User
                        showAlertDialog();
                    }
                }
                else {
                    try {
                        throw task.getException();
                    }
                    catch(FirebaseAuthInvalidUserException e){
                        editTextLoginEmail.setError("User Does not Exist or no longer Valid. Please Register again.");
                        editTextLoginEmail.requestFocus();
                    }
                    catch(FirebaseAuthInvalidCredentialsException e){
                        editTextLoginEmail.setError("Invalid Credentials, please check and re-enter.");
                        editTextLoginEmail.requestFocus();
                    }
                    catch (Exception e){
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void checkUserRole() {
        final String userID = authProfile.getCurrentUser().getUid();

        mDatabase.child("admin").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(userID)) {
                    handleUserRole("Admin_Gate-Keeper"); // Handle admin role
                } else {
                    mDatabase.child("teacher").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(userID)) {
                                handleUserRole("Faculty-Teacher"); // Handle teacher role
                            } else {
                                handleUserRole("STUDENT"); // Handle student role
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(LoginActivity.this, "Error:" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                            // Handle error
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(LoginActivity.this, "Error:" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                // Handle error
            }
        });
    }

    private void handleUserRole(String role) {
        // Handle UI updates based on role (e.g., Toast message, start activity)
        Toast.makeText(LoginActivity.this, "Logged in as a " + role, Toast.LENGTH_LONG).show();

        //  ... start activity based on role ...
        if (role=="Admin_Gate-Keeper") {
            startActivity(new Intent(LoginActivity.this, AdminActivity.class));
            finish();
        }else if(role=="Faculty-Teacher"){
            startActivity(new Intent(LoginActivity.this, TeacherActivity.class));
            finish();
        }else if(role=="STUDENT"){
            startActivity(new Intent(LoginActivity.this, UserProfileActivity.class));
            finish();
        }else{
            Toast.makeText(this, "Something went wrong while redirecting page.", Toast.LENGTH_SHORT).show();
        }
        progressBar.setVisibility(View.GONE); // Hide progress bar after handling role
    }

    private void showAlertDialog() {
        // Setup the Alert Builder

        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setTitle("Email not Verified");
        builder.setMessage("Please Verify your email before logging. You can not login without verifying your email.");

        //open the email app if user clidks/ taps continue button
        builder.setPositiveButton("continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        //Create the alert Dialog box
        AlertDialog alertDialog = builder.create();

        //Show the Alert box
        alertDialog.show();
    }

    //Extracting checkUserRole() from onStart() allows more control over when to show the progress bar.
    private void handleUserLogin() {
        progressBar.setVisibility(View.VISIBLE);
        checkUserRole();
    }


    //check if the user is already logged in or not
    @Override
    protected void onStart() {
        super.onStart();

        if (authProfile.getCurrentUser() !=null)
        {
            Toast.makeText(this, "Already logged in !!", Toast.LENGTH_SHORT).show();
            handleUserLogin();
        }
        else {
            Toast.makeText(this, "You can login Now!", Toast.LENGTH_SHORT).show();
        }
    }
}