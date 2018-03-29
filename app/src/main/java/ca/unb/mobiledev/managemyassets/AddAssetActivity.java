package ca.unb.mobiledev.managemyassets;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
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
import java.util.List;
import java.util.Locale;

import static ca.unb.mobiledev.managemyassets.Asset.ACTION;
import static ca.unb.mobiledev.managemyassets.Asset.LAT;
import static ca.unb.mobiledev.managemyassets.Asset.LNG;

public class AddAssetActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static final String INTENT_NEW_ASSET = "edit_mode";
    private static final int REQUEST_CAPTURE_IMAGE = 1;
    private static final int PERMISSION_REQUEST_EXTERNAL_STORAGE = 1;

    private EditText mNameEditText;
    private EditText mDescriptionEditText;
    private EditText mNotesEditText;
    private EditText mLatitudeEditText;
    private EditText mLongitudeEditText;
    private ImageView mAssetPictureImageView;
    private Button mCurrentLocationButton;

    private FloatingActionButton mTakePictureFab;
    private FloatingActionButton mSaveAssetFab;
    private FloatingActionButton mAddMoreFab;
    private FloatingActionButton mViewListFab;
    private FloatingActionButton mViewMapFab;
    private FloatingActionButton mViewMapLargeFab;

    private LinearLayout mViewMapFabLayout;
    private LinearLayout mViewListFabLayout;
    private LinearLayout mAddMoreFabLayout;

    private DatabaseCallTask databaseCallTask;
    private GoogleApiClient mGoogleApiClient;

    private boolean isNewAsset = true;
    private boolean inEditMode = true;
    private boolean fabMenuExpanded = false;

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

        mViewMapFabLayout = findViewById(R.id.assetViewMap_layout);
        mViewListFabLayout = findViewById(R.id.assetViewList_layout);
        mAddMoreFabLayout = findViewById(R.id.assetAddMore_layout);

        mCurrentLocationButton = findViewById(R.id.assetCurrentLocation_button);
        mSaveAssetFab = findViewById(R.id.assetSave_fab);
        mTakePictureFab = findViewById(R.id.assetTakePicture_fab);
        mAddMoreFab = findViewById(R.id.assetAddMore_fab);
        mViewListFab = findViewById(R.id.assetViewList_fab);
        mViewMapFab = findViewById(R.id.assetViewMap_fab);
        mViewMapLargeFab = findViewById(R.id.assetViewMapLarge_fab);


        Intent mapIntent = getIntent();
        double mapLat = mapIntent.getDoubleExtra(LAT, 0);
        double mapLon = mapIntent.getDoubleExtra(LNG, 0);
        Log.i("Map", " " + mapLon + "'");
        if(mapLat != 0 && mapLon != 0){
            mLatitudeEditText.setText(""+mapLat);
            mLongitudeEditText.setText(""+mapLon);
        }


        mTakePictureFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestAppPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_EXTERNAL_STORAGE);
            }
        });
        mCurrentLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(AddAssetActivity.this, mCurrentLocationButton);
                popupMenu.getMenuInflater().inflate(R.menu.get_location_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        // Disable coordinate inputs and make them blend into the background
                        mLatitudeEditText.setEnabled(false);
                        mLongitudeEditText.setEnabled(false);
                        mLatitudeEditText.setBackgroundColor(getResources().getColor(R.color.colorBackground));
                        mLongitudeEditText.setBackgroundColor(getResources().getColor(R.color.colorBackground));

                        switch (menuItem.getItemId()) {
                            case R.id.currentLocation_item:
                                requestAppPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MapActivity.PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
                                return true;
                            case R.id.addressLocation_item:
                                final EditText addressInput = new EditText(getApplicationContext());
                                addressInput.setInputType(InputType.TYPE_CLASS_TEXT);
                                addressInput.setTextColor(getResources().getColor(R.color.colorText));
                                final AlertDialog alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(AddAssetActivity.this, R.style.alertDialog))
                                        .setView(addressInput)
                                        .setTitle("Enter Address or Postal Code")
                                        .setPositiveButton("OK", null)
                                        .setNegativeButton("Cancel", null)
                                        .create();

                                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                    @Override
                                    public void onShow(DialogInterface dialogInterface) {
                                        Button okButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                                        okButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                Geocoder coder = new Geocoder(AddAssetActivity.this, Locale.getDefault());
                                                final List<Address> addresses;
                                                try {
                                                    String addressString = addressInput.getText().toString();
                                                    addresses = coder.getFromLocationName(addressString, 100);
                                                    if (!addresses.isEmpty()) {
                                                        if (addresses.size() > 1) {
                                                            // More than one result found. Alert the user
                                                            addressInput.setError("More than one location found. Please provide more detail");
                                                            addressInput.setText(addressString);
                                                        } else {
                                                            // Only one result found, grab the coordinates
                                                            Address location = addresses.get(0);
                                                            mLatitudeEditText.setText(String.valueOf(location.getLatitude()));
                                                            mLongitudeEditText.setText(String.valueOf(location.getLongitude()));
                                                            alertDialog.dismiss();
                                                        }
                                                    } else {
                                                        Toast.makeText(AddAssetActivity.this, "No address found", Toast.LENGTH_SHORT).show();
                                                    }
                                                } catch (IOException e) {
                                                    Toast.makeText(getApplicationContext(), "Unable to get location from address", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                });

                                alertDialog.show();

                                // TODO Deal with permissions
                                return true;
                            case R.id.mapLocation_item:
                                Asset asset = new Asset();
                                Intent intent = new Intent(AddAssetActivity.this, GetLocationMapsActivity.class);
                                // Store the current input fields so that they can be restored
                                if (mNameEditText.getTag() != null)
                                    asset.setId((long) mNameEditText.getTag());
                                if (mAssetPictureImageView.getTag() != null)
                                    asset.setImage((String) mAssetPictureImageView.getTag());
                                asset.setName(mNameEditText.getText().toString());
                                asset.setDescription(mDescriptionEditText.getText().toString());
                                asset.setNotes(mNotesEditText.getText().toString());

                                intent.putExtra(Asset.OBJECT_NAME, asset);
                                startActivity(intent);
                                return true;
                            case R.id.manualLocation_item:
                                // Enable the coordinate inputs and change colour to match other inputs
                                mLatitudeEditText.setEnabled(true);
                                mLongitudeEditText.setEnabled(true);
                                mLatitudeEditText.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                                mLongitudeEditText.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                popupMenu.show();
            }
        });
        mSaveAssetFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!inEditMode) {
                    mNameEditText.setEnabled(true);
                    mDescriptionEditText.setEnabled(true);
                    mNotesEditText.setEnabled(true);
                    mLatitudeEditText.setEnabled(true);
                    mLongitudeEditText.setEnabled(true);
                    mTakePictureFab.setVisibility(View.VISIBLE);
                    mCurrentLocationButton.setVisibility(View.VISIBLE);
                    mViewMapLargeFab.setVisibility(View.INVISIBLE);

                    mSaveAssetFab.setImageResource(android.R.drawable.ic_menu_save);
                    inEditMode = true;
                } else {
                    if (fabMenuExpanded) {
                        closeFabSubMenu();
                    } else {
                        openFabSubMenu();
                    }
                }
            }
        });
        mAddMoreFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Asset asset = saveAsset();

                if (asset == null) {
                    Toast.makeText(getApplicationContext(), "Unable to save asset", Toast.LENGTH_SHORT).show();
                } else {
                    // Close menu
                    closeFabSubMenu();

                    // Clear fields
                    mAssetPictureImageView.setImageResource(R.drawable.ic_default_asset_image);

                    mNameEditText.setText("");
                    mDescriptionEditText.setText("");
                    mNotesEditText.setText("");
                    mLatitudeEditText.setText("");
                    mLongitudeEditText.setText("");

                    mNameEditText.setTag("");
                    mAssetPictureImageView.setTag("");
                }
            }
        });
        mViewListFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Asset asset = saveAsset();

                if (asset == null) {
                    Toast.makeText(getApplicationContext(), "Unable to save asset", Toast.LENGTH_SHORT).show();
                } else {
                    // Open map activity
                    Intent intent = new Intent(AddAssetActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            }
        });
        mViewMapFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Asset asset = saveAsset();

                if (asset == null) {
                    Toast.makeText(getApplicationContext(), "Unable to save asset", Toast.LENGTH_SHORT).show();
                } else {
                    // Open map activity
                    Intent intent = new Intent(AddAssetActivity.this, MapActivity.class);
                    intent.putExtra(Asset.OBJECT_NAME, asset);
                    startActivity(intent);
                }
            }
        });
        mViewMapLargeFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Asset asset = saveAsset();

                if (asset == null) {
                    Toast.makeText(getApplicationContext(), "Unable to save asset", Toast.LENGTH_SHORT).show();
                } else {
                    // Open map activity
                    Intent intent = new Intent(AddAssetActivity.this, MapActivity.class);
                    intent.putExtra(Asset.OBJECT_NAME, asset);
                    startActivity(intent);
                }
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
            inEditMode = getIntent().getBooleanExtra(INTENT_NEW_ASSET, true);
            Asset asset = (Asset) getIntent().getExtras().get(Asset.OBJECT_NAME);
            if (asset.getImage() != null)
                mAssetPictureImageView.setImageBitmap(loadFromInternalStorage(asset.getImage()));

            mNameEditText.setText(asset.getName());
            mDescriptionEditText.setText(asset.getDescription());
            mNotesEditText.setText(asset.getNotes());
            mLatitudeEditText.setText(String.valueOf(asset.getLatitude()));
            mLongitudeEditText.setText(String.valueOf(asset.getLongitude()));

            mNameEditText.setTag(asset.getId());
            mAssetPictureImageView.setTag(asset.getImage());

            // Set the activity to edit mode
            if (!inEditMode) {
                isNewAsset = false;
                // Disable or hide objects
                mNameEditText.setEnabled(false);
                mDescriptionEditText.setEnabled(false);
                mNotesEditText.setEnabled(false);
                mTakePictureFab.setVisibility(View.INVISIBLE);
                mCurrentLocationButton.setVisibility(View.GONE);
                mViewMapLargeFab.setVisibility(View.VISIBLE);
                mSaveAssetFab.setImageResource(android.R.drawable.ic_menu_edit);
            }
        }

        // Hide the additional buttons initially
        closeFabSubMenu();
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

    private void openFabSubMenu() {
        mAddMoreFabLayout.setVisibility(View.VISIBLE);
        mViewListFabLayout.setVisibility(View.VISIBLE);
        mViewMapFabLayout.setVisibility(View.VISIBLE);
        fabMenuExpanded = true;
    }

    private void closeFabSubMenu() {
        mAddMoreFabLayout.setVisibility(View.INVISIBLE);
        mViewListFabLayout.setVisibility(View.INVISIBLE);
        mViewMapFabLayout.setVisibility(View.INVISIBLE);
        fabMenuExpanded = false;
    }

    private Asset saveAsset() {
        long id = 0;
        if (mNameEditText.getTag() != null)
            id = (long) mNameEditText.getTag();
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
            closeFabSubMenu();
            return null;
        }

        Asset asset = new Asset(id, name, description, notes, Double.parseDouble(latitude), Double.parseDouble(longitude), imagePath);

        if (isNewAsset) {
            databaseCallTask.execute(DatabaseCallTask.INSERT_ASSET, DatabaseCallTask.ADD_ASSET_ACTIVITY, asset);
        } else {
            // TODO Delete old image if a new one is added
            databaseCallTask.execute(DatabaseCallTask.UPDATE_ASSET, DatabaseCallTask.ADD_ASSET_ACTIVITY, asset);
        }

        return asset;
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
            Toast.makeText(getApplicationContext(), "Unable to load photo", Toast.LENGTH_SHORT).show();
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
        return;
    }
}
