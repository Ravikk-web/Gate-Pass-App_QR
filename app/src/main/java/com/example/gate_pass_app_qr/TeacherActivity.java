package com.example.gate_pass_app_qr;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class TeacherActivity extends AppCompatActivity {
    private FirebaseAuth authProfile;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseFirestore db;
    private TextView message;
    private EditText editTextTokenReason, editTextTokenStudentName, editTextTokenStudentId;
    private String tokenReason, studentName, studentId;
    private static final String TAG = "TeacherActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_teacher);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Objects.requireNonNull(getSupportActionBar()).setTitle("Gate In Charge");

        message = findViewById(R.id.textViewAdmin);
        message.setText("Welcome Faculty Member");

        editTextTokenReason = findViewById(R.id.editText_token_reason);
        editTextTokenStudentName = findViewById(R.id.editText_token_student_name);
        editTextTokenStudentId = findViewById(R.id.editText_token_student_id);


        authProfile = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        db = FirebaseFirestore.getInstance();

        Button logout = findViewById(R.id.button_logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(TeacherActivity.this, "Logout successfull !", Toast.LENGTH_SHORT).show();
                authProfile.signOut();
                startActivity(new Intent(TeacherActivity.this, MainActivity.class));
                finish();
            }
        });

        // Generate token button click listener
        Button generateToken = findViewById(R.id.button_generate_token);
        generateToken.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tokenReason = editTextTokenReason.getText().toString();
                studentName = editTextTokenStudentName.getText().toString();
                studentId = editTextTokenStudentId.getText().toString();

                if(TextUtils.isEmpty(tokenReason)){
                    editTextTokenReason.setError("Reason is Required.");
                    editTextTokenReason.requestFocus();
                }else if (TextUtils.isEmpty(studentName)){
                    editTextTokenStudentName.setError("This Field is Required. Enter Student's name.");
                    editTextTokenStudentName.requestFocus();
                }else if (TextUtils.isEmpty(studentId)){
                    editTextTokenStudentId.setError("This Field is Required. Enter Student's Enrollment Number.");
                    editTextTokenStudentId.requestFocus();
                }else{
                    // create a progress bar.

                    //generate and save token to firebase Database.
                    generateAndSaveToken();
                }
            }
        });
    }

    private void generateAndSaveToken() {
        // Generate a unique token
        String token = generateUniqueToken();

        //String teacherId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Save the token to Firebase
        Map<String, Object> tokenData = new HashMap<>();
        tokenData.put("token", token);
        tokenData.put("studentId", studentId);
        tokenData.put("studentName", studentName);
        tokenData.put("createdAt", System.currentTimeMillis());
        tokenData.put("redeemed", false);

        db.collection("tokens").add(tokenData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        // Token saved successfully
                        Toast.makeText(TeacherActivity.this, "Token saved/ Generated.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle error
                        Toast.makeText(TeacherActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, e.getMessage());
                    }
                });
    }
    private String generateUniqueToken() {
        // Implement your token generation logic here
        return UUID.randomUUID().toString();
    }

}