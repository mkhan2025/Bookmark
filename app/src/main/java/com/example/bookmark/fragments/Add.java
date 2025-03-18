package com.example.bookmark.fragments;

import static com.example.bookmark.utils.imageContent.loadSavedImages;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.canhub.cropper.CropImage;
import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;
import com.example.bookmark.R;
import com.example.bookmark.adapter.GalleryAdapter;
import com.example.bookmark.model.Galleryimages;
import com.example.bookmark.model.PostImageModel;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Add extends Fragment {

    private EditText descET;
    private ImageView img;
    private RecyclerView recyclerView;
    private ImageButton back;
    private ImageButton next;
    private GalleryAdapter adapter;
    private List<Galleryimages> list;
    private FirebaseUser user;
    private Uri imageUri; // Store selected image
    Dialog dialog;
    private Place selectedPlace;  // Stores the selected place from AutocompleteSupportFragment

    //autocomplete widget prediction
    AutocompleteSupportFragment autocompleteFragment;

    public Add() {
        // Required empty public constructor
    }

    private final ActivityResultLauncher<CropImageContractOptions> cropImage =
            registerForActivityResult(new CropImageContract(), result -> {
                if (result.isSuccessful()) {
                    imageUri = result.getUriContent();
                    if (imageUri != null) {
                        Glide.with(requireContext()).load(imageUri).into(img);
                        img.setVisibility(View.VISIBLE);
                        next.setVisibility(View.VISIBLE); // Show the next button
                    }
                } else {
                    Toast.makeText(getContext(), "Cropping failed: " + result.getError().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("FirestoreTest", "Firestore instance: " + FirebaseFirestore.getInstance());
        init(view);

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        recyclerView.setHasFixedSize(true);
        list = new ArrayList<>();
        adapter = new GalleryAdapter(list);
        recyclerView.setAdapter(adapter);

        requestPermissionsAndLoadGallery();
        clickListener();
    }

    private void clickListener() {
        // Click listener for selecting an image
        img.setOnClickListener(v -> openImagePicker());

        adapter.SendImage(picUri -> {
//            imageUri = picUri;
            CropImageOptions options = new CropImageOptions();
            options.guidelines = CropImageView.Guidelines.ON;
            options.aspectRatioX = 4;
            options.aspectRatioY = 3;
            cropImage.launch(new CropImageContractOptions(picUri, options));
        });

        next.setOnClickListener(v -> {
            if (imageUri == null) {
                Toast.makeText(getContext(), "Please select an image first!", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageReference = storage.getReference().child("Post Images/" + System.currentTimeMillis());
            dialog.show();
            storageReference.putFile(imageUri)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            storageReference.getDownloadUrl().addOnSuccessListener(uri -> uploadData(uri.toString()));
                        } else {
                            Toast.makeText(getContext(), "Upload failed!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    });
        });
    }

    private void openImagePicker() {
        CropImageOptions options = new CropImageOptions();
        options.guidelines = CropImageView.Guidelines.ON;
        cropImage.launch(new CropImageContractOptions(null, options));
    }

    private void uploadData(String imageURL) {
        Log.d("FirestoreUpload", "Uploading image with URL: " + imageURL);
        CollectionReference reference = FirebaseFirestore.getInstance().collection("Users")
                .document(user.getUid()).collection("Post Images");

        String description = descET.getText().toString();
        String id = reference.document().getId();

        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("imageUrl", imageURL);
        map.put("description", description);

//         Commented out for now as you were not uploading user details like username
         map.put("username", user.getDisplayName());
         map.put("profileImage", String.valueOf(user.getPhotoUrl()));
         map.put("likeCount", 0);
         map.put("comment", "");
         map.put("uid", user.getUid());

        if (selectedPlace != null) {
            map.put("placeId", selectedPlace.getId());
            map.put("locationName", selectedPlace.getName());
        }

        reference.document(id).set(map)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("FirestoreUpload", "Uploading image with URL: " + imageURL);
                        Log.d("FirestoreUpload", "Data uploaded successfully");
                        Toast.makeText(getContext(), "uploaded", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Log.e("FirestoreUpload", "Error: " + task.getException().getMessage());
                        Toast.makeText(getContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                });
        Log.d("FirestoreTest", "Upload complete.");
    }

    private void requestPermissionsAndLoadGallery() {
        Dexter.withContext(getContext())
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            loadGalleryImages();
                        } else {
                            Toast.makeText(getContext(), "Storage permissions are required!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    private void loadGalleryImages() {
        File directory = new File(Environment.getExternalStorageDirectory().toString() + "/Download");
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getAbsolutePath().endsWith(".jpg") || file.getAbsolutePath().endsWith(".png")) {
                        list.add(new Galleryimages(Uri.fromFile(file)));
                    }
                }
                adapter.notifyDataSetChanged();
            }
        }
    }

    private void init(View view) {
        descET = view.findViewById(R.id.descriptionET);
        img = view.findViewById(R.id.imageView);
        recyclerView = view.findViewById(R.id.recyclerView);
        back = view.findViewById(R.id.backBtn);
        next = view.findViewById(R.id.forwardBtn);
        user = FirebaseAuth.getInstance().getCurrentUser();
        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.loading_dialog);
        dialog.getWindow().setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.dialog_bg, null));
        dialog.setCancelable(false);
        autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setLocationBias(RectangularBounds.newInstance(new LatLng(39.64379259389463, -86.8643773763308), new LatLng(40.7128, 74.0060)));
        autocompleteFragment.setCountries("US");
        //specify the types of place data to return
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                selectedPlace = place;  // <-- Now we are setting it here
                Log.d("Autocomplete", "Selected Place: " + place.getName() + ", ID: " + place.getId());
                Toast.makeText(getContext(), "Selected Place: " + place.getName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(@NonNull Status status) {
                Log.e("Autocomplete", "Error: " + status);
                Toast.makeText(getContext(), "Error: " + status, Toast.LENGTH_SHORT).show();
            }
        });
        img.setVisibility(View.VISIBLE);
        next.setVisibility(View.GONE);
    }
}

//package com.example.bookmark.fragments;
//
//import static com.example.bookmark.utils.imageContent.loadSavedImages;
//
//import android.Manifest;
//import android.content.Intent;
//import android.net.Uri;
//import android.os.Bundle;
//
//import androidx.activity.result.ActivityResultLauncher;
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//import androidx.recyclerview.widget.GridLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import android.os.Environment;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.EditText;
//import android.widget.ImageButton;
//import android.widget.ImageView;
//import android.widget.Toast;
//
//import com.bumptech.glide.Glide;
//import com.canhub.cropper.CropImage;
//import com.canhub.cropper.CropImageContract;
//import com.canhub.cropper.CropImageContractOptions;
//import com.canhub.cropper.CropImageOptions;
//import com.canhub.cropper.CropImageView;
//import com.example.bookmark.R;
//import com.example.bookmark.adapter.GalleryAdapter;
//import com.example.bookmark.model.Galleryimages;
//import com.example.bookmark.model.PostImageModel;
//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.android.gms.tasks.Task;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.firestore.CollectionReference;
//import com.google.firebase.firestore.FirebaseFirestore;
//import com.google.firebase.storage.FirebaseStorage;
//import com.google.firebase.storage.StorageReference;
//import com.google.firebase.storage.UploadTask;
//import com.karumi.dexter.Dexter;
//import com.karumi.dexter.MultiplePermissionsReport;
//import com.karumi.dexter.PermissionToken;
//import com.karumi.dexter.listener.PermissionRequest;
//import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
//import com.canhub.cropper.CropImageView;
//import com.canhub.cropper.CropImageOptions;
//import com.canhub.cropper.CropImageContractOptions;
//
//import org.checkerframework.checker.units.qual.A;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class Add extends Fragment {
//    private EditText descET;
//    private ImageView img;
//    private RecyclerView recyclerView;
//    private ImageButton back;
//    private ImageButton next;
//    private GalleryAdapter adapter;
//    private List<Galleryimages> list;
//    private FirebaseUser user;
//    Uri imageUri;
//    public Add() {
//        // Required empty public constructor
//    }
//    private final ActivityResultLauncher<CropImageContractOptions> cropImage =
//            registerForActivityResult(new CropImageContract(), result -> {
//                if (result.isSuccessful()) {
//                    // Get cropped image URI
//                    imageUri = result.getUriContent();
//
//                    // Display the cropped image
//                    Glide.with(getContext()).load(imageUri).into(img);
////                    img.setVisibility(View.VISIBLE);
//                    next.setVisibility(View.VISIBLE);
//                } else {
//                    // Handle error
//                    Exception error = result.getError();
//                    Toast.makeText(getContext(), "Cropping failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
//                }
//            });
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_add, container, false);
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//        init(view);
//        img.setVisibility(View.VISIBLE);
//        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
//        recyclerView.setHasFixedSize(true);
//        list = new ArrayList<>();
//        adapter = new GalleryAdapter(list);
//        recyclerView.setAdapter(adapter);
//        clickListener();
//    }
//
//    private void clickListener(){
//        adapter.SendImage(new GalleryAdapter.SendImage() {
//            @Override
//            public void onSend(Uri picUri) {
////                imageUri = picUri;
////                Glide.with(getContext()).load(picUri).into(img);
////                img.setVisibility(View.VISIBLE);
////                next.setVisibility(View.VISIBLE);
//
//                imageUri = picUri; // Store original URI
//
//                // Launch cropping activity
//                CropImageOptions options = new CropImageOptions();
//                options.guidelines = CropImageView.Guidelines.ON;
//                options.aspectRatioX = 4;
//                options.aspectRatioY = 3;
//
//                cropImage.launch(new CropImageContractOptions(picUri, options));
//            }
//        });
//
//        next.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                FirebaseStorage storage = FirebaseStorage.getInstance();
//                StorageReference storageReference = storage.getReference().child("Post Images/" + System.currentTimeMillis());
//                storageReference.putFile(imageUri)
//                        .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//                            @Override
//                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
//                                if (task.isSuccessful())
//                                {
//                                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                                        @Override
//                                        public void onSuccess(Uri uri) {
//                                            uploadData(uri.toString());
//                                        }
//                                    });
//                                }
//                            }
//                        });
//            }
//        });
//    }
//
//    private void uploadData (String imageURL)
//    {
//        CollectionReference reference = FirebaseFirestore.getInstance().collection("Users").document(user.getUid()).collection("Post Images");
//        Map<String, Object> map = new HashMap<>();
//        String description = descET.getText().toString();
//        String id = reference.document().getId();
//
//        map.put("id", id);
//        map.put("imageUrl", imageURL);
//        map.put("description", description);
//        map.put("username", user.getDisplayName() );
//        map.put("profileImage", String.valueOf(user.getPhotoUrl()));
//        map.put("likeCount", 0);
//        map.put("comment", "");
//        map.put("uid", user.getUid());
//
//
//        reference.document(id).set(map)
//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        if (task.isSuccessful())
//                        {
//                            System.out.println();
//                        }
//                        else
//                        {
//                            Toast.makeText(getContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
//
//
//    }
//    private void init(View view)
//    {
//        descET = view.findViewById(R.id.descriptionET);
//        img = view.findViewById(R.id.imageView);
//        recyclerView = view.findViewById(R.id.recyclerView);
//        back = view.findViewById(R.id.backBtn);
//        next = view.findViewById(R.id.forwardBtn);
//        user = FirebaseAuth.getInstance().getCurrentUser();
//    }
//
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        getActivity().runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                Dexter.withContext(getContext())
//                        .withPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
//                        .withListener(new MultiplePermissionsListener() {
//                            @Override
//                            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
//                                if (multiplePermissionsReport.areAllPermissionsGranted())
//                                {
//                                    File file = new File(Environment.getExternalStorageDirectory().toString() + "/Download");
//                                    if (file.exists())
//                                    {
//                                        File [] files = file.listFiles();
//                                        assert files != null;
//                                        for (File file1: files)
//                                        {
//                                            if (file1.getAbsolutePath().endsWith(".jpg") || file1.getAbsolutePath().endsWith(".png")){
//                                                list.add(new Galleryimages(Uri.fromFile(file1)));
//                                                adapter.notifyDataSetChanged();
//                                            }
//                                        }
//                                    }
//
//                                }
//                            }
//
//                            @Override
//                            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
//
//                            }
//                        }).check();
//            }
//        });
//    }
//
//
//}