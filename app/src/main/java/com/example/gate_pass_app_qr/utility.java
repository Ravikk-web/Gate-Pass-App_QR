package com.example.gate_pass_app_qr;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class utility {
    static  void showToast(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    static CollectionReference getCollectionRefereneceForTokens(){
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;
        return FirebaseFirestore.getInstance().collection("passRequests").document(currentUser.getUid()).collection("my_tokens");
    }

    static CollectionReference getCollectionRefereneceForTokens(String refUserid){
        return FirebaseFirestore.getInstance().collection("passRequests").document(refUserid).collection("my_tokens");
    }

    static CollectionReference getCollectionRefereneceForTokensDetails() {
        return FirebaseFirestore.getInstance().collection("passDetails");
    }

    static void getDocumentReferencesToDeleteTokens(String docId, OnCompleteListener<QuerySnapshot> listener) {
        Query query = FirebaseFirestore.getInstance().collection("passDetails")
                .whereEqualTo("passRequestUserDocId", docId);

        query.get().addOnCompleteListener(listener);  // Pass OnCompleteListener for results
    }

    static CollectionReference getCollectionRefereneceForRecords() {
        return FirebaseFirestore.getInstance().collection("Records");
    }

    static String timeStampToString(Timestamp timestamp){
        return (new SimpleDateFormat("MM/dd/yyyy").format(timestamp.toDate()));
    }

    static Timestamp timeStampToReadable(Timestamp timestamp){
        // Convert Timestamp to milliseconds
        long originalSeconds = timestamp.getSeconds();
        int originalNanoseconds = timestamp.getNanoseconds();
        Timestamp readableTimestamp = new Timestamp(originalSeconds, originalNanoseconds);
        return readableTimestamp;
    }



    static String generateUniqueToken() {
        // Implement your token generation logic here
        return UUID.randomUUID().toString();
    }
}
