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
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.example.bookmark.adapter.PagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
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
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.test));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.maps));

        pagerAdapter = new PagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.getTabAt(0).setIcon(R.drawable.home_fill);
        tabLayout.getTabAt(1).setIcon(R.drawable.bookmark);
        tabLayout.getTabAt(2).setIcon(R.drawable.add);
        tabLayout.getTabAt(3).setIcon(R.drawable.heart);
        tabLayout.getTabAt(4).setIcon(R.drawable.profile_outline);
        tabLayout.getTabAt(5).setIcon(R.drawable.maps);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());

                View frameLayout = findViewById(R.id.mainFrameLayout);
                if (frameLayout != null && frameLayout.getVisibility() == View.VISIBLE) {
                    frameLayout.setVisibility(View.GONE);
                    getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                }
                
                switch(tab.getPosition()){
                    case 0:
                        tabLayout.getTabAt(0).setIcon(R.drawable.home_fill);
                        break;
                    case 1:
                        tabLayout.getTabAt(1).setIcon(R.drawable.bookmark);
                        break;
                    case 2:
                        tabLayout.getTabAt(2).setIcon(R.drawable.add);
                        break;
                    case 3:
                        tabLayout.getTabAt(3).setIcon(R.drawable.heart_fill);
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
                        tabLayout.getTabAt(1).setIcon(R.drawable.bookmark);
                        break;
                    case 2:
                        tabLayout.getTabAt(2).setIcon(R.drawable.add);
                        break;
                    case 3:
                        tabLayout.getTabAt(3).setIcon(R.drawable.heart);
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
                        tabLayout.getTabAt(1).setIcon(R.drawable.bookmark);
                        break;
                    case 2:
                        tabLayout.getTabAt(2).setIcon(R.drawable.add);
                        break;
                    case 3:
                        tabLayout.getTabAt(3).setIcon(R.drawable.heart_fill);
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