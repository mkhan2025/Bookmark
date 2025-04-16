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

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.bookmark.R;
import com.example.bookmark.ReplacerActivity;
import com.example.bookmark.adapter.HomeAdapter;
import com.example.bookmark.model.BookmarksModel;
import com.example.bookmark.model.HomeModel;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


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
        bookmarksList = new ArrayList<>();
        list = new ArrayList<>();
        adapter = new HomeAdapter(list, getActivity());
        recyclerView.setAdapter(adapter);
        loadDataFromFirestore();
        loadBookmarksFromFirestore();
        clickListener();
        String apiKey = "AIzaSyA7chOcKSTr-xNmL6bwz_Txw5LQABIzNC4";
        autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        if (autocompleteFragment != null) {
            try {
                Places.initializeWithNewPlacesApiEnabled(getContext(), apiKey);
                PlacesClient placesClient = Places.createClient(getContext());

                if (placesClient == null) {
                    Log.e("PlacesAPI", "PlacesClient is null from MainActivity");
                    return;
                }
                Log.d("PlacesAPI", "Got PlacesClient instance from MainActivity");
                autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
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
        // Handle any errors that occur during place selection
                     Log.e("PlacesAPI", "Error selecting place: " + status.getStatusMessage());
                    Toast.makeText(getContext(), "Error selecting place: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                Log.e("PlacesAPI", "Error setting up AutocompleteFragment: " + e.getMessage());
                e.printStackTrace();
            }
            //
        }
    }

        private void clickListener () {
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
//        Toolbar toolbar = view.findViewById(R.id.toolbar);
//        if (getActivity() != null)
//        {
//            ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
//        }
            recyclerView = view.findViewById(R.id.recyclerView);
            if (recyclerView == null) {
                Log.e("TrendingDebug", "RecyclerView is null");
            }
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            trendingBtn = view.findViewById(R.id.trendingButton);
            localBtn = view.findViewById(R.id.localButton);
            if (trendingBtn == null) {
                Log.e("TrendingDebug", "Trending button is null");
            } else {
                Log.d("TrendingDebug", "Trending button initialized");
            }
            FirebaseAuth auth = FirebaseAuth.getInstance();
            user = auth.getCurrentUser();

        }
        private void loadDataFromFirestore () {
            if (user == null) {
                Log.e("FirestoreError", "User is null");
                return;
            }

            list.clear(); // Clear list before adding new data
            Log.d("HomeFragment", "Starting to load posts");
            Log.d("HomeFragment", "Current user ID: " + user.getUid());
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
                    if (!snapshot.exists()) {
                        return;
                    }
                    HomeModel model = snapshot.toObject(HomeModel.class);
                    Log.d("HomeFragment", "Post name: " + model.getName());
                    list.add(new HomeModel(model.getUid(), model.getProfileImage(), model.getImageUrl(), model.getName(), model.getComment(), model.getDescription(), model.getId(), model.getLocationName(), model.getActivityType(), model.getLocalPostImage(), model.getLikeCount(), model.getLikedBy(), model.getLatitude(), model.getLongitude(), model.getTimestamp(), model.getTrendingScore()));
                }
                LIST_SIZE = list.size();
                adapter.notifyDataSetChanged();
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


}

