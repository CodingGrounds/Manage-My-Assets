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
                    return new Object[]{caller, MMAConstants.DATABASE_SELECT_ASSETS, myDatabase.selectAssets()};
                case MMAConstants.DATABASE_SELECT_ASSET:
                    return new Object[]{caller,MMAConstants.DATABASE_SELECT_ASSET, myDatabase.selectAsset((LatLng) params[2])};
                case MMAConstants.DATABASE_INSERT_ASSET:
                    return new Object[]{caller,MMAConstants.DATABASE_INSERT_ASSET, myDatabase.insertAsset((Asset) params[2])};
                case MMAConstants.DATABASE_UPDATE_ASSET:
                    return new Object[]{caller, MMAConstants.DATABASE_UPDATE_ASSET, myDatabase.updateAsset((Asset) params[2])};
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
                case MMAConstants.ORIGIN_MAIN_ACTIVITY:
                    if((int) result[1] == MMAConstants.DATABASE_SELECT_ASSETS)
                        ((MainActivity) activity).databaseCallFinished((Asset[]) result[2]);
                    break;
                case MMAConstants.ORIGIN_MAP_ACTIVITY:
                    if((int) result[1] == MMAConstants.DATABASE_SELECT_ASSETS){
                        ((MapActivity) activity).databaseCallFinished((Asset[]) result[2]);
                    }
                    else if((int) result[1] == MMAConstants.DATABASE_SELECT_ASSET){
                        ((MapActivity) activity).databaseCallFinished((Asset) result[2]);
                    }
                    break;
                case MMAConstants.ORIGIN_ADD_ASSET_ACTIVITY:
                    break;
                default:
                    break;
            }
        }
    }
}
