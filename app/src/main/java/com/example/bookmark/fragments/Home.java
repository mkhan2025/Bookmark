package com.example.bookmark.fragments;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.bookmark.R;
import com.example.bookmark.model.SpinnerModel;
import com.example.bookmark.adapter.SpinnerAdapter;
import com.example.bookmark.adapter.HomeAdapter;
import com.example.bookmark.model.BookmarksModel;
import com.example.bookmark.model.HomeModel;
import com.example.bookmark.model.UserModel;
import com.example.bookmark.utils.ActivityTypeMapper;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Home extends Fragment {
    private RecyclerView recyclerView;
    private FirebaseUser user;
    HomeAdapter adapter;
    private HomeModel singlePost; 
    private List<HomeModel> list;
    private List<BookmarksModel> bookmarksList;
    private ImageButton sendBtn;
    private com.google.android.material.floatingactionbutton.FloatingActionButton trendingBtn;
    private com.google.android.material.floatingactionbutton.FloatingActionButton localBtn;
    DocumentReference reference;
    private Place selectedPlace;
    AutocompleteSupportFragment autocompleteFragment;
    private Spinner activityDropdown;
    private String selectedActivityType = "All";
    private Set<String> addedPostIds;
    private FrameLayout placesSearchContainer;
    private boolean isSinglePostMode = false;

    Activity activity;
    public static int LIST_SIZE = 0;

    private Button userSearchButton;
    private FirebaseAuth auth;

    public Home() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            singlePost = (HomeModel) getArguments().getSerializable("post");
            isSinglePostMode = (singlePost != null);
            Log.d("HomeFragment", "onCreate: isSinglePostMode = " + isSinglePostMode);
        }
        addedPostIds = new HashSet<>();
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
        bookmarksList = new ArrayList<>();
        list = new ArrayList<>();
        adapter = new HomeAdapter(list, getActivity());
        recyclerView.setAdapter(adapter);
        
        if (isSinglePostMode && singlePost != null) {
            Log.d("HomeFragment", "Setting up single post view for post: " + singlePost.getId());
            // Hide UI elements we don't want to see in single post mode
            View searchContainer = view.findViewById(R.id.search_container);
            if (searchContainer != null) {
                searchContainer.setVisibility(View.GONE);
            }
            if (trendingBtn != null) {
                trendingBtn.setVisibility(View.GONE);
            }
            if (localBtn != null) {
                localBtn.setVisibility(View.GONE);
            }
            if (activityDropdown != null) {
                activityDropdown.setVisibility(View.GONE);
            }
            
            
            list.clear(); 
            list.add(singlePost);
            adapter.notifyDataSetChanged();
            Log.d("HomeFragment", "Single post added to list, size: " + list.size());
            loadBookmarksFromFirestore();
            clickListener();
        } else {
            Log.d("HomeFragment", "Loading full feed");
            loadDataFromFirestore();
            loadBookmarksFromFirestore();
            clickListener();
        }

        // Initialize Places API
        String apiKey = "AIzaSyA7chOcKSTr-xNmL6bwz_Txw5LQABIzNC4";
        try {
            Places.initializeWithNewPlacesApiEnabled(getContext(), apiKey);
            PlacesClient placesClient = Places.createClient(getContext());
            
            if (placesClient == null) {
                Log.e("PlacesAPI", "PlacesClient is null");
                return;
            }
            
            autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);
                
            autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
            autocompleteFragment.setHint("Search places");
            
            placesSearchContainer = view.findViewById(R.id.places_search_container);
            
            if (placesSearchContainer != null) {
                autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                    @Override
                    public void onPlaceSelected(@NonNull Place place) {
                        selectedPlace = place;
                        Search search = new Search();
                        Bundle bundle = new Bundle();
                        bundle.putString("placeId", selectedPlace.getId());
                        bundle.putString("placeName", selectedPlace.getName().toString());
                        bundle.putDouble("latitude", selectedPlace.getLatLng().latitude);
                        bundle.putDouble("longitude", selectedPlace.getLatLng().longitude);
                        search.setArguments(bundle);

                        View frameLayout = getActivity().findViewById(R.id.mainFrameLayout);
                        if (frameLayout != null) {
                            frameLayout.setVisibility(View.VISIBLE);
                        }

                        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.mainFrameLayout, search);
                        transaction.addToBackStack(null).commit();
                    }

                    @Override
                    public void onError(@NonNull Status status) {
                        Log.e("PlacesAPI", "Error selecting place: " + status.getStatusMessage());
                        Toast.makeText(getContext(), "Error selecting place: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

        } catch (Exception e) {
            Log.e("PlacesAPI", "Error setting up AutocompleteFragment: " + e.getMessage());
            e.printStackTrace();
        }
    }

        private void clickListener () {
            //adding click listener so that if a userName is clicked, the user will be redirected to the user's profile

            localBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Log.d("LocalDebug", "local btn clicked");

                        if (getActivity() == null) {
                            Log.e("LocalDebug", "Activity is null");
                            return;
                        }


                        View frameLayout = getActivity().findViewById(R.id.mainFrameLayout);
                        if (frameLayout != null) {
                            frameLayout.setVisibility(View.VISIBLE);
                        }

                        FragmentTransaction transaction = getActivity()
                                .getSupportFragmentManager()
                                .beginTransaction();

                        transaction.replace(R.id.mainFrameLayout, new LocalFragment())
                                .addToBackStack(null)
                                .commit();

                        Log.d("LocalDebug", "Transaction committed");

                    } catch (Exception e) {
                        Log.e("LocalDebug", "Error in transaction", e);
                        e.printStackTrace();
                    }
                }
            });
            trendingBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Log.d("TrendingDebug", "trending btn clicked");

                        if (getActivity() == null) {
                            Log.e("TrendingDebug", "Activity is null");
                            return;
                        }

                        // Get the ViewPager's current item view
                        // ViewGroup container = (ViewGroup) getView().getParent();
                        // View viewPager = getActivity().findViewById(R.id.viewPager);
                        // View frameLayout = getActivity().findViewById(R.id.mainFrameLayout);

                        // if (viewPager != null && frameLayout != null) {
                        //     viewPager.setVisibility(View.GONE);
                        //     frameLayout.setVisibility(View.VISIBLE);
                        // }

                        View frameLayout = getActivity().findViewById(R.id.mainFrameLayout);
                        if (frameLayout != null) {
                            frameLayout.setVisibility(View.VISIBLE);
                        }

                        FragmentTransaction transaction = getActivity()
                                .getSupportFragmentManager()
                                .beginTransaction();

                        transaction.replace(R.id.mainFrameLayout, new TrendingFragment())
                                .addToBackStack(null)
                                .commit();

                        Log.d("TrendingDebug", "Transaction committed");

                    } catch (Exception e) {
                        Log.e("TrendingDebug", "Error in transaction", e);
                        e.printStackTrace();
                    }
                }
            });
        }


        private void init (View view){
            Log.d("HomeFragment", "init called");
            recyclerView = view.findViewById(R.id.recyclerView);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            user = FirebaseAuth.getInstance().getCurrentUser();
            trendingBtn = view.findViewById(R.id.trendingButton);
            localBtn = view.findViewById(R.id.localButton);
            reference = FirebaseFirestore.getInstance().collection("Users").document(user.getUid());
            auth = FirebaseAuth.getInstance();
            list = new ArrayList<>();
            adapter = new HomeAdapter(list, getActivity());
            recyclerView.setAdapter(adapter);

            // Initialize activity dropdown
            activityDropdown = view.findViewById(R.id.activity_dropdown);
            setupActivityDropdown();

            // Initialize search containers
            FrameLayout placesSearchContainer = view.findViewById(R.id.places_search_container);
            TextInputLayout userSearchContainer = view.findViewById(R.id.user_search_container);
            EditText userSearchInput = view.findViewById(R.id.user_search_input);
            SwitchMaterial searchTypeSwitch = view.findViewById(R.id.searchTypeSwitch);

            Log.d("HomeFragment", "Search containers initialized: " +
                "placesSearchContainer=" + (placesSearchContainer != null) + ", " +
                "userSearchContainer=" + (userSearchContainer != null) + ", " +
                "userSearchInput=" + (userSearchInput != null) + ", " +
                "searchTypeSwitch=" + (searchTypeSwitch != null));

            // Set up search type switch listener
            searchTypeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                Log.d("HomeFragment", "Search type switch changed: isChecked=" + isChecked);
                if (isChecked) {
                    // User search
                    placesSearchContainer.setVisibility(View.GONE);
                    userSearchContainer.setVisibility(View.VISIBLE);
                    userSearchInput.requestFocus();
                    Log.d("HomeFragment", "Switched to user search mode");
                } else {
                    // Place search
                    placesSearchContainer.setVisibility(View.VISIBLE);
                    userSearchContainer.setVisibility(View.GONE);
                    Log.d("HomeFragment", "Switched to place search mode");
                }
            });

            // Set up user search input listener
            userSearchInput.setOnEditorActionListener((v, actionId, event) -> {
                Log.d("HomeFragment", "Editor action triggered: actionId=" + actionId);
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                    String username = v.getText().toString().trim();
                    Log.d("HomeFragment", "Search action with username: " + username);
                    if (!username.isEmpty()) {
                        if (username.startsWith("@")) {
                            username = username.substring(1);
                        }
                        Log.d("HomeFragment", "Processing search for username: " + username);
                        searchUsers(username);
                    } else {
                        Log.d("HomeFragment", "Empty username, ignoring search");
                    }
                    return true;
                }
                return false;
            });

            // Add a search button to the input layout
            userSearchContainer.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
            userSearchContainer.setEndIconDrawable(R.drawable.ic_search);
            userSearchContainer.setEndIconOnClickListener(v -> {
                String username = userSearchInput.getText().toString().trim();
                Log.d("HomeFragment", "Search icon clicked with username: " + username);
                if (!username.isEmpty()) {
                    if (username.startsWith("@")) {
                        username = username.substring(1);
                    }
                    searchUsers(username);
                }
            });

            // Add text change listener for debugging
            // userSearchInput.addTextChangedListener(new TextWatcher() {
            //     @Override
            //     public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            //         Log.d("HomeFragment", "Before text changed: " + s);
            //     }

            //     @Override
            //     public void onTextChanged(CharSequence s, int start, int before, int count) {
            //         Log.d("HomeFragment", "Text changed to: " + s);
            //     }

            //     @Override
            //     public void afterTextChanged(Editable s) {
            //         Log.d("HomeFragment", "After text changed: " + s);
            //     }
            // });
        }

        private void setupActivityDropdown() {
            List<SpinnerModel> activityTypes = new ArrayList<>();
            activityTypes.add(new SpinnerModel("All"));  // Add "All" first
            activityTypes.add(new SpinnerModel("Nature & Adventure"));
            activityTypes.add(new SpinnerModel("Cultural & Historical"));
            activityTypes.add(new SpinnerModel("Food & Drink"));
            activityTypes.add(new SpinnerModel("Events & Entertainment"));
            activityTypes.add(new SpinnerModel("Relaxation & Wellness"));
            activityTypes.add(new SpinnerModel("Shopping"));
            activityTypes.add(new SpinnerModel("Indoor"));
            activityTypes.add(new SpinnerModel("Outdoor"));
            activityTypes.add(new SpinnerModel("Transit"));

            SpinnerAdapter adapter = new SpinnerAdapter(activityTypes, requireContext());
            activityDropdown.setAdapter(adapter);
            
            // Only set up the listener if we're not in single post mode
            if (!isSinglePostMode) {
                activityDropdown.setSelection(0);  // Set "All" as default selection
                activityDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        SpinnerModel selectedModel = (SpinnerModel) parent.getItemAtPosition(position);
                        selectedActivityType = selectedModel.getActivityType();
                        Log.d("HomeFragment", "Selected activity type: " + selectedActivityType);
                        loadDataFromFirestore();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        selectedActivityType = "All";
                        loadDataFromFirestore();
                    }
                });
            } else {
                // In single post mode, disable the spinner
                activityDropdown.setEnabled(false);
                activityDropdown.setVisibility(View.GONE);
               
                Log.d("HomeFragment", "Activity dropdown disabled in single post mode");
            }
        }

        private void searchUsers(String username) {
            Log.d("HomeFragment", "Starting user search with username: " + username);
            if (username.isEmpty()) {
                Log.d("HomeFragment", "Username is empty, aborting search");
                return;
            }

            Log.d("HomeFragment", "Executing Firestore query for username: " + username);
            FirebaseFirestore.getInstance()
                .collection("Users")
                .whereGreaterThanOrEqualTo("name", username)
                .whereLessThanOrEqualTo("name", username + "\uf8ff")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d("HomeFragment", "User search successful, found " + queryDocumentSnapshots.size() + " results");
                    List<UserModel> userList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            UserModel user = document.toObject(UserModel.class);
                            userList.add(user);
                            Log.d("HomeFragment", "Added user to results: " + user.getName());
                        } catch (Exception e) {
                            Log.e("HomeFragment", "Error converting document to UserModel: " + e.getMessage(), e);
                        }
                    }
                    
                    if (userList.isEmpty()) {
                        Log.d("HomeFragment", "No users found");
                        Toast.makeText(getActivity(), "No users found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Log.d("HomeFragment", "Preparing to show search results");
                    try {
                        // Create and show the Search fragment
                        Search searchFragment = new Search();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("userResults", (Serializable) userList);
                        searchFragment.setArguments(bundle);

                        // Make sure mainFrameLayout is visible
                        View frameLayout = getActivity().findViewById(R.id.mainFrameLayout);
                        if (frameLayout != null) {
                            frameLayout.setVisibility(View.VISIBLE);
                            Log.d("HomeFragment", "Set mainFrameLayout visibility to VISIBLE");
                        }

                        // Replace the current fragment with Search fragment
                        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.mainFrameLayout, searchFragment);
                        transaction.addToBackStack(null);
                        transaction.commit();
                        Log.d("HomeFragment", "Search fragment transaction committed");

                    } catch (Exception e) {
                        Log.e("HomeFragment", "Error showing search results: " + e.getMessage(), e);
                        Toast.makeText(getActivity(), "Error showing results", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("HomeFragment", "Error searching users: " + e.getMessage(), e);
                    Toast.makeText(getActivity(), "Error searching users", Toast.LENGTH_SHORT).show();
                });
        }

        private void loadDataFromFirestore() {
            Log.d("HomeFragment", "loadDataFromFirestore called");
            
            if (user == null) {
                Log.e("FirestoreError", "User is null");
                return;
            }

            list.clear(); // Clear list before adding new data
            addedPostIds.clear(); // Clear the Set of added post IDs
            Log.d("HomeFragment", "Starting to load posts");
            Log.d("HomeFragment", "Current user ID: " + user.getUid());
            Log.d("HomeFragment", "Selected activity type: " + selectedActivityType);

            // First get the current user's following list
            FirebaseFirestore.getInstance()
                .collection("Users")
                .document(user.getUid())
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (error != null) {
                        Log.e("FirestoreError", "Error getting following list: " + error.getMessage());
                        return;
                    }

                    if (documentSnapshot == null || !documentSnapshot.exists()) {
                        Log.d("HomeFragment", "User document doesn't exist");
                        return;
                    }

                    if (documentSnapshot.exists()) {
                        Object followingObj = documentSnapshot.get("following");
                        Log.d("FirestoreDebug", "Following field type: " + (followingObj != null ? followingObj.getClass().getName() : "null"));
                        Log.d("FirestoreDebug", "Following field value: " + followingObj);
                        
                        List<String> following = new ArrayList<>();
                        if (followingObj instanceof List) {
                            following = (List<String>) followingObj;
                            // Clean up empty strings
                            following.removeIf(String::isEmpty);
                            Log.d("HomeFragment", "Cleaned following list: " + following);
                        }

                        if (following.isEmpty()) {
                            Log.d("HomeFragment", "No users being followed");
                            adapter.notifyDataSetChanged();
                            return;
                        }

                        Log.d("HomeFragment", "Processing " + following.size() + " followed users");
                        // For each user being followed, get their posts
                        for (String followedUserId : following) {
                            if (followedUserId == null || followedUserId.isEmpty()) {
                                Log.d("HomeFragment", "Skipping null or empty user ID");
                                continue;
                            }
                            
                            // Skip if this is the current user's ID
                            if (followedUserId.equals(user.getUid())) {
                                Log.d("HomeFragment", "Skipping current user's posts");
                                continue;
                            }
                            
                            Log.d("HomeFragment", "Loading posts for user: " + followedUserId);
                            FirebaseFirestore.getInstance()
                                .collection("Users")
                                .document(followedUserId)
                                .collection("Post Images")
                                .orderBy("timestamp", Query.Direction.DESCENDING)
                                .limit(20)
                                .addSnapshotListener((value, error1) -> {
                                    if (error1 != null) {
                                        Log.e("FirestoreError", "Error loading posts for user " + followedUserId + ": " + error1.getMessage());
                                        return;
                                    }

                                    if (value == null || value.isEmpty()) {
                                        Log.d("HomeFragment", "No posts found for user: " + followedUserId);
                                        return;
                                    }

                                    Log.d("HomeFragment", "Found " + value.size() + " posts for user: " + followedUserId);
                                    Log.d("HomeFragment", "Current list size: " + list.size());
                                    Log.d("HomeFragment", "Current addedPostIds size: " + addedPostIds.size());
                                    
                                    for (QueryDocumentSnapshot snapshot : value) {
                                        if (!snapshot.exists()) {
                                            continue;
                                        }
                                        HomeModel model = snapshot.toObject(HomeModel.class);
                                        String postId = model.getId();
                                        
                                        Log.d("HomeFragment", "Processing post: " + postId + " from user: " + followedUserId);
                                        
                                        // Skip if we've already added this post
                                        if (addedPostIds.contains(postId)) {
                                            Log.d("HomeFragment", "Skipping duplicate post: " + postId);
                                            continue;
                                        }
                                        
                                        String postActivityType = model.getActivityType();
                                        String mappedActivityType = ActivityTypeMapper.mapOldToNewActivityType(postActivityType);
                                        Log.d("HomeFragment", "Post activity type: " + postActivityType + ", Mapped to: " + mappedActivityType);
                                        
                                        // Only add posts that match the selected activity type or if "All" is selected
                                        if (selectedActivityType.equals("All") || 
                                            (mappedActivityType != null && mappedActivityType.equals(selectedActivityType))) {
                                            list.add(model);
                                            addedPostIds.add(postId);
                                            Log.d("HomeFragment", "Added post: " + postId + " from user: " + followedUserId);
                                            Log.d("HomeFragment", "New list size: " + list.size());
                                            Log.d("HomeFragment", "New addedPostIds size: " + addedPostIds.size());
                                        } else {
                                            Log.d("HomeFragment", "Skipping post due to activity type mismatch: " + postId);
                                        }
                                    }

                                    // Sort all posts by timestamp
                                    Collections.sort(list, (o1, o2) -> {
                                        return Long.compare(o2.getTimestamp(), o1.getTimestamp());
                                    });

                                    LIST_SIZE = list.size();
                                    Log.d("HomeFragment", "Final list size: " + LIST_SIZE);
                                    Log.d("HomeFragment", "Final addedPostIds size: " + addedPostIds.size());
                                    adapter.notifyDataSetChanged();
                                });
                        }
                    }
                });
        }
        private void loadBookmarksFromFirestore () {
            if (user == null) {
                Log.e("FirestoreError", "User is null");
                return;
            }
            bookmarksList.clear();
            CollectionReference collectionReference = FirebaseFirestore.getInstance()
                    .collection("Users")
                    .document(user.getUid())
                    .collection("Bookmarks");
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
                    BookmarksModel model = snapshot.toObject(BookmarksModel.class);
                    Log.d("BookmarksFragment", "Parsed bookmark - OriginalUserId: " + model.getOriginalUserId()
                            + ", BookmarkedPostId: " + model.getBookmarkedPostId()
                            + ", Timestamp: " + model.getTimestamp());
                    if (model.getBookmarkedPostId() == null) {
                        continue;
                    }
                    bookmarksList.add(new BookmarksModel(model.getOriginalUserId(), model.getBookmarkedPostId(), model.getTimestamp()));
                }
            });

        }

    private EditText findEditText(View view) {
        if (view instanceof EditText) {
            return (EditText) view;
        }
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                EditText editText = findEditText(child);
                if (editText != null) {
                    return editText;
                }
            }
        }
        return null;
    }

}

