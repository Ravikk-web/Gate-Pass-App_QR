package com.example.gate_pass_app_qr;

import static com.example.gate_pass_app_qr.RequestGatePassTokenActivity.parentEmail;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.rxjava3.exceptions.Exceptions;

public class onScanQRCodeSuccessActivity extends AppCompatActivity {

    private TextView textView;
    private String token, DocId;
    Timestamp currentTimestamp = Timestamp.now();
    private ProgressBar progressBar;
    private String gateKeeperName, teacherName, StudentUserId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_on_scan_qrcode_success);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //make the activity un-exit able.
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        // Set the flag before starting the process

        textView = findViewById(R.id.textView_show_state);
        textView.setVisibility(View.GONE);

        token = getIntent().getStringExtra("token");

        // this is the Document Id of 'passDetails'
        DocId = getIntent().getStringExtra("docId");

        getGateKeepername();

        progressBar = findViewById(R.id.progressBar);

        checkAvailibilityOfData(DocId);
        progressBar.setVisibility(View.VISIBLE);

    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Toast.makeText(this, "please do not exit this page until the process finishes.", Toast.LENGTH_SHORT).show();
    }

    private void getGateKeepername() {
        //Set the static value for Gate Keeper
        String GateKeeperName, TeacherName;
        String CurrentUserUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Users").child("admin");
        referenceProfile.child(CurrentUserUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                        if (childSnapshot.getKey().equals("name")) {
                            String name = (String) childSnapshot.getValue();
                            // Use the retrieved name

                            gateKeeperName = name;
                            Log.d("Firebase", "Name retrieved: " + name);
                            break; // Exit loop after finding the "name" value
                        }
                        else {
                            Toast.makeText(onScanQRCodeSuccessActivity.this, "Couldnot find key 'name'", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Log.d("Firebase", "No data found");
                    Toast.makeText(onScanQRCodeSuccessActivity.this, "No data found", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("Firebase Error", "Failed to read value:", error.toException());
                Toast.makeText(onScanQRCodeSuccessActivity.this, "Failed to reade Value", Toast.LENGTH_SHORT).show();
            }
        });
        Toast.makeText(onScanQRCodeSuccessActivity.this, "Gname: "+gateKeeperName, Toast.LENGTH_SHORT).show();
    }

    private void checkAvailibilityOfData(String docId) {
        DocumentReference docRef = FirebaseFirestore.getInstance().collection("tokens").document(docId);

        docRef.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // Document exists, handle it
                                Toast.makeText(onScanQRCodeSuccessActivity.this, "Processing...", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.VISIBLE);

                                //After verifying that the data is available we start the process to recording.
                                getTokeDataReference();

                            } else {
                                // Document does not exist, handle it
                                Log.d("onScanQRCodeSuccessActivity", "Document does not exist!");
                                progressBar.setVisibility(View.GONE);
                                textView.setText("The provided Token is Invalid. please Re-Scan/ Re-Enter the Token.");
                                textView.setVisibility(View.VISIBLE);
                                Toast.makeText(onScanQRCodeSuccessActivity.this, "This Activity will close in 3 seconds.", Toast.LENGTH_SHORT).show();
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {

                                        finish();
                                    }
                                }, 3500);
                            }
                        } else {
                            Log.w("onScanQRCodeSuccessActivity", "Error getting document:", task.getException());
                        }
                    }
                });
    }

    private void showOnSuccessDeleted(){
        progressBar.setVisibility(View.GONE);
        textView.setText("CONGRATULATIONS\nYour pass is VALID and is VERIFIED\n\nYou can exit the gate NOW..."+"\n\nTOKEN "+token+"\nDocId : "+DocId );
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                textView.setText("This Page will close in 5 Seconds.");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                },5000);
            }
        },2000);
    }

    private void getTokeDataReference() {
        DocumentReference DocRef =  FirebaseFirestore.getInstance().collection("tokens").document(DocId);
                DocRef.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String fetchedtoken = document.getString("token");
//                                String passDetailsDocId = document.getString("passDetailsDocId");
                                String passRequestUserId = document.getString("passRequestUserId");
                                String approvedBy = document.getString("approvedBy");
                                String parentEmail = document.getString("parentEmail");
                                String parentMobile = document.getString("parentMobile");
                                String passRequestUserDocId = document.getString("passRequestUserDocId");
                                // ... (Access other fields)

                                if (!fetchedtoken.isEmpty() && fetchedtoken.equals(token)) {
                                    StudentUserId = passRequestUserId;

                                    // Update fields
                                    DocRef.update("redeemed", true);  // Modify a field
                                    DocRef.update("Redeemed By", FirebaseAuth.getInstance().getCurrentUser().getUid());  // Modify a field
                                    // Set text view content with retrieved data
                                    textView.setText("Token Redeemed Successfully.");
                                    textView.setVisibility(View.VISIBLE);

                                    Toast.makeText(onScanQRCodeSuccessActivity.this, "Token Redeemed Successfully.\nPlease wait while the process is complete.\n\nDo not Move from this page", Toast.LENGTH_SHORT).show();
                                    getTeacherName(approvedBy);
                                   new Handler().postDelayed(new Runnable() {
                                       @Override
                                       public void run() {
                                           //Generating Report for the Redeemed Token
                                           generateReport(passRequestUserId, passRequestUserDocId, parentEmail, parentMobile);
                                       }
                                   },5000);

                                }else {
                                    Toast.makeText(onScanQRCodeSuccessActivity.this, "Token did not Match.", Toast.LENGTH_SHORT).show();
                                    finish();
                                }

                            } else {
                                Log.d("Firestore", "Document does not exist!");
                                Toast.makeText(onScanQRCodeSuccessActivity.this, "Token is Already Redeemed.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.w("Firestore Error", "Error getting document: ", task.getException());
                            Toast.makeText(onScanQRCodeSuccessActivity.this, "Invalid Token or Token does Not Exist.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void getTeacherName(String approvedBy) {
        //Get Teacher name (Approved By)
        DatabaseReference refProfile = FirebaseDatabase.getInstance().getReference("Users").child("teacher");
        refProfile.child(approvedBy).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                        if (childSnapshot.getKey().equals("name")) {
                            String name = (String) childSnapshot.getValue();
                            // Use the retrieved name
                            teacherName = name;
                            Log.d("Firebase", "Name retrieved: " + name);
                            break; // Exit loop after finding the "name" value
                        }
                        else {
                            Toast.makeText(onScanQRCodeSuccessActivity.this, "Could not find key 'name'", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Log.d("Firebase", "No data found");
                    Toast.makeText(onScanQRCodeSuccessActivity.this, "No data found", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("Firebase Error", "Failed to read value:", error.toException());
                Toast.makeText(onScanQRCodeSuccessActivity.this, "Failed to reade Value", Toast.LENGTH_SHORT).show();
            }
        });
        Toast.makeText(onScanQRCodeSuccessActivity.this, "Tname: "+teacherName, Toast.LENGTH_SHORT).show();
    }

    private void generateReport(String passRequestUserId, String passRequestUserDocId, String parentEmail, String parentMobile) {
        //here the 'passRequestUserId' is the Student's User ID
        //and 'passDetailsDocId' is the document id of 'passDetails' db.


        //Get the Student Details:
         //get The Details from 'passDetails'
        utility.getCollectionRefereneceForTokensDetails()
                        .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                DocumentReference documentRef = document.getReference();
                                                String StudentName = document.getString("studentName");
                                                String StudentId = document.getString("studentId");
                                                String StudentReason = document.getString("studentReason");
                                                Timestamp tokenGenerationTime = utility.timeStampToReadable(document.getTimestamp("timestamp"));

                                                //Document Id fetched

                                                //now generate and save the pass to Firebase Firestore Databse
                                                String PUid = StudentUserId;
                                                SaveRecordDataToFireStore(passRequestUserId, passRequestUserDocId, StudentName,StudentId,StudentReason,tokenGenerationTime, PUid, parentEmail, parentMobile);
                                            }
                                        } else {
                                            Log.w("Fire Store Error", "Error getting documents: ", task.getException());
                                            Toast.makeText(onScanQRCodeSuccessActivity.this, "Unable to access Fire Store Database", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

    }

    private void SaveRecordDataToFireStore(String passRequestUserId, String passRequestUserDocId, String studentName, String studentId, String studentReason, Timestamp tokenGenerationTime, String PUid, String parentEmail, String parentMobile) {
        //get Details getParentName, getParentEmail, getStudentEmail;

        Map<String, Object> recordData = new HashMap<>();
        recordData.put("Student Name", studentName);
        recordData.put("Student UserUID", PUid);
        recordData.put("Student Enrollment No", studentId);
        recordData.put("Student pass Reason", studentReason);
        recordData.put("Pass_Generation_Time", tokenGenerationTime);
        recordData.put("Pass_Exit_Time", currentTimestamp);
        recordData.put("Pass Generated By", teacherName);
        recordData.put("Pass Verified By", gateKeeperName);

        //Parent and student Details
        recordData.put("Parent Email", parentEmail);
        recordData.put("Parent Mobile No", parentMobile);

        textView.setVisibility(View.VISIBLE);
        textView.setText(recordData.toString());

        //Finally save the data to Firebase db
        utility.getCollectionRefereneceForRecords().add(recordData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        // Token saved successfully
                        Toast.makeText(onScanQRCodeSuccessActivity.this, "REPORT\nGENERATED", Toast.LENGTH_SHORT).show();

                        // Show some animation on Success
                        textView.setText("CONGRATULATIONS\nYour pass is VALID and is VERIFIED\n\nYou can exit the gate NOW..."+"\n\nTOKEN "+token+"\nDocId : "+DocId );
                        textView.setVisibility(View.VISIBLE);

                        deleteTokenFromFirebase(passRequestUserId, passRequestUserDocId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@org.checkerframework.checker.nullness.qual.NonNull Exception e) {
                        // Handle error
                        Toast.makeText(onScanQRCodeSuccessActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        progressBar.setVisibility(View.GONE);
    }

    void deleteTokenFromFirebase(String passRequestUserId, String docId){
        //here docId is 'passRequestUserDocId'

        //delete from 'tokens' database
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

        //Delete from the 'tokenDetails' database.
        DocumentReference documentReference;
        documentReference = utility.getCollectionRefereneceForTokens(passRequestUserId).document(docId);
        documentReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    //Token is deleted
                    deleteTokenDetailsFromFirebase(docId);

                    utility.showToast(onScanQRCodeSuccessActivity.this,"Token Deleted Succesfully");
                    finish();
                }else{
                    //note is not added
                    utility.showToast(onScanQRCodeSuccessActivity.this,"Failed to delete Token !");
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
                                    utility.showToast(onScanQRCodeSuccessActivity.this,"Token Deleted.");

                                    showOnSuccessDeleted();

                                }else{
                                    //note is not added
                                    utility.showToast(onScanQRCodeSuccessActivity.this,"Failed to delete Token");
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