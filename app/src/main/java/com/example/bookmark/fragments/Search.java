package com.example.bookmark.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.example.bookmark.R;
import com.example.bookmark.adapter.HomeAdapter;
import com.example.bookmark.model.HomeModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;


public class Search extends Fragment {
    private String placeId;
    private String placeName;
    private double latitude;
    private double longitude;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private HomeAdapter searchAdapter;
    private List<HomeModel> list;
    DocumentReference reference;
    FirebaseUser user;
    FirebaseAuth auth;


    public Search() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            placeId = getArguments().getString("placeId");
            placeName = getArguments().getString("placeName");
            latitude = getArguments().getDouble("latitude");
            longitude = getArguments().getDouble("longitude");
        }
    }
    

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
        list = new ArrayList<>();
        searchAdapter = new HomeAdapter(list, getActivity());
        recyclerView.setAdapter(searchAdapter);
        showLoading();
        loadSearchData();
    }

    public void init(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
         auth = FirebaseAuth.getInstance();
         user = auth.getCurrentUser();
        progressBar = view.findViewById(R.id.progressBar);
    }
    public void loadSearchData(){
        showLoading();
        list.clear();
        FirebaseUser user = auth.getCurrentUser();

        GeoPoint searchCenter = new GeoPoint(latitude, longitude);
        CollectionReference postRef= FirebaseFirestore.getInstance().collection("Users").document(user.getUid()).collection("Post Images");
        postRef.whereGreaterThanOrEqualTo("latitude", 0).get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<HomeModel> nearbyPosts = new ArrayList<>();
            for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots){
                double postLat = snapshot.getDouble("latitude");
                double postLong = snapshot.getDouble("longitude");

                if (isWithinRadius(searchCenter, postLat, postLong, 50)){
                    HomeModel model = snapshot.toObject(HomeModel.class);
                    nearbyPosts.add(model);
                }
            }
            searchAdapter = new HomeAdapter(nearbyPosts, getActivity());
            recyclerView.setAdapter(searchAdapter);
            hideLoading();
        });
        }
        private boolean isWithinRadius(GeoPoint centerPoint,
                             double pointLat, double pointLng, double radiusKm) {
        double centerLat = centerPoint.getLatitude();
        double centerLng = centerPoint.getLongitude();
        double earthRadius = 6371; // Earth's radius in kilometers
        double dLat = Math.toRadians(pointLat - centerLat);
        double dLng = Math.toRadians(pointLng - centerLng);

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
               Math.cos(Math.toRadians(centerLat)) * Math.cos(Math.toRadians(pointLat)) *
               Math.sin(dLng/2) * Math.sin(dLng/2);
    
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double distance = earthRadius * c;
    
        return distance <= radiusKm;
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