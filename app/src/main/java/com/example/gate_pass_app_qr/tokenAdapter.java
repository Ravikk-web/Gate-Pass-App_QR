package com.example.gate_pass_app_qr;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

public class tokenAdapter extends FirestoreRecyclerAdapter<Token, tokenAdapter.tokenViewHolder> {

    Context context;

    public tokenAdapter(@NonNull FirestoreRecyclerOptions<Token> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull tokenViewHolder holder, int position, @NonNull Token token) {
        holder.studentNameTextView.setText(token.studentName);
        holder.studentIdTextView.setText(token.studentId);
        holder.studentReasonTextView.setText(token.studentReason);
        holder.tokenStatusTextView.setText(token.tokenStatus);
        holder.timestampTextView.setText(utility.timeStampToString(token.timestamp));


        holder.itemView.setOnClickListener((v)->{
            Intent intent = new Intent(context,TokenDetailsActivity.class);
            intent.putExtra("studentName",token.studentName);
            intent.putExtra("studentId",token.studentId);
            intent.putExtra("studentReason",token.studentReason);
            intent.putExtra("status",token.tokenStatus);
            String docId = this.getSnapshots().getSnapshot(position).getId();
            intent.putExtra("docId",docId);
            context.startActivity(intent);
        });
    }

    @NonNull
    @Override
    public tokenViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_token_item,parent,false);
        return new tokenViewHolder(view);
    }

    class tokenViewHolder extends RecyclerView.ViewHolder{
        TextView studentNameTextView, studentIdTextView, studentReasonTextView,tokenStatusTextView,timestampTextView;
        public tokenViewHolder(@NonNull View itemView) {
            super(itemView);

            studentNameTextView = itemView.findViewById(R.id.token_studentName_text_view);
            studentIdTextView = itemView.findViewById(R.id.token_enrollment_text_view);
            studentReasonTextView = itemView.findViewById(R.id.token_reason_text_view);
            tokenStatusTextView = itemView.findViewById(R.id.token_status_text_view);
            timestampTextView = itemView.findViewById(R.id.token_timestamp_text_view);
        }
    }

}
