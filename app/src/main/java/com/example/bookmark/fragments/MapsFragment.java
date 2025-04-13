package com.example.bookmark.fragments;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import com.example.bookmark.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.maps.model.Marker;

import android.Manifest;


public class MapsFragment extends Fragment implements OnMapReadyCallback {

    private MapView mapView;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;

    public MapsFragment() {
        super(R.layout.fragment_maps);
    }

    @Override
    public void onViewCreated(@NonNull android.view.View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;

           Dexter.withContext(requireContext())
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        enableUserLocation();
                        loadBookmarkedLocations(); 
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        // Handle permission denial (e.g., show a message)
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();


        LatLng compHis = new LatLng(37.41445332379147, -122.07739827458252);
        googleMap.addMarker(new MarkerOptions()
                .position(compHis)
                .title("Computer History Museum"));

//        googleMap.moveCamera(CameraUpdateFactory.newLatLng(harvard));
    }
    private void loadBookmarkedLocations(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance()
        .collection("Users")
        .document(user.getUid())
        .collection("Bookmarks")
        .get()
        .addOnSuccessListener(querySnapshot -> {
            for (DocumentSnapshot bookmark : querySnapshot.getDocuments()){
                if (!bookmark.exists()){
                    return;
                }
                String postId = bookmark.getString("bookmarkedPostId");
                String originalUserId = bookmark.getString("originalUserId");
                fetchPostData(originalUserId, postId);
            }
        });
    }
    private void fetchPostData(String originalUserId, String postId){
        FirebaseFirestore.getInstance().collection("Users").document(originalUserId).collection("Post Images").document(postId).get().addOnSuccessListener(postDoc -> {
            if (postDoc.exists()){
                String locationName = postDoc.getString("locationName");
                double latitude = postDoc.getDouble("latitude");
                double longitude = postDoc.getDouble("longitude");
                createMarkerForPost(locationName, latitude, longitude, postDoc);
            }
        });
    }
    // Pseudo-code
private void createMarkerForPost(String title, double lat, double lng, DocumentSnapshot postData) {
    LatLng position = new LatLng(lat, lng);
    
    // Add marker to map
    Marker marker = googleMap.addMarker(new MarkerOptions()
        .position(position)
        .title(title));
}
    @SuppressLint("MissingPermission")
    private void enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        
        googleMap.setMyLocationEnabled(true);
        
        // Get user's last known location
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                googleMap.addMarker(new MarkerOptions().position(userLatLng).title("You are here"));
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f));
            }
        });
    }
    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            mapView.onDestroy();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) {
            mapView.onLowMemory();
        }
    }
}
