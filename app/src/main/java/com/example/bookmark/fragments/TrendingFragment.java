package com.example.bookmark.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookmark.R;
import com.example.bookmark.adapter.HomeAdapter;
import com.example.bookmark.model.HomeModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import com.example.bookmark.MainActivity;

public class TrendingFragment extends Fragment {
    
    private RecyclerView recyclerView;
    private FirebaseUser user;
    HomeAdapter adapter;
    private List<HomeModel> list;
    private HomeAdapter trendingAdapter;
    private ImageButton backButton;
    DocumentReference reference;
    private ProgressBar progressBar;

      public TrendingFragment() {
        Log.d("TrendingDebug", "TrendingFragment constructor called");
        // Required empty public constructor
    } 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("TrendingDebug", "onCreateView started");
        return inflater.inflate(R.layout.fragment_trending, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d("TrendingDebug", "onViewCreated started");
        super.onViewCreated(view, savedInstanceState);
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).resetTabIcons();
        }
        init(view);
        clickListener();
        list = new ArrayList<>();
        if (getActivity() != null){
            Log.d("TrendingDebug", "getActivity() is not null");
            Log.d("TrendingDebug", "Setting up adapter");
        trendingAdapter = new HomeAdapter(list, getActivity());
        recyclerView.setAdapter(trendingAdapter);
        showLoading();
        loadTrendingData();
        }

    }
    private void clickListener(){
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
    private void init(View view){
        Log.d("TrendingDebug", "init started");
        recyclerView = view.findViewById(R.id.recyclerView);
        if (recyclerView == null){
            Log.e("TrendingDebug", "RecyclerView is null");
        }
        else{
            Log.d("TrendingDebug", "RecyclerView initialized");
        }
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        progressBar = view.findViewById(R.id.progressBar);
        if (progressBar == null){
            Log.e("TrendingDebug", "ProgressBar is null");
        }
        else{
            Log.d("TrendingDebug", "ProgressBar initialized");
        }
        //need to do something with adapter
        backButton = view.findViewById(R.id.backBtn);
    }
    private void loadTrendingData(){
        showLoading();
        list.clear();

        FirebaseFirestore.getInstance().collection("Users").get().addOnSuccessListener(userSnap -> {
            final int totalUsers = userSnap.size();
            final AtomicInteger processedUsers = new AtomicInteger(0);

            if (totalUsers == 0){
                hideLoading();
                return;
            }

            for (DocumentSnapshot snapshot : userSnap){
                if (!snapshot.exists()){
                    return;
                }
                String userId = snapshot.getId(); 
                FirebaseFirestore.getInstance().collection("Users").document(userId).collection("Post Images").get().addOnSuccessListener(postSnap -> {
                    for (DocumentSnapshot postSnapshot : postSnap){
                        if (!postSnapshot.exists()){
                            return;
                        }
                        HomeModel post = postSnapshot.toObject(HomeModel.class);
   
                            // Add the timestamp check here
                            if (postSnapshot.contains("timestamp")) {
                                post.setTimestamp(postSnapshot.getLong("timestamp"));
                            } else {
                                // For older posts without timestamp, use a default
                                post.setTimestamp(System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)); // week old
                            }

                        post.setUid(userId);
                        post.setId(postSnapshot.getId());
                        list.add(post);
                    }
                    int processed = processedUsers.incrementAndGet();
                    if (processed == totalUsers){
                        calculateScore(list);
                        hideLoading();
                    }
                });
            }
            calculateScore(list); 
        }).addOnFailureListener(e -> {

            Log.e("FirestoreError", "Error fetching users", e);
            //check if all users are processed

        });
    }
    private void calculateScore(List<HomeModel> list){
        long currTime = System.currentTimeMillis();
        for (HomeModel post : list){
            long timeDiff = (currTime - post.getTimestamp())/(1000*60*60);
            timeDiff = Math.max(timeDiff, 1);
            float score = (float)post.getLikeCount()/(timeDiff);
            post.setTrendingScore(score);

        }
        Collections.sort(list, (a, b) -> Float.compare(b.getTrendingScore(), a.getTrendingScore()));
        if(list.size() > 40){       
                list = new ArrayList<>(list.subList(0, 40));  
    }
    trendingAdapter.notifyDataSetChanged();

}
private void showLoading() {
    // Show progress bar
    progressBar.setVisibility(View.VISIBLE);
    recyclerView.setVisibility(View.GONE);
}

private void hideLoading() {
    progressBar.setVisibility(View.GONE);
    recyclerView.setVisibility(View.VISIBLE);
}
}