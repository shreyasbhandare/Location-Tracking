package com.mobileappclass.assignment3;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by shreyas on 10/30/16.
 */

public class GPS_Service extends Service {

    private LocationListener listener;
    private LocationManager locationManager;
    SimpleDateFormat sdf = new SimpleDateFormat("MM-dd hh:mm:ss a");
    DatabaseHelper myDB;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        myDB = new DatabaseHelper(this);
        //Firebase.setAndroidContext(this);

        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                String str = sdf.format(new Date(location.getTime()));
                double lat = location.getLatitude();
                double lon = location.getLongitude();
                myDB.insertData(str,lat,lon);

                Intent i = new Intent("location_update");
                sendBroadcast(i);

                /*Intent i2 = new Intent("remote_update");
                sendBroadcast(i2);*/

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        };

        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        //noinspection MissingPermission
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,10000,0,listener);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(locationManager != null){
            //noinspection MissingPermission
            locationManager.removeUpdates(listener);
        }
    }
}