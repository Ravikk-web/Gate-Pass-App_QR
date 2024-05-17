package com.example.gate_pass_app_qr;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth authProfile;
    private TextView message;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Objects.requireNonNull(getSupportActionBar()).hide();
        getSupportActionBar().hide();

        message = findViewById(R.id.textView_MA_head);
        authProfile = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        Button loginBtn = findViewById(R.id.btn_MA_login);
        Button registerBtn = findViewById(R.id.btn_MA_register);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        //open register Activity
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getCollegeCode();
            }
        });

    }

    private void getCollegeCode() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter the 7 digit College Code");

        // Create an EditText for user input
        final EditText inputEditText = new EditText(this);
        inputEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(inputEditText);

        // Set positive button action
        builder.setPositiveButton("OK", (dialog, which) -> {
            String userInput = inputEditText.getText().toString();
            try {
                int number = Integer.parseInt(userInput);
                // Handle the number (e.g., save it, display it, etc.)
                if (number == 2840192){
                    progressBar.setVisibility(View.VISIBLE);
                        new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                            progressBar.setVisibility(View.GONE);
                            startActivity(intent);
                        }
                    },2000);
                }
                else{
                    Toast.makeText(this, "Invalid Code", Toast.LENGTH_SHORT).show();
                    recreate();
                }

            } catch (NumberFormatException e) {
                // Handle invalid input (not a valid number)
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                recreate();
            }
        });

        // Set negative button action (optional)
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            // Handle cancel action (if needed)
            // ...
        });

        // Show the dialog
        builder.show();
    }
}