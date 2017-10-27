package com.example.lakethomason.bluetoothinteractiontest;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.os.Bundle;
import android.os.IBinder;
import com.mbientlab.metawear.android.BtleService;

import java.util.Set;

public class MainActivity extends AppCompatActivity implements ServiceConnection {

    //Datamembers
    private TextView mHelpText;
    private Button mMetaWearButton;
    private Button mPolarButton;

    BluetoothAdapter mBluetoothAdapter;
    private final static int REQUEST_ENABLE_BT = 69;

    private final static int REQUEST_ENABLE_LOC = 777;

    private final static int REQUEST_ENABLE_BTADMIN = 420;

    private BtleService.LocalBinder serviceBinder;


    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
            }
            else if (mBluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                mHelpText.setText("Discovery started :D");
            }
            else if (mBluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                mHelpText.setText("Discovery finsished..");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHelpText =  (TextView) findViewById(R.id.HelpText);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

        mMetaWearButton = (Button) findViewById(R.id.button);
        mMetaWearButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mBluetoothAdapter.isDiscovering()) {
                    mBluetoothAdapter.cancelDiscovery();
                }
                mBluetoothAdapter.startDiscovery();
                mHelpText.setText("Discovery should have started...");
            }
        });
        mPolarButton = (Button) findViewById(R.id.button2);
        mPolarButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

            }
        });

        //request permissions
        int regularmanCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH);
        int adminCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_ADMIN);
        int locCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
        getPermission();

        //metawear
        getApplicationContext().bindService(new Intent(this, BtleService.class),
                this, Context.BIND_AUTO_CREATE);
    }

    public void getPermission(){
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_ENABLE_LOC);
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.BLUETOOTH},
                REQUEST_ENABLE_BT);
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.BLUETOOTH_ADMIN},
                REQUEST_ENABLE_BTADMIN);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mBluetoothAdapter == null) {
            mHelpText.setText("El no supporto el tootho");
            return;
        }
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
            }
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ENABLE_BTADMIN: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }
            case REQUEST_ENABLE_LOC: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }
        }
    }

    public void getPermissions() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.BLUETOOTH_ADMIN)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.BLUETOOTH_ADMIN)) {
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.BLUETOOTH_ADMIN},
                        REQUEST_ENABLE_BTADMIN);
            }
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        // Typecast the binder to the service's LocalBinder class
        serviceBinder = (BtleService.LocalBinder) service;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(mReceiver);
        getApplicationContext().unbindService(this);
    }
}
