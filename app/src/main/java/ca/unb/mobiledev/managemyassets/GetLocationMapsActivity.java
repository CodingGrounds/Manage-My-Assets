package ca.unb.mobiledev.managemyassets;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

public class GetLocationMapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Asset asset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_location_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (getIntent().getExtras() != null)
            asset = (Asset) getIntent().getExtras().get(MMAConstants.ASSET_OBJECT_NAME);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (asset == null)
                    asset = new Asset();

                asset.setLongitude(latLng.longitude);
                asset.setLatitude(latLng.latitude);

                Intent intent = new Intent(GetLocationMapsActivity.this, AddAssetActivity.class);
                intent.putExtra(MMAConstants.ASSET_OBJECT_NAME, asset);
                intent.putExtra(MMAConstants.INTENT_NEW_ASSET, true);
                startActivity(intent);
            }
        });

        // Check if the location permission is granted and request it if not
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            centerMapOnDeviceLocation();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MMAConstants.REQUEST_PERMISSION_ACCESS_FINE_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MMAConstants.REQUEST_PERMISSION_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                centerMapOnDeviceLocation();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.error_location_permission_failed), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Attempts to get the current device location and center the map view on it
     */
    private void centerMapOnDeviceLocation() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            String provider = mLocationManager.getBestProvider(new Criteria(), true);
            Location location = mLocationManager.getLastKnownLocation(provider);
            if (location != null) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.error_location_device_failed), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
