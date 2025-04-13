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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.example.bookmark.adapter.GalleryAdapter;
import com.example.bookmark.adapter.PagerAdapter;
import com.example.bookmark.model.BookmarksModel;
import com.example.bookmark.model.Galleryimages;
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

public class BookmarksFragment extends Fragment {
    private RecyclerView recyclerView;
    private FirebaseUser user;
    private GalleryAdapter adapter;  // Change to GalleryAdapter
    private List<Galleryimages> list;  // Change to List<Galleryimages>

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        Log.d("BookmarksFragment", "onCreateView called");
        return inflater.inflate(R.layout.fragment_bookmarks, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("BookmarksFragment", "onViewCreated called");
        list = new ArrayList<>();
        adapter = new GalleryAdapter(list);

        init(view);
         if (recyclerView != null) {
            recyclerView.setAdapter(adapter);
        }
       loadBookmarks();
    }

    private void init(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);

        Log.d("BookmarksFragment", "init called");
        if (recyclerView == null) {
            Log.e("BookmarksFragment", "RecyclerView is null!");
        } else {
            Log.d("BookmarksFragment", "RecyclerView found");

            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

            FirebaseAuth auth = FirebaseAuth.getInstance();
            user = auth.getCurrentUser();
               if (user == null) {
            Log.e("BookmarksFragment", "User is null!");
        }
        }
    }

    private void loadBookmarks(){
                if (user == null) {
            Log.e("BookmarksFragment", "Cannot load bookmarks: user is null");
            return;
        }

Log.d("BookmarksFragment", "Starting to load bookmarks");
Log.d("BookmarksFragment", "Current user ID: " + user.getUid());
        Query query = FirebaseFirestore.getInstance()
            .collection("Users")
            .document(user.getUid())
            .collection("Bookmarks")
            .orderBy("timestamp", Query.Direction.DESCENDING);

        query.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("BookmarksFragment", "Error loading bookmarks", error);
                return;
            }

            if (value == null || value.isEmpty()) {
                Log.d("BookmarksFragment", "No bookmarks found");
                return;
            }
            Log.d("BookmarksFragment", "Bookmarks loaded: " + value.toObjects(BookmarksModel.class));

            list.clear();
            for (BookmarksModel bookmark : value.toObjects(BookmarksModel.class)) {
                 if (bookmark.getBookmarkedPostId() == null || bookmark.getOriginalUserId() == null) {
                    Log.e("BookmarksFragment", "Bookmark data is null: postId=" + bookmark.getBookmarkedPostId() +
                        ", originalUserId=" + bookmark.getOriginalUserId());
                    continue;
                }
                // Get the original post data
                FirebaseFirestore.getInstance()
                    .collection("Users")
                    .document(bookmark.getOriginalUserId())
                    .collection("Post Images")
                    .document(bookmark.getBookmarkedPostId())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String imageUrl = documentSnapshot.getString("imageUrl");
                            if (imageUrl != null) {
                                Uri imageUri = Uri.parse(imageUrl);
                                Galleryimages galleryModel = new Galleryimages(imageUri);
                                list.add(galleryModel);

                                // Update adapter with new data
                                adapter.notifyDataSetChanged();
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("BookmarksFragment", "Error fetching original post data", e);
                    });
            }
        });
    }

    @Override
    public void onStart() {
            Log.d("BookmarksFragment", "onStart called");
        super.onStart();
            Log.d("BookmarksFragment", "Adapter set");

    }
}