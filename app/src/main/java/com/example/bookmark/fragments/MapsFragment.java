package com.example.bookmark.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
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
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.common.api.ResolvableApiException;
import android.content.IntentSender;
import android.app.Activity;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import com.example.bookmark.model.SpinnerModel;
import com.example.bookmark.adapter.SpinnerAdapter;
import com.example.bookmark.utils.ActivityTypeMapper;



public class MapsFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "MapsFragment";
    private MapView mapView;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private Slider slider;
    private Spinner spinner;
    private Map<String, Marker> markers = new HashMap<>();
    private Map<String, LatLng> locationData = new HashMap<>();  // Stores locations
    // private Set<String> selectedActivities = new HashSet<>();
    private Map<String, String> postActivities = new HashMap<>();  // postId -> activityType
    private static final int REQUEST_CHECK_SETTINGS = 100;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;

    public MapsFragment() {
        super(R.layout.fragment_maps);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "MapsFragment onCreate called");
    }

    @Override
    public void onViewCreated(@NonNull android.view.View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "MapsFragment onViewCreated called");
        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        Log.d(TAG, "MapView initialized and getMapAsync called");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        slider = view.findViewById(R.id.radiusSlider);
        spinner = view.findViewById(R.id.activity_dropdown);
        slider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                        Log.d("MapsDebug", "Slider value changed to: " + value);
                SpinnerModel selectedModel = (SpinnerModel) spinner.getSelectedItem();
                String selectedActivity = selectedModel.getActivityType();
                updateMarkers(value, selectedActivity);
            }
        });
        List<SpinnerModel> list = new ArrayList<>();
        list.add(new SpinnerModel("Nature & Adventure"));
        list.add(new SpinnerModel("Cultural & Historical"));
        list.add(new SpinnerModel("Food & Drink"));
        list.add(new SpinnerModel("Events & Entertainment"));
        list.add(new SpinnerModel("Relaxation & Wellness"));
        list.add(new SpinnerModel("Shopping"));
        list.add(new SpinnerModel("Indoor"));
        list.add(new SpinnerModel("Outdoor"));
        list.add(new SpinnerModel("Transit"));
        list.add(new SpinnerModel("All"));
        SpinnerAdapter adapter = new SpinnerAdapter(list, requireContext());
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedActivity = list.get(position).getActivityType();
         
            updateMarkers(slider.getValue(), selectedActivity);
        
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d("MapsDebug", "Nothing selected");
                updateMarkers(slider.getValue(), "All");
    }
    }); 
    }


        @Override
        public void onMapReady(@NonNull GoogleMap googleMap){
            Log.d(TAG, "onMapReady called");
            this.googleMap = googleMap;

            Dexter.withContext(requireContext())
                    .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    .withListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse response) {
                            Log.d(TAG, "Location permission granted");
                            checkLocationSettings();
                        }

                        @Override
                        public void onPermissionDenied(PermissionDeniedResponse response) {
                            Log.e(TAG, "Location permission denied");
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                            Log.d(TAG, "Showing permission rationale");
                            token.continuePermissionRequest();
                        }
                    }).check();


//        googleMap.moveCamera(CameraUpdateFactory.newLatLng(harvard));
        }
    @SuppressLint("MissingPermission")
    private void checkLocationSettings() {
        Log.d(TAG, "checkLocationSettings called");
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000); // Reduced interval for faster updates
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10); // Update every 10 meters

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setAlwaysShow(true); // Show dialog even if location is already enabled

        SettingsClient client = LocationServices.getSettingsClient(requireActivity());
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(requireActivity(), locationSettingsResponse -> {
            Log.d(TAG, "Location settings are satisfied");
            enableUserLocation();
            loadBookmarkedLocations();
        });

        task.addOnFailureListener(requireActivity(), e -> {
            Log.e(TAG, "Location settings are not satisfied: " + e.getMessage());
            if (e instanceof ResolvableApiException) {
                try {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(requireActivity(),
                            REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException sendEx) {
                    Log.e(TAG, "Error showing location settings dialog", sendEx);
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "User enabled location settings");
                enableUserLocation();
                loadBookmarkedLocations();
            } else {
                Log.e(TAG, "User did not enable location settings");
                // You might want to show a message to the user here
                Toast.makeText(requireContext(), "Location services are required for this feature", Toast.LENGTH_LONG).show();
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void enableUserLocation() {
        Log.d(TAG, "enableUserLocation called");
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Location permission not granted");
            return;
        }

        try {
            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
            Log.d(TAG, "Location enabled on map");

            // First try to get last known location
            fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        Log.d(TAG, "Got last known location: " + location.getLatitude() + ", " + location.getLongitude());
                        LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f));
                    } else {
                        Log.d(TAG, "Last known location is null, waiting for updates");
                    }
                });

            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null) {
                        Log.e(TAG, "Location result is null");
                        return;
                    }
                    for (Location location : locationResult.getLocations()) {
                        LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        Log.d(TAG, "Location updated: " + userLatLng.latitude + ", " + userLatLng.longitude + 
                              " Accuracy: " + location.getAccuracy() + " Provider: " + location.getProvider());
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f));
                    }
                }
            };

            Log.d(TAG, "Requesting location updates");
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Successfully requested location updates"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to request location updates: " + e.getMessage()));
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException in enableUserLocation: " + e.getMessage());
        }
    }
        private void loadBookmarkedLocations () {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) return;

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
                    updateMarkers(slider.getValue(), spinner.getSelectedItem().toString());
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
                String activityType = postDoc.getString("activityType");
                Log.d("MapsDebug", "Location data: " + locationName + " at " + latitude + "," + longitude);
                //to get user's current location
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                
                fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                    if (location != null) {
                        createMarkerForPost(locationName, latitude, longitude, activityType, postDoc);
                        updateMarkers(slider.getValue(), spinner.getSelectedItem().toString());
                    }
                });
                // createMarkerForPost(locationName, latitude, longitude, postDoc);
            }
        });
    }
    private void createMarkerForPost(String title,double lat, double lng, String activityType, DocumentSnapshot
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
        postActivities.put(postId, activityType);
        Log.d("MapsDebug", "Marker created and stored. Total markers: " + markers.size());
    }
    private void updateMarkers(float radius, String selectedActivity) {
        Log.d("MapsDebug", "Updating markers for radius: " + radius + ", Total markers: " + markers.size());
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                    String markerActivity = postActivities.get(postId);
                    String mappedMarkerActivity = ActivityTypeMapper.mapOldToNewActivityType(markerActivity);
                    boolean isWithinRadius = distance <= radius;
                    boolean matchesActivity = selectedActivity.equals("All") || 
                                           (mappedMarkerActivity != null && mappedMarkerActivity.equals(selectedActivity));
                    
                    Log.d("MapsDebug", String.format(
                        "Marker %s: distance=%.2f, activity=%s, mapped=%s, selected=%s, visible=%b",
                        postId, distance, markerActivity, mappedMarkerActivity, selectedActivity, (isWithinRadius && matchesActivity)
                    ));
                    
                    entry.getValue().setVisible(isWithinRadius && matchesActivity);
                }
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
            Log.d(TAG, "onResume called");
            if (mapView != null) {
                mapView.onResume();
            }
            
            // Check location settings again when fragment resumes
            if (googleMap != null) {
                Dexter.withContext(requireContext())
                    .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    .withListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse response) {
                            Log.d(TAG, "Location permission granted in onResume");
                            checkLocationSettings();
                        }

                        @Override
                        public void onPermissionDenied(PermissionDeniedResponse response) {
                            Log.e(TAG, "Location permission denied in onResume");
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                            token.continuePermissionRequest();
                        }
                    }).check();
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
            if (fusedLocationClient != null && locationCallback != null) {
                fusedLocationClient.removeLocationUpdates(locationCallback);
            }
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

