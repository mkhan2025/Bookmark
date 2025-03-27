package com.example.bookmark.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.helper.widget.Layer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookmark.R;
import com.example.bookmark.model.BookmarksModel;
import com.example.bookmark.model.CommentModel;
import com.example.bookmark.model.HomeModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.HomeHolder> {
    private List<HomeModel> list;
    Context context;


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
        checkBookmarkStatus(list.get(position).getId(), holder.bookmarkBtn);
//        holder.timeTV.setText(list.get(position).getTimeStamp());
        Glide.with(context.getApplicationContext()).load(list.get(position).getProfileImage()).placeholder(R.drawable.profile_image).timeout(6500).into(holder.profilePic);
        Glide.with(context.getApplicationContext())
                .load(list.get(position).getImageUrl())  // Use local resource
                .placeholder(R.drawable.map)  // Replace with a placeholder if needed
                .timeout(7000)
                .into(holder.imageView);

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
                holder.commentBtn.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.comment, null);
        view.setLayoutParams(new ViewGroup.LayoutParams(
    ViewGroup.LayoutParams.MATCH_PARENT,
    ViewGroup.LayoutParams.MATCH_PARENT
));
        
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView); 
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setHasFixedSize(true);
        EditText commentEditText = view.findViewById(R.id.commentET);
        ImageButton sendBtn = view.findViewById(R.id.sendBtn);
        CommentAdapter commentAdapter = new CommentAdapter(new ArrayList<>(), context);
        recyclerView.setAdapter(commentAdapter);
        ViewGroup.LayoutParams params = recyclerView.getLayoutParams();
        params.height = (int) (context.getResources().getDisplayMetrics().heightPixels * 0.7); // 70% of screen height
        recyclerView.setLayoutParams(params);
        HomeModel post = list.get(position);

        // Set up real-time listener HERE
        FirebaseFirestore.getInstance()
            .collection("Users")
            .document(post.getUid())
            .collection("Post Images")
            .document(post.getId())
            .collection("Comments")
            .addSnapshotListener((value, error) -> {
                if (error != null) {
                    Log.e("CommentListener", "Error loading comments", error);
                    Toast.makeText(context, "Error loading comments", Toast.LENGTH_SHORT).show();
                    return;
                }
                   if (value == null) {
                Log.d("CommentListener", "No comments found");
                return;
                }
                
                List<CommentModel> comments = new ArrayList<>();
                for (DocumentSnapshot doc : value.getDocuments()) {
                    CommentModel comment = doc.toObject(CommentModel.class);
                    Log.d("CommentListener", "Comment loaded: " + comment.getComment() + " by " + comment.getUsername());
                    comments.add(comment);
                }

                // Update CommentAdapter with new comments
                Log.d("CommentListener", "Total comments: " + comments.size());
                commentAdapter.updateComments(comments);
            });
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();

sendBtn.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        String commentText = commentEditText.getText().toString().trim();
        if(!commentText.isEmpty()){
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            
            // First get the username from Users collection
            FirebaseFirestore.getInstance()
                .collection("Users")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    // Add logging to see what fields are available
                    Log.d("UserData", "User document fields: " + documentSnapshot.getData());
                    
                    // Create a final variable for username
                    final String finalUsername;
                    
                    // Try different possible field names for username
                    String username = documentSnapshot.getString("name");
                    // if (username == null) {
                    //     username = documentSnapshot.getString("name");
                    // }
                    // if (username == null) {
                    //     username = documentSnapshot.getString("userName");
                    // }
                    
                    finalUsername = username != null ? username : "Unknown User";
                    Log.d("UserData", "Found username: " + finalUsername);
                    
                    CommentModel comment = new CommentModel();
                    comment.setComment(commentText);
                    comment.setUid(currentUserId);
                    comment.setUsername(finalUsername);
                    comment.setProfileImage(documentSnapshot.getString("profileImage"));
                    comment.setTimestamp(new Date(System.currentTimeMillis()));

                    FirebaseFirestore.getInstance()
                        .collection("Users")
                        .document(post.getUid())
                        .collection("Post Images")
                        .document(post.getId())
                        .collection("Comments")
                        .add(comment)
                        .addOnSuccessListener(documentReference -> {
                            Log.d("CommentSuccess", "Comment added with username: " + finalUsername);
                            commentEditText.setText("");
                        })
                        .addOnFailureListener(e -> {
                            Log.e("CommentError", "Failed to add comment", e);
                            Toast.makeText(context, "Failed to post comment", Toast.LENGTH_SHORT).show();
                        });
                })
                .addOnFailureListener(e -> {
                    Log.e("UserData", "Failed to get user data", e);
                    Toast.makeText(context, "Failed to get user data", Toast.LENGTH_SHORT).show();
                });
        }
    }
});
        
 
    }
});
//holder.bookmarkBtn.setOnClickListener(new View.OnClickListener() {
//    @Override
//    public void onClick(View v) {
//        Log.d("Bookmark", "Bookmark button clicked");
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        if(user == null){
//            Toast.makeText(context, "Please login to bookmark", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        String postId = list.get(position).getId();
//        DocumentReference bookmarkRef = FirebaseFirestore.getInstance().collection("Users").document(user.getUid()).collection("Bookmarks").document(postId);
//        bookmarkRef.get().addOnSuccessListener(documentSnapshot -> {
//            BookmarksModel bookmark = null;
//            if (documentSnapshot.exists()) {
//                bookmarkRef.delete().addOnSuccessListener(aVoid -> {
//                    holder.bookmarkBtn.setImageResource(R.drawable.bookmark);
//                    Toast.makeText(context, "Bookmark removed", Toast.LENGTH_SHORT).show();
//                }).addOnFailureListener(e -> {
//                    Toast.makeText(context, "Failed to remove bookmark", Toast.LENGTH_SHORT).show();
//                });
//            } else {
//                bookmark = new BookmarksModel(postId, list.get(position).getUid(), new Date(System.currentTimeMillis()));
//            }
//            bookmarkRef.set(bookmark).addOnSuccessListener(aVoid -> {
//                holder.bookmarkBtn.setImageResource(R.drawable.heart);
//                Toast.makeText(context, "Bookmark added", Toast.LENGTH_SHORT).show();
//            }).addOnFailureListener(e -> {
//                Toast.makeText(context, "Failed to add bookmark", Toast.LENGTH_SHORT).show();
//            });
//
//        });
//
//    }
//});
        holder.bookmarkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Bookmark", "Bookmark button clicked");
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user == null){
                    Toast.makeText(context, "Please login to bookmark", Toast.LENGTH_SHORT).show();
                    return;
                }
                String postId = list.get(position).getId();
                String originalUserId = list.get(position).getUid();  // Get the original post owner's ID

                DocumentReference bookmarkRef = FirebaseFirestore.getInstance()
                        .collection("Users")
                        .document(user.getUid())
                        .collection("Bookmarks")
                        .document(postId);

                bookmarkRef.get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Remove bookmark
                        bookmarkRef.delete()
                                .addOnSuccessListener(aVoid -> {
                                    holder.bookmarkBtn.setImageResource(R.drawable.bookmark);
                                    Toast.makeText(context, "Bookmark removed", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(context, "Failed to remove bookmark", Toast.LENGTH_SHORT).show());
                    } else {
                        // Add bookmark
                        BookmarksModel bookmark = new BookmarksModel(
                                originalUserId,  // original post owner's ID
                                postId,         // post ID
                                new Date(System.currentTimeMillis())  // current timestamp
                        );

                        bookmarkRef.set(bookmark)
                                .addOnSuccessListener(aVoid -> {
                                    holder.bookmarkBtn.setImageResource(R.drawable.bookmark);
                                    Toast.makeText(context, "Post bookmarked", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(context, "Failed to bookmark", Toast.LENGTH_SHORT).show());
                    }
                });
            }
        });
        holder.descriptionTV.setText(list.get(position).getDescription());
        holder.locationTV.setText(list.get(position).getLocationName());
        holder.activityTypeTV.setText(list.get(position).getActivityType());

    }
    private void checkBookmarkStatus(String postId, ImageView bookmarkBtn) {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    if (user == null) return;

    FirebaseFirestore.getInstance()
        .collection("Users")
        .document(user.getUid())
        .collection("Bookmarks")
        .document(postId)
        .get()
        .addOnSuccessListener(documentSnapshot -> {
            boolean isBookmarked = documentSnapshot.exists();
            bookmarkBtn.setImageResource(isBookmarked ? 
                R.drawable.heart : R.drawable.bookmark);
        });
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
            commentBtn = itemView.findViewById(R.id.commentBtn);


        }
    }
}
