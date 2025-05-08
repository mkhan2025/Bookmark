package com.example.bookmark;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.example.bookmark.adapter.PagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuth.AuthStateListener;

import androidx.annotation.NonNull;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    //sets the tabs at the bottom
    private TabLayout tabLayout;
    //empty space where the app content will go
    private ViewPager viewPager;
    PagerAdapter pagerAdapter;
    //intiating curr firebase user 
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private ImageButton sendBtn;

//    public PlacesClient getPlacesClient() {
//        return placesClient;
//    }
//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        init();

        FirebaseApp.initializeApp(/*context=*/ this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance());

        FirebaseAuth.getInstance().addAuthStateListener(new AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                if (currentUser == null) {
                    // User is signed out, start ReplacerActivity
                    Intent intent = new Intent(MainActivity.this, ReplacerActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }
        });

        addTabs();
    }
    private void init(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
    }
    private void addTabs(){
        Log.d(TAG, "addTabs called");
        tabLayout.setTabGravity(TabLayout.GRAVITY_CENTER);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);

        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.home));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.bookmark));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.add));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.heart));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.user));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.maps));

        pagerAdapter = new PagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.getTabAt(0).setIcon(R.drawable.home_fill);
        tabLayout.getTabAt(1).setIcon(R.drawable.bookmark);
        tabLayout.getTabAt(2).setIcon(R.drawable.add);
        tabLayout.getTabAt(3).setIcon(R.drawable.heart);
        tabLayout.getTabAt(4).setIcon(R.drawable.user);
        tabLayout.getTabAt(5).setIcon(R.drawable.maps);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.d(TAG, "onTabSelected called for position: " + tab.getPosition());
                
                View frameLayout = findViewById(R.id.mainFrameLayout);
                Log.d(TAG, "FrameLayout visibility: " + (frameLayout != null ? frameLayout.getVisibility() : "null"));
                
                if (frameLayout != null && frameLayout.getVisibility() == View.VISIBLE) {
                    Log.d(TAG, "Clearing back stack and hiding frame layout");
                    // Clear the back stack and hide the frame layout
                    getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    frameLayout.setVisibility(View.GONE);
                }
                
                // Set the ViewPager position after handling the frame layout
                viewPager.setCurrentItem(tab.getPosition(), false);
                
                // Reset all tab icons to unfilled state first
                resetTabIcons();
                
                // Then set the selected tab's icon to filled
                switch(tab.getPosition()){
                    case 0:
                        Log.d(TAG, "Setting home tab icon to filled");
                        tabLayout.getTabAt(0).setIcon(R.drawable.home_fill);
                        break;
                    case 1:
                        Log.d(TAG, "Setting bookmark tab icon to filled");
                        tabLayout.getTabAt(1).setIcon(R.drawable.bookmark_fill);
                        break;
                    case 2:
                        Log.d(TAG, "Setting add tab icon to filled");
                        tabLayout.getTabAt(2).setIcon(R.drawable.add_fill);
                        break;
                    case 3:
                        Log.d(TAG, "Setting heart tab icon to filled");
                        tabLayout.getTabAt(3).setIcon(R.drawable.heart_fill);
                        break;
                    case 4:
                        Log.d(TAG, "Setting user tab icon to filled");
                        tabLayout.getTabAt(4).setIcon(R.drawable.test);
                        break;
                    case 5:
                        Log.d(TAG, "Setting maps tab icon to filled");
                        tabLayout.getTabAt(5).setIcon(R.drawable.maps_fill);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                Log.d(TAG, "onTabUnselected called for position: " + tab.getPosition());
                switch(tab.getPosition()){
                    case 0:
                        Log.d(TAG, "Setting home tab icon to unfilled");
                        tabLayout.getTabAt(0).setIcon(R.drawable.home);
                        break;
                    case 1:
                        Log.d(TAG, "Setting bookmark tab icon to unfilled");
                        tabLayout.getTabAt(1).setIcon(R.drawable.bookmark);
                        break;
                    case 2:
                        Log.d(TAG, "Setting add tab icon to unfilled");
                        tabLayout.getTabAt(2).setIcon(R.drawable.add);
                        break;
                    case 3:
                        Log.d(TAG, "Setting heart tab icon to unfilled");
                        tabLayout.getTabAt(3).setIcon(R.drawable.heart);
                        break;
                    case 4:
                        Log.d(TAG, "Setting user tab icon to unfilled");
                        tabLayout.getTabAt(4).setIcon(R.drawable.user);
                        break;
                    case 5:
                        Log.d(TAG, "Setting maps tab icon to unfilled");
                        tabLayout.getTabAt(5).setIcon(R.drawable.maps);
                        break;
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                Log.d(TAG, "onTabReselected called for position: " + tab.getPosition());
                
                View frameLayout = findViewById(R.id.mainFrameLayout);
                Log.d(TAG, "FrameLayout visibility on reselect: " + (frameLayout != null ? frameLayout.getVisibility() : "null"));
                
                if (frameLayout != null && frameLayout.getVisibility() == View.VISIBLE) {
                    Log.d(TAG, "Clearing back stack and hiding frame layout on reselect");
                    // Clear the back stack and hide the frame layout
                    getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    frameLayout.setVisibility(View.GONE);
                }
                
                switch(tab.getPosition()){
                    case 0:
                        Log.d(TAG, "Resetting home tab icon to filled");
                        tabLayout.getTabAt(0).setIcon(R.drawable.home_fill);
                        break;
                    case 1:
                        Log.d(TAG, "Resetting bookmark tab icon to filled");
                        tabLayout.getTabAt(1).setIcon(R.drawable.bookmark_fill);
                        break;
                    case 2:
                        Log.d(TAG, "Resetting add tab icon to filled");
                        tabLayout.getTabAt(2).setIcon(R.drawable.add_fill);
                        break;
                    case 3:
                        Log.d(TAG, "Resetting heart tab icon to filled");
                        tabLayout.getTabAt(3).setIcon(R.drawable.heart_fill);
                        break;
                    case 4:
                        Log.d(TAG, "Resetting user tab icon to filled");
                        tabLayout.getTabAt(4).setIcon(R.drawable.test);
                        break;
                    case 5:
                        Log.d(TAG, "Resetting maps tab icon to filled");
                        tabLayout.getTabAt(5).setIcon(R.drawable.maps_fill);
                        break;
                }
            }
        });
    }

    public void resetTabIcons() {
        Log.d(TAG, "Resetting all tab icons to unfilled state");
        tabLayout.getTabAt(0).setIcon(R.drawable.home);
        tabLayout.getTabAt(1).setIcon(R.drawable.bookmark);
        tabLayout.getTabAt(2).setIcon(R.drawable.add);
        tabLayout.getTabAt(3).setIcon(R.drawable.heart);
        tabLayout.getTabAt(4).setIcon(R.drawable.user);
        tabLayout.getTabAt(5).setIcon(R.drawable.maps);
    }
}