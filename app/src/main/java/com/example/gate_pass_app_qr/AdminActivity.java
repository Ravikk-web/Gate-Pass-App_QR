package com.example.gate_pass_app_qr;

import android.content.Intent;
import android.os.Bundle;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Objects;

public class AdminActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private FirebaseAuth authProfile;
    private TextView message;
    private EditText editTextRedeemToken;
    private FirebaseFirestore db;
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Objects.requireNonNull(getSupportActionBar()).setTitle("Gate In Charge");

        message = findViewById(R.id.textViewAdmin);
        message.setText("Welcome Gate-Keeper");
        editTextRedeemToken = findViewById(R.id.editText_redeem_token);

        authProfile = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        Button logout = findViewById(R.id.button_logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AdminActivity.this, "Logout successfull !", Toast.LENGTH_SHORT).show();
                authProfile.signOut();
                startActivity(new Intent(AdminActivity.this, MainActivity.class));
                finish();
            }
        });

        Button buttonRedeemToken = findViewById(R.id.button_Redeem_token);
        buttonRedeemToken.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String token = editTextRedeemToken.getText().toString();
                redeemToken(token);
            }
        });
    }

    private void redeemToken(String token) {

        // Check if the token exists and is not redeemed
        db.collection("tokens").whereEqualTo("token", token)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot tokenDoc = queryDocumentSnapshots.getDocuments().get(0);
                            Boolean redeemed = tokenDoc.getBoolean("redeemed");
                            if (redeemed != null && !redeemed) {
                                // Mark the token as redeemed
                                tokenDoc.getReference().update("redeemed", true,
                                        "redeemedBy", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                // Handle token redemption logic here
                                Toast.makeText(AdminActivity.this, "Token Redeemed Successfully.", Toast.LENGTH_SHORT).show();
                            } else {
                                // Token is already redeemed
                                Toast.makeText(AdminActivity.this, "Token is Already Redeemed.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Token does not exist
                            Toast.makeText(AdminActivity.this, "Token does Not Exist.", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle error
                        Toast.makeText(AdminActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}