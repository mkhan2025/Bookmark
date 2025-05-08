package com.example.bookmark.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookmark.R;

import java.util.List;

public class ImageCarouselAdapter extends RecyclerView.Adapter<ImageCarouselAdapter.CarouselViewHolder> {
    private List<?> images;

    public ImageCarouselAdapter(List<?> images) {
        this.images = images;
    }

    @NonNull
    @Override
    public CarouselViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.carousel_image_item, parent, false);
        return new CarouselViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarouselViewHolder holder, int position) {
        Object imageSource = images.get(position);
        if (imageSource instanceof Uri) {
            Glide.with(holder.itemView.getContext())
                    .load((Uri) imageSource)
                    .placeholder(R.drawable.map)
                    .timeout(7000)
                    .into(holder.imageView);
        } else if (imageSource instanceof String) {
            Glide.with(holder.itemView.getContext())
                    .load((String) imageSource)
                    .placeholder(R.drawable.map)
                    .timeout(7000)
                    .into(holder.imageView);
        }
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public void setImages(List<?> newImages) {
        this.images = newImages;
        notifyDataSetChanged();
    }

    static class CarouselViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public CarouselViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.carouselImageView);
        }
    }
} 