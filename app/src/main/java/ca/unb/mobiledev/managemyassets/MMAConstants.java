package ca.unb.mobiledev.managemyassets;

/**
 * Created by Jason on 2018-03-31.
 */

public class MMAConstants {

    /* Identifiers for activities */
    public static final int ORIGIN_MAIN_ACTIVITY = 1;
    public static final int ORIGIN_ADD_ASSET_ACTIVITY = 2;
    public static final int ORIGIN_MAP_ACTIVITY = 3;

    /* Identifiers for requests */
    public static final int REQUEST_PERMISSION_ACCESS_FINE_LOCATION = 1;
    public static final int REQUEST_PERMISSION_EXTERNAL_STORAGE = 2;
    public static final int REQUEST_INTENT_GATHER_IMAGE = 1;

    /* Identifiers for how to get a picture for an asset */
    public static final int PICTURE_ORIGIN_CAMERA = 1;
    public static final int PICTURE_ORIGIN_GALLERY = 2;

    /* Identifiers for asset variables */
    public static final String ASSET_OBJECT_NAME = "asset";
    public static final String ASSET_ID = "id";
    public static final String ASSET_NAME = "name";
    public static final String ASSET_DESCRIPTION = "description";
    public static final String ASSET_NOTES = "notes";
    public static final String ASSET_LATITUDE = "latitude";
    public static final String ASSET_LONGITUDE = "longitude";
    public static final String ASSET_IMAGE = "image";

    /* Identifiers for database requests */
    public static final int DATABASE_SELECT_ASSETS = 1;
    public static final int DATABASE_SELECT_ASSET = 2;
    public static final int DATABASE_INSERT_ASSET = 3;
    public static final int DATABASE_UPDATE_ASSET = 4;
    public static final int DATABASE_DELETE_ASSET = 5;

    /* Database constants */
    public static final String DATABASE_NAME = "ManageMyAssets.db";
    public static final int DATABASE_VERSION = 3;

    /* Database table constants */
    public static final String ASSET_TABLE_NAME = "assets";
    public static final String ASSET_DROP_TABLE_QUERY = "DROP TABLE IF EXISTS assets;";
    public static final String ASSET_CREATE_TABLE_QUERY = "CREATE TABLE assets (id INTEGER PRIMARY KEY, name TEXT, description TEXT, notes TEXT, latitude REAL, longitude REAL, image TEXT);";

    /* Miscellaneous constants */
    public static final String INTENT_NEW_ASSET = "edit_mode";

    private MMAConstants() {}
}
