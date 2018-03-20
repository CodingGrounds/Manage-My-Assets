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
    private static final String DATABASE_NAME = "ManageMyAssets.db";
    private static final int DATABASE_VERSION = 2;

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
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
        database.execSQL(Asset.CREATE_TABLE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        // Drop database and tables and recreate them
        database.execSQL(Asset.DROP_TABLE_QUERY);
        onCreate(database);
    }

    public Asset[] selectAssets() {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor;
        int assetCount, arrayPosition;
        String query = "SELECT * FROM " + Asset.TABLE_NAME;

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

                assets[assetCount - arrayPosition] = asset;
                arrayPosition--;
            } while (cursor.moveToNext());
        }

        database.close();
        cursor.close();

        return assets;
    }

    public Asset selectAsset(LatLng latLng) {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor;
        Asset asset = null;

        String whereColumns = Asset.LAT + " = ? AND " + Asset.LNG + " = ? ";
        String[] whereArgs = {String.valueOf(latLng.latitude), String.valueOf(latLng.longitude)};
        cursor = database.query(Asset.TABLE_NAME, null, whereColumns, whereArgs, null, null, null);

        if (cursor.moveToFirst()) {
            asset = new Asset();
            asset.setId(cursor.getInt(0));
            asset.setName(cursor.getString(1));
            asset.setDescription(cursor.getString(2));
            asset.setNotes(cursor.getString(3));
            asset.setLatitude(cursor.getDouble(4));
            asset.setLongitude(cursor.getDouble(5));
        }

        database.close();
        cursor.close();

        return asset;
    }

    public boolean insertAsset(Asset asset) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put("name", asset.getName());
        contentValues.put("description", asset.getDescription());
        contentValues.put("notes", asset.getNotes());
        contentValues.put("latitude", asset.getLatitude());
        contentValues.put("longitude", asset.getLongitude());

        long result = database.insert(Asset.TABLE_NAME, null, contentValues);
        database.close();

        return result != -1;
    }

    // TODO This doesn't work that great. Fix it sometime
    public boolean updateAsset(Asset asset) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        String whereClause = "latitude = ? AND longitude = ?";
        String[] whereArgs = {asset.getName(), String.valueOf(asset.getLatitude()), String.valueOf(asset.getLongitude())};

        contentValues.put("name", asset.getName());
        contentValues.put("description", asset.getDescription());
        contentValues.put("latitude", asset.getLatitude());
        contentValues.put("longitude", asset.getLongitude());

        long result = database.update(Asset.TABLE_NAME, contentValues, whereClause, whereArgs);
        database.close();

        return result != -1;
    }

    public boolean deleteAsset(Asset asset) {
        SQLiteDatabase database = this.getWritableDatabase();

        String whereClause = "name = ? AND latitude = ? AND longitude = ?";
        String[] whereArgs = {asset.getName(), String.valueOf(asset.getLatitude()), String.valueOf(asset.getLongitude())};

        long result = database.delete(Asset.TABLE_NAME, whereClause, whereArgs);
        database.close();

        return result != -1;
    }
}
