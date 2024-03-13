package com.sblc.controller;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {
    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static BluetoothAdapter myBluetoothAdapter;
    private ArrayAdapter<String> BTArrayAdapter;
    public static ConnectedThread ct = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (myBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Thiết bị của bạn không có Bluetooth", Toast.LENGTH_LONG).show();
            finish();
            System.exit(0);
        }

        if (!myBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            startActivityForResult(enableBtIntent, 1);
        }

        Button listBtn = findViewById(R.id.paired);
        listBtn.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                list(v);
            }
        });

        Button findBtn = (Button)findViewById(R.id.search);
        findBtn.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                find(v);
            }
        });

        ListView myListView = findViewById(R.id.listView1);

        myListView.setOnItemClickListener((parent, view, position, id) -> {
            String[] deviceInfo = BTArrayAdapter.getItem(position).split("\\n");
            String deviceAddress = deviceInfo[1];
            BluetoothDevice currentDevice = myBluetoothAdapter.getRemoteDevice(deviceAddress);

            ConnectThread connectThread = new ConnectThread(currentDevice);
            connectThread.start();
        });

        BTArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        myListView.setAdapter(BTArrayAdapter);
    }

    public void list(View view) {
        // get paired devices
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Set<BluetoothDevice> pairedDevices = myBluetoothAdapter.getBondedDevices();

        // put it's one to the adapter
        for (BluetoothDevice device : pairedDevices)
            BTArrayAdapter.add(device.getName() + "\n" + device.getAddress());

        Toast.makeText(getApplicationContext(), "Show Paired Devices",
                Toast.LENGTH_SHORT).show();
    }

    final BroadcastReceiver bReceiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // add the name and the MAC address of the object to the arrayAdapter
                BTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                BTArrayAdapter.notifyDataSetChanged();
            }
        }
    };

    public void find(View view) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED)
            return;
        if (myBluetoothAdapter.isDiscovering()) {
            // the button is pressed when it discovers, so cancel the discovery
            myBluetoothAdapter.cancelDiscovery();
        } else {
            BTArrayAdapter.clear();
            myBluetoothAdapter.startDiscovery();

            registerReceiver(bReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        unregisterReceiver(bReceiver);
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;

        @SuppressLint("MissingPermission")
        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException ignored) {
            }
            mmSocket = tmp;
        }

        @SuppressLint("MissingPermission")
        public void run() {
            // Cancel discovery because it will slow down the connection
            myBluetoothAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException ignored) { }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            manageConnectedSocket(mmSocket);
        }

        private void manageConnectedSocket(final BluetoothSocket mmSocket)
        {
            ct = new ConnectedThread(mmSocket);

            Intent i = new Intent(MainActivity.this, ControlActivity.class);
            startActivity(i);
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException ignored) { }
        }
    }

    public static class ConnectedThread extends Thread {
        public final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException ignored) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
                    //mHandler.obtainMessage(, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException ignored) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException ignored) { }
        }
    }
}