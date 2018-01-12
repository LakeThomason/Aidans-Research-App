package com.example.lakethomason.bluetoothinteractiontest;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHealth;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;



public class MainActivity extends AppCompatActivity {
    private TextView mHelpText;
    private ListView mBluetoothList;
    private Button mContinueButton;
    private Button mLogButton;
    private CheckBox mPolarH7;
    private TextView mRefreshButton;
    private BluetoothAdapter mBluetoothAdapter;
    private final static int REQUEST_ENABLE_BT = 2;
    private final static int REQUEST_ENABLE_LOC = 3;
    private final static int REQUEST_ENABLE_BTADMIN = 4;
    private final static int REQUEST_WRITE_EXTERNAL_STORAGE = 5;
    private final static int REQUEST_READ_EXTERNAL_STORAGE = 6;
    private List<String> macAddressList;
    private List<String> deviceList;
    private ArrayAdapter<String> arrayAdapter;
    private BluetoothHealth mPolarDevice;

    private MetawearConnected metawearConnectedState;
    private EasyToast easyToast;

    /***********************************************************************************************
     * Android life cycle methods
     **********************************************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        easyToast = new EasyToast(MainActivity.this);
        metawearConnectedState = new MetawearConnected(MainActivity.this, easyToast);

        requestAllPermissions();
        prepareDataMembers();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //prepare for listitem clicks
        mBluetoothList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listItemClicked(position);
            }
        });
        mRefreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                beginDiscovery();
                easyToast.makeToast("Refreshing bluetooth list..");
            }
        });
        mLogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logButtonClicked();
            }
        });

        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
        mBluetoothAdapter.getProfileProxy(MainActivity.this, mProfileListener, BluetoothProfile.HEALTH);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //check if device is bluetooth available on device
        if (mBluetoothAdapter == null) {
            easyToast.makeToast("Bluetooth not supported, closing app...");
            System.exit(0);
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        beginDiscovery();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // unregister the ACTION_FOUND receiver.
        unregisterReceiver(mReceiver);
        metawearConnectedState.destroyService();
    }

    /***********************************************************************************************
     * Helper methods
     **********************************************************************************************/
    public void prepareDataMembers() {
        mHelpText =  (TextView) findViewById(R.id.HelpText);
        mRefreshButton = (TextView) findViewById(R.id.refreshText);
        mPolarH7 = (CheckBox) findViewById(R.id.polarh7CheckBox);
        mContinueButton = (Button) findViewById(R.id.continueButton);
        mLogButton  = (Button) findViewById(R.id.logButton);
        deviceList = new ArrayList<String>();
        macAddressList = new ArrayList<String>();
        mBluetoothList = (ListView) findViewById(R.id.LIbluetoothList);
        arrayAdapter = new ArrayAdapter<String> (this, android.R.layout.simple_list_item_1, deviceList);
        mBluetoothList.setAdapter(arrayAdapter);
    }

    public void beginDiscovery() {
        deviceList.clear();
        macAddressList.clear();
        arrayAdapter.notifyDataSetChanged();
        //if we're already discovering, cancel it first
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        mBluetoothAdapter.startDiscovery();
    }

    public void requestAllPermissions(){
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_ENABLE_LOC);
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.BLUETOOTH},
                REQUEST_ENABLE_BT);
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.BLUETOOTH_ADMIN},
                REQUEST_ENABLE_BTADMIN);
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                REQUEST_READ_EXTERNAL_STORAGE);
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_WRITE_EXTERNAL_STORAGE);
    }

    /***********************************************************************************************
     * Interaction methods
     **********************************************************************************************/
    public void listItemClicked(int position) {
        String deviceName, deviceAddress;
        if (deviceList != null && macAddressList != null){
            deviceName = deviceList.get(position);
            deviceAddress = macAddressList.get(position);

            easyToast.makeToast("Trying to connect with " + deviceName);
            metawearConnectedState.connectToMetawearDevice(deviceAddress, deviceName);
        }
        //TODO check what kind of device is being clicked
        //TODO check if the device is already connected
        //TODO connect to the device if the above are false
    }


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
                //make sure its not already in the list if discovery is started again
                if (deviceName != null) {
                    deviceList.add(deviceName);
                    macAddressList.add(deviceHardwareAddress);
                    arrayAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    private BluetoothProfile.ServiceListener mProfileListener = new BluetoothProfile.ServiceListener() {
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == BluetoothProfile.HEALTH) {
                mPolarDevice = (BluetoothHealth) proxy;
            }
        }
        public void onServiceDisconnected(int profile) {
            if (profile == BluetoothProfile.HEALTH) {
                mPolarDevice = null;
            }
        }
    };
    public void logButtonClicked() {
        //if (state == State.Logging)
        if (mLogButton.getText().toString().equals("Log")) //temporary //TODO STATE
            metawearConnectedState.beginLogging();
        else
        //else if (state == State.ReadyToLog || state == State.MetaConnected) //TODO remove second param once polar is setup
            metawearConnectedState.stopLogging();
    }
}