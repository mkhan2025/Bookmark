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

import androidx.annotation.NonNull;
import androidx.constraintlayout.helper.widget.Layer;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookmark.R;
import com.example.bookmark.model.HomeModel;

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
        holder.usernameTV.setText(list.get(position).getName());
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
        holder.descriptionTV.setText(list.get(position).getDescription());
        holder.locationTV.setText(list.get(position).getLocationName());

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


        }
    }
}
