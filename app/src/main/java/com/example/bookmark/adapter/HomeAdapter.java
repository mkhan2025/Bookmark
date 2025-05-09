package com.example.bookmark.adapter;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.widget.Button;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.bookmark.R;
import com.example.bookmark.fragments.Profile;
import com.example.bookmark.model.BookmarksModel;
import com.example.bookmark.model.CommentModel;
import com.example.bookmark.model.HomeModel;
import com.example.bookmark.utils.ActivityTypeMapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import androidx.core.content.ContextCompat;
import com.tbuonomo.viewpagerdotsindicator.SpringDotsIndicator;

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
        if (list.get(position).getUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
        holder.trashBtn.setVisibility(View.VISIBLE);
        }
        Log.d("HomeAdapter", "Setting username: " + list.get(position).getName());
        checkBookmarkStatus(list.get(position).getId(), holder.bookmarkBtn);

        // Set initial like button state
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && list.get(position).getLikedBy().contains(currentUser.getUid())) {
            holder.likeBtn.setImageDrawable(context.getDrawable(R.drawable.heart_fill));
        } else {
            holder.likeBtn.setImageDrawable(context.getDrawable(R.drawable.heart));
        }

        Glide.with(context.getApplicationContext()).load(list.get(position).getProfileImage()).placeholder(R.drawable.profile_image).timeout(6500).into(holder.profilePic);
        
        // Set up image carousel
        List<String> imageUrls = list.get(position).getImageUrls();
        if (imageUrls != null && !imageUrls.isEmpty()) {
            ImageCarouselAdapter carouselAdapter = new ImageCarouselAdapter(imageUrls);
            holder.imageViewPager.setAdapter(carouselAdapter);
            holder.dotsIndicator.setViewPager2(holder.imageViewPager);
            holder.dotsIndicator.setVisibility(View.VISIBLE);
        } else {
            // Fallback to single image if no multiple images
            List<String> singleImage = new ArrayList<>();
            singleImage.add(list.get(position).getImageUrl());
            ImageCarouselAdapter carouselAdapter = new ImageCarouselAdapter(singleImage);
            holder.imageViewPager.setAdapter(carouselAdapter);
            holder.dotsIndicator.setVisibility(View.GONE);
        }

        holder.locationTV.setText(list.get(position).getLocationName());
        holder.descriptionTV.setText(list.get(position).getDescription());
        holder.activityTypeTV.setImageResource(R.drawable.profile_image);
        setActivityTagStyle(holder.activityTypeTV, list.get(position).getActivityType());

        holder.usernameTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("HomeAdapter", "Username clicked: " + list.get(position).getName());
                HomeModel post = list.get(position); 
                String postOwnerId = post.getUid();
                Log.d("HomeAdapter", "Post owner ID: " + postOwnerId);
                
                Profile profileFragment = new Profile();
                Bundle bundle = new Bundle();
                bundle.putString("userId", postOwnerId);
                profileFragment.setArguments(bundle);
                Log.d("HomeAdapter", "Created Profile fragment with user ID: " + postOwnerId);

                // Make sure mainFrameLayout is visible
                View frameLayout = ((FragmentActivity) context).findViewById(R.id.mainFrameLayout);
                if (frameLayout != null) {
                    frameLayout.setVisibility(View.VISIBLE);
                    Log.d("HomeAdapter", "Set mainFrameLayout visibility to VISIBLE");
                } else {
                    Log.e("HomeAdapter", "mainFrameLayout not found");
                }

                FragmentTransaction transaction = ((FragmentActivity) context).getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.mainFrameLayout, profileFragment);
                transaction.addToBackStack(null);
                transaction.commit();
                Log.d("HomeAdapter", "Fragment transaction committed");
            }
        }); 

        holder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser == null) {
                    Toast.makeText(context, "Please login to like posts", Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.d("LikeDebug", "Like button clicked for post: " + list.get(position).getId());
                Log.d("LikeDebug", "Current user ID: " + currentUser.getUid());
                Log.d("LikeDebug", "Current likedBy array: " + list.get(position).getLikedBy());
                Log.d("LikeDebug", "Current like count: " + list.get(position).getLikeCount());

                HomeModel post = list.get(position);
                String currentUserId = currentUser.getUid();
                int currLikes = post.getLikeCount();

                if(post.getLikedBy().contains(currentUserId)) {
                    Log.d("LikeDebug", "User has already liked this post, proceeding to unlike");
                    holder.likeBtn.setImageDrawable(context.getDrawable(R.drawable.heart));
                    int newLikes = currLikes - 1; 
                    post.getLikedBy().remove(currentUserId);
                    
                    FirebaseFirestore.getInstance()
                        .collection("Users")
                        .document(post.getUid())
                        .collection("Post Images")
                        .document(post.getId())
                        .update(
                            "likedBy", FieldValue.arrayRemove(currentUserId),
                            "likeCount", FieldValue.increment(-1)
                        )
                        .addOnSuccessListener(aVoid -> {
                            Log.d("LikeDebug", "Successfully removed like from Firestore");
                            post.setLikeCount(newLikes);
                            holder.likeCountTV.setText(String.valueOf(newLikes));
                            notifyDataSetChanged();
                        })
                        .addOnFailureListener(e -> {
                            Log.e("LikeDebug", "Error removing like from Firestore: " + e.getMessage());
                            // Revert local changes
                            post.getLikedBy().add(currentUserId);
                            post.setLikeCount(currLikes);
                            holder.likeCountTV.setText(String.valueOf(currLikes));
                            notifyDataSetChanged();
                            Toast.makeText(context, "Failed to unlike", Toast.LENGTH_SHORT).show();
                        });
                } else {
                    Log.d("LikeDebug", "User has not liked this post, proceeding to like");
                    holder.likeBtn.setImageDrawable(context.getDrawable(R.drawable.heart_fill));
                    int newLikes = currLikes + 1;
                    post.getLikedBy().add(currentUserId);
                    
                    FirebaseFirestore.getInstance()
                        .collection("Users")
                        .document(post.getUid())
                        .collection("Post Images")
                        .document(post.getId())
                        .update(
                            "likedBy", FieldValue.arrayUnion(currentUserId),
                            "likeCount", FieldValue.increment(1)
                        )
                        .addOnSuccessListener(aVoid -> {
                            Log.d("LikeDebug", "Successfully added like to Firestore");
                            post.setLikeCount(newLikes);
                            holder.likeCountTV.setText(String.valueOf(newLikes));
                            notifyDataSetChanged();
                        })
                        .addOnFailureListener(e -> {
                            Log.e("LikeDebug", "Error adding like to Firestore: " + e.getMessage());
                            // Revert local changes
                            post.getLikedBy().remove(currentUserId);
                            post.setLikeCount(currLikes);
                            holder.likeCountTV.setText(String.valueOf(currLikes));
                            notifyDataSetChanged();
                            Toast.makeText(context, "Failed to like", Toast.LENGTH_SHORT).show();
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

        // Move trash button click listener here, outside of any other click listeners
        holder.trashBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("TrashButton", "Trash button clicked - initial click detected");
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser == null) {
                    Log.d("TrashButton", "No current user found");
                    Toast.makeText(context, "Please login to delete posts", Toast.LENGTH_SHORT).show();
                    return;
                }
                String postId = list.get(position).getId();
                String originalUserId = list.get(position).getUid();
                Log.d("TrashButton", "Deleting post with ID: " + postId);
                Log.d("TrashButton", "Original User ID: " + originalUserId);
                Log.d("TrashButton", "Current User ID: " + currentUser.getUid());

                if (postId == null || originalUserId == null) {
                    Log.e("TrashButton", "Cannot delete post - Post ID or Original User ID is null");
                    Toast.makeText(context, "Error: Invalid post data", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Show confirmation dialog
                AlertDialog dialog = new AlertDialog.Builder(context)
                    .setTitle("Delete Post")
                    .setMessage("Are you sure you want to delete this post?")
                    .setPositiveButton("Delete", (dialogInterface, which) -> {
                        Log.d("TrashButton", "User confirmed deletion");
                        DocumentReference postRef = FirebaseFirestore.getInstance()
                            .collection("Users")
                            .document(originalUserId)
                            .collection("Post Images")
                            .document(postId);
                        postRef.delete()
                            .addOnSuccessListener(aVoid -> {
                                Log.d("TrashButton", "Post deleted successfully from Firestore");
                                Toast.makeText(context, "Post deleted successfully", Toast.LENGTH_SHORT).show();
                                list.remove(position);
                                notifyItemRemoved(position);
                            })
                            .addOnFailureListener(e -> {
                                Log.e("TrashButton", "Error deleting post from Firestore", e);
                                Toast.makeText(context, "Failed to delete post", Toast.LENGTH_SHORT).show();
                            });
                    })
                    .setNegativeButton("Cancel", (dialogInterface, which) -> {
                        Log.d("TrashButton", "User cancelled deletion");
                        dialogInterface.dismiss();
                    })
                    .create();

                dialog.setOnShowListener(dialogInterface -> {
                    Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                    
                    // Set text color for both buttons
                    positiveButton.setTextColor(ContextCompat.getColor(context, R.color.colorBlack));
                    negativeButton.setTextColor(ContextCompat.getColor(context, R.color.colorBlack));
                });

                dialog.show();
            }
        });

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
                                    holder.bookmarkBtn.setImageDrawable(context.getDrawable(R.drawable.bookmark_home));
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
                                    holder.bookmarkBtn.setImageDrawable(context.getDrawable(R.drawable.bookmark_home_fill));
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
        holder.activityTypeTV.setImageResource(R.drawable.profile_image);
        setActivityTagStyle(holder.activityTypeTV, list.get(position).getActivityType());

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
                bookmarkBtn.setImageDrawable(context.getDrawable(isBookmarked ? 
                    R.drawable.bookmark_home_fill : R.drawable.bookmark_home));
                Log.d("isBookmarkedCheck", "Bookmark status: " + isBookmarked);
            });
    }

    private void setActivityTagStyle(ImageView activityTV, String activityType) {
        String mappedType = ActivityTypeMapper.mapOldToNewActivityType(activityType);
        switch (mappedType.toLowerCase()) {
            case "nature & adventure":
                activityTV.setImageResource(R.drawable.adventure);
                break;
            case "cultural & historical":
                activityTV.setImageResource(R.drawable.cultural);
                break;
            case "food & drink":
                activityTV.setImageResource(R.drawable.eating);
                break;
            case "events & entertainment":
                activityTV.setImageResource(R.drawable.events);
                break;
            case "relaxation & wellness":
                activityTV.setImageResource(R.drawable.wellness);
                break;
            case "shopping":
                activityTV.setImageResource(R.drawable.shopping);
                break;
            case "indoor":
                activityTV.setImageResource(R.drawable.indoor);
                break;
            case "outdoor":
                activityTV.setImageResource(R.drawable.outdoor);
                break;
            case "transit":
                activityTV.setImageResource(R.drawable.transit);
                break;
            default:
                activityTV.setImageResource(R.drawable.profile_image);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class HomeHolder extends RecyclerView.ViewHolder {
        private CircleImageView profilePic;
        private TextView usernameTV;
        private TextView locationTV;
        private TextView likeCountTV;
        private TextView descriptionTV;
        private ImageView activityTypeTV;
        private ViewPager2 imageViewPager;
        private SpringDotsIndicator dotsIndicator;
        private ImageButton likeBtn;
        private ImageButton commentBtn;
        private ImageButton shareBtn;
        private ImageButton bookmarkBtn;
        private ImageButton trashBtn;

        public HomeHolder(@NonNull View itemView) {
            super(itemView);
            profilePic = itemView.findViewById(R.id.profilePic);
            usernameTV = itemView.findViewById(R.id.usernameTV);
            likeCountTV = itemView.findViewById(R.id.likeCountTV);
            imageViewPager = itemView.findViewById(R.id.imageViewPager);
            dotsIndicator = itemView.findViewById(R.id.dotsIndicator);
            likeBtn = itemView.findViewById(R.id.likeBtn);
            shareBtn = itemView.findViewById(R.id.shareBtn);
            bookmarkBtn = itemView.findViewById(R.id.bookmarkBtn);
            descriptionTV = itemView.findViewById(R.id.descriptionTV);
            locationTV = itemView.findViewById(R.id.locationTV);
            activityTypeTV = itemView.findViewById(R.id.activityTV);
            commentBtn = itemView.findViewById(R.id.commentBtn);
            trashBtn = itemView.findViewById(R.id.trashBtn);
        }
    }

    private void migrateActivityTypes() {
        FirebaseFirestore.getInstance()
            .collection("Users")
            .get()
            .addOnSuccessListener(userDocuments -> {
                for (DocumentSnapshot userDoc : userDocuments) {
                    userDoc.getReference()
                        .collection("Post Images")
                        .get()
                        .addOnSuccessListener(postDocuments -> {
                            for (DocumentSnapshot postDoc : postDocuments) {
                                String oldActivityType = postDoc.getString("activityType");
                                String newActivityType = mapOldToNewActivityType(oldActivityType);
                                
                                if (newActivityType != null) {
                                    postDoc.getReference()
                                        .update("activityType", newActivityType)
                                        .addOnSuccessListener(aVoid -> 
                                            Log.d("Migration", "Updated activity type for post: " + postDoc.getId()))
                                        .addOnFailureListener(e -> 
                                            Log.e("Migration", "Error updating post: " + postDoc.getId(), e));
                                }
                            }
                        });
                }
            });
    }

    private String mapOldToNewActivityType(String oldType) {
        switch (oldType.toLowerCase()) {
            case "outdoor":
                return "Nature & Adventure";
            case "indoor":
                return "Indoor";
            case "adventure":
                return "Nature & Adventure";
            case "eating":
                return "Food & Drink";
            case "tourist":
                return "Cultural & Historical";
            default:
                return null;
        }
    }
}
