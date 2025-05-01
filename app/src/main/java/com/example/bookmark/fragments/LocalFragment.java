package com.example.bookmark.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.app.ActivityCompat;

import com.example.bookmark.R;
import com.example.bookmark.adapter.HomeAdapter;
import com.example.bookmark.model.HomeModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import android.location.Location;
import android.Manifest;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class LocalFragment extends Fragment {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    // HomeAdapter adapter;
    private List<HomeModel> list;
    private HomeAdapter localAdapter;
    private ImageButton backButton;
    DocumentReference reference;

    public LocalFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_local, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
        clickListener();
        list = new ArrayList<>();
        localAdapter = new HomeAdapter(list, getActivity());
        recyclerView.setAdapter(localAdapter);
        showLoading();
        loadLocalData();
    }

    public void init(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        progressBar = view.findViewById(R.id.progressBar);
        backButton = view.findViewById(R.id.backButton);
    }
    public void clickListener() {
        backButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                // Hide the mainFrameLayout first
                View frameLayout = getActivity().findViewById(R.id.mainFrameLayout);
                if (frameLayout != null) {
                    frameLayout.setVisibility(View.GONE);
                }
                // Then pop the back stack
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }
    public void loadLocalData() {
        showLoading();
        list.clear();

        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        if (ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            hideLoading();
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location == null) {
                hideLoading();
                return;
            }

            final LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());

            FirebaseFirestore.getInstance().collection("Users").get().addOnSuccessListener(userSnap -> {
                final int totalUsers = userSnap.size();
                final AtomicInteger processedUsers = new AtomicInteger(0);

                if (totalUsers == 0) {
                    hideLoading();
                    return;
                }

                for (DocumentSnapshot snapshot : userSnap) {
                    if (!snapshot.exists()) {
                        continue;
                    }
                    String userId = snapshot.getId();

                    FirebaseFirestore.getInstance()
                            .collection("Users")
                            .document(userId)
                            .collection("Post Images")
                            .get()
                            .addOnSuccessListener(postSnap -> {
                                for (DocumentSnapshot postSnapshot : postSnap) {
                                    if (!postSnapshot.exists()) {
                                        continue;
                                    }
                                    HomeModel post = postSnapshot.toObject(HomeModel.class);
                                    post.setUid(userId);
                                    post.setId(postSnapshot.getId());

                                    LatLng postLocation = new LatLng(
                                            post.getLatitude(),
                                            post.getLongitude()
                                    );

                                    float[] results = new float[1];
                                    Location.distanceBetween(
                                            userLocation.latitude,
                                            userLocation.longitude,
                                            postLocation.latitude,
                                            postLocation.longitude,
                                            results
                                    );

                                    // Convert meters to miles
                                    float distanceInMiles = results[0] * 0.000621371f;

                                    // Only add posts within 10 miles
                                    if (distanceInMiles <= 10) {
                                        list.add(post);
                                    }
                                }

                                int processed = processedUsers.incrementAndGet();
                                if (processed == totalUsers) {
                                    // Sort by distance (closest first)
                                    Collections.sort(list, (post1, post2) -> {
                                        float[] results1 = new float[1];
                                        float[] results2 = new float[1];

                                        Location.distanceBetween(
                                                userLocation.latitude,
                                                userLocation.longitude,
                                                post1.getLatitude(),
                                                post1.getLongitude(),
                                                results1
                                        );

                                        Location.distanceBetween(
                                                userLocation.latitude,
                                                userLocation.longitude,
                                                post2.getLatitude(),
                                                post2.getLongitude(),
                                                results2
                                        );

                                        return Float.compare(results1[0], results2[0]);
                                    });

                                    localAdapter.notifyDataSetChanged();
                                    hideLoading();
                                }
                            });
                }
            });
        });
    }

    private void showLoading() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    private void hideLoading() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }
}