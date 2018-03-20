package ca.unb.mobiledev.managemyassets;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter assetAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private FloatingActionButton mAddAssetFab;
    private DatabaseCallTask databaseCallTask;
    private ArrayList<Asset> assetList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.asset_viewMap_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent mapIntent = new Intent(MainActivity.this, MapActivity.class);
                startActivity(mapIntent);
            }
        });

        // Initialize variables
        databaseCallTask = new DatabaseCallTask(this);
        assetList = new ArrayList<>();

        recyclerView = findViewById(R.id.asset_recycler_view);
        layoutManager = new LinearLayoutManager(this);
        assetAdapter = new AssetAdapter();

        mAddAssetFab = findViewById(R.id.assetAdd_fab);
        mAddAssetFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddAssetActivity.class);
                startActivity(intent);
            }
        });

//          Populate database with test data
//          databaseHelper.insertAsset(new Asset("UNB", "This place sucks", 45.944569, -66.641527 ));
//          databaseHelper.insertAsset(new Asset("North Side", "This place is the worst", 45.979458, -66.655975));
//          databaseHelper.insertAsset(new Asset("South Side", "Up Towns nice", 45.939981, -66.666241));
//          databaseHelper.insertAsset(new Asset("Harvey", "Land of the free, hope of the brave", 45.736118, -66.997903));

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(assetAdapter);

        databaseCallTask.execute(DatabaseCallTask.SELECT_ASSETS, null);
    }

    public void databaseCallFinished(Asset[] assets) {
        assetList = new ArrayList<>(Arrays.asList(assets));
        assetAdapter.notifyDataSetChanged();
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
