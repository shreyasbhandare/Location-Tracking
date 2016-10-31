package com.mobileappclass.assignment3;

import android.app.FragmentManager;
import android.content.res.Configuration;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        return super.onOptionsItemSelected(item);
    }
}
