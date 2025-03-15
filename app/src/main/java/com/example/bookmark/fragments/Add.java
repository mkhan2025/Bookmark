package com.example.bookmark.fragments;

import static com.example.bookmark.utils.imageContent.loadSavedImages;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.canhub.cropper.CropImageView;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageContractOptions;

import org.checkerframework.checker.units.qual.A;

import java.io.File;
import java.util.ArrayList;
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
    Uri imageUri;
    public Add() {
        // Required empty public constructor
    }
    private final ActivityResultLauncher<CropImageContractOptions> cropImage =
            registerForActivityResult(new CropImageContract(), result -> {
                if (result.isSuccessful()) {
                    // Get cropped image URI
                    imageUri = result.getUriContent();

                    // Display the cropped image
                    Glide.with(getContext()).load(imageUri).into(img);
                    img.setVisibility(View.VISIBLE);
                    next.setVisibility(View.VISIBLE);
                } else {
                    // Handle error
                    Exception error = result.getError();
                    Toast.makeText(getContext(), "Cropping failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        recyclerView.setHasFixedSize(true);
        list = new ArrayList<>();
        adapter = new GalleryAdapter(list);
        recyclerView.setAdapter(adapter);
        clickListener();
    }

    private void clickListener(){
        adapter.SendImage(new GalleryAdapter.SendImage() {
            @Override
            public void onSend(Uri picUri) {
//                imageUri = picUri;
//                Glide.with(getContext()).load(picUri).into(img);
//                img.setVisibility(View.VISIBLE);
//                next.setVisibility(View.VISIBLE);

                imageUri = picUri; // Store original URI

                // Launch cropping activity
                CropImageOptions options = new CropImageOptions();
                options.guidelines = CropImageView.Guidelines.ON;
                options.aspectRatioX = 4;
                options.aspectRatioY = 3;

                cropImage.launch(new CropImageContractOptions(picUri, options));
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageReference = storage.getReference().child("Post Images/" + System.currentTimeMillis());
                storageReference.putFile(imageUri)
                        .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful())
                                {
                                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            uploadData(uri.toString());
                                        }
                                    });
                                }
                            }
                        });
            }
        });
    }

    private void uploadData (String imageURL)
    {
        CollectionReference reference = FirebaseFirestore.getInstance().collection("Users").document(user.getUid()).collection("Post Images");
        Map<String, Object> map = new HashMap<>();
        String description = descET.getText().toString();
        String id = reference.document().getId();

        map.put("id", id);
        map.put("imageUrl", imageURL);
        map.put("description", description);
        map.put("username", user.getDisplayName() );
        map.put("profileImage", String.valueOf(user.getPhotoUrl()));
        map.put("likeCount", 0);
        map.put("comment", "");
        map.put("uid", user.getUid());


        reference.document(id).set(map)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            System.out.println();
                        }
                        else
                        {
                            Toast.makeText(getContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        

    }
    private void init(View view)
    {
        descET = view.findViewById(R.id.descriptionET);
        img = view.findViewById(R.id.imageView);
        recyclerView = view.findViewById(R.id.recyclerView);
        back = view.findViewById(R.id.backBtn);
        next = view.findViewById(R.id.forwardBtn);
        user = FirebaseAuth.getInstance().getCurrentUser();
    }


    @Override
    public void onResume() {
        super.onResume();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Dexter.withContext(getContext())
                        .withPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                        .withListener(new MultiplePermissionsListener() {
                            @Override
                            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                                if (multiplePermissionsReport.areAllPermissionsGranted())
                                {
                                    File file = new File(Environment.getExternalStorageDirectory().toString() + "/Download");
                                    if (file.exists())
                                    {
                                        File [] files = file.listFiles();
                                        assert files != null;
                                        for (File file1: files)
                                        {
                                            if (file1.getAbsolutePath().endsWith(".jpg") || file1.getAbsolutePath().endsWith(".png")){
                                                list.add(new Galleryimages(Uri.fromFile(file1)));
                                                adapter.notifyDataSetChanged();
                                            }
                                        }
                                    }

                                }
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {

                            }
                        }).check();
            }
        });
    }


}