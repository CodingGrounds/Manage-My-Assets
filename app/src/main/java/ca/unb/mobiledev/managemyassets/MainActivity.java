package ca.unb.mobiledev.managemyassets;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter assetAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private DatabaseHelper databaseHelper;
    private ArrayList<Asset> assetList;

    private FusedLocationProviderClient mFusedLocationClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.button_send);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent mapIntent = new Intent(MainActivity.this, MapActivity.class);
                startActivity(mapIntent);
            }
        });

        // Initialize variables
        databaseHelper = DatabaseHelper.getDatabaseHelper(MainActivity.this);

        // Populate database with test data
//          databaseHelper.insertAsset(new Asset("UNB", "This place sucks", 45.944569, -66.641527 ));
//          databaseHelper.insertAsset(new Asset("North Side", "This place is the worst", 45.979458, -66.655975));
//          databaseHelper.insertAsset(new Asset("South Side", "Up Towns nice", 45.939981, -66.666241));
//          databaseHelper.insertAsset(new Asset("Harvey", "Land of the free, hope of the brave", 45.736118, -66.997903));

        assetList = new ArrayList<>(Arrays.asList(databaseHelper.selectAssets()));

        recyclerView = findViewById(R.id.asset_recycler_view);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        assetAdapter = new AssetAdapter();
        recyclerView.setAdapter(assetAdapter);

        GetLocation mGetLocation = new GetLocation();

        final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

        // No explanation needed; request the permission
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                MY_PERMISSIONS_REQUEST_LOCATION);

        Location location = mGetLocation.getDeviceLocation(this);
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();
    }

    public class AssetAdapter extends RecyclerView.Adapter<AssetAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView view = (TextView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.asset_layout, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder viewHolder, int position) {

            viewHolder.asset = assetList.get(position);
            viewHolder.mAssetTextView.setText(Html.fromHtml(assetList.get(position).toString()));
            viewHolder.mAssetTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
                    intent.putExtra(Asset.OBJECT_NAME, viewHolder.asset);
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return assetList.size();
        }

        /*
            This class manages the  individual 'tiles' that are displayed in the RecyclerView
         */
        public class ViewHolder extends RecyclerView.ViewHolder {

            public TextView mAssetTextView;
            public Asset asset;

            public ViewHolder(TextView view) {
                super(view);
                mAssetTextView = view;
            }
        }
    }


}
