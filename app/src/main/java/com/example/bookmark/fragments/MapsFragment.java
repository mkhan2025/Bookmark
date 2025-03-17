package com.example.bookmark.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.bookmark.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsFragment extends Fragment implements OnMapReadyCallback {

    private MapView mapView;

    public MapsFragment() {
        super(R.layout.fragment_maps);
    }

    @Override
    public void onViewCreated(@NonNull android.view.View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        LatLng scoops = new LatLng(39.64379259389463, -86.8643773763308);
        googleMap.addMarker(new MarkerOptions()
                .position(scoops)
                .title("Scoops"));
        LatLng conspire = new LatLng(39.643988204670954, -86.86397886781502);
        googleMap.addMarker(new MarkerOptions()
                .position(conspire)
                .title("Conspire"));
        LatLng roy = new LatLng(39.64101970800983, -86.86389084934379);
        googleMap.addMarker(new MarkerOptions()
                .position(roy)
                .title("Roy"));
        LatLng gcpa = new LatLng(39.63833602816779, -86.86170354564707);
        googleMap.addMarker(new MarkerOptions()
                .position(gcpa)
                .title("GCPA"));
        LatLng julian = new LatLng(39.639278042872604, -86.86271916468584);
        googleMap.addMarker(new MarkerOptions()
                .position(julian)
                .title("Julian"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(scoops));
    }


    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
