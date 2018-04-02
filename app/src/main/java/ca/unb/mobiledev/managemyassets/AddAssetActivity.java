package ca.unb.mobiledev.managemyassets;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddAssetActivity extends AppCompatActivity {

    private EditText mNameEditText;
    private EditText mDescriptionEditText;
    private EditText mNotesEditText;
    private EditText mLatitudeEditText;
    private EditText mLongitudeEditText;
    private ImageView mAssetPictureImageView;
    private Button mCurrentLocationButton;

    private FloatingActionButton mSaveAssetFab;
    private FloatingActionButton mViewMapLargeFab;

    private LinearLayout mViewMapFabLayout;
    private LinearLayout mViewListFabLayout;
    private LinearLayout mAddMoreFabLayout;

    private DatabaseCallTask databaseCallTask;

    private boolean mIsNewAsset = true;
    private boolean mInEditMode = true;
    private boolean mFabMenuExpanded = false;
    private int mFileOrigin = 0;

    /* Overridden functions */

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
        mViewMapLargeFab = findViewById(R.id.assetViewMapLarge_fab);

        FloatingActionButton mAddMoreFab = findViewById(R.id.assetAddMore_fab);
        FloatingActionButton mViewListFab = findViewById(R.id.assetViewList_fab);
        FloatingActionButton mViewMapFab = findViewById(R.id.assetViewMap_fab);

        // Set click listeners
        mAssetPictureImageView.setClickable(true);
        mAssetPictureImageView.setOnClickListener(mAssetPictureImageViewClickListener);
        mCurrentLocationButton.setOnClickListener(mCurrentLocationButtonClickListener);
        mSaveAssetFab.setOnClickListener(mSaveAssetFabClickListener);
        mViewMapLargeFab.setOnClickListener(mFabClickListener);
        mAddMoreFab.setOnClickListener(mFabClickListener);
        mViewListFab.setOnClickListener(mFabClickListener);
        mViewMapFab.setOnClickListener(mFabClickListener);

        // Populate the fields if data was passed in the intent
        if (getIntent().getExtras() != null) {
            mInEditMode = getIntent().getBooleanExtra(MMAConstants.INTENT_NEW_ASSET, true);
            Asset asset = (Asset) getIntent().getExtras().get(MMAConstants.ASSET_OBJECT_NAME);

            if (asset != null) {
                if (asset.getImage() != null)
                    mAssetPictureImageView.setImageBitmap(loadFromInternalStorage(asset.getImage()));

                mNameEditText.setText(asset.getName());
                mDescriptionEditText.setText(asset.getDescription());
                mNotesEditText.setText(asset.getNotes());
                mLatitudeEditText.setText(String.valueOf(asset.getLatitude()));
                mLongitudeEditText.setText(String.valueOf(asset.getLongitude()));

                mNameEditText.setTag(asset.getId());
                mAssetPictureImageView.setTag(asset.getImage());
            }

            // Set the activity to edit mode
            if (!mInEditMode) {
                mIsNewAsset = false;
                toggleEditMode(false);
            }
        }

        // Hide the additional buttons initially
        closeFabSubMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mIsNewAsset) {
            MenuInflater menuInflater = getMenuInflater();
            menuInflater.inflate(R.menu.add_asset_menu, menu);
            return true;
        } else {
            return super.onCreateOptionsMenu(menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.assetDelete_action:
                AlertDialog alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(AddAssetActivity.this, R.style.alertDialog))
                        .setTitle(getString(R.string.asset_delete))
                        .setMessage(getString(R.string.asset_delete_confirm))
                        .setNegativeButton(getString(R.string.input_button_no), null)
                        .setPositiveButton(getString(R.string.input_button_yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Asset asset = getViewData();
                                databaseCallTask.execute(MMAConstants.DATABASE_DELETE_ASSET, MMAConstants.ORIGIN_ADD_ASSET_ACTIVITY, asset);
                            }
                        })
                        .create();
                alertDialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == MMAConstants.REQUEST_PERMISSION_ACCESS_FINE_LOCATION) {
                getDeviceLocation();
            } else if (requestCode == MMAConstants.REQUEST_PERMISSION_EXTERNAL_STORAGE) {
                saveImageFile();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MMAConstants.REQUEST_INTENT_GATHER_IMAGE && resultCode == RESULT_OK && data != null) {
            Bitmap imageBitmap = null;

            if (mFileOrigin == MMAConstants.PICTURE_ORIGIN_CAMERA && data.getExtras() != null) {
                imageBitmap = (Bitmap) data.getExtras().get("data");
            }
            if (mFileOrigin == MMAConstants.PICTURE_ORIGIN_GALLERY && data.getData() != null) {
                Uri imageUri = data.getData();
                try {
                    InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    imageBitmap = BitmapFactory.decodeStream(imageStream);
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), getString(R.string.error_photo_load_failed), Toast.LENGTH_SHORT).show();
                }
            }

            if (imageBitmap != null) {
                String oldFilePath = (String) mAssetPictureImageView.getTag();
                String filePath = saveToInternalStorage(imageBitmap);

                // Delete existing file if there is one
                if (oldFilePath != null) {
                    String[] filePathParts = oldFilePath.split("/");
                    getApplicationContext().deleteFile(filePathParts[filePathParts.length - 1]);
                }

                // Set the ImageView to show the new file
                mAssetPictureImageView.setImageBitmap(imageBitmap);
                mAssetPictureImageView.setTag(filePath);
            }
        }
    }

    /* UI Functions */

    private void openFabSubMenu() {
        mAddMoreFabLayout.setVisibility(View.VISIBLE);
        mViewListFabLayout.setVisibility(View.VISIBLE);
        mViewMapFabLayout.setVisibility(View.VISIBLE);
        mFabMenuExpanded = true;
    }

    private void closeFabSubMenu() {
        mAddMoreFabLayout.setVisibility(View.INVISIBLE);
        mViewListFabLayout.setVisibility(View.INVISIBLE);
        mViewMapFabLayout.setVisibility(View.INVISIBLE);
        mFabMenuExpanded = false;
    }

    private void resetFields() {
        mAssetPictureImageView.setImageResource(R.drawable.ic_default_asset_image);
        mIsNewAsset = true;

        mNameEditText.setText(null);
        mDescriptionEditText.setText(null);
        mNotesEditText.setText(null);
        mLatitudeEditText.setText(null);
        mLongitudeEditText.setText(null);

        mNameEditText.setTag(null);
        mAssetPictureImageView.setTag(null);
    }

    private void toggleEditMode(boolean enterEditMode) {
        if (enterEditMode) {
            mNameEditText.setEnabled(true);
            mDescriptionEditText.setEnabled(true);
            mNotesEditText.setEnabled(true);
            mAssetPictureImageView.setClickable(true);
            mCurrentLocationButton.setVisibility(View.VISIBLE);
            mViewMapLargeFab.setVisibility(View.GONE);
            mSaveAssetFab.setImageResource(android.R.drawable.ic_menu_save);
        } else {
            mNameEditText.setEnabled(false);
            mDescriptionEditText.setEnabled(false);
            mNotesEditText.setEnabled(false);
            mAssetPictureImageView.setClickable(false);
            mCurrentLocationButton.setVisibility(View.GONE);
            mViewMapLargeFab.setVisibility(View.VISIBLE);
            mSaveAssetFab.setImageResource(android.R.drawable.ic_menu_edit);
        }
        invalidateOptionsMenu();
    }

    public void databaseCallFinished() {
        Intent detailsIntent = new Intent(AddAssetActivity.this, MainActivity.class);
        startActivity(detailsIntent);
    }

    /* Data functions */

    @Nullable
    private Asset getViewData() {
        long id = 0;
        if (mNameEditText.getTag() != null)
            id = (long) mNameEditText.getTag();
        String name = mNameEditText.getText().toString();
        String description = mDescriptionEditText.getText().toString();
        String notes = mNotesEditText.getText().toString();
        String latitude = mLatitudeEditText.getText().toString();
        String longitude = mLongitudeEditText.getText().toString();
        String imagePath = (String) mAssetPictureImageView.getTag();

        // Make sure the required fields are filled
        if (TextUtils.isEmpty(name)) {
            mNameEditText.setError(getString(R.string.required_asset_name));
        }
        if (TextUtils.isEmpty(latitude) || TextUtils.isEmpty(longitude)) {
            mCurrentLocationButton.setError(getString(R.string.required_asset_location));
        }

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(latitude) || TextUtils.isEmpty(longitude)) {
            // Don't attempt to store anything if the correct fields aren't provided
            closeFabSubMenu();
            return null;
        }

        // Id gets set even if null but only gets used for an update; when it would actually have a value
        return new Asset(id, name, description, notes, Double.parseDouble(latitude), Double.parseDouble(longitude), imagePath);
    }

    @Nullable
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
            Toast.makeText(getApplicationContext(), getString(R.string.error_photo_save_failed), Toast.LENGTH_SHORT).show();
        }

        return filePath;
    }

    @Nullable
    private Bitmap loadFromInternalStorage(String filePath) {
        Bitmap bitmap = null;

        try {
            File imageFile = new File(filePath);
            bitmap = BitmapFactory.decodeStream(new FileInputStream(imageFile));
        } catch (FileNotFoundException e) {
            Toast.makeText(getApplicationContext(), getString(R.string.error_photo_load_failed), Toast.LENGTH_SHORT).show();
        }

        return bitmap;
    }

    /* Helper functions */

    /**
     * Attempts to request permissions from the user.
     *
     * @param permissions         A String array of permissions to check
     * @param permissionRequestId A unique id to identify the permission request call
     */
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

    /**
     * Attempts to get the device's current location. Updates the latitude and longitude fields if successful
     */
    private void getDeviceLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            String provider = mLocationManager.getBestProvider(new Criteria(), true);
            Location lastLocation = mLocationManager.getLastKnownLocation(provider);
            if (lastLocation != null) {
                mLatitudeEditText.setText(String.valueOf(lastLocation.getLatitude()));
                mLongitudeEditText.setText(String.valueOf(lastLocation.getLongitude()));
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.error_location_device_failed), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Attempts to create and start an intent to get the user's desired picture.
     */
    private void saveImageFile() {
        Intent intent = null;

        switch (mFileOrigin) {
            case MMAConstants.PICTURE_ORIGIN_CAMERA:
                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                break;
            case MMAConstants.PICTURE_ORIGIN_GALLERY:
                intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                break;
            default:
                break;
        }

        if (intent != null && intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, MMAConstants.REQUEST_INTENT_GATHER_IMAGE);
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.error_photo_source_failed), Toast.LENGTH_SHORT).show();
        }
    }

    /* Click Listeners */

    private final View.OnClickListener mAssetPictureImageViewClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            PopupMenu popupMenu = new PopupMenu(AddAssetActivity.this, mAssetPictureImageView);
            popupMenu.getMenuInflater().inflate(R.menu.get_picture_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case R.id.pictureCamera_item:
                            mFileOrigin = MMAConstants.PICTURE_ORIGIN_CAMERA;
                            break;
                        case R.id.pictureGallery_item:
                            mFileOrigin = MMAConstants.PICTURE_ORIGIN_GALLERY;
                            break;
                        default:
                            break;
                    }
                    requestAppPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, MMAConstants.REQUEST_PERMISSION_EXTERNAL_STORAGE);
                    return true;
                }
            });
            popupMenu.show();
        }
    };

    private final View.OnClickListener mCurrentLocationButtonClickListener = new View.OnClickListener() {
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
                            // Get the current device location
                            requestAppPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MMAConstants.REQUEST_PERMISSION_ACCESS_FINE_LOCATION);
                            return true;
                        case R.id.addressLocation_item:
                            // Create a popup window for the user to enter an address or postal code into
                            final EditText addressInput = new EditText(getApplicationContext());
                            addressInput.setInputType(InputType.TYPE_CLASS_TEXT);
                            addressInput.setTextColor(getResources().getColor(R.color.colorText));
                            final AlertDialog alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(AddAssetActivity.this, R.style.alertDialog))
                                    .setView(addressInput)
                                    .setTitle(getString(R.string.input_location_address))
                                    .setPositiveButton(getString(R.string.input_button_ok), null)
                                    .setNegativeButton(getString(R.string.input_button_cancel), null)
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
                                                        addressInput.setError(getString(R.string.error_location_address_ambiguous));
                                                        addressInput.setText(addressString);
                                                    } else {
                                                        // Only one result found, grab the coordinates
                                                        Address location = addresses.get(0);
                                                        mLatitudeEditText.setText(String.valueOf(location.getLatitude()));
                                                        mLongitudeEditText.setText(String.valueOf(location.getLongitude()));
                                                        alertDialog.dismiss();
                                                    }
                                                }
                                            } catch (IOException e) {
                                                Toast.makeText(getApplicationContext(), getString(R.string.error_location_address_failed), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            });

                            alertDialog.show();
                            return true;
                        case R.id.mapLocation_item:
                            // Open a map activity for the user to click a location on
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

                            intent.putExtra(MMAConstants.ASSET_OBJECT_NAME, asset);
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
    };

    private final View.OnClickListener mSaveAssetFabClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (!mInEditMode) {
                toggleEditMode(true);
                mInEditMode = true;
            } else {
                if (mFabMenuExpanded) {
                    closeFabSubMenu();
                } else {
                    openFabSubMenu();
                }
            }
        }
    };

    private final View.OnClickListener mFabClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Asset asset = getViewData();

            if (asset == null) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_asset_save_failed), Toast.LENGTH_SHORT).show();
            } else {
                // Close menu
                closeFabSubMenu();
                if (mIsNewAsset) {
                    databaseCallTask.execute(MMAConstants.DATABASE_INSERT_ASSET, MMAConstants.ORIGIN_ADD_ASSET_ACTIVITY, asset);
                } else {
                    databaseCallTask.execute(MMAConstants.DATABASE_UPDATE_ASSET, MMAConstants.ORIGIN_ADD_ASSET_ACTIVITY, asset);
                }

                // Determine which fab was clicked
                switch (view.getId()) {
                    case R.id.assetAddMore_fab:
                        // Reset fields for another asset
                        resetFields();
                        break;
                    case R.id.assetViewList_fab:
                        // Return to the main list of assets
                        Intent returnToMainListIntent = new Intent(AddAssetActivity.this, MainActivity.class);
                        startActivity(returnToMainListIntent);
                        break;
                    case R.id.assetViewMap_fab:
                    case R.id.assetViewMapLarge_fab:
                        // Open the map view centered on this asset
                        Intent viewOnMapIntent = new Intent(AddAssetActivity.this, MapActivity.class);
                        viewOnMapIntent.putExtra(MMAConstants.ASSET_OBJECT_NAME, asset);
                        startActivity(viewOnMapIntent);
                        break;
                    default:
                        Toast.makeText(getApplicationContext(), getString(R.string.error_default_case_needed), Toast.LENGTH_LONG).show();
                        break;
                }
            }
        }
    };
}
