package com.example.gate_pass_app_qr;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class RequestGatePassTokenActivity extends AppCompatActivity {

    private FirebaseAuth authProfile;
    private Button buttonRequestPass;
    private TextView textViewShowStudentName, textViewShowStudentEnrollNo;
    private EditText editTextGetStudentReason;
    private String studentName, studentId, studentReason, tokenStatus, docId;
    Timestamp currentTimestamp = Timestamp.now();
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_request_gate_pass_token);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        authProfile = FirebaseAuth.getInstance();

        textViewShowStudentName = findViewById(R.id.textView_show_student_name);
        textViewShowStudentEnrollNo = findViewById(R.id.textView_show_student_id);
        editTextGetStudentReason = findViewById(R.id.editText_get_student_reason);

        progressBar = findViewById(R.id.progressBar);
        buttonRequestPass = findViewById(R.id.button_requestGPT);
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        //getting the details from the server
        if (authProfile.getCurrentUser() !=null)
        {
            progressBar.setVisibility(View.VISIBLE);
            getStudentNameAndEnroll(firebaseUser);
        }
        else {
            Toast.makeText(this, "Error getting Details.", Toast.LENGTH_SHORT).show();
        }

//        Query query =  utility.getCollectionRefereneceForTokens().orderBy("timestamp", Query.Direction.DESCENDING);
//        FirestoreRecyclerOptions<Token> options = new FirestoreRecyclerOptions.Builder<Token>().setQuery(query, Token.class).build();
        buttonRequestPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // on click of request button
                progressBar.setVisibility(View.VISIBLE);
                saveToken();
            }
        });

    }

    void saveToken(){

        Token token = new Token();
        studentName = textViewShowStudentName.getText().toString();
        studentId =  textViewShowStudentEnrollNo.getText().toString();
        studentReason = editTextGetStudentReason.getText().toString();
        tokenStatus = "pending";
        docId = null;

        //Toast.makeText(this, "Save Token -> TokenStatus: "+tokenStatus, Toast.LENGTH_SHORT).show();

        if(studentReason == null || studentReason.isEmpty()){
            editTextGetStudentReason.setError("A valid Reason is Required !!");
            editTextGetStudentReason.requestFocus();
            progressBar.setVisibility(View.GONE);
            return;
        }

        token.setStudentName(studentName);
        token.setStudentId(studentId);
        token.setStudentReason(studentReason);
        token.setTokenStatus(tokenStatus);

        token.setTimestamp(currentTimestamp);

        saveNoteToFirebase(token);
    }

    void saveNoteToFirebase(Token token){
        DocumentReference documentReference;
        documentReference = utility.getCollectionRefereneceForTokens().document();
        documentReference.set(token).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    //Notes is added
                    utility.showToast(RequestGatePassTokenActivity.this,"Pass Applied Successfully.");
                    progressBar.setVisibility(View.GONE);
                    finish();
                }else{
                    //note is not added
                    utility.showToast(RequestGatePassTokenActivity.this,"Failed while applying pass !");
                }
            }
        });
        progressBar.setVisibility(View.GONE);
    }

    private void setNameAndIdToTextView(String studentName, String StudentId){
        //setting the details in the textView
            textViewShowStudentName.setText(studentName);
            textViewShowStudentEnrollNo.setText(StudentId);
    }
    private void getStudentNameAndEnroll(FirebaseUser firebaseUser) {
        String userID = firebaseUser.getUid();
        //Extracting user reference from the database.
        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Users").child("student");
        referenceProfile.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ReadWriteUserDetails readUserDetails = snapshot.getValue(ReadWriteUserDetails.class);
                if(readUserDetails != null){
                    studentName = firebaseUser.getDisplayName();
                    studentId = readUserDetails.enrollno;
                    setNameAndIdToTextView(studentName, studentId);
                }else {
                    Toast.makeText(RequestGatePassTokenActivity.this, "Something Went WRONG !", Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RequestGatePassTokenActivity.this, "Something Went Wrong.", Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

//    private void createNewToken(FirebaseUser firebaseUser) {
//
//        //Getting document reference from the firestrore dtabase
//        DocumentReference documentReference;
//
//        documentReference = utility.getCollectionRefereneceForTokens().document(docId);
//        Token token1 = new Token();
//        token1.setTokenStatus("pending");
//
//    }
}