package com.mobileappclass.assignment3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;


public class localDBFragment extends Fragment {

    //private OnFragmentInteractionListener mListener;
    DatabaseHelper myDB;
    ListView lv;
    ArrayAdapter<String> adapter;
    ArrayList<String> rows;
    private BroadcastReceiver broadcastReceiver;

    public localDBFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_local_db, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        String[] arr={};
        rows = new ArrayList<>(Arrays.asList(arr));

        lv = (ListView) getView().findViewById(R.id.localDBView);
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, rows);
        lv.setAdapter(adapter);

        viewAll();

        /*
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                viewAll();
            }
        };*/
    }


    public void viewAll() {
        rows.clear();
        adapter.notifyDataSetChanged();
        myDB = new DatabaseHelper(getContext());
        Cursor res = myDB.getAllData();
        if(res.getCount() == 0) {
            // show message if nothing found in sqlite db
            Toast.makeText(getContext(),"no data found",Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuffer buffer = new StringBuffer();
        while (res.moveToNext()) {
            //buffer.append(res.getString(0)+" ");
            buffer.append(res.getString(1)+"  ");
            buffer.append(res.getString(2)+"  ");
            buffer.append(res.getString(3));

            rows.add(buffer.toString());
            buffer.delete(0, buffer.length());
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(broadcastReceiver == null){
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    viewAll();
                }
            };
        }
        getActivity().registerReceiver(broadcastReceiver,new IntentFilter("location_update"));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        /*
        if(broadcastReceiver != null){
            getActivity().unregisterReceiver(broadcastReceiver);
        }*/
    }
}
