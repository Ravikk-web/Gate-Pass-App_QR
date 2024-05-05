package com.example.gate_pass_app_qr;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

public class ShowRequestTokensActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    tokenAdapter tokenAdapter;
    FloatingActionButton acceptRequestBtn;

    private final static String TAG = "ShowRequestTokensActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_show_request_tokens);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_recycler_view), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getSupportActionBar().setTitle("Tokens");

        acceptRequestBtn=findViewById(R.id.accept_request_btn);
        //acceptRequestBtn.setVisibility(View.GONE);

        recyclerView=findViewById(R.id.recyclerView);

        acceptRequestBtn.setOnClickListener((v)->startActivity(new Intent(ShowRequestTokensActivity.this, TokenDetailsActivity.class)));

        setupRecyclerView();
    }
    void setupRecyclerView(){

        Query query =  utility.getCollectionRefereneceForTokens().orderBy("timestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Token> options = new FirestoreRecyclerOptions.Builder<Token>().setQuery(query, Token.class).build();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        tokenAdapter = new tokenAdapter(options, this);
        recyclerView.setAdapter(tokenAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        tokenAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        tokenAdapter.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        tokenAdapter.notifyDataSetChanged();
    }
}