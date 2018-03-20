package ca.unb.mobiledev.managemyassets;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by laver on 2018-03-18.
 */

public class DatabaseCallTask extends AsyncTask<Object, Integer, Object[]> {

    /* Database helper object */
    private static DatabaseHelper myDatabase;

    /* Activity calling the async task */
    private Activity activity;

    public DatabaseCallTask(Activity activity){
        this.activity = activity;
    }

    @Override
    protected void onPreExecute() {
        myDatabase = MainActivity.databaseHelper;
    }


    @Override
    protected Object[] doInBackground(Object... params) {
        Log.i("Background", "Working bg");
        if(params.length > 0){
            String option = (String) params[0];
            switch(option){
                case "SELECT ASSETS":
                    return new Object[]{option, myDatabase.selectAssets()};
                case "SELECT BY LatLng":
                    return new Object[]{option, myDatabase.selectAsset((LatLng)params[1])};
                case "INSERT ASSET":
                    return new Object[]{option, myDatabase.insertAsset((Asset)params[1])};
            }
        }
        return null;
    }


    @Override
    protected void onPostExecute(Object[] result) {
        super.onPostExecute(result);

        if(result.length > 0){
            Log.i("Post", "Done Post");
            String option = (String) result[0];
            switch(option){
                case "SELECT ASSETS":
                    Toast.makeText( activity, "LIST UPDATED", Toast.LENGTH_LONG).show();
                    this.cancel(true);

                    ((MainActivity) activity).displayAssets((Asset[])result[1]) ;
                    break;
                case "INSERT ASSET":
                    Toast.makeText(activity, "Asset Added", Toast.LENGTH_LONG).show();
                    ((AddAssetActivity) activity).displayNewAsset();
                    break;
                case "SELECT BY LatLng":
                    Toast.makeText(activity, "Asset Details", Toast.LENGTH_LONG).show();
                    ((MapActivity)activity).displayDetailsView((Asset) result[1]);
                    break;
            }
        }
    }
}
