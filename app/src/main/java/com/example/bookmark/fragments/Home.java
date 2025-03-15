package com.example.bookmark.fragments;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.example.bookmark.R;
import com.example.bookmark.ReplacerActivity;
import com.example.bookmark.adapter.HomeAdapter;
import com.example.bookmark.model.HomeModel;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class Home extends Fragment {
    private RecyclerView recyclerView;
    private FirebaseUser user;
    HomeAdapter adapter;
    private List<HomeModel> list;
    private ImageButton sendBtn;

    DocumentReference reference;

    Activity activity;

    public Home() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity = getActivity();
        init(view);
//        reference = FirebaseFirestore.getInstance().collection("Posts").document(user.getUid());
        list = new ArrayList<>();
        adapter = new HomeAdapter(list, getActivity());
        recyclerView.setAdapter(adapter);
        loadDataFromFirestore();

        //

    }
    private void init(View view){
//        Toolbar toolbar = view.findViewById(R.id.toolbar);
//        if (getActivity() != null)
//        {
//            ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
//        }
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

    }
    private void loadDataFromFirestore() {
        if (user == null) {
            Log.e("FirestoreError", "User is null");
            return;
        }

        list.clear(); // Clear list before adding new data

        CollectionReference collectionReference = FirebaseFirestore.getInstance()
                .collection("Users")
                .document(user.getUid())
                .collection("Post Images");

        collectionReference.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("FirestoreError", error.getMessage());
                return;
            }

            if (value == null || value.isEmpty()) {
                Log.e("FirestoreError", "No documents found");
                return;
            }

            for (QueryDocumentSnapshot snapshot : value) {
                String uid = snapshot.getString("uid") != null ? snapshot.getString("uid") : "";
                String profileImage = snapshot.getString("profileImage") != null ? snapshot.getString("profileImage") : "";
                String postImage = snapshot.getString("postImage") != null ? snapshot.getString("postImage") : "";
                String username = snapshot.getString("name") != null ? snapshot.getString("username") : "";
                String comment = snapshot.getString("comment") != null ? snapshot.getString("comment") : "";
                String description = snapshot.getString("description") != null ? snapshot.getString("description") : "";
                String id = snapshot.getString("id") != null ? snapshot.getString("id") : "";
                int likeCount = snapshot.contains("likeCount") ? snapshot.getLong("likeCount").intValue() : 0;
                int localPostImage = snapshot.contains("localPostImage") ? snapshot.getLong("localPostImage").intValue() : 0;

                list.add(new HomeModel(uid, profileImage, postImage, username, comment, description, id, likeCount, localPostImage));
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
            }
        });
    }

}

