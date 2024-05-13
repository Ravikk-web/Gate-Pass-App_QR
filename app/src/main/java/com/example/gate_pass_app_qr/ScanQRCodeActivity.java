package com.example.gate_pass_app_qr;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.VIBRATE;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.UUID;

import eu.livotov.labs.android.camview.ScannerLiveView;
import eu.livotov.labs.android.camview.scanner.decoder.zxing.ZXDecoder;

public class ScanQRCodeActivity extends AppCompatActivity {

    private ScannerLiveView scannerLiveView;
    private TextView textViewScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_scan_qrcode);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        scannerLiveView = findViewById(R.id.camView);
        textViewScanner = findViewById(R.id.textView_scan_qrCode);

        textViewScanner.setVisibility(View.GONE);

        if (checkPermissions()){
            Toast.makeText(this, "Permissions Granted !", Toast.LENGTH_SHORT).show();
        }else{
            requestPermissions();
        }

        scannerLiveView.setScannerViewEventListener(new ScannerLiveView.ScannerViewEventListener() {
            @Override
            public void onScannerStarted(ScannerLiveView scanner) {
                textViewScanner.setVisibility(View.GONE);

            }

            @Override
            public void onScannerStopped(ScannerLiveView scanner) {

            }

            @Override
            public void onScannerError(Throwable err) {

            }

            @Override
            public void onCodeScanned(String data) {
                Toast.makeText(ScanQRCodeActivity.this, "Data length : "+data.length(), Toast.LENGTH_SHORT).show();
                if (data.length() < 74 || data.length() > 80) {
                    Toast.makeText(ScanQRCodeActivity.this, "QR is not Valid.", Toast.LENGTH_SHORT).show();
                    textViewScanner.setVisibility(View.VISIBLE);
                    textViewScanner.setText("Please Provide a Valid QR");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    },2000);
                }else {
                    textViewScanner.setVisibility(View.VISIBLE);
                    textViewScanner.setText("TOKEN\n\n" + data);
                    String[] tokenData = dataUtils.decodeAndSeparate(data);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            String token = tokenData[0];
                            String DocId = tokenData[1];

                            Intent intent = new Intent(ScanQRCodeActivity.this, onScanQRCodeSuccessActivity.class);
                            intent.putExtra("token", token);
                            intent.putExtra("docId", DocId);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                    }, 2500);
                }
            }
        });
    }

    private boolean checkPermissions(){
        int camera_permission = ContextCompat.checkSelfPermission(getApplicationContext(),CAMERA);
        int vibrate_permission = ContextCompat.checkSelfPermission(getApplicationContext(),VIBRATE);
        return camera_permission == PackageManager.PERMISSION_GRANTED && vibrate_permission == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions(){
        int PERMISSION_CODE = 201;
        ActivityCompat.requestPermissions(this, new String[]{CAMERA, VIBRATE},PERMISSION_CODE);
    }

    @Override
    protected void onPause() {
        scannerLiveView.stopScanner();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ZXDecoder decoder = new ZXDecoder();
        decoder.setScanAreaPercent(0.6);
        scannerLiveView.setDecoder(decoder);
        scannerLiveView.startScanner();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length>0){
            boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            boolean vibrationAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

            if (cameraAccepted && vibrationAccepted){
                Toast.makeText(this, "Permissions Granated...", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "Permissions Denied !\nYou cannot use the app without Permissions.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}