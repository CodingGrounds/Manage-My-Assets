package ca.unb.mobiledev.managemyassets;


//import android.FusedLocationProviderClient

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

/**
 * Created by Mattias Schroer on 2018-02-22.
 */


public class GetLocation {

    //private FusedLocationProviderClient mFusedLocationClient;



    public Location getDeviceLocation(Context context) {

        Location targetLocation = new Location("");//provider name is unnecessary
        targetLocation.setLatitude(0.0d);//your coords of course
        targetLocation.setLongitude(0.0d);


        if (ContextCompat.checkSelfPermission(context.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return targetLocation;
        }
        else{
            LocationManager lm = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            targetLocation.setLatitude(69.0);
            targetLocation.setLongitude(69.0);

            if (location != null) {
                return location;
            }
            else return targetLocation;
        }

    }

}


