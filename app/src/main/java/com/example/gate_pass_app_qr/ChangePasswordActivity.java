package com.example.gate_pass_app_qr;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    private FirebaseAuth authProfile;
    private EditText editTextPwdCurr, editTextPwdNew, editTextPwdConfirmNew;
    private TextView textViewAuthenticated;
    private Button buttonChangePwd, buttonReAuthenticate;
    private ProgressBar progressBar;
    private String userCurrPwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getSupportActionBar().setTitle("Change Password");

        textViewAuthenticated = findViewById(R.id.textView_change_pwd_authenticate);
        editTextPwdNew = findViewById(R.id.editText_change_pwd_new);
        editTextPwdCurr = findViewById(R.id.editText_change_pwd_current);
        editTextPwdConfirmNew = findViewById(R.id.editText_change_pwd_new_confirm);

        progressBar = findViewById(R.id.progressBar);
        buttonReAuthenticate = findViewById(R.id.button_change_pwd_authenticate_user);
        buttonChangePwd = findViewById(R.id.button_change_pwd);

        //Disable editText for new password, Confirm password and disable the changePwd Button

        editTextPwdNew.setEnabled(false);
        editTextPwdConfirmNew.setEnabled(false);
        buttonChangePwd.setEnabled(false);

        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        if (firebaseUser.equals("")){
            Toast.makeText(ChangePasswordActivity.this, "Something Went Wrong !! User's Details not available.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ChangePasswordActivity.this, UserProfileActivity.class);
            startActivity(intent);
            finish();
        }else{
            reAuthenticateUser(firebaseUser);
        }
    }

    //Re-Authenticate the user before changing the password
    private void reAuthenticateUser(FirebaseUser firebaseUser) {

        buttonReAuthenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userCurrPwd = editTextPwdCurr.getText().toString();

                if (TextUtils.isEmpty(userCurrPwd)){
                    Toast.makeText(ChangePasswordActivity.this, "Password is needed !", Toast.LENGTH_SHORT).show();
                    editTextPwdCurr.setError("Please enter your current password to authenticate.");
                    editTextPwdCurr.requestFocus();
                }else{
                    progressBar.setVisibility(View.VISIBLE);

                    //Re-Authenticate the user now
                    AuthCredential credintials = EmailAuthProvider.getCredential(firebaseUser.getEmail(), userCurrPwd);

                    firebaseUser.reauthenticate(credintials).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                progressBar.setVisibility(View.GONE);

                                //Disable the edit text of authentication section
                                editTextPwdCurr.setEnabled(false);
                                buttonReAuthenticate.setEnabled(false);

                                //Enable the change password section
                                editTextPwdNew.setEnabled(true);
                                editTextPwdConfirmNew.setEnabled(true);
                                buttonChangePwd.setEnabled(true);

                                //Set TextView to show user is Authenticated
                                textViewAuthenticated.setText("You are Authenticated/ Verified \nYou can change you password now.");
                                Toast.makeText(ChangePasswordActivity.this, "password has been verified. Change password now.", Toast.LENGTH_SHORT).show();

                                //change the color of the button
                                buttonChangePwd.setBackgroundTintList(ContextCompat.getColorStateList(ChangePasswordActivity.this, R.color.dark_green));

                                buttonChangePwd.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        changePwd(firebaseUser);
                                    }
                                });
                            }else{
                                try {
                                    throw task.getException();
                                }catch(Exception e){
                                    Toast.makeText(ChangePasswordActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }

                                progressBar.setVisibility(View.GONE);
                            }
                        }
                    });
                }
            }
        });

    }

    private void changePwd(FirebaseUser firebaseUser) {

        String userPwdNew = editTextPwdNew.getText().toString();
        String userPwdConfirmNew = editTextPwdConfirmNew.getText().toString();

        if (TextUtils.isEmpty(userPwdNew)){
            Toast.makeText(ChangePasswordActivity.this, "New password is needed.", Toast.LENGTH_SHORT).show();
            editTextPwdNew.setError("Please Enter your new password");
            editTextPwdNew.requestFocus();
        }else if (TextUtils.isEmpty(userPwdConfirmNew)){
            Toast.makeText(ChangePasswordActivity.this,"Please confirm your password", Toast.LENGTH_SHORT).show();
            editTextPwdConfirmNew.setError("Please Confirm your password.");
            editTextPwdConfirmNew.requestFocus();
        }else if (!userPwdNew.matches(userPwdConfirmNew)){
            Toast.makeText(ChangePasswordActivity.this,"Password did not match", Toast.LENGTH_SHORT).show();
            editTextPwdConfirmNew.setError("Please re-enter your password.");
            editTextPwdConfirmNew.requestFocus();
        }else if (userCurrPwd.matches(userPwdNew)){
            Toast.makeText(ChangePasswordActivity.this,"New password cannot be same as the previous password.", Toast.LENGTH_SHORT).show();
            editTextPwdNew.setError("Please enter a new password");
            editTextPwdNew.requestFocus();
        }else{
            progressBar.setVisibility(View.VISIBLE);

            firebaseUser.updatePassword(userPwdNew).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(ChangePasswordActivity.this, "Password Changed Successfully!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(ChangePasswordActivity.this, UserProfileActivity.class));
                        finish();
                    }else{
                        try {
                            throw task.getException();
                        }catch(Exception e){
                            Toast.makeText(ChangePasswordActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                        progressBar.setVisibility(View.GONE);
                    }
                }
            });
        }
    }

    private void showLogoutDialog(String title, String message) {
        // Setup the Alert Builder

        AlertDialog.Builder builder = new AlertDialog.Builder(ChangePasswordActivity.this);
        builder.setTitle(title.toUpperCase());
        builder.setMessage(message);

        //open the email app if user clicks/ taps continue button
        builder.setPositiveButton("continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                authProfile.signOut();
                Toast.makeText(ChangePasswordActivity.this, "logout Successful !", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ChangePasswordActivity.this, MainActivity.class);

                //Clear stack to prevent user from coming back after logout.
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        //Create the alert Dialog box
        AlertDialog alertDialog = builder.create();

        //Show the Alert box
        alertDialog.show();
    }

    //Create ActionBar menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate menu items
        getMenuInflater().inflate(R.menu.common_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //When any menu item is Selected
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.menu_refersh){
            //Refresh Activity
            startActivity(getIntent());
            finish();
            overridePendingTransition(0,2);
        } else if (id == R.id.menu_logout) {
            //Logout the user
            showLogoutDialog("Logout", "Would you like to Logout.");
        }else if (id == R.id.menu_update_profile){
            startActivity(new Intent(ChangePasswordActivity.this, UpdateProfileActivity.class));
            finish();
        }else if (id == R.id.menu_update_email){
            startActivity(new Intent(ChangePasswordActivity.this, UpdateEmailActivity.class));
            finish();
        }else if (id == R.id.menu_settings){
            Toast.makeText(this, "Feature (Biometrics) Work in Progress", Toast.LENGTH_SHORT).show();
        }else if (id == R.id.menu_change_password){
            startActivity(new Intent(ChangePasswordActivity.this, ChangePasswordActivity.class));
            finish();
        }
        else if (id == R.id.menu_delete_profile){
            startActivity(new Intent(ChangePasswordActivity.this, DeleteProfileActivity.class));
            finish();
        }
        else {
            Toast.makeText(this, "Something Went Wrong !", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }
}