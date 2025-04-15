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
import com.google.android.material.slider.Slider;
import android.Manifest;
import android.util.Log;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class MapsFragment extends Fragment implements OnMapReadyCallback {

    private MapView mapView;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private Slider slider;
    private Map<String, Marker> markers = new HashMap<>();
    private Map<String, LatLng> locationData = new HashMap<>();  // Stores locations

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
        slider = view.findViewById(R.id.radiusSlider);
        slider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                        Log.d("MapsDebug", "Slider value changed to: " + value);
                updateMarkersForRadius(value);
            }
        });
    }

        @Override
        public void onMapReady(@NonNull GoogleMap googleMap){
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


//        googleMap.moveCamera(CameraUpdateFactory.newLatLng(harvard));
        }
    @SuppressLint("MissingPermission")
    private void enableUserLocation () {
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
        private void loadBookmarkedLocations () {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) return;

                Log.d("MapsDebug", "Starting to load bookmarked locations");
                clearAllMarkers();
            FirebaseFirestore.getInstance()
                    .collection("Users")
                    .document(user.getUid())
                    .collection("Bookmarks")
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        Log.d("MapsDebug", "Found " + querySnapshot.size() + " bookmarks");
                            Set<String> processedPosts = new HashSet<>(); //prevent duplicate posts
                        for (DocumentSnapshot bookmark : querySnapshot.getDocuments()) {
                            if (!bookmark.exists()) {
                                Log.e("MapsDebug", "Bookmark document doesn't exist");
                                return;
                            }
                            String postId = bookmark.getString("bookmarkedPostId");
                            String originalUserId = bookmark.getString("originalUserId");
                            if (processedPosts.contains(postId)) {
                                Log.d("MapsDebug", "Skipping duplicate postId: " + postId);
                                continue;
                            }
                            
                            processedPosts.add(postId);
                            Log.d("MapsDebug", "Fetching post data for postId: " + postId + " and originalUserId: " + originalUserId);
                            fetchPostData(originalUserId, postId);
                        }
                        Log.d("MapsDebug", "Updating markers for radius: " + slider.getValue());
                        updateMarkersForRadius(slider.getValue());
                    });
        }
        private void clearAllMarkers() {
    for (Marker marker : markers.values()) {
        marker.remove();
    }
    markers.clear();
    locationData.clear();
}
    private void fetchPostData (String originalUserId, String postId){
        Log.d("MapsDebug", "Fetching post data for postId: " + postId + " and originalUserId: " + originalUserId);
        FirebaseFirestore.getInstance().collection("Users").document(originalUserId).collection("Post Images").document(postId).get().addOnSuccessListener(postDoc -> {
            if (postDoc.exists()) {
                Log.d("MapsDebug", "Post document exists");
                String locationName = postDoc.getString("locationName");
                double latitude = postDoc.getDouble("latitude");
                double longitude = postDoc.getDouble("longitude");
                Log.d("MapsDebug", "Location data: " + locationName + " at " + latitude + "," + longitude);
                //to get user's current location
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                else {
                Log.e("MapsDebug", "Post document doesn't exist");
            }
                fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                    if (location != null) {
                        double currLatitude = location.getLatitude();
                        double currLongitude = location.getLongitude();
                        // double distance = calculateDistance(latitude, longitude, currLatitude, currLongitude);
                        //to get the radius from the slider
                        createMarkerForPost(locationName, latitude, longitude, postDoc);
                        updateMarkersForRadius(slider.getValue());
                    }
                });
                // createMarkerForPost(locationName, latitude, longitude, postDoc);
            }
        });
    }
    private void createMarkerForPost(String title,double lat, double lng, DocumentSnapshot
            postData){
        Log.d("MapsDebug", "Creating marker for: " + title);
        LatLng position = new LatLng(lat, lng);

        // Add marker to map
        Marker marker = googleMap.addMarker(new MarkerOptions()
                .position(position)
                .title(title));
        String postId = postData.getId();
        markers.put(postId, marker);
        locationData.put(postId, position);
        Log.d("MapsDebug", "Marker created and stored. Total markers: " + markers.size());
    }
    private void updateMarkersForRadius(float radius) {
            Log.d("MapsDebug", "Updating markers for radius: " + radius + ", Total markers: " + markers.size());
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                Log.d("MapsDebug", "Current location: " + currentLocation.latitude + "," + currentLocation.longitude);
                for (Map.Entry<String, Marker> entry : markers.entrySet()) {
                    String postId = entry.getKey();
                    LatLng markerLocation = entry.getValue().getPosition();
                    double distance = calculateDistance(markerLocation.latitude, markerLocation.longitude, currentLocation.latitude, currentLocation.longitude);
                    Log.d("MapsDebug", "Marker " + postId + " distance: " + distance + " km");
                    if (distance <= radius) {
                        entry.getValue().setVisible(true);
                        Log.d("MapsDebug", "Showing marker at distance: " + distance);
                    } else {
                        entry.getValue().setVisible(false);
                        Log.d("MapsDebug", "Hiding marker at distance: " + distance);
                    }
                }
            }
            else {
            Log.e("MapsDebug", "Location is null");
        }
        });
    }

        private double calculateDistance( double lat1, double lng1, double lat2, double lng2){
            double earthRadius = 6371; // Radius of the earth in kilometers
            double dLat = Math.toRadians(lat2 - lat1);
            double dLng = Math.toRadians(lng2 - lng1);
            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            return earthRadius * c;
        }


        @Override
        public void onResume () {
            super.onResume();
            if (mapView != null) {
                mapView.onResume();
            }
        }

        @Override
        public void onPause () {
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

