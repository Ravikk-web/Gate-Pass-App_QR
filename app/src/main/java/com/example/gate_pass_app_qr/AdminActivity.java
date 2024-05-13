package com.example.gate_pass_app_qr;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Arrays;
import java.util.Objects;

public class AdminActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private FirebaseAuth authProfile;
    private TextView message, textViewOR;
    private EditText editTextRedeemToken;
    private Button btnEnterToken, btnScanQR;
    private LinearLayout linearLayoutFillToken;
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
        btnEnterToken = findViewById(R.id.button_enter_token);
        btnScanQR = findViewById(R.id.button_scan_qr_code);
        linearLayoutFillToken = findViewById(R.id.linearlayout_fill_token);
        textViewOR = findViewById(R.id.textView_or);

        authProfile = FirebaseAuth.getInstance();

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

        //Enter Token Button Action
        btnEnterToken.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnEnterToken.setVisibility(View.GONE);
                linearLayoutFillToken.setVisibility(View.VISIBLE);
                textViewOR.setVisibility(View.GONE);
            }
        });

        //Enter scanQR Button Action
        btnScanQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnEnterToken.setVisibility(View.VISIBLE);
                linearLayoutFillToken.setVisibility(View.GONE);
                textViewOR.setVisibility(View.VISIBLE);

                startActivity(new Intent(AdminActivity.this, ScanQRCodeActivity.class));
            }
        });

        Button buttonRedeemToken = findViewById(R.id.button_Redeem_token);
        buttonRedeemToken.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String encodedtoken = editTextRedeemToken.getText().toString();
                //Decrypt the Token
                if (TextUtils.isEmpty(encodedtoken)){
                    editTextRedeemToken.setError("Please Enter the TOKEN to Redeem.");
                    editTextRedeemToken.requestFocus();
                } else if (encodedtoken.length() < 74 || encodedtoken.length() > 80) {
                    editTextRedeemToken.setError("Please Enter a Valid Pass Token");
                    editTextRedeemToken.requestFocus();
                } else{

                String[] tokenData = dataUtils.decodeAndSeparate(encodedtoken);
                String token = tokenData[0];
                String DocId = tokenData[1];

                Intent intent = new Intent(AdminActivity.this, onScanQRCodeSuccessActivity.class);
                intent.putExtra("token",token);
                intent.putExtra("docId",DocId);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                }

            }
        });
    }

}