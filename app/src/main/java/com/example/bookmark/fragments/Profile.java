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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


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
    String uid;
    private Uri imageUri;

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
        init(view);
        if (isMyProfile)
        {
            followBtn.setVisibility(View.GONE);
            countLayout.setVisibility(View.VISIBLE);
        }
        else {
            followBtn.setVisibility(View.VISIBLE);
            countLayout.setVisibility(View.GONE);
        }

        getBasicData();
        //new addition

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        editProfileBtn.setOnClickListener(v -> {
            CropImageOptions options = new CropImageOptions();
            options.guidelines = CropImageView.Guidelines.ON;
            options.aspectRatioX = 1;
            options.aspectRatioY = 1;

            cropImage.launch(new CropImageContractOptions(null, options));
        });

    }

    private void init(View view){

//        Toolbar toolbar = view.findViewById(R.id.toolbar);
//        if (getActivity() != null)
//        {
//            ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
//        }

        nameTV = view.findViewById(R.id.nameTV);
//        toolbarNameTV = view.findViewById(R.id.toolbarNameTV);
        bioTV = view.findViewById(R.id.bioTV);
        countLayout = view.findViewById(R.id.countLayout);

        followersCountTV = view.findViewById(R.id.followersCountTV);
        followingCountTV = view.findViewById(R.id.followingCountTV);
        postCountTV = view.findViewById(R.id.postCountTV);

        profilePic = view.findViewById(R.id.profilePic);
        followBtn = view.findViewById(R.id.followBtn);

        recyclerView = view.findViewById(R.id.recyclerView);
        editProfileBtn = view.findViewById(R.id.edit_profileImage);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
    }
    private void getBasicData(){
        DocumentReference userRef = FirebaseFirestore.getInstance().collection("Users")
                .document(user.getUid());
        userRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null)
                {
                   return;
                }
                assert value != null;
                if (value.exists())
                {
                    String name = value.getString("name");
                    String bio = value.getString("bio");
                    int following = value.getLong("following").intValue();
                    int followers = value.getLong("followers").intValue();
                    final String profileURL = value.getString("profileImage");

                    nameTV.setText(name);
//                    toolbarNameTV.setText(name);
                    bioTV.setText(bio);
                    followersCountTV.setText(String.valueOf(followers));
                    followingCountTV.setText(String.valueOf(following));

                    Glide.with(getContext().getApplicationContext()).load(profileURL).placeholder(R.drawable.girl).timeout(6500).into(profilePic);


                }
            }
        });
        postCountTV.setText(""+ LIST_SIZE);
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
}