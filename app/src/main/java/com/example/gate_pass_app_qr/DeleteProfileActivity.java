package com.example.gate_pass_app_qr;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class DeleteProfileActivity extends AppCompatActivity {

    private FirebaseAuth authProfile;
    private FirebaseUser firebaseUser;
    private EditText editTextUserPwd;
    private TextView textViewAuthenticated;
    private ProgressBar progressBar;
    private String userPwd;
    private Button buttonReAuthenticate, buttonDeleteUser;
    private static final String TAG = "DeleteProfileActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_delete_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getSupportActionBar().setTitle("Delete User");

        progressBar = findViewById(R.id.progressBar);
        editTextUserPwd = findViewById(R.id.editText_delete_user_pwd);
        textViewAuthenticated = findViewById(R.id.textView_delete_user_authenticated);

        buttonReAuthenticate = findViewById(R.id.button_delete_user_authenticate);
        buttonDeleteUser = findViewById(R.id.button_delete_user);

        //Disable the Delete User Section
        buttonDeleteUser.setEnabled(false);

        authProfile = FirebaseAuth.getInstance();

        firebaseUser = authProfile.getCurrentUser();

        if (firebaseUser==null || firebaseUser.equals("")){
            Toast.makeText(DeleteProfileActivity.this, "Something Went Wrong! User details are not availabe at the moment.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(DeleteProfileActivity.this, UserProfileActivity.class));
            finish();
        }else {
            reAuthenticateUser(firebaseUser);
        }
    }

    //Re-Authenticate the user before changing the password
    private void reAuthenticateUser(FirebaseUser firebaseUser) {

        buttonReAuthenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userPwd = editTextUserPwd.getText().toString();

                if (TextUtils.isEmpty(userPwd)){
                    Toast.makeText(DeleteProfileActivity.this, "Password is needed !", Toast.LENGTH_SHORT).show();
                    editTextUserPwd.setError("Please enter your current password to authenticate.");
                    editTextUserPwd.requestFocus();
                }else{
                    progressBar.setVisibility(View.VISIBLE);

                    //Re-Authenticate the user now
                    AuthCredential credintials = EmailAuthProvider.getCredential(firebaseUser.getEmail(), userPwd);

                    firebaseUser.reauthenticate(credintials).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                progressBar.setVisibility(View.GONE);

                                //Disable the authentication section
                                editTextUserPwd.setEnabled(false);
                                buttonReAuthenticate.setEnabled(false);

                                //Enable the Delete User section
                                buttonDeleteUser.setEnabled(true);

                                //Set TextView to show user can delete the profile
                                textViewAuthenticated.setText("You are Authenticated/ Verified \nYou can now DELETE your account.");
                                Toast.makeText(DeleteProfileActivity.this, "Password: verified. You can delete Profile now. Be careful, This action is irreversible.", Toast.LENGTH_SHORT).show();

                                //change the color of the button
                                buttonDeleteUser.setBackgroundTintList(ContextCompat.getColorStateList(DeleteProfileActivity.this, R.color.dark_green));

                                buttonDeleteUser.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        showAlertDiaglog();
                                    }
                                });
                            }else{
                                try {
                                    throw task.getException();
                                }catch(Exception e){
                                    Toast.makeText(DeleteProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }

                                progressBar.setVisibility(View.GONE);
                            }
                        }
                    });
                }
            }
        });

    }

    private void showAlertDiaglog() {
        // Setup the Alert Builder

        AlertDialog.Builder builder = new AlertDialog.Builder(DeleteProfileActivity.this);
        builder.setTitle("Delete user and Related Data?");
        builder.setMessage("Do you really want to delete your profile and its Related Data? this action is irreversible/ permanent !");

        //open the email app if user clicks/ taps continue button
        builder.setPositiveButton("continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteUserData(firebaseUser);
            }
        });

        //Return to the userProfile Activity if user clicks 'cancel' button
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(DeleteProfileActivity.this, UserProfileActivity.class));
                finish();
            }
        });

        //Create the alert Dialog box
        AlertDialog alertDialog = builder.create();

        //change the button color to continue
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.red));
            }
        });

        //Show the Alert box
        alertDialog.show();
    }

    private void deleteUser() {

        firebaseUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){

                    //Delete the user's Data
                    authProfile.signOut();
                    Toast.makeText(DeleteProfileActivity.this, "The user has been deleted Successfully.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(DeleteProfileActivity.this, MainActivity.class));
                }else {
                    try {
                        throw task.getException();
                    }catch (Exception e){
                        Toast.makeText(DeleteProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

                progressBar.setVisibility(View.GONE);
            }
        });

    }

    //Delete all the data of the user
    private void deleteUserData(FirebaseUser firebaseUser) {

        //Chck if the user has uploaded the profile picture before deleting.
        if (firebaseUser.getPhotoUrl() != null){
            //Delete Display Pic
            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
            StorageReference storageReference = firebaseStorage.getReferenceFromUrl(firebaseUser.getPhotoUrl().toString());
            storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Log.d(TAG, "OnSuccess: Photo Deleted");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG,e.getMessage());
                    Toast.makeText(DeleteProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        //Delete the data from Realtime Database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Registered Users");
        databaseReference.child(firebaseUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d(TAG, "OnSuccess: User Data Deleted");

                //Now delete the user after deleting the user's data
                deleteUser();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG,e.getMessage());
                Toast.makeText(DeleteProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLogoutDialog(String title, String message) {
        // Setup the Alert Builder

        AlertDialog.Builder builder = new AlertDialog.Builder(DeleteProfileActivity.this);
        builder.setTitle(title.toUpperCase());
        builder.setMessage(message);

        //open the email app if user clicks/ taps continue button
        builder.setPositiveButton("continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                authProfile.signOut();
                Toast.makeText(DeleteProfileActivity.this, "logout Successful !", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(DeleteProfileActivity.this, MainActivity.class);

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
        }else if (id == R.id.menu_logout) {
            //Logout the user
            showLogoutDialog("Logout", "Would you like to Logout.");
        }else if (id == R.id.menu_update_profile){
            startActivity(new Intent(DeleteProfileActivity.this, UpdateProfileActivity.class));
            finish();
        }else if (id == R.id.menu_update_email){
            startActivity(new Intent(DeleteProfileActivity.this, UpdateEmailActivity.class));
            finish();
        }else if (id == R.id.menu_change_password){
            startActivity(new Intent(DeleteProfileActivity.this, ChangePasswordActivity.class));
            finish();
        }else if (id == R.id.menu_settings){
            Toast.makeText(this, "Feature Work in Progress (Biometrics)", Toast.LENGTH_SHORT).show();
        }else if (id == R.id.menu_delete_profile){
            startActivity(new Intent(DeleteProfileActivity.this, DeleteProfileActivity.class));
            finish();
        }
        else {
            Toast.makeText(this, "Something Went Wrong !", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }
}