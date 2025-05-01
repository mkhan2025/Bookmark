package com.example.bookmark.adapter;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookmark.R;
import com.example.bookmark.fragments.Profile;
import com.example.bookmark.model.UserModel;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserSearchAdapter extends RecyclerView.Adapter<UserSearchAdapter.UserSearchHolder> {
    private List<UserModel> list;
    private Context context;

    public UserSearchAdapter(List<UserModel> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public UserSearchHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_search_item, parent, false);
        return new UserSearchHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserSearchHolder holder, int position) {
        UserModel user = list.get(position);
        
        holder.username.setText(user.getName());
        
        if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
            Glide.with(context)
                .load(user.getProfileImage())
                .placeholder(R.drawable.profile_image)
                .into(holder.profileImage);
        }

        holder.itemView.setOnClickListener(v -> {
            Log.d("UserSearchAdapter", "User clicked: " + user.getName());
            Profile profile = new Profile();
            Bundle bundle = new Bundle();
            bundle.putString("userId", user.getUid());
            profile.setArguments(bundle);
            
            // Make sure mainFrameLayout is visible
            View frameLayout = ((FragmentActivity) context).findViewById(R.id.mainFrameLayout);
            if (frameLayout != null) {
                frameLayout.setVisibility(View.VISIBLE);
                Log.d("UserSearchAdapter", "Set mainFrameLayout visibility to VISIBLE");
            } else {
                Log.e("UserSearchAdapter", "mainFrameLayout not found");
            }

            FragmentTransaction transaction = ((FragmentActivity) context)
                .getSupportFragmentManager()
                .beginTransaction();
            transaction.replace(R.id.mainFrameLayout, profile);
            transaction.addToBackStack(null);
            transaction.commit();
            Log.d("UserSearchAdapter", "Fragment transaction committed");
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void updateList(List<UserModel> newList) {
        list = newList;
        notifyDataSetChanged();
    }

    static class UserSearchHolder extends RecyclerView.ViewHolder {
        private CircleImageView profileImage;
        private TextView username;
        private View itemView;

        public UserSearchHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            profileImage = itemView.findViewById(R.id.profileImage);
            username = itemView.findViewById(R.id.username);
        }
    }
} 