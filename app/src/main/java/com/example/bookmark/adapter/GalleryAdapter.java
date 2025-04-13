package com.example.bookmark.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookmark.R;

import java.util.BitSet;
import com.example.bookmark.model.Galleryimages;
import java.util.List;

//GalleryAdapter is a adapter that is used to display the images in the RecyclerView. I have used the same adapter for the bookmarks and the home page
public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryHolder> {

    private List<Galleryimages> list;
    //sendImage is a interface that is used to send the image to the activity that is hosting the RecyclerView which in this case is the BookmarksFragment
    SendImage onSendimage;

    public GalleryAdapter(List<Galleryimages> list) {
        this.list = list;
    }

    @NonNull
    @Override

    public GalleryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //image_items.xml only has one imageView so we can use the same adapter for the bookmarks and the home page
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_items, parent, false); 
        return new GalleryHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryHolder holder, int position) {
//        holder.imageView.setImageURI(list.get(position).getPicUri());
//        using Glide instead to load image
        Log.d("GalleryAdapter", "Binding item at position: " + position);

        Glide.with(holder.itemView.getContext().getApplicationContext())
                        .load(list.get(position).getPicUri()).into(holder.imageView);
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            //this method gets the position of the image that is clicked and then sends the image to the activity that is hosting the RecyclerView which in this case is the BookmarksFragment
            public void onClick(View v) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    //calling the chooseImage to send the picUri to the recyclerView
                    chooseImage(list.get(adapterPosition).getPicUri());
                }
            }
        });
    }
    private void chooseImage(Uri picUri)
    {
        onSendimage.onSend(picUri);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class GalleryHolder extends RecyclerView.ViewHolder{
        private ImageView imageView;
        public GalleryHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
    public interface SendImage {
        void onSend(Uri picUri);
    }
    //This method is used to send the image to the activity that is hosting the RecyclerView which in this case is the BookmarksFragment. It is called by the BookmarksFragment
    public void SendImage (SendImage sendImage){
        this.onSendimage = sendImage;
    }
}
