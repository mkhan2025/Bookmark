package com.example.bookmark.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.bookmark.R;
import com.example.bookmark.adapter.HomeAdapter;
import com.example.bookmark.adapter.SpinnerAdapter;
import com.example.bookmark.adapter.UserSearchAdapter;
import com.example.bookmark.model.HomeModel;
import com.example.bookmark.model.SpinnerModel;
import com.example.bookmark.model.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Search extends Fragment {
    private String placeId;
    private String placeName;
    private double latitude;
    private double longitude;
    private RecyclerView recyclerView;
    private RecyclerView userRecyclerView;
    private ProgressBar progressBar;
    private HomeAdapter searchAdapter;
    private SpinnerAdapter spinnerAdapter;
    private List<HomeModel> list;
    private List<UserModel> userList;
    private FrameLayout frameLayout;
    DocumentReference reference;
    FirebaseUser user;
    FirebaseAuth auth;
    private Spinner activityDropdown;
    private Map<String, String> postActivities = new HashMap<>();
    private String selectedActivity = "All";  // Add this as a class field
    private static final String TAG = "SearchFragment";
    private UserSearchAdapter userSearchAdapter;
    private TextView noResultsText;


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

        Log.d("SearchFragment", "Received place: " + placeName);
        Log.d("SearchFragment", "Latitude: " + latitude);
        Log.d("SearchFragment", "Longitude: " + longitude);
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
        Log.d("SearchFragment", "onViewCreated called");

        init(view);
        
        // Check if we have user search results
        if (getArguments() != null) {
            Log.d("SearchFragment", "Found arguments in bundle");
            if (getArguments().containsKey("userResults")) {
                Log.d("SearchFragment", "Found userResults key in arguments");
                userList = (List<UserModel>) getArguments().getSerializable("userResults");
                if (userList != null) {
                    Log.d("SearchFragment", "User list size: " + userList.size());
                    for (UserModel user : userList) {
                        Log.d("SearchFragment", "User in list: " + user.getName());
                    }
                    if (!userList.isEmpty()) {
                        Log.d("SearchFragment", "Showing user results");
                        showUserResults();
                    } else {
                        Log.d("SearchFragment", "User list is empty");
                        showNoResults();
                    }
                } else {
                    Log.d("SearchFragment", "User list is null");
                    showNoResults();
                }
            } else if (getArguments().containsKey("placeId")) {
                Log.d("SearchFragment", "Found place search parameters");
                loadSearchData();
            } else {
                Log.d("SearchFragment", "No search parameters found in arguments");
                showNoResults();
            }
        } else {
            Log.d("SearchFragment", "No arguments found");
            showNoResults();
        }
    }
    public void setUpActivityDropdown(){
                Log.d("SearchFragment", "Setting up activity dropdown");
        List<SpinnerModel> list = new ArrayList<>();
        list.add(new SpinnerModel("Outdoor"));
        list.add(new SpinnerModel("Indoor"));
        list.add(new SpinnerModel("Adventure"));
        list.add(new SpinnerModel("Eating"));
        list.add(new SpinnerModel("Tourist"));
        list.add(new SpinnerModel("All"));
        SpinnerAdapter adapter = new SpinnerAdapter(list, requireContext());
        activityDropdown.setAdapter(adapter);

        activityDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedActivity = list.get(position).getActivityType();
                                Log.d("SearchFragment", "Activity selected: " + selectedActivity);
                loadSearchData();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
                Log.d("SearchFragment", "No activity selected");
            }


        });
    }

    public void init(View view) {
        Log.d("SearchFragment", "init called");
        try {
            recyclerView = view.findViewById(R.id.recyclerView);
            userRecyclerView = view.findViewById(R.id.userRecyclerView);
            noResultsText = view.findViewById(R.id.noResultsText);
            progressBar = view.findViewById(R.id.progressBar);
            auth = FirebaseAuth.getInstance();
            user = auth.getCurrentUser();
            activityDropdown = view.findViewById(R.id.activity_dropdown);
            frameLayout = view.findViewById(R.id.frameLayout);
            
            Log.d("SearchFragment", "Views initialized: " +
                "recyclerView=" + (recyclerView != null) + ", " +
                "userRecyclerView=" + (userRecyclerView != null) + ", " +
                "noResultsText=" + (noResultsText != null) + ", " +
                "progressBar=" + (progressBar != null) + ", " +
                "activityDropdown=" + (activityDropdown != null) + ", " +
                "frameLayout=" + (frameLayout != null));
            
            list = new ArrayList<>();
            userList = new ArrayList<>();
            
            searchAdapter = new HomeAdapter(list, getActivity());
            userSearchAdapter = new UserSearchAdapter(userList, getActivity());
            
            recyclerView.setAdapter(searchAdapter);
            userRecyclerView.setAdapter(userSearchAdapter);
            
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            userRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        } catch (Exception e) {
            Log.e("SearchFragment", "Error in init: " + e.getMessage(), e);
        }
    }
    public void loadSearchData() {
        Log.d("SearchFragment", "loadSearchData called");
        showLoading();
        list.clear();

        if (user == null) {
            user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Log.e("SearchFragment", "User is null in loadSearchData");
                hideLoading();
                return;
            }
        }

        GeoPoint searchCenter = new GeoPoint(latitude, longitude);
        CollectionReference postRef = FirebaseFirestore.getInstance()
            .collection("Users")
            .document(user.getUid())
            .collection("Post Images");
        
        Log.d("SearchFragment", "Starting Firestore query for user: " + user.getUid());
        
        // Get the selected activity type correctly
        SpinnerModel selectedModel = (SpinnerModel) activityDropdown.getSelectedItem();
        String selectedActivityType = selectedModel != null ? 
            selectedModel.getActivityType() : "All";
        
        Log.d("SearchFragment", "Selected activity type: " + selectedActivityType);
        
        postRef.whereGreaterThanOrEqualTo("latitude", 0)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                Log.d("SearchFragment", "Query successful, found " + queryDocumentSnapshots.size() + " documents");
                List<HomeModel> nearbyPosts = new ArrayList<>();
                
                for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                    double postLat = snapshot.getDouble("latitude");
                    double postLong = snapshot.getDouble("longitude");
                    String postActivity = snapshot.getString("activityType");
                    
                    Log.d("SearchFragment", "Checking post at: " + postLat + ", " + postLong + 
                          " with activity: " + postActivity);
                    
                    boolean isWithinRadius = isWithinRadius(searchCenter, postLat, postLong, 100);
                    Log.d("SearchFragment", "Is within radius: " + isWithinRadius);
                    
                    boolean matchesActivity = selectedActivityType.equals("All") || 
                                            (postActivity != null && postActivity.equals(selectedActivityType));
                    Log.d("SearchFragment", "Matches activity: " + matchesActivity);
                    
                    if (isWithinRadius && matchesActivity) {
                        HomeModel model = snapshot.toObject(HomeModel.class);
                        nearbyPosts.add(model);
                        Log.d("SearchFragment", "Added post: " + model.getName());
                    }
                }
                
                Log.d("SearchFragment", "Found " + nearbyPosts.size() + " matching posts");
                
                // Update the adapter
                list.addAll(nearbyPosts);
                searchAdapter.notifyDataSetChanged();
                
                // Show/hide frameLayout based on results
                if (nearbyPosts.isEmpty()) {
                    Log.d("SearchFragment", "No posts found, showing empty state");
                    frameLayout.setVisibility(View.GONE);
                } else {
                    Log.d("SearchFragment", "Posts found, showing frameLayout");
                    frameLayout.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
                
                hideLoading();
            })
            .addOnFailureListener(e -> {
                Log.e("SearchFragment", "Error loading posts: " + e.getMessage());
                hideLoading();
            });
    }
    private boolean isWithinRadius(GeoPoint centerPoint,
                         double pointLat, double pointLng, double radiusKm) {
        double distance = calculateDistance(centerPoint, pointLat, pointLng);
        Log.d("SearchFragment", "Distance from center: " + distance + " km");
        return distance <= radiusKm;
    }

    private double calculateDistance(GeoPoint centerPoint, double pointLat, double pointLng) {
        double centerLat = centerPoint.getLatitude();
        double centerLng = centerPoint.getLongitude();
        double earthRadius = 6371; // Earth's radius in kilometers
        double dLat = Math.toRadians(pointLat - centerLat);
        double dLng = Math.toRadians(pointLng - centerLng);

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
               Math.cos(Math.toRadians(centerLat)) * Math.cos(Math.toRadians(pointLat)) *
               Math.sin(dLng/2) * Math.sin(dLng/2);
    
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return earthRadius * c;
    }

    private void showLoading() {
            Log.d("SearchFragment", "Showing loading");

        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        if (frameLayout != null) {
            frameLayout.setVisibility(View.GONE);
        }
    }

    private void hideLoading() {
                Log.d("SearchFragment", "Hiding loading");
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void showUserResults() {
        Log.d("SearchFragment", "showUserResults called");
        if (userRecyclerView != null) {
            // Hide place search UI elements
            activityDropdown.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            
            // Show user search UI elements
            userRecyclerView.setVisibility(View.VISIBLE);
            frameLayout.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            noResultsText.setVisibility(View.GONE);
            
            // Update adapter with user results
            try {
                userSearchAdapter.updateList(userList);
                Log.d("SearchFragment", "User results adapter updated with " + userList.size() + " items");
            } catch (Exception e) {
                Log.e("SearchFragment", "Error updating user search adapter: " + e.getMessage(), e);
                showNoResults();
            }
        } else {
            Log.e("SearchFragment", "userRecyclerView is null");
            showNoResults();
        }
    }

    private void showNoResults() {
        Log.d("SearchFragment", "showNoResults called");
        if (frameLayout != null && noResultsText != null) {
            frameLayout.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            noResultsText.setVisibility(View.VISIBLE);
            activityDropdown.setVisibility(View.GONE);
        } else {
            Log.e("SearchFragment", "Required views are null");
        }
    }
}