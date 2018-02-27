package ca.unb.mobiledev.managemyassets;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Arrays;

import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_HYBRID;

/**
 * Created by laver on 2018-02-18.
 */

public class MapActivity extends FragmentActivity implements OnMapReadyCallback , LocationListener {

    private DatabaseHelper databaseHelper;
    private ArrayList<Asset> assetList;
    private GoogleMap mMap;
    private Asset detailAsset;
    private LocationManager locationManager;
    protected static final int PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
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
        googleMap.setMapType(MAP_TYPE_HYBRID);
        updateMapAssets();
        setCurrentLocationEnabled();
        if (detailAsset != null) {
            // Zoom in on the marker the user chooses to view
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(detailAsset.getLatitude(), detailAsset.getLongitude()), 15));
        }


    }
    
    private void updateMapAssets(){

        // Add all assets from the database to the map
        for (Asset asset : assetList) {
            mMap.addMarker(new MarkerOptions().position(new LatLng(asset.getLatitude(), asset.getLongitude())).title(asset.getName()).snippet(asset.getDescription()));
        }

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.showInfoWindow();
                return true;
            }
        });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Intent detailsIntent = new Intent(MapActivity.this, DetailsActivity.class);
                detailsIntent.putExtra(Asset.OBJECT_NAME, databaseHelper.selectAsset(marker.getPosition()));
                startActivity(detailsIntent);
            }
        });

    }


    //Method for checking current location
    @Override
    public void onLocationChanged(Location location) {
        Log.i("location change", "OnLocationChange");
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        //Only when no element is clicked
        if(detailAsset == null){
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
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
            locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            String provider = locationManager.getBestProvider(criteria, true);
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                onLocationChanged(location);
            }

            locationManager.requestLocationUpdates(provider, 2000, 10, this);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                this.requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

}
