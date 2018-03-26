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
    private static final int DATABASE_VERSION = 3;

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

        String whereColumns = Asset.ID + " = ? ";
        String[] whereArgs = {String.valueOf(id)};
        cursor = database.query(Asset.TABLE_NAME, null, whereColumns, whereArgs, null, null, null);

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
            asset.setImage(cursor.getString(6));
        }

        database.close();
        cursor.close();

        return asset;
    }

    public Asset insertAsset(Asset asset) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(Asset.NAME, asset.getName());
        contentValues.put(Asset.DESCRIPTION, asset.getDescription());
        contentValues.put(Asset.NOTES, asset.getNotes());
        contentValues.put(Asset.LAT, asset.getLatitude());
        contentValues.put(Asset.LNG, asset.getLongitude());
        contentValues.put(Asset.IMAGE, asset.getImage());

        long result = database.insert(Asset.TABLE_NAME, null, contentValues);
        database.close();

        return selectAsset(result);
    }

    public Asset updateAsset(Asset asset) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        String whereClause = "id = ?";
        String[] whereArgs = {String.valueOf(asset.getId())};

        contentValues.put(Asset.NAME, asset.getName());
        contentValues.put(Asset.DESCRIPTION, asset.getDescription());
        contentValues.put(Asset.NOTES, asset.getNotes());
        contentValues.put(Asset.LAT, asset.getLatitude());
        contentValues.put(Asset.LNG, asset.getLongitude());
        contentValues.put(Asset.IMAGE, asset.getImage());

        long result = database.update(Asset.TABLE_NAME, contentValues, whereClause, whereArgs);
        database.close();

        if (result > 0)
            return selectAsset(asset.getId());
        else
            return asset;
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
