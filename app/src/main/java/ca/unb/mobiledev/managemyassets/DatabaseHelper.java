package ca.unb.mobiledev.managemyassets;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Jason on 2018-01-30.
 */

class DatabaseHelper extends SQLiteOpenHelper {

    private static DatabaseHelper databaseHelper;

    private DatabaseHelper(Context context) {
        super(context, MMAConstants.DATABASE_NAME, null, MMAConstants.DATABASE_VERSION);
    }

    public static synchronized DatabaseHelper getDatabaseHelper(Context context) {
        if (databaseHelper == null) {
            databaseHelper = new DatabaseHelper(context);
        }
        return databaseHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        // Create database and all tables
        database.execSQL(MMAConstants.ASSET_CREATE_TABLE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        // Drop database and tables and recreate them
        database.execSQL(MMAConstants.ASSET_DROP_TABLE_QUERY);
        onCreate(database);
    }

    public Asset[] selectAssets() {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor;
        int assetCount;
        int arrayPosition;
        String query = "SELECT * FROM " + MMAConstants.ASSET_TABLE_NAME;

        cursor = database.rawQuery(query, null);
        assetCount = arrayPosition = cursor.getCount();

        Asset[] assets = new Asset[assetCount];

        if (cursor.moveToFirst()) {
            do {
                Asset asset = new Asset();
                asset.setId(cursor.getInt(0));
                asset.setName(cursor.getString(1));
                asset.setDescription(cursor.getString(2));
                asset.setNotes(cursor.getString(3));
                asset.setLatitude(cursor.getDouble(4));
                asset.setLongitude(cursor.getDouble(5));
                asset.setImage(cursor.getString(6));

                assets[assetCount - arrayPosition] = asset;
                arrayPosition--;
            } while (cursor.moveToNext());
        }

        database.close();
        cursor.close();

        return assets;
    }

    public Asset selectAsset(long id) {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor;
        Asset asset = null;

        String whereColumns = MMAConstants.ASSET_ID + " = ? ";
        String[] whereArgs = {String.valueOf(id)};
        cursor = database.query(MMAConstants.ASSET_TABLE_NAME, null, whereColumns, whereArgs, null, null, null);

        if (cursor.moveToFirst()) {
            asset = new Asset();
            asset.setId(cursor.getInt(0));
            asset.setName(cursor.getString(1));
            asset.setDescription(cursor.getString(2));
            asset.setNotes(cursor.getString(3));
            asset.setLatitude(cursor.getDouble(4));
            asset.setLongitude(cursor.getDouble(5));
            asset.setImage(cursor.getString(6));
        }

        database.close();
        cursor.close();

        return asset;
    }

    public Asset selectAsset(LatLng latLng) {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor;
        Asset asset = null;

        String whereColumns = MMAConstants.ASSET_LATITUDE + " = ? AND " + MMAConstants.ASSET_LONGITUDE + " = ? ";
        String[] whereArgs = {String.valueOf(latLng.latitude), String.valueOf(latLng.longitude)};
        cursor = database.query(MMAConstants.ASSET_TABLE_NAME, null, whereColumns, whereArgs, null, null, null);

        if (cursor.moveToFirst()) {
            asset = new Asset();
            asset.setId(cursor.getInt(0));
            asset.setName(cursor.getString(1));
            asset.setDescription(cursor.getString(2));
            asset.setNotes(cursor.getString(3));
            asset.setLatitude(cursor.getDouble(4));
            asset.setLongitude(cursor.getDouble(5));
            asset.setImage(cursor.getString(6));
        }

        database.close();
        cursor.close();

        return asset;
    }

    public Asset insertAsset(Asset asset) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(MMAConstants.ASSET_NAME, asset.getName());
        contentValues.put(MMAConstants.ASSET_DESCRIPTION, asset.getDescription());
        contentValues.put(MMAConstants.ASSET_NOTES, asset.getNotes());
        contentValues.put(MMAConstants.ASSET_LATITUDE, asset.getLatitude());
        contentValues.put(MMAConstants.ASSET_LONGITUDE, asset.getLongitude());
        contentValues.put(MMAConstants.ASSET_IMAGE, asset.getImage());

        long result = database.insert(MMAConstants.ASSET_TABLE_NAME, null, contentValues);
        database.close();

        return selectAsset(result);
    }

    public Asset updateAsset(Asset asset) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        String whereClause = "id = ?";
        String[] whereArgs = {String.valueOf(asset.getId())};

        contentValues.put(MMAConstants.ASSET_NAME, asset.getName());
        contentValues.put(MMAConstants.ASSET_DESCRIPTION, asset.getDescription());
        contentValues.put(MMAConstants.ASSET_NOTES, asset.getNotes());
        contentValues.put(MMAConstants.ASSET_LATITUDE, asset.getLatitude());
        contentValues.put(MMAConstants.ASSET_LONGITUDE, asset.getLongitude());
        contentValues.put(MMAConstants.ASSET_IMAGE, asset.getImage());

        long result = database.update(MMAConstants.ASSET_TABLE_NAME, contentValues, whereClause, whereArgs);
        database.close();

        if (result > 0)
            return selectAsset(asset.getId());
        else
            return asset;
    }

    public Asset[] deleteAsset(Asset asset) {
        SQLiteDatabase database = this.getWritableDatabase();

        String whereClause = "id = ?";
        String[] whereArgs = {String.valueOf(asset.getId())};

        database.delete(MMAConstants.ASSET_TABLE_NAME, whereClause, whereArgs);
        database.close();

        return selectAssets();
    }
}
