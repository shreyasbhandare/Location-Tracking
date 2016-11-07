package com.mobileappclass.assignment3;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class queryFragment extends Fragment {

    ArrayAdapter<CharSequence>adapter;
    Spinner sp;
    EditText et;
    DatabaseReference ref3;
    ListView lv3;
    ArrayAdapter<String> adapter3;
    ArrayList<String> rows3;
    Button btn;
    String order;
    String netid;

    public queryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_query, container, false);
    }

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
        rows3 = new ArrayList<>(Arrays.asList(arr));

        //setting up listview for query
        lv3 = (ListView) getView().findViewById(R.id.listQurView);
        adapter3 = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, rows3);
        lv3.setAdapter(adapter3);

        //setting spinner
        sp = (Spinner) getView().findViewById(R.id.spinner_menu);
        adapter = ArrayAdapter.createFromResource(getContext(), R.array.items, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(adapter);

        //button
        btn = (Button) getView().findViewById(R.id.runQur);


        // spinner dropdown choices
        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(getContext(), "You selected: "+parent.getItemAtPosition(position), Toast.LENGTH_SHORT).show();
                order = (String)parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et = (EditText) getView().findViewById(R.id.netid);
                netid = et.getText().toString();
                if(netid.isEmpty()){
                    Toast.makeText(getContext(),"Please Enter NetID", Toast.LENGTH_SHORT).show();
                    return;
                }

                getGradQuery(order,netid);
            }
        });


    }

    public void getGradQuery(final String order, String netid){
        ref3 = FirebaseDatabase.getInstance().getReference("Students/"+netid);
        final StringBuffer buffer = new StringBuffer();
        rows3.clear();
        adapter3.notifyDataSetChanged();

        ref3.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    for (DataSnapshot ds2 : ds.getChildren()){
                        for(DataSnapshot ds3 : ds2.getChildren()){
                            buffer.append(ds3.getValue()+" ");
                        }
                        buffer.append(ds2.getValue()+" ");

                    }
                    rows3.add(buffer.toString());
                    buffer.delete(0, buffer.length());
                }
                if(order.equals("Descending")){
                    //reverse order
                    Collections.reverse(rows3);

                }
                adapter3.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        /*
        Query query1 = ref3.equalTo(netid);
        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    for (DataSnapshot ds2 : ds.getChildren()){
                        for(DataSnapshot ds3 : ds2.getChildren()){
                            buffer.append(ds3.getValue()+" ");
                        }
                        rows3.add(buffer.toString());
                        buffer.delete(0, buffer.length());
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/
    }

}
