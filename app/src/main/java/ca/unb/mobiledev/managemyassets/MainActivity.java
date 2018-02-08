package ca.unb.mobiledev.managemyassets;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseHelper = DatabaseHelper.getDatabaseHelper(MainActivity.this);

        databaseHelper.insertAsset(new Asset("fsdkjfhsdkjf", "jkfhkdsjhfs", 0, 0));
        databaseHelper.deleteAsset(new Asset("fsdkjfhsdkjf", "jkfhkdsjhfs", 0, 0));
    }
}
