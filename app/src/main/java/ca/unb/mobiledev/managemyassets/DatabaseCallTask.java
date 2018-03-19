package ca.unb.mobiledev.managemyassets;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by laver on 2018-03-18.
 */

public class DatabaseCallTask extends AsyncTask<String, Integer, Asset[]> {

    /* Database helper object */
    private static DatabaseHelper myDatabase;

    /* Activity calling the async task */
    private MainActivity activity;

    public DatabaseCallTask(MainActivity activity){
        this.activity = activity;
    }

    @Override
    protected void onPreExecute() {

        myDatabase = MainActivity.databaseHelper;

    }

    //TODO GREAT CRUD Update
    @Override
    protected Asset[] doInBackground(String... strings) {
        String option;
        Log.i("Background", "Working bg");
        if(strings.length > 0){
            option = strings[0];
            switch(option){
                case "SELECT ASSETS":
                    return myDatabase.selectAssets();
                case "SELECT ASSET":

                    break;

            }
        }

        return null;
    }


    @Override
    protected void onPostExecute(Asset[] assets) {
        super.onPostExecute(assets);
        Log.i("Post", "Done Post");
        Toast.makeText(activity, "LIST UPDATED", Toast.LENGTH_LONG).show();
        this.cancel(true);
        activity.displayAssets(assets);
    }
}
