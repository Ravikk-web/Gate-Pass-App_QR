package com.example.gate_pass_app_qr;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TokenDetailsActivity extends AppCompatActivity {

    EditText studentNameEditText, studentEnrollEditText, studentReasonEditText;
    ImageButton saveTokenBtn;
    TextView pageTitleTextView, tokenStatusTextView;
    String studentName,studentId, studentReason,tokenStatus, docId, passRequestUserId;
    boolean isEditMode = false;
    TextView deleteTokenTextViewBtn;
    Button approveTokenBtn;
    Timestamp currentTimestamp = Timestamp.now();
    ProgressBar progressBar;
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
        progressBar = findViewById(R.id.progressBar);

        //Receive Data from intent
        studentName = getIntent().getStringExtra("studentName");
        studentId = getIntent().getStringExtra("studentId");
        studentReason = getIntent().getStringExtra("studentReason");
        tokenStatus = getIntent().getStringExtra("status");
        docId = getIntent().getStringExtra("passRequestDocId");
//        String passRequestUserDocId = getIntent().getStringExtra("passRequestDocId");
        passRequestUserId = getIntent().getStringExtra("passRequestUserId");


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
            studentNameEditText.setEnabled(false);
            studentEnrollEditText.setEnabled(false);
        }

        saveTokenBtn.setOnClickListener((v)-> saveToken());

        deleteTokenTextViewBtn.setOnClickListener((v)->showAlertDialog(0,"DELETE PASS","Do you Really want to Delete the Pass? This Action is irreversible."));

        approveTokenBtn = findViewById(R.id.approveTokenTextViewBtn);
        approveTokenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                approveTokenBtn.setEnabled(false);
                studentReasonEditText.setEnabled(false);
                showAlertDialog(1,"APPROVE PASS", "Do you want to Approve the pass for "+studentName.toUpperCase()+" !");
            }
        });
    }

    private void showAlertDialog(int task, String headText, String contextText) {
        // Setup the Alert Builder

        AlertDialog.Builder builder = new AlertDialog.Builder(TokenDetailsActivity.this);
        builder.setTitle(headText);
        builder.setMessage(contextText);

        //open the email app if user clidks/ taps continue button
        builder.setPositiveButton("continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (task==1){
                    approveToken(passRequestUserId, docId);
                }
                else if (task == 0){
                    deleteTokenFromFirebase();
                }
                else {
                    Toast.makeText(TokenDetailsActivity.this, "Something went wrong !", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                approveTokenBtn.setEnabled(true);
                studentReasonEditText.setEnabled(true);

            }
        });

        //Create the alert Dialog box
        AlertDialog alertDialog = builder.create();

        //Show the Alert box
        alertDialog.show();
    }

    private void approveToken(String passRequestUserId, String passRequestUserDocId) {
        progressBar.setVisibility(View.VISIBLE);
        deleteTokenTextViewBtn.setVisibility(View.GONE);

        //getToken Details and save the Token.
        utility.getCollectionRefereneceForTokensDetails().whereEqualTo("passRequestUserDocId", passRequestUserDocId)
                .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        DocumentReference documentRef = document.getReference();
                                        String documentId = documentRef.getId();

                                        //Document Id fetched

                                        //now generate the new Token
                                        generateAndSaveToken(passRequestUserId, passRequestUserDocId, documentId);

                                    }
                                } else {
                                    Log.w("Fire Store Error", "Error getting documents: ", task.getException());
                                }
                            }
                        });
    }

    private void generateAndSaveToken(String passRequestUserId, String passRequestUserDocId, String passDetailsDocId) {
        // Generate a unique token
        String token = utility.generateUniqueToken();
        String approvedBy = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        // Save the token to Firebase
        Map<String, Object> tokenData = new HashMap<>();
        tokenData.put("token", token);
        tokenData.put("passRequestUserId", passRequestUserId);
        tokenData.put("passRequestUserDocId", passRequestUserDocId);
        tokenData.put("passDetailsDocId", passDetailsDocId);
        tokenData.put("approvedBy", approvedBy);
        tokenData.put("createdAt", currentTimestamp);
        tokenData.put("redeemed", false);

        FirebaseFirestore.getInstance().collection("tokens").add(tokenData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        // Token saved successfully
                        Toast.makeText(TokenDetailsActivity.this, "TOKEN\nGENERATED", Toast.LENGTH_SHORT).show();

                        //Update the token status for the TokenDetails ans pass Request;
                        updateTokenDetails_tokenStatus(passDetailsDocId, passRequestUserId, passRequestUserDocId);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@org.checkerframework.checker.nullness.qual.NonNull Exception e) {
                        // Handle error
                        Toast.makeText(TokenDetailsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateTokenDetails_tokenStatus(String DocumentId, String passRequestUserId, String passRequestUserDocId) {

        // Create a Map to hold the updated fields and the new field
        Map<String, Object> updates = new HashMap<>();
        updates.put("tokenStatus", "approved");
        updates.put("approvedBy", FirebaseAuth.getInstance().getCurrentUser().getUid());  // Add the new field

        //Change the token Status of the tokenDetails

        FirebaseFirestore.getInstance().collection("passDetails").document(DocumentId)
                .update(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(TokenDetailsActivity.this, "Token 'Approved' ", Toast.LENGTH_SHORT).show();
                        Log.w("TokenDetailsActivity", " 'tokenStatus' For TokenDetails table updated to 'approved'");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("TokenDetailsActivity", e.getMessage());
                    }
                });

        //Change the token Status of the passRequest
        FirebaseFirestore.getInstance().collection("passRequests").document(passRequestUserId).collection("my_tokens").document(passRequestUserDocId)
                .update(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(TokenDetailsActivity.this, "Token 'Approved' ", Toast.LENGTH_SHORT).show();
                        Log.w("TokenDetailsActivity", " 'tokenStatus' For TokenDetails table updated to 'approved'");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("TokenDetailsActivity", e.getMessage());
                    }
                });
        progressBar.setVisibility(View.GONE);
        finish();
    }

    void saveToken(){

        Token token = new Token();
        String studentName = studentNameEditText.getText().toString();
        String studentId = studentEnrollEditText.getText().toString();
        String studentReason = studentReasonEditText.getText().toString();
        String tokenStatus = token.getTokenStatus();


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
            documentReference = utility.getCollectionRefereneceForTokens(passRequestUserId).document(docId);
            Token token1 = new Token();
            token1.setTokenStatus("pending");
        }
        else {
            //Create a new note
            documentReference = utility.getCollectionRefereneceForTokens(passRequestUserId).document();
        }
        documentReference.set(token).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    //Notes is added
                    utility.showToast(TokenDetailsActivity.this,"Token added Succesfully");
                    saveTokenDetials(token, passRequestUserId, docId);
                    finish();
                }else{
                    //note is not added
                    utility.showToast(TokenDetailsActivity.this,"Failed while adding Token !");
                }
            }
        });
    }

    private void saveTokenDetials(Token token, String refUserId, String DocumentId) {

        //[TokenDetailsUserId] get the token's user id.
        TokenDetails tokenDetails = new TokenDetails();

        tokenDetails.setStudentName(token.getStudentName());
        tokenDetails.setStudentId(token.getStudentId());
        tokenDetails.setStudentReason(token.getStudentReason());
        tokenDetails.setTokenStatus(token.getTokenStatus());
        tokenDetails.setTimestamp(token.getTimestamp());
        tokenDetails.setPassRequestUserId(refUserId);

        tokenDetails.setPassRequestUserDocId(DocumentId);

        //Save the token to firebase:
        saveTokenDetailsToFirebase(tokenDetails,DocumentId );
    }

    void saveTokenDetailsToFirebase(TokenDetails token,String DocumentId) {
        utility.getCollectionRefereneceForTokensDetails().whereEqualTo("passRequestUserDocId", DocumentId)
                        .get()
                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        if (!queryDocumentSnapshots.isEmpty()) {
                                            DocumentSnapshot tokenDoc = queryDocumentSnapshots.getDocuments().get(0);
                                            String studentReason = tokenDoc.getString("studentReason");
                                            if (studentReason != null && !studentReason.isEmpty()) {
                                                // Mark the token as redeemed
                                                tokenDoc.getReference().update("studentReason", token.studentReason,
                                                        "Edited by:", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                                // Handle token redemption logic here
                                                Toast.makeText(TokenDetailsActivity.this, "Token Edited Successfully.", Toast.LENGTH_SHORT).show();
                                            } else {
                                                // Token is already redeemed
                                                Toast.makeText(TokenDetailsActivity.this, "Token Details are empty..", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            // Token does not exist
                                            Toast.makeText(TokenDetailsActivity.this, "Token does Not Exist.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@org.checkerframework.checker.nullness.qual.NonNull Exception e) {
                        // Handle error
                        Toast.makeText(TokenDetailsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    void deleteTokenFromFirebase(){

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference tokensCollection = FirebaseFirestore.getInstance().collection("tokens");
        Query query = tokensCollection.whereEqualTo("passRequestUserDocId", docId);

        // Get documents matching the query
        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Get the Document ID
                                String documentId = document.getId();

                                // Delete the document with the retrieved ID
                                tokensCollection.document(documentId)
                                        .delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d("Firestore", "Document deleted with ID: " + documentId);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w("Firestore Error", "Error deleting document: ", e);
                                            }
                                        });

                                // You can optionally break out of the loop after finding the first document
                                // if you only want to delete the first matching document
                                break;
                            }
                        } else {
                            Log.w("Firestore Error", "Error getting documents: ", task.getException());
                        }
                    }
                });

        DocumentReference documentReference;
        documentReference = utility.getCollectionRefereneceForTokens(passRequestUserId).document(docId);
        documentReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    //Token is deleted
                    deleteTokenDetailsFromFirebase(docId);

                    utility.showToast(TokenDetailsActivity.this,"Token Deleted Succesfully");
                    finish();
                }else{
                    //note is not added
                    utility.showToast(TokenDetailsActivity.this,"Failed to delete Token !");
                }
            }
        });
    }

    void deleteTokenDetailsFromFirebase(String docId){
        utility.getDocumentReferencesToDeleteTokens(docId, new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        DocumentReference documentRef = document.getReference();
                        documentRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    //Note is deleted
                                    utility.showToast(TokenDetailsActivity.this,"Token Deleted.");
                                    finish();
                                }else{
                                    //note is not added
                                    utility.showToast(TokenDetailsActivity.this,"Failed to delete Token");
                                }
                            }
                        });
                    }
                } else {
                    Log.w("Fire Store Error", "Error getting documents: ", task.getException());
                }
            }
        });
    }

}