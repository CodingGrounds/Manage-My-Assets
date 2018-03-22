package ca.unb.mobiledev.managemyassets;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

public class AddAssetActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private EditText mNameEditText;
    private EditText mDescriptionEditText;
    private EditText mNotesEditText;
    private EditText mLatitudeEditText;
    private EditText mLongitudeEditText;
    private ImageView mAssetPictureImageView;
    private Button mCurrentLocationButton;
    private FloatingActionButton mSaveAssetFab;

    private DatabaseCallTask databaseCallTask;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_asset);

        databaseCallTask = new DatabaseCallTask(this);

        mNameEditText = findViewById(R.id.assetName_editText);
        mDescriptionEditText = findViewById(R.id.assetDescription_editText);
        mNotesEditText = findViewById(R.id.assetNotes_editText);
        mLatitudeEditText = findViewById(R.id.assetLatitude_editText);
        mLongitudeEditText = findViewById(R.id.assetLongitude_editText);
        mAssetPictureImageView = findViewById(R.id.assetPicture_imageView);

        mCurrentLocationButton = findViewById(R.id.assetCurrentLocation_button);
        mSaveAssetFab = findViewById(R.id.assetSave_fab);

        mAssetPictureImageView.setClickable(true);
        mAssetPictureImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(AddAssetActivity.this, "Implement picture taking function", Toast.LENGTH_SHORT).show();
            }
        });
        mCurrentLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestPermissions();
            }
        });
        mSaveAssetFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String name = mNameEditText.getText().toString();
                String description = mDescriptionEditText.getText().toString();
                String notes = mNotesEditText.getText().toString();
                String latitude = mLatitudeEditText.getText().toString();
                String longitude = mLongitudeEditText.getText().toString();

                if (TextUtils.isEmpty(name)) {
                    mNameEditText.setError("Name cannot be empty");
                }
                if (TextUtils.isEmpty(latitude)) {
                    mLatitudeEditText.setError("Latitude cannot be empty");
                }
                if (TextUtils.isEmpty(longitude)) {
                    mLongitudeEditText.setError("Longitude cannot be empty");
                }

                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(latitude) || TextUtils.isEmpty(longitude)) {
                    // Don't attempt to store anything if the correct fields aren't provided
                    return;
                }

                Asset asset = new Asset(name, description, notes, Double.parseDouble(latitude), Double.parseDouble(longitude));
                databaseCallTask.execute(DatabaseCallTask.INSERT_ASSET, asset);
            }
        });

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        mCurrentLocationButton.setEnabled(true);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        mCurrentLocationButton.setEnabled(false);
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Toast.makeText(this, "Connection to Google Play Services failed", Toast.LENGTH_SHORT).show();
    }

    public void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MapActivity.PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
        } else {
            getDeviceLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MapActivity.PERMISSION_REQUEST_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getDeviceLocation();
                }
        }
    }

    private void getDeviceLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (lastLocation != null) {
                mLatitudeEditText.setText(String.valueOf(lastLocation.getLatitude()));
                mLongitudeEditText.setText(String.valueOf(lastLocation.getLongitude()));
            } else {
                Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void databaseCallFinished(Asset asset) {
        Intent intent = new Intent(AddAssetActivity.this, MapActivity.class);
        intent.putExtra(Asset.OBJECT_NAME, asset);
        startActivity(intent);
    }
}
