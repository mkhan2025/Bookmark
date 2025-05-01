package com.example.bookmark.fragments;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.bookmark.R;
import com.example.bookmark.ReplacerActivity;
import com.example.bookmark.adapter.HomeAdapter;
import com.example.bookmark.model.BookmarksModel;
import com.example.bookmark.model.HomeModel;
import com.example.bookmark.model.UserModel;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import androidx.navigation.Navigation;
import androidx.navigation.NavController;
import com.example.bookmark.MainActivity;

public class Home extends Fragment {
    private RecyclerView recyclerView;
    private FirebaseUser user;
    HomeAdapter adapter;
    private List<HomeModel> list;
    private List<BookmarksModel> bookmarksList;
    private ImageButton sendBtn;
    private Button trendingBtn;
    private Button localBtn;
    DocumentReference reference;
    private Place selectedPlace;
    AutocompleteSupportFragment autocompleteFragment;

    Activity activity;
    public static int LIST_SIZE = 0;

    private Button userSearchButton;
    private FirebaseAuth auth;

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
        bookmarksList = new ArrayList<>();
        list = new ArrayList<>();
        adapter = new HomeAdapter(list, getActivity());
        recyclerView.setAdapter(adapter);
        loadDataFromFirestore();
        loadBookmarksFromFirestore();
        clickListener();

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
                
            // Set up Places API
            autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
            
            // Get the search container
            FrameLayout placesSearchContainer = view.findViewById(R.id.places_search_container);
            
            if (placesSearchContainer != null) {
                // Set up the Places Autocomplete listener
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
            localBtn = view.findViewById(R.id.localButton);
            trendingBtn = view.findViewById(R.id.trendingButton);
            auth = FirebaseAuth.getInstance();
            user = auth.getCurrentUser();
            list = new ArrayList<>();
            adapter = new HomeAdapter(list, getActivity());
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

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
            userSearchInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    Log.d("HomeFragment", "Before text changed: " + s);
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    Log.d("HomeFragment", "Text changed to: " + s);
                }

                @Override
                public void afterTextChanged(Editable s) {
                    Log.d("HomeFragment", "After text changed: " + s);
                }
            });
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
            if (user == null) {
                Log.e("FirestoreError", "User is null");
                return;
            }

            list.clear(); // Clear list before adding new data
            Log.d("HomeFragment", "Starting to load posts");
            Log.d("HomeFragment", "Current user ID: " + user.getUid());

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

                        // For each user being followed, get their posts
                        for (String followedUserId : following) {
                            if (followedUserId == null || followedUserId.isEmpty()) {
                                Log.d("HomeFragment", "Skipping null or empty user ID");
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
                                    for (QueryDocumentSnapshot snapshot : value) {
                                        if (!snapshot.exists()) {
                                            continue;
                                        }
                                        HomeModel model = snapshot.toObject(HomeModel.class);
                                        // Make sure we don't add duplicate posts
                                        if (!list.contains(model)) {
                                            list.add(model);
                                            Log.d("HomeFragment", "Added post: " + model.getId() + " from user: " + followedUserId);
                                        }
                                    }

                                    // Sort all posts by timestamp
                                    Collections.sort(list, (o1, o2) -> {
                                        // Since timestamp is a long primitive, we don't need null check
                                        return Long.compare(o2.getTimestamp(), o1.getTimestamp());
                                    });

                                    LIST_SIZE = list.size();
                                    Log.d("HomeFragment", "Total posts in feed: " + LIST_SIZE);
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

