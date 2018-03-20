package ca.unb.mobiledev.managemyassets;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class DetailsActivity extends AppCompatActivity {

    private ImageView mPictureImageView;
    private TextView mNameTextView;
    private TextView mDescriptionTextView;
    private TextView mNotesTextView;
    private TextView mLatitudeTextView;
    private TextView mLongitudeTextView;
    private Button mViewOnMapButton;
    private Asset asset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        asset = (Asset) getIntent().getSerializableExtra("asset");

        mPictureImageView = findViewById(R.id.assetImage_imageView);
        mNameTextView = findViewById(R.id.assetName_textView);
        mDescriptionTextView = findViewById(R.id.assetDescription_textView);
        mNotesTextView = findViewById(R.id.assetNotes_textView);
        mLatitudeTextView = findViewById(R.id.assetLatitude_textView);
        mLongitudeTextView = findViewById(R.id.assetLongitude_textView);

        mNameTextView.setText(asset.getName());
        mDescriptionTextView.setText(asset.getDescription());
        mNotesTextView.setText(asset.getNotes());
        mLatitudeTextView.setText(String.valueOf(asset.getLatitude()));
        mLongitudeTextView.setText(String.valueOf(asset.getLongitude()));

        mViewOnMapButton = findViewById(R.id.asset_viewOnMap_button);
        mViewOnMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DetailsActivity.this, MapActivity.class);
                intent.putExtra(Asset.OBJECT_NAME, asset);
                startActivity(intent);
            }
        });
    }
}
