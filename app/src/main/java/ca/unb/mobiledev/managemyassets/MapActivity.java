package ca.unb.mobiledev.managemyassets;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
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

/**
 * Created by laver on 2018-02-18.
 */

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private DatabaseHelper databaseHelper;
    private ArrayList<Asset> assetList;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        databaseHelper = DatabaseHelper.getDatabaseHelper(MapActivity.this);
        assetList = new ArrayList<>(Arrays.asList(databaseHelper.selectAssets()));

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapActivity.this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap){
        mMap = googleMap;

        LatLng fredericton = new LatLng(45.957319, -66.647818);
        for(Asset asset:assetList){
            mMap.addMarker(new MarkerOptions().position(new LatLng(asset.getLatitude(), asset.getLongitude())).title(asset.getName()).snippet(asset.getDescription()));
        }

        //mMap.addMarker(new MarkerOptions().position(fredericton).title("Fredericton!"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(fredericton, 5));

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener(){

            @Override
            public boolean onMarkerClick(Marker marker){
                Log.i("MARKER", "This " + marker.getTitle());
                marker.showInfoWindow();
                return true;
            }
        });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Intent detailsIntent = new Intent(MapActivity.this, DetailsActivity.class);
                detailsIntent.putExtra("asset", databaseHelper.selectAsset(marker.getPosition()));
                startActivity(detailsIntent);
            }
        });




    }



}
