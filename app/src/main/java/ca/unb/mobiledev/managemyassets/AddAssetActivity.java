package ca.unb.mobiledev.managemyassets;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

public class AddAssetActivity extends AppCompatActivity {

    private EditText mNameEditText;
    private EditText mDescriptionEditText;
    private EditText mLatitudeEditText;
    private EditText mLongitudeEditText;
    private ImageView mAssetPictureImageView;
    private CheckBox mCurrentLocationCheckBox;
    private FloatingActionButton mSaveAssetFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_asset);

        mNameEditText = findViewById(R.id.assetName_editText);
        mDescriptionEditText = findViewById(R.id.assetDescription_editText);
        mLatitudeEditText = findViewById(R.id.assetLatitude_editText);
        mLongitudeEditText = findViewById(R.id.assetLongitude_editText);
        mAssetPictureImageView = findViewById(R.id.assetPicture_imageView);

        mCurrentLocationCheckBox = findViewById(R.id.assetCurrentLocation_checkBox);
        mSaveAssetFab = findViewById(R.id.assetSave_fab);

        mAssetPictureImageView.setClickable(true);
        mAssetPictureImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(AddAssetActivity.this, "Implement picture taking function", Toast.LENGTH_SHORT).show();
            }
        });
        mCurrentLocationCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentLocationCheckBox.isChecked()) {
                    LatLng coordinates = DeviceLocation.getDeviceLocation();
                    // Grey out the longitude and latitude fields
                    mLatitudeEditText.setEnabled(false);
                    mLongitudeEditText.setEnabled(false);
                    // Put the device's current coordinates in the boxes
                    mLatitudeEditText.setText(String.valueOf(coordinates.latitude));
                    mLongitudeEditText.setText(String.valueOf(coordinates.longitude));
                    // TODO Add a refresh button on the coordinates
                } else {
                    // Re-enable the fields
                    mLatitudeEditText.setEnabled(true);
                    mLongitudeEditText.setEnabled(true);
                    // Empty the text fields
                    mLongitudeEditText.setText("");
                    mLatitudeEditText.setText("");
                }
            }
        });
        mSaveAssetFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // TODO Have options to: view on map, view list, add another asset, or get directions
                String name = mNameEditText.getText().toString();
                String description = mDescriptionEditText.getText().toString();
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

                Asset asset = new Asset(name, description, Double.parseDouble(latitude), Double.parseDouble(longitude));

                DatabaseHelper.getDatabaseHelper(AddAssetActivity.this).insertAsset(asset);

                Intent intent = new Intent(AddAssetActivity.this, MapActivity.class);
                intent.putExtra(Asset.OBJECT_NAME, asset);
                startActivity(intent);
            }
        });
    }
}
