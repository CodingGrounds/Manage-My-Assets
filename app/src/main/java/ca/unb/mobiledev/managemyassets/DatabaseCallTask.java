package ca.unb.mobiledev.managemyassets;

import android.app.Activity;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import java.lang.ref.WeakReference;

/**
 * Created by laver on 2018-03-18.
 */

public class DatabaseCallTask extends AsyncTask<Object, Integer, Object[]> {

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
            switch (option) {
                case SELECT_ASSETS:
                    return new Object[]{option, myDatabase.selectAssets()};
                case SELECT_ASSET:
                    return new Object[]{option, myDatabase.selectAsset((LatLng) params[1])};
                case INSERT_ASSET:
                    return new Object[]{option, myDatabase.insertAsset((Asset) params[1])};
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
            int option = (int) result[0];
            switch (option) {
                case SELECT_ASSETS:
                    ((MainActivity) activity).databaseCallFinished((Asset[]) result[1]);
                    break;
                case SELECT_ASSET:
                    ((MapActivity) activity).databaseCallFinished((Asset) result[1]);
                    break;
                case INSERT_ASSET:
                    ((AddAssetActivity) activity).databaseCallFinished((Asset) result[1]);
                    break;
                default:
                    break;
            }
        }
    }
}
