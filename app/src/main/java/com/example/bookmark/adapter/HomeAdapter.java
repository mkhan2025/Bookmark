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

//responsible for displaying the posts in the RecyclerView 
//HomeHolder is a nested class that stored references to the views in the home_items layout
//HomeAdapter extends RecyclerView.Adapter<HomeAdapter.HomeHolder>
//HomeModel is the class that represents the data of the post, which is stored in the firestore database
//The job of the adapter is to bind the data (HomeModel) to the views (HomeHolder) and to handle the click events
//HomeAdapter is a bridge between the data and the views
public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.HomeHolder> {
    private List<HomeModel> list;
    Context context;


    public HomeAdapter(List<HomeModel> list, Context context) {
        this.list = list;
        this.context = context;
        //context is the activity that is hosting the RecyclerView
    }

    @NonNull
    @Override
    public HomeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //HomeHolder inflates home_items layout and creates a new HomeHolder object
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_items, parent, false);
        return new HomeHolder(view);
    }

//onBindViewHolder is called by the RecyclerView to display the data at the specified position
    @Override
    public void onBindViewHolder(@NonNull HomeHolder holder, int position) {
        int likeCount = list.get(position).getLikeCount();
        holder.likeCountTV.setText(String.valueOf(likeCount));
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
        holder.locationTV.setText(list.get(position).getLocationName());
        holder.descriptionTV.setText(list.get(position).getDescription());
        holder.activityTypeTV.setText(list.get(position).getActivityType());


        holder.likeBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d("Like","liked by button clicked");
        
                // String uid = list.get(position).getUid();
                HomeModel post = list.get(position);
                String uid = post.getUid();
                int currLikes = post.getLikeCount();
                if(post.getLikedBy().contains(uid))
                {
                    holder.likeBtn.setImageResource(R.drawable.heart);
                    int newLikes = currLikes - 1; 
                    post.getLikedBy().remove(uid);
                    //onSuccessListener is a callback that is called when the update is successful and aVoid is a void object
                    FirebaseFirestore.getInstance().collection("Users").document(post.getUid()).collection("Post Images").document(post.getId()).update("likeCount", newLikes).addOnSuccessListener(aVoid -> {
                            // Update the UI
                            post.setLikeCount(newLikes);
                            holder.likeCountTV.setText(String.valueOf(newLikes));
                            holder.likeCountTV.setVisibility(View.VISIBLE);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(context, "Failed to unlike", Toast.LENGTH_SHORT).show();
                        });
                    Log.d("Like","liked by finished");
                }
                else{
                    //else statement is executed if the post is not liked by the user
                holder.likeBtn.setImageResource(R.drawable.heart_fill);
                int newLikes = currLikes + 1;
                post.getLikedBy().add(uid);
                FirebaseFirestore.getInstance().collection("Users").document(post.getUid()).collection("Post Images").document(post.getId()).update("likeCount", newLikes).addOnSuccessListener(aVoid -> {
                            // Update the UI
                            post.setLikeCount(newLikes);
                            holder.likeCountTV.setText(String.valueOf(newLikes));
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
        //bottomSheetDialog is a dialog where you write the comments of the post
        //LayoutInflater inflates the layout of comment.xml
        View view = LayoutInflater.from(context).inflate(R.layout.comment, null);
        view.setLayoutParams(new ViewGroup.LayoutParams(
    ViewGroup.LayoutParams.MATCH_PARENT,
    ViewGroup.LayoutParams.MATCH_PARENT
));
        //R.id.recyclerView is the id of the RecyclerView in the comment.xml layout which is used to display the comments
        //the difference between the functions of recyclerView and the function of bottomSheetDialog is that recyclerView is used to display the comments and bottomSheetDialog is used to display the comments in a dialog
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView); 
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setHasFixedSize(true);
        EditText commentEditText = view.findViewById(R.id.commentET);
        ImageButton sendBtn = view.findViewById(R.id.sendBtn);
        CommentAdapter commentAdapter = new CommentAdapter(new ArrayList<>(), context);
        //commentAdapter is a adapter that is used to display the comments in the RecyclerView 
        recyclerView.setAdapter(commentAdapter);
        ViewGroup.LayoutParams params = recyclerView.getLayoutParams();
        params.height = (int) (context.getResources().getDisplayMetrics().heightPixels * 0.7); // 70% of screen height
        recyclerView.setLayoutParams(params);
        HomeModel post = list.get(position);
        //post is the post that is currently being displayed so we need this info to get the comments of the post

        // Set up real-time listener HERE
        //addSnapshotListener is a method that is used to listen to the comments of the post where value is the comments and error is the error that is returned if the comments are not found
        FirebaseFirestore.getInstance()
            .collection("Users")
            .document(post.getUid())
            .collection("Post Images")
            .document(post.getId())
            .collection("Comments")
            .addSnapshotListener((value, error) -> {
                if (error == null) {
                    Log.e("CommentListener", "Error loading comments", error);
                    Toast.makeText(context, "Error loading comments", Toast.LENGTH_SHORT).show();
                    return;
                }
                   if (value == null) {
                Log.d("CommentListener", "No comments found");
                return;
                }
                //comments is a list that is used to store the comments of the post
                List<CommentModel> comments = new ArrayList<>();
                //DocumentSnapshot is a class that is used to store the comments of the post
                for (DocumentSnapshot doc : value.getDocuments()) {
                    //docToObject is a method that is used to convert the comments of the post to a CommentModel object
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
                Log.d("BookmarksFragment", "Click called: ");

                Log.d("Bookmark", "Bookmark button clicked");
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user == null){
                    Toast.makeText(context, "Please login to bookmark", Toast.LENGTH_SHORT).show();
                    return;
                }
                String postId = list.get(position).getId();
                String originalUserId = list.get(position).getUid();  // Get the original post owner's ID
                Log.d("Bookmark", "Creating bookmark with data:");
        Log.d("Bookmark", "Post ID: " + postId);
        Log.d("Bookmark", "Original User ID: " + originalUserId);
        Log.d("Bookmark", "Current User ID: " + user.getUid());

         if (postId == null || originalUserId == null) {
            Log.e("Bookmark", "Cannot create bookmark - Post ID or Original User ID is null");
            Toast.makeText(context, "Error: Invalid post data", Toast.LENGTH_SHORT).show();
            return;
        }
        //DocumentReference is a class that is used to store the bookmark of the post
                DocumentReference bookmarkRef = FirebaseFirestore.getInstance()
                        .collection("Users")
                        .document(user.getUid())
                        .collection("Bookmarks")
                        .document(postId);
                Log.d("BookmarksFragment", "Bookmar trying to add - bookmarkRef: " + bookmarkRef);

//the onSuccessListener is a callback that is called when the bookmark is successfully added or removed. get method is used to get the bookmark of the post
                bookmarkRef.get().addOnSuccessListener(documentSnapshot -> {
                    Log.d("BookmarksFragment", "documenet snapshot: " + documentSnapshot.exists());
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
                        Log.d("BookmarksFragment", "Adding bookmark now: ");

                        // Add bookmark
                        BookmarksModel bookmark = new BookmarksModel(
                                originalUserId,  // original post owner's ID
                                postId,         // post ID
                                new Date(System.currentTimeMillis())  // current timestamp
                        );

                        bookmarkRef.set(bookmark)
                                .addOnSuccessListener(aVoid -> {
                                    holder.bookmarkBtn.setImageResource(R.drawable.bookmark_fill);
                                    Toast.makeText(context, "Post bookmarked", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(context, "Failed to bookmark", Toast.LENGTH_SHORT).show());
                    }
                }).addOnFailureListener(e -> {
                    Log.d("BookmarksFragment", "Error happening: ", e);

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
//            bookmarkBtn.setImageResource(isBookmarked ?
//                R.drawable.bookmark_fill : R.drawable.bookmark);
            Log.d("isBookmarkedCheck","COMPLETED");
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
