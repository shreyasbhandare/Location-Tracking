package com.mobileappclass.assignment3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class remoteDBFragment extends Fragment {

    //private OnFragmentInteractionListener mListener;
    Button sb;
    private FirebaseDatabase db;
    DatabaseHelper myDB;
    ListView lv2;
    ArrayAdapter<String>adapter2;
    ArrayList<String>rows2;
    DatabaseReference ref;
    TextView tt, ss;
    BroadcastReceiver broadcastReceiver;

    public remoteDBFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_remote_db, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
        //mListener = null;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        sb = (Button) getView().findViewById(R.id.sync);
        String[] arr={};
        rows2 = new ArrayList<>(Arrays.asList(arr));
        tt = (TextView) getView().findViewById(R.id.ntype);
        ss = (TextView) getView().findViewById(R.id.status);

        lv2 = (ListView) getView().findViewById(R.id.remoteDBView);
        adapter2 = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, rows2);
        lv2.setAdapter(adapter2);

        checkState(getContext());


        viewAll();

        sb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectivityManager cm = (ConnectivityManager) getContext()
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

                boolean isMobile = activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE;
                boolean isWifi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
                if(isMobile || isWifi){
                    myDB = new DatabaseHelper(getContext());
                    Cursor res = myDB.getAllData();
                    if(res.getCount() == 0) {
                        // show message if nothing found in sqlite db
                        //Toast.makeText(getContext(),"no data found",Toast.LENGTH_SHORT).show();
                        return;
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
                    Toast.makeText(getContext(),"Manually Sync to Server",Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(getContext(),"No Network Detected. Can't Sync",Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });

    }


    public void viewAll() {
        ConnectivityManager cm = (ConnectivityManager) getContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isMobile = activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE;
        boolean isWifi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
        if(isWifi || isMobile){
            rows2.clear();
            adapter2.notifyDataSetChanged();
            DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference("Students");
            final StringBuffer buffer = new StringBuffer();

            ref2.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot ds : dataSnapshot.getChildren()){
                        for (DataSnapshot ds2 : ds.getChildren()){
                            if(rows2.size()==200)
                                break;
                            for(DataSnapshot ds3 : ds2.getChildren()){
                                buffer.append(ds3.getValue()+" ");
                            }
                            rows2.add(buffer.toString());
                            buffer.delete(0, buffer.length());
                        }
                    }
                    adapter2.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        else {
            Toast.makeText(getContext(),"Cellular data and wifi not found. Can't pull data from server",Toast.LENGTH_SHORT).show();
        }
    }

    public void checkState(Context context){
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isMobile = activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE;
        boolean isWifi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
        //No internet is 0 state
        if(isWifi && isMobile){
            tt.setText("NO INTERNET");
        }
        else if(isWifi && !isMobile){
            tt.setText("WIFI : "+activeNetwork.getExtraInfo());
        }
        if(isMobile && isWifi){
            tt.setText("WIFI : "+activeNetwork.getExtraInfo());
        }
        else if(isMobile && !isWifi){
            tt.setText("CELLULAR : "+activeNetwork.getExtraInfo());
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
                        viewAll();
                        ss.setText("Connected");
                    }
                    else
                    {
                        ss.setText("not connected");
                    }
                }
            };
        }
        getActivity().registerReceiver(broadcastReceiver,new IntentFilter("location_update"));
    }
}
