package ca.unb.mobiledev.managemyassets;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;


import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter assetAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private DatabaseHelper databaseHelper;
    private ArrayList<Asset> assetList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TODO Temp Button will need to remove
        Button button = (Button) findViewById(R.id.button_send);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent mapIntent = new Intent(MainActivity.this, MapActivity.class);
                startActivity(mapIntent);
            }
        });

        // Initialize variables
        databaseHelper = DatabaseHelper.getDatabaseHelper(MainActivity.this);

        // Populate database with test data
        databaseHelper.insertAsset(new Asset("Test point 1", "This is a test point", 150, -150));
        databaseHelper.insertAsset(new Asset("Test point 2", "This is a test point", 250, -250));
        databaseHelper.insertAsset(new Asset("Test point 3", "This is a test point", 350, -350));
        databaseHelper.insertAsset(new Asset("Test point 4", "This is a test point", 450, -450));

        assetList = new ArrayList<>(Arrays.asList(databaseHelper.selectAssets()));

        recyclerView = findViewById(R.id.asset_recycler_view);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        assetAdapter = new AssetAdapter();
        recyclerView.setAdapter(assetAdapter);
    }

    public class AssetAdapter extends RecyclerView.Adapter<AssetAdapter.ViewHolder> {

        public AssetAdapter() {
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView view = (TextView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.asset_layout, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int position) {

            viewHolder.asset = assetList.get(position);
            viewHolder.mAssetTextView.setText(Html.fromHtml(assetList.get(position).toString()));
            viewHolder.mAssetTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // TODO Do something when an item is clicked
                    Toast.makeText(MainActivity.this, "Item clicked", Toast.LENGTH_SHORT).show();
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
