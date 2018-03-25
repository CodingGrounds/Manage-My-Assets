package ca.unb.mobiledev.managemyassets;

import android.app.Activity;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import java.lang.ref.WeakReference;

/**
 * Created by laver on 2018-03-18.
 */

public class DatabaseCallTask extends AsyncTask<Object, Integer, Object[]> {

    public static final int MAIN_ACTIVITY = 1;
    public static final int ADD_ASSET_ACTIVITY = 2;
    public static final int MAP_ACTIVITY = 3;

    public static final int SELECT_ASSETS = 1;
    public static final int SELECT_ASSET = 2;
    public static final int INSERT_ASSET = 3;
    public static final int UPDATE_ASSET = 4;
    public static final int DELETE_ASSET = 5;

    /* Database helper object */
    private DatabaseHelper myDatabase;

    /* Activity calling the async task */
    private WeakReference<Activity> weakActivity;

    public DatabaseCallTask(Activity activity) {
        this.weakActivity = new WeakReference<>(activity);
    }

    @Override
    protected void onPreExecute() {
        Activity activity = weakActivity.get();
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            this.cancel(true);
            return;
        }

        myDatabase = DatabaseHelper.getDatabaseHelper(activity);
    }


    @Override
    protected Object[] doInBackground(Object... params) {
        if (params.length > 0) {
            int option = (int) params[0];
            int caller = (int) params[1];
            switch (option) {
                case SELECT_ASSETS:
                    return new Object[]{caller, myDatabase.selectAssets()};
                case SELECT_ASSET:
                    return new Object[]{caller, myDatabase.selectAsset((LatLng) params[2])};
                case INSERT_ASSET:
                    return new Object[]{caller, myDatabase.insertAsset((Asset) params[2])};
                case UPDATE_ASSET:
                    return new Object[]{caller, myDatabase.updateAsset((Asset) params[2])};
                default:
                    return new Object[0];
            }
        } else {
            return new Object[0];
        }
    }


    @Override
    protected void onPostExecute(Object[] result) {
        super.onPostExecute(result);

        Activity activity = weakActivity.get();
        if (activity == null || activity.isFinishing() || activity.isDestroyed())
            return;

        if (result.length > 0) {
            int caller = (int) result[0];
            switch (caller) {
                case MAIN_ACTIVITY:
                    ((MainActivity) activity).databaseCallFinished((Asset[]) result[1]);
                    break;
                case MAP_ACTIVITY:
                    ((MapActivity) activity).databaseCallFinished((Asset) result[1]);
                    break;
                case ADD_ASSET_ACTIVITY:
                    ((AddAssetActivity) activity).databaseCallFinished((Asset) result[1]);
                    break;
                default:
                    break;
            }
        }
    }
}
