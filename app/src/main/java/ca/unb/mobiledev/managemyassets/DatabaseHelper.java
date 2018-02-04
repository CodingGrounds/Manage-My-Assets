package ca.unb.mobiledev.managemyassets;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Jason on 2018-01-30.
 */

class DatabaseHelper extends SQLiteOpenHelper {

    public DatabaseHelper(Context context, String databaseName, int databaseVersion) {
        super(context, databaseName, null, databaseVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        // Create database and all tables
        database.execSQL(Asset.getDatabaseSchema());
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        // Drop database and tables and recreate them
        database.execSQL(Asset.getDatabaseDropStatement());
        onCreate(database);
    }

    public Asset[] selectAssets() {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor;
        int assetCount, arrayPosition;
        String query = "SELECT * FROM asset";

        cursor = database.rawQuery(query, null);
        assetCount = arrayPosition = cursor.getCount();

        Asset[] assets = new Asset[assetCount];

        if (cursor.moveToFirst()) {
            do {
                Asset asset = new Asset();
                asset.setName(cursor.getString(1));
                asset.setDescription(cursor.getString(2));
                asset.setLatitude(cursor.getDouble(3));
                asset.setLongitude(cursor.getDouble(4));

                assets[assetCount - arrayPosition] = asset;
                arrayPosition--;
            } while (cursor.moveToNext());
        }

        database.close();
        cursor.close();

        return  assets;
    }

    public boolean insertAsset(Asset asset) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put("name", asset.getName());
        contentValues.put("description", asset.getDescription());
        contentValues.put("latitude", asset.getLatitude());
        contentValues.put("longitude", asset.getLongitude());

        long result = database.insert("asset", null, contentValues);
        database.close();

        return result != -1;
    }

    public boolean updateAsset(Asset asset) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        String whereClause = "name = ? AND latitude = ? AND longitude = ?";
        String[] whereArgs = {asset.getName(), String.valueOf(asset.getLatitude()), String.valueOf(asset.getLongitude())};

        contentValues.put("name", asset.getName());
        contentValues.put("description", asset.getDescription());
        contentValues.put("latitude", asset.getLatitude());
        contentValues.put("longitude", asset.getLongitude());

        long result = database.update("asset", contentValues, whereClause, whereArgs);
        database.close();

        return result != -1;
    }

    public boolean deleteAsset(Asset asset) {
        SQLiteDatabase database = this.getWritableDatabase();

        String whereClause = "name = ? AND latitude = ? AND longitude = ?";
        String[] whereArgs = {asset.getName(), String.valueOf(asset.getLatitude()), String.valueOf(asset.getLongitude())};

        long result = database.delete("asset", whereClause, whereArgs);
        database.close();

        return result != -1;
    }
}
