package com.example.gate_pass_app_qr;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class RequestGatePassTokenActivity extends AppCompatActivity {
    private FirebaseAuth authProfile;
    private Button buttonRequestPass, buttonRevokePass;
    private TextView textViewShowStudentName, textViewShowStudentEnrollNo, textViewShowStudentTimeStamp, textViewShowStudentTokenStatus;
    private EditText editTextGetStudentReason;
    private String studentReason, tokenStatus, docId, timeStamp;
    private static String TokenDetailsDocId =null, TokenDetailsUserId=null, studentName, studentId;
    static String parentEmail;
    private static String parentMobile;
    private FirebaseFirestore db;
    private static final String CHANNEL_ID = "swift_exit_channel_id";
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
        TokenDetailsUserId = authProfile.getCurrentUser().getUid();

        textViewShowStudentName = findViewById(R.id.textView_show_student_name);
        textViewShowStudentEnrollNo = findViewById(R.id.textView_show_student_id);
        editTextGetStudentReason = findViewById(R.id.editText_get_student_reason);
        textViewShowStudentTimeStamp = findViewById(R.id.editText_get_student_timestamp);
        textViewShowStudentTokenStatus = findViewById(R.id.editText_get_student_token_status);

        progressBar = findViewById(R.id.progressBar);
        buttonRequestPass = findViewById(R.id.button_requestGPT);
        buttonRevokePass = findViewById(R.id.button_deleteGPT);
        FirebaseUser firebaseUser = authProfile.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        buttonRequestPass.setEnabled(false);
        editTextGetStudentReason.setEnabled(false);
        textViewShowStudentTimeStamp.setVisibility(View.VISIBLE);
        textViewShowStudentTokenStatus.setVisibility(View.VISIBLE);


        //getting the details from the server
        if (authProfile.getCurrentUser() != null) {
            progressBar.setVisibility(View.VISIBLE);

            //Set the Student name and ID
            getStudentNameAndEnroll(firebaseUser);
            //Check if the Document is available or not
            checkAvailableDocument();

        } else {
            Toast.makeText(this, "Error getting Details.", Toast.LENGTH_SHORT).show();
        }

        Query query =  utility.getCollectionRefereneceForTokens().orderBy("timestamp", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<Token> options = new FirestoreRecyclerOptions.Builder<Token>().setQuery(query, Token.class).build();

        //request Button
        buttonRequestPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // on click of request button
                progressBar.setVisibility(View.VISIBLE);
                saveToken();
            }
        });

        buttonRevokePass.setOnClickListener((v)->showAlertDialog());
    }

    private void checkIfApproved(String uid) {
        FirebaseFirestore.getInstance().collection("tokens").whereEqualTo("passRequestUserId", uid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                DocumentReference documentRef = document.getReference();
                                String documentId = documentRef.getId();
                                String token = document.getString("token");

                                // Create a hash map to update the document
                                Map<String, Object> updateData = new HashMap<>();
                                updateData.put("parentEmail", parentEmail); // Replace with the actual email address
                                updateData.put("parentMobile", parentMobile); // Replace with the actual email address

                                // Update the document with the new field
                                documentRef.update(updateData)
                                        .addOnSuccessListener(aVoid -> {
                                            // Successfully updated the document
                                            sendDataForQR(token, documentId);
                                        })
                                        .addOnFailureListener(e -> {
                                            // Handle update failure
                                            Log.e("Firestore Error", "Error updating document: " + e.getMessage());
                                            Toast.makeText(RequestGatePassTokenActivity.this, "Failed to update Parent Email.", Toast.LENGTH_SHORT).show();
                                        });
                            }

                        } else {
                            Log.w("Fire Store Error", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void saveTokenDetials(Token token, String DocumentId) {

        //[TokenDetailsUserId] get the token's user id.
        TokenDetailsDocId = DocumentId;


        TokenDetails tokenDetails = new TokenDetails();

        tokenDetails.setStudentName(token.getStudentName());
        tokenDetails.setStudentId(token.getStudentId());
        tokenDetails.setStudentReason(token.getStudentReason());
        tokenDetails.setTokenStatus(token.getTokenStatus());
        tokenDetails.setTimestamp(token.getTimestamp());
        tokenDetails.setPassRequestUserId(TokenDetailsUserId);

        tokenDetails.setPassRequestUserDocId(TokenDetailsDocId);

        //Save the token to firebase:
        saveTokenDetailsToFirebase(tokenDetails);
    }

    private void checkAvailableDocument() {

        Task<QuerySnapshot> task= utility.getCollectionRefereneceForTokens().get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot snapshot = task.getResult();
                    if (snapshot != null && snapshot.isEmpty()) {
                        // No documents found in the collection
                        Toast.makeText(RequestGatePassTokenActivity.this, "Please fill the 'reason' field.", Toast.LENGTH_SHORT).show();
                        editTextGetStudentReason.requestFocus();
                        buttonRequestPass.setEnabled(true);
                        editTextGetStudentReason.setEnabled(true);
                        textViewShowStudentTimeStamp.setVisibility(View.GONE);
                        textViewShowStudentTokenStatus.setVisibility(View.GONE);
                        buttonRevokePass.setVisibility(View.GONE);

                    } else {
                        // Documents exist in the collection
                        String tokenStatus = null;

                        for (DocumentSnapshot doc : snapshot) {
                            // Perform actions based on the data (e.g., display in a list)

                            //STATIC VARIABLES
                            studentName = doc.getString("studentName");
                            studentId = doc.getString("studentId");

                            studentReason = doc.getString("studentReason");
                            tokenStatus = doc.getString("tokenStatus");
                            timeStamp = utility.timeStampToString(doc.getTimestamp("timestamp"));
                            docId = doc.getId();
                            TokenDetailsDocId = doc.getId();

                            editTextGetStudentReason.setText(studentReason);
                            textViewShowStudentTimeStamp.setText(timeStamp);

                            textViewShowStudentTokenStatus.setText(tokenStatus);

                            sendNotification("Token is PENDING",studentReason);

                        }
                        Toast.makeText(RequestGatePassTokenActivity.this, "Token already Available for the User.", Toast.LENGTH_SHORT).show();

                        if (tokenStatus != null && tokenStatus.equals("approved")){
                            checkIfApproved(authProfile.getCurrentUser().getUid());
                        }

                    }
                } else {
                    Toast.makeText(RequestGatePassTokenActivity.this, "Error getting documents: ", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void sendDataForQR(String token,String documentId) {
        String data = dataUtils.combineAndEncode(token, documentId);

        //push Notification
        sendNotification("Token already Available",data);

        Intent intent = new Intent(RequestGatePassTokenActivity.this,GenerateQRCodeActivity.class);
        intent.putExtra("data",data);
        RequestGatePassTokenActivity.this.startActivity(intent);
        finish();
    }


    void saveToken() {

        Token token = new Token();
        studentName = textViewShowStudentName.getText().toString();
        studentId = textViewShowStudentEnrollNo.getText().toString();
        studentReason = editTextGetStudentReason.getText().toString();
        tokenStatus = "pending";
        docId = null;


        if (studentReason == null || studentReason.isEmpty()) {
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

    void saveNoteToFirebase(Token token) {
        DocumentReference documentReference;
        documentReference = utility.getCollectionRefereneceForTokens().document();
        documentReference.set(token).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    String documentId = documentReference.getId(); // Get document ID

                    //Notes is added
                    utility.showToast(RequestGatePassTokenActivity.this, "Pass Applied Successfully.");

                    // ... (rest of your success logic)
                    saveTokenDetials(token, documentId);
                    progressBar.setVisibility(View.GONE);
                    finish();
                } else {
                    //note is not added
                    utility.showToast(RequestGatePassTokenActivity.this, "Failed while applying pass !");
                }

            }
        });

        //This Method saved another collection of the token details.
        //progressBar.setVisibility(View.GONE);
    }
    void saveTokenDetailsToFirebase(TokenDetails token) {
        DocumentReference documentReference;
        documentReference = utility.getCollectionRefereneceForTokensDetails().document();
        documentReference.set(token).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    //Notes is added
                    utility.showToast(RequestGatePassTokenActivity.this, "Token Details saved Successfully.");

                    progressBar.setVisibility(View.GONE);
                    finish();
                } else {
                    //note is not added
                    utility.showToast(RequestGatePassTokenActivity.this, "Failed while saving Token Details !");
                }
            }
        });

        //This Method saved another collection of the token details.
        progressBar.setVisibility(View.GONE);
    }

    private void sendNotification(String title, String body) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Swift Exit Notification Channel", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("This channel shows the status of the tokens/ passes.");
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo_gpa_no_bg) // Your notification icon resource ID
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(18, builder.build());
    }


    private void getStudentNameAndEnroll(FirebaseUser firebaseUser) {
        String userID = firebaseUser.getUid();
        //Extracting user reference from the database.
        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Users").child("student");
        referenceProfile.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ReadWriteUserDetails readUserDetails = snapshot.getValue(ReadWriteUserDetails.class);
                if (readUserDetails != null) {
                    studentName = firebaseUser.getDisplayName();
                    studentId = readUserDetails.enrollno;

                    RequestGatePassTokenActivity.parentEmail = readUserDetails.parentEmail;
                    RequestGatePassTokenActivity.parentMobile = readUserDetails.parentMobile;

                    textViewShowStudentName.setText(studentName);
                    textViewShowStudentEnrollNo.setText(studentId);
                } else {
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

    private void showAlertDialog() {
        // Setup the Alert Builder

        AlertDialog.Builder builder = new AlertDialog.Builder(RequestGatePassTokenActivity.this);
        builder.setTitle("REVOKE PASS");
        builder.setMessage("Do You really want to Revoke your Pass. Once Revoked, your pass will be deleted from Query.");

        //open the email app if user clidks/ taps continue button
        builder.setPositiveButton("continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteTokenFromFirebase();
            }
        });
        //Create the alert Dialog box
        AlertDialog alertDialog = builder.create();

        //Show the Alert box
        alertDialog.show();
    }

    void deleteTokenFromFirebase(){
        DocumentReference documentReference;
        documentReference = utility.getCollectionRefereneceForTokens().document(docId);

        documentReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    //Note is deleted
                    deleteTokenDetailsFromFirebase(docId);
                    utility.showToast(RequestGatePassTokenActivity.this,"Token Revoked Succesfully");
                    finish();
                }else{
                    //note is not added
                    utility.showToast(RequestGatePassTokenActivity.this,"Failed to revoke Token!");
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
                                    utility.showToast(RequestGatePassTokenActivity.this,"Token Deleted.");
                                    finish();
                                }else{
                                    //note is not added
                                    utility.showToast(RequestGatePassTokenActivity.this,"Failed to delete Token");
                                }
                            }
                        });
                    }
                } else {
                    Log.w("Firestore Error", "Error getting documents: ", task.getException());
                }
            }
        });
    }
}