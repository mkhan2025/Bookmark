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
import androidx.viewpager2.widget.ViewPager2;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.canhub.cropper.CropImage;
import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;
import com.example.bookmark.MainActivity;
import com.example.bookmark.R;
import com.example.bookmark.adapter.GalleryAdapter;
import com.example.bookmark.adapter.ImageCarouselAdapter;
import com.example.bookmark.model.Galleryimages;
import com.example.bookmark.model.PostImageModel;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
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
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tbuonomo.viewpagerdotsindicator.SpringDotsIndicator;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
//Add is a fragment that is used to add a new post. It uses the GalleryAdapter to display the images in the RecyclerView. It also uses the AutocompleteSupportFragment to get the location of the post.
public class Add extends Fragment {

    private EditText descET;
    private ViewPager2 imageViewPager;
    private SpringDotsIndicator dotsIndicator;
    private TextView imageCountText;
    private RecyclerView selectedImagesRecyclerView;
    private ImageButton back;
    private ImageButton next;
    private ImageCarouselAdapter carouselAdapter;
    private List<Uri> selectedImages;
    private FirebaseUser user;
    private Dialog dialog;
    private Place selectedPlace;
    private FloatingActionButton cameraButton;
    private static final int MAX_IMAGES = 8;

    //autocomplete widget prediction for location selection
    AutocompleteSupportFragment autocompleteFragment;
    String [] activityType = {"Nature & Adventure", "Cultural & Historical", "Food & Drink", "Events & Entertainment", "Relaxation & Wellness", "Shopping", "Indoor", "Outdoor", "Transit"};
    //autocomplete widget prediction for activity type selection
    AutoCompleteTextView autoCompleteTextView;
    //ArrayAdapter is an adapter that is used to display the activity type in the dropdown
    ArrayAdapter<String> adapterItems;
    String activityItem;


    public Add() {
        Log.d("Add", "started");
        // Required empty public constructor
    }
    //ActivityResultLauncher is a class that is used to launch the activity result of the cropImage
    private final ActivityResultLauncher<CropImageContractOptions> cropImage =
            //method registerForActivityResult is used to register the activity result of the cropImage
            registerForActivityResult(new CropImageContract(), result -> {
                Log.d("AddFragment", "CropImage result received");
                if (result.isSuccessful()) {
                    Log.d("AddFragment", "CropImage was successful");
                    selectedImages.add(result.getUriContent());
                    if (selectedImages.size() > 0) {
                        Log.d("AddFragment", "Image URI is not null: " + selectedImages.get(selectedImages.size() - 1).toString());
                        carouselAdapter.setImages(selectedImages);
                        imageViewPager.setCurrentItem(selectedImages.size() - 1, false);
                        updateImageCount();
                    } else {
                        Log.e("AddFragment", "Image URI is null after successful crop");
                    }
                } else {
                    Log.e("AddFragment", "CropImage failed: " + result.getError().getMessage());
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
        Log.d("AddFragment", "onViewCreated called");
        Log.d("FirestoreTest", "Firestore instance: " + FirebaseFirestore.getInstance());
        selectedImages = new ArrayList<>();
        init(view);
        setupImageCarousel();
        setupImageSelection();
        setupPlacesAPI();
        clickListener();
    }

    private void setupImageCarousel() {
        carouselAdapter = new ImageCarouselAdapter(selectedImages);
        imageViewPager.setAdapter(carouselAdapter);
        dotsIndicator.setViewPager2(imageViewPager);

        imageViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateImageCount();
            }
        });
    }

    private void setupImageSelection() {
        cameraButton.setOnClickListener(v -> {
            if (selectedImages.size() >= MAX_IMAGES) {
                Toast.makeText(getContext(), "Maximum " + MAX_IMAGES + " images allowed", Toast.LENGTH_SHORT).show();
                return;
            }
            openImagePicker();
        });
    }

    private void openImagePicker() {
        CropImageOptions options = new CropImageOptions();
        options.guidelines = CropImageView.Guidelines.ON;
        cropImage.launch(new CropImageContractOptions(null, options));
    }

    private void updateImageCount() {
        if (!selectedImages.isEmpty()) {
            imageCountText.setVisibility(View.VISIBLE);
            imageCountText.setText(String.format("%d/%d", imageViewPager.getCurrentItem() + 1, selectedImages.size()));
            next.setVisibility(View.VISIBLE);
        } else {
            imageCountText.setVisibility(View.GONE);
            next.setVisibility(View.GONE);
        }
    }

    private void init(View view) {
        Log.d("AddFragment", "init called");
        cameraButton = view.findViewById(R.id.camera_button);
        descET = view.findViewById(R.id.descriptionET);
        imageViewPager = view.findViewById(R.id.imageViewPager);
        dotsIndicator = view.findViewById(R.id.dots_indicator);
        imageCountText = view.findViewById(R.id.imageCountText);
        selectedImagesRecyclerView = view.findViewById(R.id.selectedImagesRecyclerView);
        back = view.findViewById(R.id.backBtn);
        next = view.findViewById(R.id.forwardBtn);
        Log.d("AddFragment", "Next button found: " + (next != null));
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
        autoCompleteTextView = view.findViewById(R.id.autocomplete_text);
        adapterItems = new ArrayAdapter<String>(getContext(), R.layout.list_item, activityType);
        autoCompleteTextView.setAdapter(adapterItems);
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                activityItem = parent.getItemAtPosition(position).toString();
                Toast.makeText(getContext(), "Activity Item: " + activityItem, Toast.LENGTH_SHORT).show();
            }
        });
        // Ensure dropdown shows when clicked or focused
        autoCompleteTextView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                autoCompleteTextView.showDropDown();
            }
        });

        autoCompleteTextView.setOnClickListener(v -> autoCompleteTextView.showDropDown());
        autoCompleteTextView.setThreshold(1);
        imageViewPager.setVisibility(View.VISIBLE);
        next.setVisibility(View.GONE);
    }

    private void setupPlacesAPI() {
        String apiKey = "AIzaSyA7chOcKSTr-xNmL6bwz_Txw5LQABIzNC4";
//        Places.initialize(getApplicationContext(), apiKey);
//        PlacesClient placesClient = Places.createClient(this);

        // Initialize App Check
        // Initialize App Check

// Initialize Places SDK



        // Initialize AutocompleteSupportFragment
        Log.d("PlacesAPI", "Setting up AutocompleteSupportFragment in Add fragment");
        autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        Log.d("PlacesAPI", "AutocompleteFragment found: " + (autocompleteFragment != null));

        if (autocompleteFragment != null) {
            try {
                Places.initializeWithNewPlacesApiEnabled(getContext(), apiKey);
                //PlacesClient is a client that is used to get the places from the Places API
                PlacesClient placesClient = Places.createClient(getContext());

                // Get the shared PlacesClient instance
//                PlacesClient placesClient = MainActivity.getPlacesClient();
                if (placesClient == null) {
                    Log.e("PlacesAPI", "PlacesClient is null from MainActivity");
                    return;
                }
                Log.d("PlacesAPI", "Got PlacesClient instance from MainActivity");
                //set the place fields to be returned from the Places API
                autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
                Log.d("PlacesAPI", "Place fields set successfully");
                //set the place selection listener
                autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                    @Override
                    public void onPlaceSelected(@NonNull Place place) {
                        selectedPlace = place;
                        Log.d("Autocomplete", "Selected Place: " + place.getName() + ", ID: " + place.getId());
                        Toast.makeText(getContext(), "Selected Place: " + place.getName(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(@NonNull Status status) {
                        Log.e("PlacesAPI", "Place selection error: " + status.getStatusMessage());
                        Log.e("PlacesAPI", "Status code: " + status.getStatusCode());
                        Log.e("PlacesAPI", "Error details: " + status.getStatusMessage());
                    }
                });
                imageViewPager.setVisibility(View.VISIBLE);
                 next.setVisibility(View.GONE);
                Log.d("PlacesAPI", "Place selection listener set successfully");
            } catch (Exception e) {
                Log.e("PlacesAPI", "Error setting up AutocompleteFragment: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void clickListener() {
        Log.d("AddFragment", "clickListener called");
        // Click listener for selecting an image
        imageViewPager.setOnClickListener(v -> {
            Log.d("AddFragment", "Image clicked, opening image picker");
            openImagePicker();
        });
        next.setOnClickListener(v -> uploadImages());
    }

    private void uploadImages() {
        if (selectedImages.isEmpty()) {
            Toast.makeText(getContext(), "Please select at least one image!", Toast.LENGTH_SHORT).show();
            return;
        }

        dialog.show();
        List<String> uploadedUrls = new ArrayList<>();
        AtomicInteger uploadCount = new AtomicInteger(0);

        for (Uri imageUri : selectedImages) {
            StorageReference storageReference = FirebaseStorage.getInstance()
                .getReference()
                .child("Post Images/" + System.currentTimeMillis() + "_" + uploadCount.get());

            storageReference.putFile(imageUri)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            uploadedUrls.add(uri.toString());
                            if (uploadedUrls.size() == selectedImages.size()) {
                                uploadData(uploadedUrls);
                            }
                        });
                    } else {
                        dialog.dismiss();
                        Toast.makeText(getContext(), "Upload failed!", Toast.LENGTH_SHORT).show();
                    }
                });
            uploadCount.incrementAndGet();
        }
    }

    private void uploadData(List<String> imageUrls) {
        Log.d("FirestoreUpload", "Uploading activity type: " + imageUrls);
        //addOnSuccessListener is a method that is used to get the user's name from the Firestore database
        FirebaseFirestore.getInstance().collection("Users").document(user.getUid()).get().addOnSuccessListener(documentSnapshot -> {
            String userName = documentSnapshot.getString("name");
            Log.d("AddFragment", "User name from profile: " + userName);

        CollectionReference reference = FirebaseFirestore.getInstance().collection("Users")
                .document(user.getUid()).collection("Post Images");

        String description = descET.getText().toString();
        String id = reference.document().getId();

        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("imageUrls", imageUrls);
        map.put("description", description);

//         Commented out for now as you were not uploading user details like username
         map.put("name", userName);
         map.put("profileImage", String.valueOf(user.getPhotoUrl()));
         map.put("likeCount", 0);
         map.put("comment", "");
         map.put("uid", user.getUid());
         map.put("timestamp", System.currentTimeMillis());

        if (selectedPlace != null) {
            map.put("placeId", selectedPlace.getId());
            map.put("locationName", selectedPlace.getName());
        
        if (selectedPlace.getLatLng() != null){
            map.put("latitude", selectedPlace.getLatLng().latitude);
            map.put("longitude", selectedPlace.getLatLng().longitude);
        }
        }
        if(activityItem != null){
            map.put("activityType", activityItem);
        }

        reference.document(id).set(map)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("FirestoreUploadSuccess", "Uploading activity type: " + activityItem);
                        Log.d("FirestoreUpload", "Data uploaded successfully");
                        Toast.makeText(getContext(), "Uploaded successfully", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Log.e("FirestoreUpload", "Error: " + task.getException().getMessage());
                        Toast.makeText(getContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                });
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
                        selectedImages.add(Uri.fromFile(file));
                    }
                }
                carouselAdapter.notifyDataSetChanged();
            }
        }
    }
}