package com.example.bookmark.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.example.bookmark.fragments.Add;
import com.example.bookmark.fragments.Home;
import com.example.bookmark.fragments.MapsFragment;
import com.example.bookmark.fragments.Notification;
import com.example.bookmark.fragments.Profile;
import com.example.bookmark.fragments.Search;

public class PagerAdapter extends FragmentStatePagerAdapter {
    int noOfTabs;
//    private ToolbarVisibilityListener listener;

//    public interface ToolbarVisibilityListener {
//        void onToolbarVisibilityChanged(boolean isVisible);
//    }
//
//    public void setToolbarVisibilityListener(ToolbarVisibilityListener listener) {
//        this.listener = listener;
//    }
    public PagerAdapter(@NonNull FragmentManager fm, int noOfTabs) {
        super(fm);
        this.noOfTabs = noOfTabs;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
//        if (listener != null) {
//            if (position == 4) {  // Profile Tab
//                listener.onToolbarVisibilityChanged(false);
//            } else {
//                listener.onToolbarVisibilityChanged(true);
//            }
//        }
        switch(position){
            case 0:
                return new Home();
            case 1:
                return new Search();
            case 2:
                return new Add();
            case 3:
                return new Notification();
            case 4:
                return new Profile();
            case 5:  // New Maps tab
                return new MapsFragment();
            default:
                return new Home();

        }
    }

    @Override
    public int getCount() {
        return noOfTabs;
    }
}
