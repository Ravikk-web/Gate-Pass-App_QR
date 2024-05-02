package com.example.gate_pass_app_qr;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextRegisterFullName, editTextRegisterEmail,editTextRegisterParentFullName, editTextRegisterParentEmail, editTextRegisterDoB, editTextRegisterMobile,
            editTextRegisterParentMobile, editTextRegisterPassword,editTextRegisterEnrollno ,editTextRegisterConfirmPassword;
    private ProgressBar progressBar;
    private RadioGroup radioGroupRegisterGender;
    private RadioButton radioButtonRegisterGenderSelected;
    private DatePickerDialog picker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Objects.requireNonNull(getSupportActionBar()).hide();

        Toast.makeText(this, "You Can Register Now !!", Toast.LENGTH_SHORT).show();

        //Students details
        editTextRegisterFullName = findViewById(R.id.editText_register_full_name);
        editTextRegisterEmail = findViewById(R.id.editText_register_email);
        editTextRegisterEnrollno = findViewById(R.id.editText_register_enrollno);
        editTextRegisterDoB = findViewById(R.id.editText_register_dob);
        editTextRegisterMobile = findViewById(R.id.editText_register_mobile);
        editTextRegisterPassword = findViewById(R.id.editText_register_password);
        editTextRegisterConfirmPassword = findViewById(R.id.editText_register_confirm_password);

        //Parents Details
        editTextRegisterParentFullName = findViewById(R.id.editText_register_parent_full_name);
        editTextRegisterParentEmail = findViewById(R.id.editText_register_parent_email);
        editTextRegisterParentMobile = findViewById(R.id.editText_register_parent_mobile);

        progressBar = findViewById(R.id.progressBar);

        radioGroupRegisterGender = findViewById(R.id.radioGroup_register_gender);
        radioGroupRegisterGender.clearCheck();

        //Setting Up DatePicker on EditText
        editTextRegisterDoB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);

                //Date Picker Dialog
                picker = new DatePickerDialog(RegisterActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        editTextRegisterDoB.setText(dayOfMonth+"/"+(month + 1)+"/"+year);
                    }
                },year,month,day);
                picker.show();
            }
        });

        //Validating details and Registering the User
        Button buttonRegister = findViewById(R.id.register_button);
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedGenderID = radioGroupRegisterGender.getCheckedRadioButtonId();
                radioButtonRegisterGenderSelected = findViewById(selectedGenderID);

                //Contains the entered data of Student
                String textFullName = editTextRegisterFullName.getText().toString();
                String textEmail = editTextRegisterEmail.getText().toString();
                String textEnrollno = editTextRegisterEnrollno.getText().toString();
                String textDoB = editTextRegisterDoB.getText().toString();
                String textMoblie = editTextRegisterMobile.getText().toString();
                String textPwd = editTextRegisterPassword.getText().toString();
                String textConfirmPwd = editTextRegisterConfirmPassword.getText().toString();
                String textGender;  //cannot obtain value before varifying if any button is selectd or not.

                //Contains the entered data of Parent
                String textParentFullName = editTextRegisterParentFullName.getText().toString();
                String textParentEmail = editTextRegisterParentEmail.getText().toString();
                String textParentMoblie = editTextRegisterParentMobile.getText().toString();


                //Vaidate the Mobile  Number
                String mobileRegex = "[6-9][0-9]{9}"; //First no. can be {6,8,9} and rest 9 numbers can be any numbers.
                Matcher mobileMatcher;
                Matcher mobileMatcherParent;
                Pattern mobilepattern = Pattern.compile(mobileRegex);

                mobileMatcher = mobilepattern.matcher(textMoblie);
                mobileMatcherParent = mobilepattern.matcher(textParentMoblie);

                if (TextUtils.isEmpty(textFullName)){
                    editTextRegisterFullName.setError("Full Name is Required");
                    editTextRegisterFullName.requestFocus();
                } else if (TextUtils.isEmpty(textEmail)) {
                    editTextRegisterEmail.setError("Email is Required");
                    editTextRegisterEmail.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(textEmail).matches()) {
                    editTextRegisterEmail.setError("Please enter a valid email");
                    editTextRegisterEmail.requestFocus();
                } else if (TextUtils.isEmpty(textEnrollno)) {
                    editTextRegisterEnrollno.setError("College Enrollment Number is Required");
                    editTextRegisterEnrollno.requestFocus();
                }else if (textEnrollno.length()!=11) {
                    editTextRegisterEnrollno.setError("Enrollment Number must be of 11 digits");
                    editTextRegisterEnrollno.requestFocus();
                }else if (TextUtils.isEmpty(textDoB)) {
                    editTextRegisterDoB.setError("Date of Birth is Required");
                    editTextRegisterDoB.requestFocus();
                } else if (radioGroupRegisterGender.getCheckedRadioButtonId() == -1) {
                    radioButtonRegisterGenderSelected.setError("Please select a Gender");
                    radioButtonRegisterGenderSelected.requestFocus();
                }else if (TextUtils.isEmpty(textMoblie)) {
                    editTextRegisterMobile.setError("Mobile No. is Required");
                    editTextRegisterMobile.requestFocus();
                } else if (textMoblie.length() != 10) {
                    editTextRegisterMobile.setError("Mobile No. should be 10 digits");
                    editTextRegisterMobile.requestFocus();
                } else if (!mobileMatcher.find()) {
                    editTextRegisterMobile.setError("Invalid Mobile Number.");
                    editTextRegisterMobile.requestFocus();
                } else if (TextUtils.isEmpty(textParentFullName)){
                    editTextRegisterParentFullName.setError("Parent's Full Name is Required");
                    editTextRegisterParentFullName.requestFocus();
                }else if (TextUtils.isEmpty(textParentEmail)) {
                    editTextRegisterParentEmail.setError("Parent's Email is Required");
                    editTextRegisterParentEmail.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(textParentEmail).matches()) {
                    editTextRegisterParentEmail.setError("Please enter a valid email");
                    editTextRegisterParentEmail.requestFocus();
                }else if (TextUtils.isEmpty(textParentMoblie)) {
                    editTextRegisterParentMobile.setError("Parent Mobile No. is Required");
                    editTextRegisterParentMobile.requestFocus();
                } else if (textParentMoblie.length() != 10) {
                    editTextRegisterParentMobile.setError("Mobile No. should be 10 digits");
                    editTextRegisterParentMobile.requestFocus();
                } else if (!mobileMatcherParent.find()) {
                    editTextRegisterParentMobile.setError("Invalid Mobile Number.");
                    editTextRegisterParentMobile.requestFocus();
                }else if (TextUtils.isEmpty(textPwd)) {
                    editTextRegisterPassword.setError("Password is Required");
                    editTextRegisterPassword.requestFocus();
                }else if (textPwd.length() < 6) {
                    editTextRegisterPassword.setError("Password too short. Should be at-least 6 digits.");
                    editTextRegisterPassword.requestFocus();
                }else if (TextUtils.isEmpty(textConfirmPwd)) {
                    editTextRegisterConfirmPassword.setError("Please confirm your Password");
                    editTextRegisterConfirmPassword.requestFocus();
                }else if (!textConfirmPwd.equals(textPwd) ) {
                    editTextRegisterConfirmPassword.setError("Password did not match.");
                    editTextRegisterConfirmPassword.requestFocus();
                    //clears the entered password
                    editTextRegisterPassword.clearComposingText();
                    editTextRegisterConfirmPassword.clearComposingText();
                }
                else {
                    textGender = radioButtonRegisterGenderSelected.getText().toString();
                    progressBar.setVisibility(View.VISIBLE);

                    registerUser(textFullName, textEnrollno, textEmail, textDoB, textGender,textMoblie, textPwd, textParentFullName, textParentEmail, textParentMoblie);
                }
            }

            private void registerUser(String textFullName, String textEnrollno, String textEmail, String textDoB, String textGender, String textMoblie, String textPwd, String textParentFullName, String textParentEmail, String textParentMoblie) {

                Toast.makeText(RegisterActivity.this, "Email is : "+textEmail, Toast.LENGTH_SHORT).show();
                //Register to firebase according to the credentials given
                FirebaseAuth auth  = FirebaseAuth.getInstance();

                //Create User Profile using Email and password
                auth.createUserWithEmailAndPassword(textEmail,textPwd).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){

                            FirebaseUser firebaseUser = auth.getCurrentUser();

                            //Update Display Name of the User
                            UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(textFullName).build();

                            firebaseUser.updateProfile(profileChangeRequest);

                            Toast.makeText(RegisterActivity.this, "Saving Data to Database...", Toast.LENGTH_SHORT).show();
                            //Enter User data into Firebase Realtime database
                            ReadWriteUserDetails writeUserDetails = new ReadWriteUserDetails( textEnrollno, textDoB, textGender, textMoblie, textParentFullName, textParentEmail, textParentMoblie);


//                            //Extracting user reference from database for "Registered Users"
                            DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Users").child("student");
                            referenceProfile.child(firebaseUser.getUid()).setValue(writeUserDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(RegisterActivity.this, "User Registered Successfully.", Toast.LENGTH_SHORT).show();

                                        //Send Verification Email
                                        firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Toast.makeText(RegisterActivity.this, "Email Verification has been send to your email", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                        //Open user profile after successfully Registration
                                        Intent intent = new Intent(RegisterActivity.this, UserProfileActivity.class);

                                        //To prevent User from returning back to Register Activity on pressing back button after registration.
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

                                        startActivity(intent);
                                        finish();       //to close Registration Activity
                                    }
                                    else{
                                        Toast.makeText(RegisterActivity.this, "The Registration has Failed !!!", Toast.LENGTH_SHORT).show();
                                    }
                                    //Hide progressBar whether the user is creation is successfully or not.
                                    progressBar.setVisibility(View.GONE);
                                }
                            });
                        }
                    }
                });
            }
        });
    }
}