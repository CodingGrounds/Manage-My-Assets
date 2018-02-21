package ca.unb.mobiledev.managemyassets;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class DetailsActivity extends AppCompatActivity {

    public static final String NAME = "asset_name";
    public static final String DESCRIPTION = "asset_description";
    public static final String LNG = "asset_longitutde";
    public static final String LAT = "asset_latitude";

    private ImageView mPictureImageView;
    private TextView mNameTextView;
    private TextView mDescriptionTextView;
    private TextView mLatitudeTextView;
    private TextView mLongitudeTextView;
    private Button mViewOnMapButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        mPictureImageView = findViewById(R.id.assetImage_imageView);
        mNameTextView = findViewById(R.id.assetName_textView);
        mDescriptionTextView = findViewById(R.id.assetDescription_textView);
        mLatitudeTextView = findViewById(R.id.assetLatitude_textView);
        mLongitudeTextView = findViewById(R.id.assetLongitude_textView);

        mNameTextView.setText(getIntent().getStringExtra(NAME));
        mDescriptionTextView.setText(getIntent().getStringExtra(DESCRIPTION));
        mLatitudeTextView.setText(String.valueOf(getIntent().getDoubleExtra(LAT, 0)));
        mLongitudeTextView.setText(String.valueOf(getIntent().getDoubleExtra(LNG, 0)));

        mViewOnMapButton = findViewById(R.id.asset_viewOnMap_button);
        mViewOnMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DetailsActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });
    }
}
