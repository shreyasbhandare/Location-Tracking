package com.mobileappclass.assignment3;

import android.Manifest;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    DatabaseReference ref;
    DownloadTask ds;
    BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //myDB = new DatabaseHelper(this);
        if(!runtime_permissions()){
            Intent ii = new Intent(getApplicationContext(),GPS_Service.class);
            startService(ii);
        }

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            localDBFragment ldb = new localDBFragment();
            fragmentTransaction.replace(R.id.land_container, ldb);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();

            fragmentTransaction = getSupportFragmentManager().beginTransaction();
            remoteDBFragment rdb = new remoteDBFragment();
            fragmentTransaction.replace(R.id.land_container2, rdb);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }

        else {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            localDBFragment ldb = new localDBFragment();
            fragmentTransaction.replace(R.id.port_container, ldb);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
        MenuInflater inflater = getMenuInflater(); // reads XML
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            inflater.inflate(R.menu.menu_activity, menu); // to create
            return super.onCreateOptionsMenu(menu); // the menu
        }
        else
            return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO: handle clicks on the menu items
        if (item.getItemId() == R.id.action_offline) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            localDBFragment ldb = new localDBFragment();
            fragmentTransaction.replace(R.id.port_container, ldb);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }

        else if (item.getItemId() == R.id.action_online) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            remoteDBFragment rdb = new remoteDBFragment();
            fragmentTransaction.replace(R.id.port_container, rdb);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
        else if(item.getItemId() == R.id.action_query) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            queryFragment qf = new queryFragment();
            fragmentTransaction.replace(R.id.port_container, qf);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean runtime_permissions() {
        if(Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},100);

            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 100){
            if( grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                //enable_buttons();
                //start location service in the background
                Intent ii = new Intent(getApplicationContext(),GPS_Service.class);
                startService(ii);

            }else {
                runtime_permissions();
            }
        }
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
            //additional code
        FragmentManager fm = getFragmentManager();
        for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {
            fm.popBackStack();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        if(broadcastReceiver == null){
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    ConnectivityManager cm = (ConnectivityManager) context
                            .getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                    boolean isMobile = activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE;
                    boolean isWifi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
                    // if wifi is connected run the asynch task to upload data.
                    if(isWifi){
                        ds = new DownloadTask(getBaseContext());
                        ds.execute();
                    }
                    else if(isMobile)
                        Toast.makeText(context,"Can't update remote database! Please connect to wifi or Sync manually",Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(context,"Can't update remote database! Please connect to wifi or Sync manually with Mobile data turn On",Toast.LENGTH_SHORT).show();
                }
            };
        }
        registerReceiver(broadcastReceiver,new IntentFilter("location_update"));
    }

    // async task calss to update remote database on wifi connection
    private class DownloadTask extends AsyncTask<String, Integer, Void>{

        Context mContext;
        DatabaseHelper myDB;

        public DownloadTask(Context context){
            mContext = context;
        }

        @Override
        protected Void doInBackground(String... links) {
            myDB = new DatabaseHelper(mContext);
            Cursor res = myDB.getAllData();
            if(res.getCount() == 0) {
                // show message if nothing found in sqlite db
                return null;
            }

            ref = FirebaseDatabase.getInstance().getReference("Students"); // What database can I actually talk to?
            DatabaseReference students = ref.child("ssb146");
            while (res.moveToNext()) {
                DatabaseReference bart = students.child(res.getString(1)); // put date-time
                bart.child("date").setValue(res.getString(1));
                bart.child("netid").setValue("ssb146");
                bart.child("x").setValue(res.getString(2));
                bart.child("y").setValue(res.getString(3));
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(mContext,"updated remote database",Toast.LENGTH_SHORT).show();
        }
    }

}


