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
import com.example.bookmark.model.HomeModel;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.HomeHolder> {
    private List<HomeModel> list;
    Context context;

    Random random = new Random();

    public HomeAdapter(List<HomeModel> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public HomeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_items, parent, false);
        return new HomeHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeHolder holder, int position) {
        int likeCount = list.get(position).getLikeCount();
        holder.likeCountTV.setText(likeCount + " Likes");
        holder.usernameTV.setText(list.get(position).getName());
        Log.d("HomeAdapter", "Setting username: " + list.get(position).getName());
//        holder.timeTV.setText(list.get(position).getTimeStamp());
        Glide.with(context.getApplicationContext()).load(list.get(position).getProfileImage()).placeholder(R.drawable.profile_image).timeout(6500).into(holder.profilePic);
        Glide.with(context.getApplicationContext())
                .load(list.get(position).getImageUrl())  // Use local resource
                .placeholder(R.drawable.map)  // Replace with a placeholder if needed
                .timeout(7000)
                .into(holder.imageView);

//        int likeCount = list.get(position).getLikeCount();
//        if (likeCount == 0) {
//            holder.likeCountTV.setVisibility(View.INVISIBLE);
//        } else if (likeCount == 1) {
//            holder.likeCountTV.setText(likeCount + " Like");
//        } else {
//            holder.likeCountTV.setText(likeCount + " Likes");
//        }
        holder.likeBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d("Like","liked by button clicked");
        
                String uid = list.get(position).getUid();
                HomeModel post = list.get(position);
                int currLikes = post.getLikeCount();
                if(post.getLikedBy().contains(uid))
                {
                    holder.likeBtn.setImageResource(R.drawable.heart);
                    int newLikes = currLikes - 1; 
                    post.getLikedBy().remove(uid);
                    FirebaseFirestore.getInstance().collection("Users").document(post.getUid()).collection("Post Images").document(post.getId()).update("likeCount", newLikes).addOnSuccessListener(aVoid -> {
                            // Update the UI
                            post.setLikeCount(newLikes);
                            holder.likeCountTV.setText(newLikes + " Likes");
                            holder.likeCountTV.setVisibility(View.VISIBLE);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(context, "Failed to unlike", Toast.LENGTH_SHORT).show();
                        });
                    Log.d("Like","liked by finished");
                }
                else{
                holder.likeBtn.setImageResource(R.drawable.heart_fill);
                int newLikes = currLikes + 1;
                post.getLikedBy().add(uid);
                FirebaseFirestore.getInstance().collection("Users").document(post.getUid()).collection("Post Images").document(post.getId()).update("likeCount", newLikes).addOnSuccessListener(aVoid -> {
                            // Update the UI
                            post.setLikeCount(newLikes);
                            holder.likeCountTV.setText(newLikes + " Likes");
                            holder.likeCountTV.setVisibility(View.VISIBLE);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(context, "Failed to update likes", Toast.LENGTH_SHORT).show();
                        });
                }
            
            }
        });
        holder.descriptionTV.setText(list.get(position).getDescription());
        holder.locationTV.setText(list.get(position).getLocationName());
        holder.activityTypeTV.setText(list.get(position).getActivityType());

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class HomeHolder extends RecyclerView.ViewHolder {
        private CircleImageView profilePic;
        private TextView usernameTV;
        private TextView locationTV;

//        private TextView timeTV;

        private TextView likeCountTV;
        private TextView descriptionTV;
        private TextView activityTypeTV;
        private ImageView imageView;
        private ImageButton likeBtn;
        private ImageButton commentBtn;
        private ImageButton shareBtn;

        private ImageButton bookmarkBtn;

        public HomeHolder(@NonNull View itemView) {
            super(itemView);
            profilePic = itemView.findViewById(R.id.profilePic);
            usernameTV = itemView.findViewById(R.id.usernameTV);
//            timeTV = itemView.findViewById(R.id.timeTV);
            likeCountTV = itemView.findViewById(R.id.likeCountTV);
            imageView = itemView.findViewById(R.id.imageView);
            likeBtn = itemView.findViewById(R.id.likeBtn);
            shareBtn = itemView.findViewById(R.id.shareBtn);
            bookmarkBtn = itemView.findViewById(R.id.bookmarkBtn);
            descriptionTV = itemView.findViewById(R.id.descriptionTV);
            locationTV = itemView.findViewById(R.id.locationTV);
            activityTypeTV = itemView.findViewById(R.id.activityTV);


        }
    }
}
