package com.example.bookmark.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.helper.widget.Layer;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookmark.R;
import com.example.bookmark.model.CommentModel;
import com.example.bookmark.model.HomeModel;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentHolder> {
    private List<CommentModel> list;
    private Context context;


    public CommentAdapter(List<CommentModel> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public CommentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_items, parent, false);
        return new CommentHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentHolder holder, int position) {
        CommentModel comment = list.get(position);
        Log.d("CommentAdapter", "Binding comment at position " + position + ": " + comment.getComment());
        holder.commentUsername.setText(comment.getUsername());
    if (comment.getTimestamp() != null) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        holder.commentTimestamp.setText(sdf.format(comment.getTimestamp()));
    } else {
        holder.commentTimestamp.setText("Just now"); // or any default text
    }
        Glide.with(context.getApplicationContext()).load(comment.getProfileImage()).placeholder(R.drawable.profile_image).timeout(6500).into(holder.commentProfilePic);
        holder.commentText.setText(comment.getComment());



    }
    private String formatTimestamp(Date timestamp) {
    // SimpleDateFormat for relative time (e.g., "2 hours ago")
    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
    return sdf.format(timestamp);
}

    @Override
    public int getItemCount() {
        return list.size();
    }
    public void updateComments(List<CommentModel> newComments) {
    this.list = newComments;
    notifyDataSetChanged();
}

    static class CommentHolder extends RecyclerView.ViewHolder {
        CircleImageView commentProfilePic;
        TextView commentUsername;
        private final TextView commentTimestamp;
        TextView commentText;


        public CommentHolder(@NonNull View itemView) {
            super(itemView);
            commentProfilePic = itemView.findViewById(R.id.commentProfilePic);
            commentUsername = itemView.findViewById(R.id.commentUsername);
            commentTimestamp = itemView.findViewById(R.id.commentTimestamp);
            commentText = itemView.findViewById(R.id.commentText);


        }
    }
}
