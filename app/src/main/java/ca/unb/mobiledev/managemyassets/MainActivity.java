package ca.unb.mobiledev.managemyassets;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button button = findViewById(R.id.openMap);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                intent.putExtra("latitude", 34.8098080980);
                intent.putExtra("longitude", 67.09098898);
                startActivity(intent);

                //In MapActivity.java onCreate method get lattitude and longitude and use it
//                Intent intent1 = getIntent();
//                double lat = intent1.getDoubleExtra("latitude", 0);
//                double lng  = intent1.getDoubleExtra("longitude",0);
//                //Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
//                //        Uri.parse("google.navigation:q=an+address+city"));
//                startActivity(intent);
            }
        });
    }
}
