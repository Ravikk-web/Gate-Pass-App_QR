package com.example.gate_pass_app_qr;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
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

public class UpdateEmailActivity extends AppCompatActivity {

    private FirebaseAuth authProfile;
    private FirebaseUser firebaseUser;
    private ProgressBar progressBar;
    private TextView textViewAuthenticated;
    private String userOldEmail, userNewEmail, userPwd;
    private Button buttonUpdateEmail;
    private EditText editTextNewEmail, editTextPwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_email);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getSupportActionBar().setTitle("Update Email");

        progressBar = findViewById(R.id.progressBar);
        editTextPwd = findViewById(R.id.editText_update_email_verify_password);
        editTextNewEmail = findViewById(R.id.editText_update_email_new);
        textViewAuthenticated = findViewById(R.id.textView_update_email_authenticate);
        buttonUpdateEmail = findViewById(R.id.button_update_email);

        buttonUpdateEmail.setEnabled(false);    //make button disabled in the beginning until the user is verified
        editTextNewEmail.setEnabled(false);     //make editText disabled in the beginning until the user is verified

        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();

        //set Old email id into the TextView
        userOldEmail = firebaseUser.getEmail();
        TextView TextViewOldEmail = findViewById(R.id.textView_update_email_old);
        TextViewOldEmail.setText(userOldEmail);

        //Check if the firebaseuser is Null or not
        if (firebaseUser.equals("")){
            Toast.makeText(UpdateEmailActivity.this, "Something went Wrong. User Details not available.", Toast.LENGTH_LONG).show();
        }else{
            reAutheticate(firebaseUser);
        }
    }

    //Re-Authenticate/ Verify the user before changing the email ID
    private void reAutheticate(FirebaseUser firebaseUser) {

        Button buttonVerifyUser = findViewById(R.id.button_authenticate_user);
        buttonVerifyUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Obtain password for authentication
                userPwd = editTextPwd.getText().toString();

                if (TextUtils.isEmpty(userPwd)){
                    Toast.makeText(UpdateEmailActivity.this, "Password is needed to continue.", Toast.LENGTH_SHORT).show();
                    editTextPwd.setError("Please enter your password for authentication.");
                    editTextPwd.requestFocus();
                }else {
                    progressBar.setVisibility(View.VISIBLE);

                    AuthCredential credential = EmailAuthProvider.getCredential(userOldEmail, userPwd);

                    firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            progressBar.setVisibility(View.GONE);
                            if (task.isSuccessful()){
                                Toast.makeText(UpdateEmailActivity.this, "Password has been verified. \nYou can Update Email now.", Toast.LENGTH_SHORT).show();

                                //set the TextView to show after the user is authenticated
                                textViewAuthenticated.setText("You are AUTHENTICATED. You can update email now.");

                                //Disable editText for password and enable EditTExt for new Email and update email address
                                editTextNewEmail.setEnabled(true);
                                editTextPwd.setEnabled(false);
                                buttonVerifyUser.setEnabled(false);
                                buttonUpdateEmail.setEnabled(true);

                                //Change the color of the update email button
                                buttonUpdateEmail.setBackgroundTintList(ContextCompat.getColorStateList(UpdateEmailActivity.this, R.color.dark_green));

                                buttonUpdateEmail.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        userNewEmail = editTextNewEmail.getText().toString();
                                        if (TextUtils.isEmpty(userNewEmail)){
                                            Toast.makeText(UpdateEmailActivity.this, "At-least Don't forget to enter the new Email !!! ", Toast.LENGTH_SHORT).show();
                                            editTextNewEmail.setError("Please enter the new Email");
                                            editTextNewEmail.requestFocus();
                                        } else if (!Patterns.EMAIL_ADDRESS.matcher(userNewEmail).matches()) {
                                            Toast.makeText(UpdateEmailActivity.this, "Please enter a valid Email.", Toast.LENGTH_SHORT).show();
                                            editTextNewEmail.setError("Please provide a valid Email");
                                            editTextNewEmail.requestFocus();
                                        }else if (userOldEmail.matches(userNewEmail)){
                                            Toast.makeText(UpdateEmailActivity.this, "New Email cannot be same as old Email.", Toast.LENGTH_SHORT).show();
                                            editTextNewEmail.setError("Please enter a new Email");
                                            editTextNewEmail.requestFocus();
                                        }else {
                                            progressBar.setVisibility(View.VISIBLE);
                                            updateEmail(firebaseUser);
                                        }
                                    }
                                });

                            }else {
                                try {
                                    throw task.getException();
                                }catch(Exception e){
                                    Toast.makeText(UpdateEmailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                }
            }
        });

    }

    //Update the new email as the main email
    private void updateEmail(FirebaseUser firebaseUser) {
        firebaseUser.updateEmail(userNewEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){

                    //Verify the email
                    firebaseUser.sendEmailVerification();
                    Toast.makeText(UpdateEmailActivity.this, "Email has been updated please verify your new Email.", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(UpdateEmailActivity.this, UserProfileActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    try {
                        throw task.getException();
                    }catch(Exception e){
                        Toast.makeText(UpdateEmailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void showLogoutDialog(String title, String message) {
        // Setup the Alert Builder

        AlertDialog.Builder builder = new AlertDialog.Builder(UpdateEmailActivity.this);
        builder.setTitle(title.toUpperCase());
        builder.setMessage(message);

        //open the email app if user clicks/ taps continue button
        builder.setPositiveButton("continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                authProfile.signOut();
                Toast.makeText(UpdateEmailActivity.this, "logout Successful !", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(UpdateEmailActivity.this, MainActivity.class);

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
            startActivity(new Intent(UpdateEmailActivity.this, UpdateProfileActivity.class));
            finish();
        }else if (id == R.id.menu_update_email){
            startActivity(new Intent(UpdateEmailActivity.this, UpdateEmailActivity.class));
            finish();
        }else if (id == R.id.menu_change_password){
            startActivity(new Intent(UpdateEmailActivity.this, ChangePasswordActivity.class));
            finish();
        }else if (id == R.id.menu_settings){
            Toast.makeText(this, "Feature Work in Progress (Biometrics)", Toast.LENGTH_SHORT).show();
        }else if (id == R.id.menu_delete_profile){
            startActivity(new Intent(UpdateEmailActivity.this, DeleteProfileActivity.class));
            finish();
        }
        else {
            Toast.makeText(this, "Something Went Wrong !", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }
}