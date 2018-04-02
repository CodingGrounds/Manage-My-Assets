package ca.unb.mobiledev.managemyassets;

import android.app.Activity;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import java.lang.ref.WeakReference;

/**
 * Created by laver on 2018-03-18.
 */

public class DatabaseCallTask extends AsyncTask<Object, Integer, Object[]> {

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
                case MMAConstants.DATABASE_SELECT_ASSETS:
                    return new Object[]{caller, myDatabase.selectAssets()};
                case MMAConstants.DATABASE_SELECT_ASSET:
                    return new Object[]{caller, myDatabase.selectAsset((LatLng) params[2])};
                case MMAConstants.DATABASE_INSERT_ASSET:
                    return new Object[]{caller, myDatabase.insertAsset((Asset) params[2])};
                case MMAConstants.DATABASE_UPDATE_ASSET:
                    return new Object[]{caller, myDatabase.updateAsset((Asset) params[2])};
                case MMAConstants.DATABASE_DELETE_ASSET:
                    return new Object[]{caller, myDatabase.deleteAsset((Asset) params[2])};
                default:
                    return new Object[0];
            }
        } else {
            return new Object[0];
        }
    }


    @Override
    protected void onPostExecute(Object... params) {
        super.onPostExecute(params);

        Activity activity = weakActivity.get();
        if (activity == null || activity.isFinishing() || activity.isDestroyed())
            return;

        if (params.length > 0) {
            int caller = (int) params[0];
            switch (caller) {
                case MMAConstants.ORIGIN_MAIN_ACTIVITY:
                    ((MainActivity) activity).databaseCallFinished((Asset[]) params[1]);
                    break;
                case MMAConstants.ORIGIN_MAP_ACTIVITY:
                    ((MapActivity) activity).databaseCallFinished((Asset) params[1]);
                    break;
                case MMAConstants.ORIGIN_ADD_ASSET_ACTIVITY:
                    break;
                default:
                    break;
            }
        }
    }
}
