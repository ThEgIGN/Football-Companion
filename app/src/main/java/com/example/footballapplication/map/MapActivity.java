package com.example.footballapplication.map;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.footballapplication.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.SphericalUtil;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private LatLng stadiumLocation;
    private FusedLocationProviderClient locationProvider;
    private String stadiumName;
    private GoogleMap mainMap;

    private final int COARSE_LOCATION_PERMISSION_CODE = 90;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_map);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        locationProvider = LocationServices.getFusedLocationProviderClient(this);

        Intent intent = getIntent();
        // If stadium information can't be found, default is set to Allianz Arena location
        stadiumName = intent.getStringExtra("stadiumName");
        if (TextUtils.isEmpty(stadiumName)) {
            stadiumName = "Allianz Arena";
        }
        double latitude = intent.getDoubleExtra("latitude", 11.624664);
        double longitude = intent.getDoubleExtra("longitude", 48.218808);
        stadiumLocation = new LatLng(longitude, latitude);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mainMap = googleMap;
        mainMap.addMarker(new MarkerOptions().position(stadiumLocation).title(stadiumName));
        mainMap.animateCamera(CameraUpdateFactory.newLatLngZoom(stadiumLocation, 15));
        calculateDistance();
    }

    private void calculateDistance() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, COARSE_LOCATION_PERMISSION_CODE);
        } else {
            Task<Location> task = locationProvider.getLastLocation();
            task.addOnSuccessListener(location -> {
                if (location != null) {
                    LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mainMap.addMarker(new MarkerOptions().position(userLocation).title(getString(R.string.user_location)));

                    double distance = (SphericalUtil.computeDistanceBetween(userLocation, stadiumLocation)) / 1000;
                    int distanceRounded = (int) Math.round(distance);

                    String distanceString = getString(R.string.calculate_the_distance) + " " + distanceRounded + " km";
                    Toast.makeText(this, distanceString, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, getString(R.string.calculate_the_distance_not), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == COARSE_LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                calculateDistance();
            } else {
                Toast.makeText(this, getString(R.string.calculate_the_distance_not), Toast.LENGTH_SHORT).show();
            }
        }
    }
}