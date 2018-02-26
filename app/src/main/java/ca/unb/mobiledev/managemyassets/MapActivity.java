package ca.unb.mobiledev.managemyassets;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

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

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private DatabaseHelper databaseHelper;
    private ArrayList<Asset> assetList;
    private GoogleMap mMap;
    private Asset detailAsset;

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

        // TODO Change to zoom into the users current location
        LatLng fredericton = new LatLng(45.957319, -66.647818);
        // Add all assets from the database to the map
        for (Asset asset : assetList) {
            mMap.addMarker(new MarkerOptions().position(new LatLng(asset.getLatitude(), asset.getLongitude())).title(asset.getName()).snippet(asset.getDescription()));
        }

        if (detailAsset != null) {
            // Zoom in on the marker the user chooses to view
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(detailAsset.getLatitude(), detailAsset.getLongitude()), 15));
        } else {
            // Zoom in on the users current location (Currently Fredericton for testing)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(fredericton, 10));
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
}
