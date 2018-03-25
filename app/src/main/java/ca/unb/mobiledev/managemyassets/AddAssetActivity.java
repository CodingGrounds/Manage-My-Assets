package ca.unb.mobiledev.managemyassets;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.provider.MediaStore;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddAssetActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int REQUEST_CAPTURE_IMAGE = 1;
    private static final int PERMISSION_REQUEST_EXTERNAL_STORAGE = 1;

    private EditText mNameEditText;
    private EditText mDescriptionEditText;
    private EditText mNotesEditText;
    private EditText mLatitudeEditText;
    private EditText mLongitudeEditText;
    private ImageView mAssetPictureImageView;
    private Button mCurrentLocationButton;
    private FloatingActionButton mSaveAssetFab;
    private FloatingActionButton mTakePictureFab;

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
        mTakePictureFab = findViewById(R.id.assetTakePicture_fab);

        mTakePictureFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestAppPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_EXTERNAL_STORAGE);
            }
        });
        mCurrentLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestAppPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MapActivity.PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
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
                String imagePath = (String) mAssetPictureImageView.getTag();

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

                Asset asset = new Asset(name, description, notes, Double.parseDouble(latitude), Double.parseDouble(longitude), imagePath);
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

        if (getIntent().getExtras() != null) {
            Asset asset = (Asset) getIntent().getExtras().get(Asset.OBJECT_NAME);
            if (asset.getImage() != null)
                mAssetPictureImageView.setImageBitmap(loadFromInternalStorage(asset.getImage()));

            mNameEditText.setText(asset.getName());
            mDescriptionEditText.setText(asset.getDescription());
            mNotesEditText.setText(asset.getNotes());
            mLatitudeEditText.setText(String.valueOf(asset.getLatitude()));
            mLongitudeEditText.setText(String.valueOf(asset.getLongitude()));
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

    public void requestAppPermissions(String[] permissions, int permissionRequestId) {
        boolean allPermissionsGranted = true;

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
            }
        }

        if (!allPermissionsGranted) {
            ActivityCompat.requestPermissions(this, permissions, permissionRequestId);
        } else {
            // Call the permission handler with the permission granted parameters
            onRequestPermissionsResult(permissionRequestId, permissions, new int[]{PackageManager.PERMISSION_GRANTED});
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MapActivity.PERMISSION_REQUEST_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getDeviceLocation();
                }
                break;
            case PERMISSION_REQUEST_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    saveImageFile();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CAPTURE_IMAGE && resultCode == RESULT_OK) {
            if (data != null && data.getExtras() != null) {
                Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                String filePath = saveToInternalStorage(imageBitmap);

                if (filePath != null) {
                    mAssetPictureImageView.setImageBitmap(imageBitmap);
                }
                mAssetPictureImageView.setTag(filePath);
            }
        }
    }

    private String saveToInternalStorage(Bitmap bitmap) {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss-SSS", Locale.CANADA).format(new Date());
        String fileName = "MMA_" + timestamp + ".jpg";
        String filePath = null;

        File photoFile = new File(getFilesDir(), fileName);

        try {
            FileOutputStream outputStream = new FileOutputStream(photoFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            filePath = photoFile.getPath();
            outputStream.close();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Unable to save photo", Toast.LENGTH_SHORT).show();
        }
        return filePath;
    }

    private Bitmap loadFromInternalStorage(String filePath) {
        Bitmap bitmap = null;

        try {
            File imageFile = new File(filePath);
            bitmap = BitmapFactory.decodeStream(new FileInputStream(imageFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return bitmap;
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

    private void saveImageFile() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CAPTURE_IMAGE);
        } else {
            Toast.makeText(AddAssetActivity.this, "Unable to find a suitable camera app", Toast.LENGTH_SHORT).show();
        }
    }

    public void databaseCallFinished(Asset asset) {
        // TODO Change to allow the user to choose where to navigate to
        Intent intent = new Intent(AddAssetActivity.this, MapActivity.class);
        intent.putExtra(Asset.OBJECT_NAME, asset);
        startActivity(intent);
    }
}
