package com.example.bookmark;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

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
import androidx.viewpager.widget.ViewPager;

import com.example.bookmark.adapter.PagerAdapter;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;

    PagerAdapter pagerAdapter;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private ImageButton sendBtn;
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
//
//        String apiKey = getString(R.string.google_maps_key);
//        Places.initialize(getApplicationContext(), apiKey);
//        PlacesClient placesClient = Places.createClient(this);
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
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.search));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.add));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.heart));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.test));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.maps));




        pagerAdapter = new PagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
//        Toolbar toolbar = findViewById(R.id.toolbar);
//
//        // Set the toolbar visibility listener
//        pagerAdapter.setToolbarVisibilityListener(isVisible -> {
//            if (isVisible) {
//                toolbar.setVisibility(View.VISIBLE);
//            } else {
//                toolbar.setVisibility(View.GONE);
//            }
//        });

//By default tab icon
        //new stuff
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.getTabAt(0).setIcon(R.drawable.home_fill);
        tabLayout.getTabAt(1).setIcon(R.drawable.search);
        tabLayout.getTabAt(2).setIcon(R.drawable.add);
        tabLayout.getTabAt(3).setIcon(R.drawable.heart);
        tabLayout.getTabAt(4).setIcon(R.drawable.profile_outline);
        tabLayout.getTabAt(5).setIcon(R.drawable.maps);



        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());

                switch(tab.getPosition()){

                    case 0:
                        tabLayout.getTabAt(0).setIcon(R.drawable.home_fill);
                        break;
                    case 1:
                        tabLayout.getTabAt(1).setIcon(R.drawable.search);
                        break;
                    case 2:
                        tabLayout.getTabAt(2).setIcon(R.drawable.add);
                        break;
                    case 3:
                        tabLayout.getTabAt(3).setIcon(R.drawable.heart_fill);
//                        tab.setIcon(R.drawable.heart_fill);
                        break;
                    case 4:
                        tabLayout.getTabAt(4).setIcon(R.drawable.test);
                        break;
                    case 5:
                        tabLayout.getTabAt(5).setIcon(R.drawable.maps_fill);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                switch(tab.getPosition()){

                    case 0:
                        tabLayout.getTabAt(0).setIcon(R.drawable.home);
                        break;
                    case 1:
                        tabLayout.getTabAt(1).setIcon(R.drawable.search);
                        break;
                    case 2:
                        tabLayout.getTabAt(2).setIcon(R.drawable.add);
                        break;
                    case 3:
                        tabLayout.getTabAt(3).setIcon(R.drawable.heart);
//                        tab.setIcon(R.drawable.heart_fill);
                        break;
                    case 4:
                        tabLayout.getTabAt(4).setIcon(R.drawable.profile_outline);
                        break;
                    case 5:
                        tabLayout.getTabAt(5).setIcon(R.drawable.maps);
                        break;

                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                switch(tab.getPosition()){
                    case 0:
                        tabLayout.getTabAt(0).setIcon(R.drawable.home_fill);
                        break;
                    case 1:
                        tabLayout.getTabAt(1).setIcon(R.drawable.search);
                        break;
                    case 2:
                        tabLayout.getTabAt(2).setIcon(R.drawable.add);
                        break;
                    case 3:
                        tabLayout.getTabAt(3).setIcon(R.drawable.heart_fill);
//                        tab.setIcon(R.drawable.heart_fill);
                        break;
                    case 4:
                        tabLayout.getTabAt(4).setIcon(R.drawable.test);
                        break;
                    case 5:
                        tabLayout.getTabAt(5).setIcon(R.drawable.maps_fill);
                        break;

                }
            }
        });

    }

}