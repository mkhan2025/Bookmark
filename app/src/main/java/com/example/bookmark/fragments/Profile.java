package com.example.bookmark.fragments;

import static android.app.Activity.RESULT_OK;
import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import static com.example.bookmark.fragments.Home.LIST_SIZE;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.canhub.cropper.CropImage;
import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;
import com.example.bookmark.R;
import com.example.bookmark.adapter.PagerAdapter;
import com.example.bookmark.model.HomeModel;
import com.example.bookmark.model.PostImageModel;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import java.io.Serializable;
public class Profile extends Fragment {

    //add toolbarName
    private TextView nameTV, bioTV, followingCountTV, followersCountTV, postCountTV;
    private CircleImageView profilePic;
    private Button followBtn;
    private RecyclerView recyclerView;
    private LinearLayout countLayout;


    private FirebaseUser user;
    boolean isMyProfile = true;
    private ImageButton editProfileBtn;
    ImageView bookmarkBtn; 
    String currentUserId;  // The ID of the current user viewing the profile
    String profileUserId;  // The ID of the profile being viewed
    private Uri imageUri;
    //FirestoreRecyclerAdapter is a class that is used to get the post images from the Firestore database and display the user's posts in a grid. 
    FirestoreRecyclerAdapter adapter;

    public Profile() {
        // Required empty public constructor
    }

    private final ActivityResultLauncher<CropImageContractOptions> cropImage =
            registerForActivityResult(new CropImageContract(), result -> {
                if (result.isSuccessful()) {
                    imageUri = result.getUriContent();
                    uploadImage(imageUri);
                } else {
                    Exception error = result.getError();
                    Toast.makeText(getContext(), "Cropping failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.d("Profile", "yo profile started");
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("ProfileDebug", "onViewCreated - profileUserId: " + profileUserId);
        init(view);
        
        // Test follow functionality
        String testUserId = "QZiluGdKbqesEDAXtI4PEssI3Mg2";
        
        // First get the current following list
        FirebaseFirestore.getInstance()
            .collection("Users")
            .document(user.getUid())
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    List<String> following = (List<String>) documentSnapshot.get("following");
                    if (following == null) {
                        following = new ArrayList<>();
                    }
                    
                    // Clean up any empty strings
                    following.removeIf(String::isEmpty);
                    
                    // Add the test user if not already following
                    if (!following.contains(testUserId)) {
                        following.add(testUserId);
                        
                        // Update both users' documents
                        FirebaseFirestore.getInstance()
                            .collection("Users")
                            .document(user.getUid())
                            .update("following", following)
                            .addOnSuccessListener(aVoid -> {
                                // Update the other user's followers
                                FirebaseFirestore.getInstance()
                                    .collection("Users")
                                    .document(testUserId)
                                    .get()
                                    .addOnSuccessListener(testUserDoc -> {
                                        List<String> followers = (List<String>) testUserDoc.get("followers");
                                        if (followers == null) {
                                            followers = new ArrayList<>();
                                        }
                                        followers.removeIf(String::isEmpty);
                                        
                                        if (!followers.contains(user.getUid())) {
                                            followers.add(user.getUid());
                                            FirebaseFirestore.getInstance()
                                                .collection("Users")
                                                .document(testUserId)
                                                .update("followers", followers)
                                                .addOnSuccessListener(aVoid1 -> {
                                                    Log.d("FollowTest", "Successfully followed user: " + testUserId);
                                                    Toast.makeText(getContext(), "Followed test user", Toast.LENGTH_SHORT).show();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e("FollowTest", "Error updating followers: " + e.getMessage());
                                                });
                                        }
                                    });
                            })
                            .addOnFailureListener(e -> {
                                Log.e("FollowTest", "Error updating following: " + e.getMessage());
                            });
                    }
                }
            });

        if (isMyProfile) {
            followBtn.setVisibility(View.GONE);
            countLayout.setVisibility(View.VISIBLE);
        } else {
            followBtn.setVisibility(View.VISIBLE);
            countLayout.setVisibility(View.GONE);
        }

        getBasicData();
        //new addition

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        loadPostImages();
        recyclerView.setAdapter(adapter);
        editProfileBtn.setOnClickListener(v -> {
            CropImageOptions options = new CropImageOptions();
            options.guidelines = CropImageView.Guidelines.ON;
            options.aspectRatioX = 1;
            options.aspectRatioY = 1;

            cropImage.launch(new CropImageContractOptions(null, options));
        });

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize Firebase Auth first
        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        currentUserId = user.getUid();
        
        // By default, show current user's profile
        profileUserId = currentUserId;
        isMyProfile = true;
        
        // Check if we're viewing a different user's profile
        if (getArguments() != null) {
            String userId = getArguments().getString("userId");
            Log.d("ProfileDebug", "Received user ID in onCreate: " + userId);
            
            if (userId != null) {
                profileUserId = userId;
                isMyProfile = userId.equals(currentUserId);
                Log.d("ProfileDebug", "isMyProfile set to: " + isMyProfile);
            }
        } else {
            Log.d("ProfileDebug", "No arguments received, showing current user's profile");
        }
    }

    private void init(View view){
        Log.d("ProfileDebug", "init - Starting initialization");
        Log.d("ProfileDebug", "init - Current profileUserId: " + profileUserId);
        
        bookmarkBtn = view.findViewById(R.id.bookmarkBtn);
        bookmarkBtn.setOnClickListener(v -> {
            Log.d("Profile", "Bookmark button clicked");
            ViewPager viewPager = getActivity().findViewById(R.id.viewPager);
            viewPager.setCurrentItem(1);  // Switch to Bookmarks tab (index 1)
        });
        nameTV = view.findViewById(R.id.nameTV);
        bioTV = view.findViewById(R.id.bioTV);
        countLayout = view.findViewById(R.id.countLayout);

        followersCountTV = view.findViewById(R.id.followersCountTV);
        followingCountTV = view.findViewById(R.id.followingCountTV);
        postCountTV = view.findViewById(R.id.postCountTV);

        profilePic = view.findViewById(R.id.profilePic);
        followBtn = view.findViewById(R.id.followBtn);

        recyclerView = view.findViewById(R.id.recyclerView);
        editProfileBtn = view.findViewById(R.id.edit_profileImage);

        setUpFollowButton();
    }
    private void setUpFollowButton() {
        if (user == null) {
            Log.e("ProfileError", "User is null");
            return;
        }

        // Hide follow button if it's the user's own profile
        if (user.getUid().equals(profileUserId)) {
            followBtn.setVisibility(View.GONE);
            return;
        }

        followBtn.setVisibility(View.VISIBLE);
        followBtn.setEnabled(false); // Disable button while loading

        // Check if current user is following this profile
        FirebaseFirestore.getInstance()
            .collection("Users")
            .document(user.getUid())
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    List<String> following = (List<String>) documentSnapshot.get("following");
                    if (following != null && following.contains(profileUserId)) {
                        followBtn.setText("Following");
                        followBtn.setBackgroundColor(getResources().getColor(R.color.colorBlack));
                    } else {
                        followBtn.setText("Follow");
                        followBtn.setBackgroundColor(getResources().getColor(R.color.colorBlack));
                    }
                }
                followBtn.setEnabled(true);
            })
            .addOnFailureListener(e -> {
                Log.e("ProfileError", "Error checking follow status: " + e.getMessage());
                followBtn.setEnabled(true);
            });

        followBtn.setOnClickListener(v -> {
            followBtn.setEnabled(false);
            FirebaseFirestore.getInstance()
                .collection("Users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> following = (List<String>) documentSnapshot.get("following");
                        if (following == null) {
                            following = new ArrayList<>();
                        }

                        if (following.contains(profileUserId)) {
                            // Unfollow
                            FirebaseFirestore.getInstance()
                                .collection("Users")
                                .document(user.getUid())
                                .update("following", FieldValue.arrayRemove(profileUserId))
                                .addOnSuccessListener(aVoid -> {
                                    FirebaseFirestore.getInstance()
                                        .collection("Users")
                                        .document(profileUserId)
                                        .update("followers", FieldValue.arrayRemove(user.getUid()))
                                        .addOnSuccessListener(aVoid1 -> {
                                            followBtn.setText("Follow");
                                            followBtn.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                                            updateFollowersCount();
                                            followBtn.setEnabled(true);
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("ProfileError", "Error updating followers: " + e.getMessage());
                                            followBtn.setEnabled(true);
                                        });
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("ProfileError", "Error updating following: " + e.getMessage());
                                    followBtn.setEnabled(true);
                                });
                        } else {
                            // Follow
                            FirebaseFirestore.getInstance()
                                .collection("Users")
                                .document(user.getUid())
                                .update("following", FieldValue.arrayUnion(profileUserId))
                                .addOnSuccessListener(aVoid -> {
                                    FirebaseFirestore.getInstance()
                                        .collection("Users")
                                        .document(profileUserId)
                                        .update("followers", FieldValue.arrayUnion(user.getUid()))
                                        .addOnSuccessListener(aVoid1 -> {
                                            followBtn.setText("Following");
                                            followBtn.setBackgroundColor(getResources().getColor(R.color.colorBlack));
                                            updateFollowersCount();
                                            followBtn.setEnabled(true);
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("ProfileError", "Error updating followers: " + e.getMessage());
                                            followBtn.setEnabled(true);
                                        });
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("ProfileError", "Error updating following: " + e.getMessage());
                                    followBtn.setEnabled(true);
                                });
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ProfileError", "Error checking follow status: " + e.getMessage());
                    followBtn.setEnabled(true);
                });
        });
    }

    private void updateFollowersCount() {
        FirebaseFirestore.getInstance()
            .collection("Users")
            .document(profileUserId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    List<String> followers = (List<String>) documentSnapshot.get("followers");
                    Log.d("ProfileDebug", "UpdateFollowersCount - Followers array: " + followers);
                    int count = followers != null ? followers.size() : 0;
                    Log.d("ProfileDebug", "UpdateFollowersCount - Setting count to: " + count);
                    followersCountTV.setText(String.valueOf(count));
                }
            });
    }

    private void getBasicData(){
        Log.d("ProfileDebug", "getBasicData - Starting with profileUserId: " + profileUserId);
        DocumentReference userRef = FirebaseFirestore.getInstance().collection("Users")
                .document(profileUserId);
        Log.d("ProfileDebug", "getBasicData - Created reference for user: " + profileUserId);
        userRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("ProfileError", "Error getting user data: " + error.getMessage());
                    return;
                }
                if (value != null && value.exists()) {
                    Log.d("ProfileDebug", "getBasicData - Successfully loaded data for user: " + profileUserId);
                    String name = value.getString("name");
                    String bio = value.getString("bio");
                    
                    // Get following and followers as lists
                    List<String> followingList = (List<String>) value.get("following");
                    List<String> followersList = (List<String>) value.get("followers");
                    
                    // Clean up empty strings
                    if (followingList != null) {
                        followingList.removeIf(String::isEmpty);
                    }
                    if (followersList != null) {
                        followersList.removeIf(String::isEmpty);
                    }
                    
                    // Log the arrays after cleanup
                    Log.d("ProfileDebug", "Following array after cleanup: " + followingList);
                    Log.d("ProfileDebug", "Followers array after cleanup: " + followersList);
                    
                    // Convert to counts
                    int following = followingList != null ? followingList.size() : 0;
                    int followers = followersList != null ? followersList.size() : 0;
                    
                    // Log the counts before setting
                    Log.d("ProfileDebug", "Following count: " + following);
                    Log.d("ProfileDebug", "Followers count: " + followers);
                    
                    final String profileURL = value.getString("profileImage");
                    
                    // Update UI with retrieved data
                    nameTV.setText(name);
                    bioTV.setText(bio);
                    followersCountTV.setText(String.valueOf(followers));
                    followingCountTV.setText(String.valueOf(following));
                    
                    // Log after setting
                    Log.d("ProfileDebug", "Set following count TV to: " + followingCountTV.getText());
                    Log.d("ProfileDebug", "Set followers count TV to: " + followersCountTV.getText());
                    
                    // Load profile image
                    if (profileURL != null && !profileURL.isEmpty()) {
                        Glide.with(getContext().getApplicationContext())
                            .load(profileURL)
                            .placeholder(R.drawable.girl)
                            .timeout(6500)
                            .into(profilePic);
                    }

                    // Count the user's posts
                    userRef.collection("Post Images")
                        .get()
                        .addOnSuccessListener(querySnapshot -> {
                            int postCount = querySnapshot.size();
                            postCountTV.setText(String.valueOf(postCount));
                            Log.d("Profile", "User has " + postCount + " posts");
                        })
                        .addOnFailureListener(e -> {
                            Log.e("ProfileError", "Error counting posts: " + e.getMessage());
                            postCountTV.setText("0");
                        });
                } else {
                    Log.d("ProfileDebug", "getBasicData - No data found for user: " + profileUserId);
                }
            }
        });
    }


    private void uploadImage(Uri uri)
    {
        StorageReference reference = FirebaseStorage.getInstance().getReference().child("Profile Images");
        reference.putFile(uri)
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful())
                        {
                            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageURL = uri.toString();
                                    UserProfileChangeRequest.Builder request = new UserProfileChangeRequest.Builder();
                                    request.setPhotoUri(uri);
                                    user.updateProfile(request.build());
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("profileImage", imageURL);
                                    FirebaseFirestore.getInstance().collection("Users").document(user.getUid()).update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                Toast.makeText(getContext(), "Update Successful", Toast.LENGTH_SHORT).show();
                                            }
                                            else {
                                                Toast.makeText(getContext(), "ERROR: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            });
                        }
                        else {
                            Toast.makeText(getContext(), "ERROR: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }
    private void loadPostImages(){
        DocumentReference reference = FirebaseFirestore.getInstance()
            .collection("Users")
            .document(profileUserId);

        Query query = reference.collection("Post Images");
        FirestoreRecyclerOptions<PostImageModel> options = new FirestoreRecyclerOptions.Builder<PostImageModel>()
                .setQuery(query, PostImageModel.class)
                .build();
        adapter = new FirestoreRecyclerAdapter<PostImageModel, PostImageHolder>(options) {
            @NonNull
            @Override
            public PostImageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_items, parent, false);
                return new PostImageHolder(view);
            }

            @Override
            public void onBindViewHolder(PostImageHolder holder, int position, PostImageModel model) {
                Glide.with(holder.itemView.getContext().getApplicationContext())
                        .load(model.getImageUrl())
                        .timeout(6500)
                        .into(holder.imageView);
                
                holder.imageView.setOnClickListener(v -> {
                    FirebaseFirestore.getInstance().collection("Users").document(profileUserId).collection("Post Images").document(model.getId()).get().addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            HomeModel post = documentSnapshot.toObject(HomeModel.class);
                            post.setUid(profileUserId);
                            post.setId(model.getId());
                            Home home = new Home(); 
                            Bundle bundle = new Bundle(); 
                            bundle.putSerializable("post", post);
                            home.setArguments(bundle);

                            View frameLayout = getActivity().findViewById(R.id.mainFrameLayout);
                            if (frameLayout != null) {
                                frameLayout.setVisibility(View.VISIBLE);
                            }
                            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                            transaction.replace(R.id.mainFrameLayout, home);
                            transaction.addToBackStack(null).commit();
                        }

                    });
                }); 
            }
            
        };
    }
    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }
    private static class PostImageHolder extends RecyclerView.ViewHolder {

        ImageView imageView;

        public PostImageHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView);

        }

    }
}