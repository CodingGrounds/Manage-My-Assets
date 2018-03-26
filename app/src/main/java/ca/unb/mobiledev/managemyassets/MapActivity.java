package ca.unb.mobiledev.managemyassets;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Arrays;

import static ca.unb.mobiledev.managemyassets.Asset.LAT;
import static ca.unb.mobiledev.managemyassets.Asset.LNG;

/**
 * Created by laver on 2018-02-18.
 */


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private DatabaseHelper databaseHelper;
    private ArrayList<Asset> assetList;
    private GoogleMap mMap;
    private Asset detailAsset;
    private LocationManager locationManager;
    private FloatingActionButton directionFab;
    protected static final int PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 0;
    private LatLng currentLocation;
    private Marker directionsMarker;
    private int locationCounter;
    private DatabaseCallTask databaseCallTask;
    FragmentManager fm = getSupportFragmentManager();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        locationCounter = 0;
        directionFab = findViewById(R.id.directions_fab);
        databaseHelper = DatabaseHelper.getDatabaseHelper(MapActivity.this);
        assetList = new ArrayList<>(Arrays.asList(databaseHelper.selectAssets()));
        detailAsset = (Asset) getIntent().getSerializableExtra(Asset.OBJECT_NAME);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapActivity.this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(true);
        updateMapAssets();
        setCurrentLocationEnabled();
        if (detailAsset != null) {
            // Zoom in on the marker the user chooses to view
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(detailAsset.getLatitude(), detailAsset.getLongitude()), 15));
        } else if (currentLocation != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.latitude, currentLocation.longitude), 15));
        }

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Log.i("OnMapClick", " " + latLng.toString());
                AddDFragment addDFragment = new AddDFragment();
                Bundle bundle = new Bundle();
                bundle.putDouble(LAT, latLng.latitude);
                bundle.putDouble(LNG, latLng.longitude);
                addDFragment.setArguments(bundle);
                addDFragment.show(fm, "Add Dialog");
            }
        });
    }

    private void updateMapAssets() {

        // Add all assets from the database to the map
        for (Asset asset : assetList) {
            mMap.addMarker(new MarkerOptions().position(new LatLng(asset.getLatitude(), asset.getLongitude())).title(asset.getName()).snippet(asset.getDescription()));
        }

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.showInfoWindow();
                mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                addDirectionsButton(marker);
                return true;
            }
        });


        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                databaseCallTask = new DatabaseCallTask(MapActivity.this);
                databaseCallTask.execute("SELECT BY LatLng", marker.getPosition());
            }
        });

    }

    public void databaseCallFinished(Asset asset) {
        Intent detailsIntent = new Intent(MapActivity.this, DetailsActivity.class);
        detailsIntent.putExtra(Asset.OBJECT_NAME, asset);
        startActivity(detailsIntent);
    }

    //Method for adding direction Fab
    private void addDirectionsButton(Marker marker) {
        directionsMarker = marker;
        directionFab.setVisibility(View.VISIBLE);
        directionFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(
                        "http://maps.google.com/maps?saddr=" + currentLocation.latitude + "," + currentLocation.longitude + "&daddr=" + directionsMarker.getPosition().latitude + "," + directionsMarker.getPosition().longitude));
                startActivity(intent);
            }
        });
        //DON'T Know if u want this here
        mMap.setOnInfoWindowCloseListener(new GoogleMap.OnInfoWindowCloseListener() {
            @Override
            public void onInfoWindowClose(Marker marker) {

                directionFab.setVisibility(View.INVISIBLE);
                marker.hideInfoWindow();
            }
        });

    }

    //Method for checking current location
    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        currentLocation = latLng;
        //Only when no element is clicked

        if (detailAsset == null && locationCounter == 0) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
            locationCounter++;
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    //Helper method for setting location and checking permissions
    private void setCurrentLocationEnabled() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            String provider = locationManager.getBestProvider(criteria, true);
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                onLocationChanged(location);
            }

            locationManager.requestLocationUpdates(provider, 2000, 10, this);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

}
