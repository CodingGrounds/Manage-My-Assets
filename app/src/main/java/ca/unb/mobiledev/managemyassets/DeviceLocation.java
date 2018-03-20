package ca.unb.mobiledev.managemyassets;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import static ca.unb.mobiledev.managemyassets.MapActivity.PERMISSION_REQUEST_ACCESS_FINE_LOCATION;

/**
 * Created by Jason on 2018-02-26.
 */

/**
 * Created by Mattias Schroer on 2018-02-22.
 */


public class DeviceLocation {

    public Location getDeviceLocation(Context context) {

        //provider name is unnecessary
        Location defaultLocation = new Location("");
        defaultLocation.setLatitude(45.964993);
        defaultLocation.setLongitude(-66.646332);

        if (ContextCompat.checkSelfPermission(context.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
        }
        else{
            LocationManager locationManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            String provider = locationManager.getBestProvider(criteria, true);
            Location location = locationManager.getLastKnownLocation(provider);

            if (location != null) {
                return location;
            }
            else{
                return defaultLocation;
            }

        }
        return defaultLocation;
    }

}
