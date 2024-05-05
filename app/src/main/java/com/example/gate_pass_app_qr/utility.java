package com.example.gate_pass_app_qr;

import android.content.Context;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;

public class utility {
    static  void showToast(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    static CollectionReference getCollectionRefereneceForTokens(){
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;
        return FirebaseFirestore.getInstance().collection("passRequests").document(currentUser.getUid()).collection("my_tokens");
    }
    static String timeStampToString(Timestamp timestamp){
        return (new SimpleDateFormat("MM/dd/yyyy").format(timestamp.toDate()));
    }
}
