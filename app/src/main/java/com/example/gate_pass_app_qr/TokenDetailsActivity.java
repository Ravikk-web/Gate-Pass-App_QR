package com.example.gate_pass_app_qr;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

public class TokenDetailsActivity extends AppCompatActivity {

    EditText studentNameEditText, studentEnrollEditText, studentReasonEditText;
    ImageButton saveTokenBtn;
    TextView pageTitleTextView, tokenStatusTextView;
    String studentName,studentId, studentReason,tokenStatus, docId;
    boolean isEditMode = false;
    TextView deleteTokenTextViewBtn;
    Timestamp currentTimestamp = Timestamp.now();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_token_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        studentNameEditText=findViewById(R.id.studentName);
        studentEnrollEditText=findViewById(R.id.studentId);
        studentReasonEditText = findViewById(R.id.studentReason);
        tokenStatusTextView = findViewById(R.id.textView_tokenStatus);
        saveTokenBtn=findViewById(R.id.saveTokenButton);
        deleteTokenTextViewBtn=findViewById(R.id.deleteTokenTextViewBtn);
        deleteTokenTextViewBtn.setVisibility(View.GONE);

        pageTitleTextView=findViewById(R.id.PageTitle);

        //Receive Data from intent
        studentName = getIntent().getStringExtra("studentName");
        studentId = getIntent().getStringExtra("studentId");
        studentReason = getIntent().getStringExtra("studentReason");
        tokenStatus = getIntent().getStringExtra("status");
        docId = getIntent().getStringExtra("docId");

        if (docId != null && !docId.isEmpty()){
            isEditMode = true;
        }

        studentNameEditText.setText(studentName);
        studentEnrollEditText.setText(studentId);
        studentReasonEditText.setText(studentReason);
        tokenStatusTextView.setText(tokenStatus);

        if (isEditMode){
            pageTitleTextView.setText("Edit the Token.");
            deleteTokenTextViewBtn.setVisibility(View.VISIBLE);
        }

        saveTokenBtn.setOnClickListener((v)-> saveToken());

        deleteTokenTextViewBtn.setOnClickListener((v)->deleteTokenFromFirebase());

    }

    void saveToken(){

        Token token = new Token();

        String studentName = studentNameEditText.getText().toString();
        String studentId = studentEnrollEditText.getText().toString();
        String studentReason = studentReasonEditText.getText().toString();
        String tokenStatus = token.getTokenStatus();

        //Toast.makeText(this, "Save Token -> TokenStatus: "+tokenStatus, Toast.LENGTH_SHORT).show();

        if(studentName == null || studentName.isEmpty()){
            studentNameEditText.setError("Name Is is Required");
            return;
        } else if (studentId == null || studentId.isEmpty()) {
            studentEnrollEditText.setError("Enrollment Number is Required");
        }
        if(tokenStatus == null || tokenStatus.isEmpty()){
            tokenStatus = "pending";
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
        if (isEditMode){
            //Update the note
            documentReference = utility.getCollectionRefereneceForTokens().document(docId);
            Token token1 = new Token();
            token1.setTokenStatus("pending");

        }
        else {
            //Create a new note
            documentReference = utility.getCollectionRefereneceForTokens().document();
        }
        documentReference.set(token).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    //Notes is added
                    utility.showToast(TokenDetailsActivity.this,"Token added Succesfully");
                    finish();
                }else{
                    //note is not added
                    utility.showToast(TokenDetailsActivity.this,"Failed while adding notes !");
                }
            }
        });
    }

    void deleteTokenFromFirebase(){
        DocumentReference documentReference;
        documentReference = utility.getCollectionRefereneceForTokens().document(docId);

        documentReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    //Note is deleted
                    utility.showToast(TokenDetailsActivity.this,"Token Deleted Succesfully");
                    finish();
                }else{
                    //note is not added
                    utility.showToast(TokenDetailsActivity.this,"Failed to delete Token !");
                }
            }
        });
    }

}