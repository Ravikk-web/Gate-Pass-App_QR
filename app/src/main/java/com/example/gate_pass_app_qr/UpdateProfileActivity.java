package com.example.gate_pass_app_qr;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateProfileActivity extends AppCompatActivity {

    private EditText editTextUpdateName, editTextUpdateDoB, editTextUpdateMobile;
    private RadioGroup radioGroupUpdateGender;
    private RadioButton radioButtonUpdateGenderSelected;
    private String textFullName, textDoB, textGender, textMobile, textEnrollno;
    private FirebaseAuth authProfile;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Objects.requireNonNull(getSupportActionBar()).setTitle("Update Profile");

        progressBar = findViewById(R.id.progressBar);

        editTextUpdateName = findViewById(R.id.editText_update_profile_name);
        editTextUpdateDoB = findViewById(R.id.editText_update_profile_dob);
        editTextUpdateMobile = findViewById(R.id.editText_update_profile_mobile);

        radioGroupUpdateGender = findViewById(R.id.radioGroup_update_gender);

        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        //Show Profile Data
        showProfile(firebaseUser);

        //Upload Profile Pic
        Button buttonUploadProfilePic = findViewById(R.id.button_update_profile_pic);
        buttonUploadProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UpdateProfileActivity.this, UploadProfilePicActivity.class));
                finish();
            }
        });

        //Update Email ***
//        Button buttonUpdateEmail = findViewById(R.id.button_update_email);
//        buttonUpdateEmail.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(UpdateProfileActivity.this, UpdateEmailActivity.class));
//                finish();
//            }
//        });

        //Setting Up DatePicker on EditText
        editTextUpdateDoB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Extracting the saved date dd,mm,yyyy into different variables by creating an array delimited by '/'
                String textSADob[] = textDoB.split("/");


                int day = Integer.parseInt(textSADob[0]);
                int month = Integer.parseInt(textSADob[1]) - 1; // To take care of the month index starting from 0
                int year = Integer.parseInt(textSADob[2]);

                DatePickerDialog picker;

                //Date Picker Dialog
                picker = new DatePickerDialog(UpdateProfileActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        editTextUpdateDoB.setText(dayOfMonth+"/"+(month + 1)+"/"+year);
                    }
                },year,month,day);
                picker.show();
            }
        });

        // Update Profile Button
        Button buttonUpdateProfile = findViewById(R.id.button_update_profile);
        buttonUpdateProfile.setOnClickListener((v)->UpdateProfile(firebaseUser));

    }

    private void UpdateProfile(FirebaseUser firebaseUser) {
        int selectedGenderID = radioGroupUpdateGender.getCheckedRadioButtonId();
        radioButtonUpdateGenderSelected = findViewById(selectedGenderID);

        //Vaidate the Mobile  Number
        String mobileRegex = "[6-9][0-9]{9}"; //First no. can be {6,8,9} and rest 9 numbers can be any numbers.
        Matcher mobileMatcher;
        Pattern mobilepattern = Pattern.compile(mobileRegex);

        mobileMatcher = mobilepattern.matcher(textMobile);

        if (TextUtils.isEmpty(textFullName)){
            editTextUpdateName.setError("Full Name is Required");
            editTextUpdateName.requestFocus();
        }else if (TextUtils.isEmpty(textDoB)) {
            editTextUpdateDoB.setError("Date of Birth is Required");
            editTextUpdateDoB.requestFocus();
        } else if (TextUtils.isEmpty(radioButtonUpdateGenderSelected.getText())) {
            radioButtonUpdateGenderSelected.setError("Please select a Gender");
            radioButtonUpdateGenderSelected.requestFocus();
        }else if (TextUtils.isEmpty(textMobile)) {
            editTextUpdateMobile.setError("Mobile No. is Required");
            editTextUpdateMobile.requestFocus();
        } else if (textMobile.length() != 10) {
            editTextUpdateMobile.setError("Mobile No. should be 10 digits");
            editTextUpdateMobile.requestFocus();
        } else if (!mobileMatcher.find()) {
            editTextUpdateMobile.setError("Invalid Mobile Number.");
            editTextUpdateMobile.requestFocus();
        }else {
            textGender = radioButtonUpdateGenderSelected.getText().toString();
            textFullName = editTextUpdateName.getText().toString();
            textDoB = editTextUpdateDoB.getText().toString();
            textMobile = editTextUpdateMobile.getText().toString();

            //Enter User Data into the Firebase Realtime Database, set up dependencies
            ReadWriteUserDetails writeUserDetails = new ReadWriteUserDetails(textEnrollno,textDoB,textGender,textMobile);

            //Extract User reference from Database for "Registered Users"
            DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");

            String userId = firebaseUser.getUid();

            progressBar.setVisibility(View.VISIBLE);

            referenceProfile.child(userId).setValue(writeUserDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        //Setting new Display Name
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(textFullName).build();
                        firebaseUser.updateProfile(profileUpdates);

                        Toast.makeText(UpdateProfileActivity.this, "Update Successfull", Toast.LENGTH_SHORT).show();

                        //stop user from returning back through back button
                        Intent intent = new Intent(UpdateProfileActivity.this, UserProfileActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK );
                        startActivity(intent);
                        finish();
                    }else {
                        try {
                            throw task.getException();
                        }catch (Exception e){
                            Toast.makeText(UpdateProfileActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }

                    progressBar.setVisibility(View.GONE);

                }
            });
        }
    }

    private void showProfile(FirebaseUser firebaseUser) {

        String userIDofRegisteredUser = firebaseUser.getUid();

        //Extracting the reference of user from the Database for 'Registered Users'
        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");

        progressBar.setVisibility(View.VISIBLE);

        referenceProfile.child(userIDofRegisteredUser).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ReadWriteUserDetails readUserDetails = snapshot.getValue(ReadWriteUserDetails.class);
                if (readUserDetails != null){
                    textFullName = firebaseUser.getDisplayName();
                    textDoB = readUserDetails.dob;
                    textGender = readUserDetails.gender;
                    textMobile = readUserDetails.mobile;
                    textEnrollno = readUserDetails.enrollno;

                    editTextUpdateName.setText(textFullName);
                    editTextUpdateDoB.setText(textDoB);
                    editTextUpdateMobile.setText(textMobile);

                    //show Gender through RadioButton
                    if(textGender.equalsIgnoreCase("MALE")){
                        radioButtonUpdateGenderSelected = findViewById(R.id.radio_male);
                    }else if(textGender.equalsIgnoreCase("OTHER")){
                        radioButtonUpdateGenderSelected = findViewById(R.id.radio_other);
                    }else{
                        radioButtonUpdateGenderSelected = findViewById(R.id.radio_female);
                    }
                    radioButtonUpdateGenderSelected.setChecked(true);

                }else{
                    Toast.makeText(UpdateProfileActivity.this, "Something Went Wrong!", Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.VISIBLE);
                Toast.makeText(UpdateProfileActivity.this, "Something Went Wrong!", Toast.LENGTH_SHORT).show();
            }
        });

    }
    private void showLogoutDialog(String title, String message) {
        // Setup the Alert Builder

        AlertDialog.Builder builder = new AlertDialog.Builder(UpdateProfileActivity.this);
        builder.setTitle(title.toUpperCase());
        builder.setMessage(message);

        //open the email app if user clicks/ taps continue button
        builder.setPositiveButton("continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                authProfile.signOut();
                Toast.makeText(UpdateProfileActivity.this, "logout Successful !", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(UpdateProfileActivity.this, MainActivity.class);

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
            startActivity(new Intent(UpdateProfileActivity.this, UpdateProfileActivity.class));
            finish();
        }
//        else if (id == R.id.menu_update_email){
//            startActivity(new Intent(UploadProfilePicActivity.this, UpdateEmailActivity.class));
//        }else if (id == R.id.menu_settings){
//            Toast.makeText(this, "Feature Work in Progress", Toast.LENGTH_SHORT).show();
//        }else if (id == R.id.menu_change_password){
//            startActivity(new Intent(UploadProfilePicActivity.this, ChangePasswordActivity.class));
//        }else if (id == R.id.menu_delete_profile){
//            startActivity(new Intent(UploadProfilePicActivity.this, DeleteProfileActivity.class));
//        }
        else {
            Toast.makeText(this, "Something Went Wrong !", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }
}