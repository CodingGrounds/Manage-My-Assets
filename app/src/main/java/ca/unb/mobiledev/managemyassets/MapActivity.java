package ca.unb.mobiledev.managemyassets;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by laver on 2018-02-18.
 */

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private DatabaseHelper databaseHelper;
    private ArrayList<Asset> assetList;
    private GoogleMap mMap;
    private Asset detailAsset;
    private FloatingActionButton directionFab;
    private LatLng currentLocation;
    private Marker directionsMarker;
    private int locationCounter;
    private DatabaseCallTask databaseCallTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        locationCounter = 0;
        directionFab = findViewById(R.id.directions_fab);
        databaseHelper = DatabaseHelper.getDatabaseHelper(MapActivity.this);
        assetList = null;
        detailAsset = (Asset) getIntent().getSerializableExtra(MMAConstants.ASSET_OBJECT_NAME);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapActivity.this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(true);

        DatabaseCallTask databaseCallTask = new DatabaseCallTask(this);
        databaseCallTask.execute(MMAConstants.DATABASE_SELECT_ASSETS, MMAConstants.ORIGIN_MAP_ACTIVITY, null);

        //updateMapAssets();
        setCurrentLocationEnabled();
        if (detailAsset != null) {
            // Zoom in on the marker the user chooses to view
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(detailAsset.getLatitude(), detailAsset.getLongitude()), 15));
        } else if (currentLocation != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.latitude, currentLocation.longitude), 15));
        }
        addMapClickListener(false);
    }



    public void databaseCallFinished(Asset[] assets) {
        assetList = new ArrayList<>(Arrays.asList(assets));
        Log.i("DatabaseFinished", " " + assets.toString());
        updateMapAssets();
    }

    public void databaseCallFinished(Asset asset) {
        Intent detailsIntent = new Intent(MapActivity.this, AddAssetActivity.class);
        detailsIntent.putExtra(MMAConstants.ASSET_OBJECT_NAME, asset);
        detailsIntent.putExtra(MMAConstants.INTENT_NEW_ASSET, false);
        startActivity(detailsIntent);
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
                mMap.setOnMapClickListener(null);
                return true;
            }
        });
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                databaseCallTask = new DatabaseCallTask(MapActivity.this);
                databaseCallTask.execute(MMAConstants.DATABASE_SELECT_ASSET, MMAConstants.ORIGIN_MAP_ACTIVITY, marker.getPosition());
            }
        });

    }

    //Flag for window close or not
    private void addMapClickListener(final boolean flag){
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(!flag){
                    final Asset newAsset = new Asset();
                    newAsset.setLatitude(latLng.latitude);
                    newAsset.setLongitude(latLng.longitude);

                    final AlertDialog alertDialog = new AlertDialog.Builder(MapActivity.this)
                            .setTitle(getString(R.string.location_map_add_title))
                            .setMessage(getString(R.string.location_map_add_message))
                            .setPositiveButton(getString(R.string.input_button_yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent addAsset = new Intent(MapActivity.this, AddAssetActivity.class);
                                    addAsset.putExtra(MMAConstants.ASSET_OBJECT_NAME, newAsset);
                                    addAsset.putExtra(MMAConstants.INTENT_NEW_ASSET,  true);
                                    startActivity(addAsset);
                                }
                            })
                            .setNegativeButton(getString(R.string.input_button_no), null)
                            .create();
                    alertDialog.show();
                }
            }
        });
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
        mMap.setOnInfoWindowCloseListener(new GoogleMap.OnInfoWindowCloseListener() {
            @Override
            public void onInfoWindowClose(Marker marker) {
                directionFab.setVisibility(View.INVISIBLE);
                addMapClickListener(true);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MMAConstants.REQUEST_PERMISSION_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setCurrentLocationEnabled();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.error_location_permission_failed), Toast.LENGTH_SHORT).show();
            }
        }
    }

    //Helper method for setting location and checking permissions
    private void setCurrentLocationEnabled() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            String provider = locationManager.getBestProvider(criteria, true);
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                onLocationChanged(location);
            }
            locationManager.requestLocationUpdates(provider, 2000, 10, this);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MMAConstants.REQUEST_PERMISSION_ACCESS_FINE_LOCATION);
        }
    }
}
