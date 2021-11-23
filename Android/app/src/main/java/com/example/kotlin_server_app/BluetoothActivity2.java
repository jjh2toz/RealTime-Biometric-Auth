package com.example.kotlin_server_app;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public class BluetoothActivity2 extends AppCompatActivity {
    ListView pairedListView;
    BluetoothAdapter btAdapter;
    ArrayAdapter<String> pairedArrayAdapter;
    ArrayList<String> pairedArrayList;
    Set<BluetoothDevice> pairedDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        btAdapter = BluetoothAdapter.getDefaultAdapter();

        pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedArrayList = new ArrayList<>();
        pairedArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

        SearchPaired();
        pairedListView.setAdapter(pairedArrayAdapter);

        // 클릭 event 연결
        pairedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                btAdapter.cancelDiscovery();

                final String address = pairedArrayList.get(position);

                Intent intent = new Intent(getBaseContext(), MeasureActivity2.class);
                intent.putExtra("address", address);
                startActivity(intent);
            }
        });
    }

    public void SearchPaired() {
        pairedDevices = btAdapter.getBondedDevices();
        for(BluetoothDevice device : pairedDevices) {
            pairedArrayAdapter.add(device.getName());
            pairedArrayList.add(device.getAddress());
        }
    }

    // 블루투스 셋팅을 열어서 기기를 페어링 할 수 있도록 함.
    public void OnClickBtnSearch(View view){
        final Intent intent = new Intent();
        intent.setAction(Settings.ACTION_BLUETOOTH_SETTINGS);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}