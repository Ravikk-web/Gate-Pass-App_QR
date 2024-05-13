package com.example.gate_pass_app_qr;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class GenerateQRCodeActivity extends AppCompatActivity {

    private TextView textviewQRCode;
    private ImageView imageViewQRCode;
    private TextView dataView;
    private String encodedData;
    private QRGEncoder qrgEncoder;
    private Bitmap bitmap;
    private static final String TAG = "GenerateQRCodeActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_generate_qrcode);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        textviewQRCode = findViewById(R.id.textView_generate_qrCode);
        imageViewQRCode = findViewById(R.id.imageView_generate_qrCode);
        dataView = findViewById(R.id.textView_data);
        encodedData = getIntent().getStringExtra("data");
        dataView.setText("TOKEN : "+encodedData);

        String data = encodedData;

        if (encodedData.isEmpty()){
            Toast.makeText(GenerateQRCodeActivity.this, "Encoded Data not Found.\nPlease Enter a Reason to generate QR-Code!", Toast.LENGTH_SHORT).show();
        }else{
            WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
            Display display = manager.getDefaultDisplay();
            Point point = new Point();
            display.getSize(point);
            int width = point.x;
            int height = point.y;
            int dimen = width<height ? width : height;
            dimen = dimen * 3/4;

            qrgEncoder = new QRGEncoder(data,null, QRGContents.Type.TEXT, dimen);
            qrgEncoder.setColorBlack(Color.BLACK);
            qrgEncoder.setColorWhite(Color.WHITE);
            try {
                bitmap = qrgEncoder.getBitmap(0);
                textviewQRCode.setVisibility(View.GONE);
                imageViewQRCode.setImageBitmap(bitmap);
            }
            catch (Exception e){
                Toast.makeText(GenerateQRCodeActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                Log.e(TAG, "Error generating barcode or QR code: " + e.getMessage(), e);
            }

        }

    }
}