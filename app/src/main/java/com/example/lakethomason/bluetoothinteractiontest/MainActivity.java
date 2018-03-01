package com.example.lakethomason.bluetoothinteractiontest;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private ListView mBluetoothList;
    private Button mMetaWearLogButton;
    private Button mPolarH7LogButton;
    private TextView mRefreshButton;
    private Button mSubjectsListButton;
    private BluetoothAdapter mBluetoothAdapter;
    private final static int ALL_PERMISSIONS = 1;
    private final static int REQUEST_ENABLE_BT = 2;
    private List<String> macAddressList;
    private List<String> deviceList;
    private ArrayAdapter<String> arrayAdapter;

    private Metawear metawearDevice;
    private PolarH7 polarH7Device;
    private EasyToast easyToast;

    /***********************************************************************************************
     * Android life cycle methods
     **********************************************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestAllPermissions();
        prepareDataMembers();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        easyToast = new EasyToast(MainActivity.this);
        metawearDevice = new Metawear(MainActivity.this);
        polarH7Device = new PolarH7(MainActivity.this, easyToast, mBluetoothAdapter);

        mBluetoothAdapter.getBluetoothLeScanner().startScan(polarH7Device.scanCallback);

        //prepare onclick listeners
        mSubjectsListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SubjectListActivity.class);
                intent.putExtra("MetawearBoard", metawearDevice);
                startActivity(intent);
            }
        });
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
                mBluetoothAdapter.getBluetoothLeScanner().startScan(polarH7Device.scanCallback);
                easyToast.makeToast("Refreshing bluetooth list..");
            }
        });
        mMetaWearLogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                metaWearLogButtonClicked();
            }
        });
        mPolarH7LogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                polarH7LogButtonClicked();
            }
        });

        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
        //mBluetoothAdapter.getProfileProxy(MainActivity.this, mProfileListener, BluetoothProfile.HEALTH);
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
        metawearDevice.destroyService();
    }

    /***********************************************************************************************
     * Helper methods
     **********************************************************************************************/
    public void prepareDataMembers() {
        mRefreshButton = (TextView) findViewById(R.id.refreshText);
        mMetaWearLogButton  = (Button) findViewById(R.id.logMetaWearButton);
        mPolarH7LogButton  = (Button) findViewById(R.id.logPolarButton);
        mSubjectsListButton = findViewById(R.id.testSubjectsButton);
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
        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE
                , Manifest.permission.WRITE_EXTERNAL_STORAGE
                , Manifest.permission.ACCESS_COARSE_LOCATION
                , Manifest.permission.BLUETOOTH
                , Manifest.permission.BLUETOOTH_ADMIN};
        ActivityCompat.requestPermissions(MainActivity.this, permissions, ALL_PERMISSIONS);
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
            metawearDevice.connectToMetawearDevice(deviceAddress, deviceName);
        }
        //TODO check what kind of device is being clicked
        //TODO check if the device is already connected
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
                if (deviceName != null && !deviceList.contains(deviceName)) {
                    deviceList.add(deviceName);
                    macAddressList.add(deviceHardwareAddress);
                    arrayAdapter.notifyDataSetChanged();
                    if (deviceName.equals("MetaWear")) {
                        easyToast.makeToast("Trying to connect with " + deviceName);
                        metawearDevice.connectToMetawearDevice(deviceHardwareAddress, deviceName);
                    }
                }
            }
        }
    };

    public void metaWearLogButtonClicked() {
        //if (state == State.Logging)
        if (mMetaWearLogButton.getText().toString().equals("Log Meta")) {
            metawearDevice.beginLogging();
            mMetaWearLogButton.setText("Stop");
        }
        else {
            metawearDevice.stopLogging(new Runnable() {
                public void run() {
                    //sendCSVs();
                }
            });
            mMetaWearLogButton.setText("Log Meta");
        }
    }

    public void polarH7LogButtonClicked() {
        //if (state == State.Logging)
        if (mPolarH7LogButton.getText().toString().equals("Log Polar")) {

            polarH7Device.beginLogging();
            mPolarH7LogButton.setText("Stop");
        }
        else {
            polarH7Device.stopLogging();
            mPolarH7LogButton.setText("Log Polar");
        }
    }
}