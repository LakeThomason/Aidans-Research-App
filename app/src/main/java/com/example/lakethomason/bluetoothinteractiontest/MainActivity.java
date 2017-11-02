package com.example.lakethomason.bluetoothinteractiontest;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
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
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.android.BtleService;
import com.mbientlab.metawear.module.Led;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import bolts.Continuation;
import bolts.Task;

public class MainActivity extends AppCompatActivity implements ServiceConnection {

    //Datamembers
    private TextView mHelpText;
    private ListView mBluetoothList;
    private Button mContinueButton;
    private CheckBox mMetawearCheckBox;
    private CheckBox mPolarH7;
    private MetaWearBoard board;
    private TextView mRefreshButton;

    private BluetoothAdapter mBluetoothAdapter;
    private Toast myToast;

    private final static int REQUEST_ENABLE_BT = 2;
    private final static int REQUEST_ENABLE_LOC = 3;
    private final static int REQUEST_ENABLE_BTADMIN = 4;

    private List<String> macAddressList;
    private List<String> deviceList;
    private ArrayAdapter<String> arrayAdapter;

    private BtleService.LocalBinder serviceBinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //request permissions
        requestPermissions();

        //get the adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //prepare visual elements
        mHelpText =  (TextView) findViewById(R.id.HelpText);
        mRefreshButton = (TextView) findViewById(R.id.refreshText);
        mBluetoothList = (ListView) findViewById(R.id.LIbluetoothList);
        mMetawearCheckBox = (CheckBox) findViewById(R.id.metawearCheckBox);
        mContinueButton = (Button) findViewById(R.id.continueButton);
        //TODO make the onclick for continue button
        //TODO make activity switch function when both device are connected

        //prepare the listview contents for button onclick
        deviceList = new ArrayList<String>();
        macAddressList = new ArrayList<String>();
        arrayAdapter = new ArrayAdapter<String> (this, android.R.layout.simple_list_item_1, deviceList);
        mBluetoothList.setAdapter(arrayAdapter);

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
                makeToast("Refreshing bluetooth list..");
            }
        });
        // Bind the service when the activity is created
        getApplicationContext().bindService(new Intent(this, BtleService.class), this, Context.BIND_AUTO_CREATE);

        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mBluetoothAdapter == null) {
            mHelpText.setText("Bluetooth is not supported on this device");
            return;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        beginDiscovery();
    }

    public void requestPermissions(){
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

    public void retrieveBoard(String macAddress) {
        final BluetoothManager btManager=
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothDevice remoteDevice=
                btManager.getAdapter().getRemoteDevice(macAddress);
        board = serviceBinder.getMetaWearBoard(remoteDevice);
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

    public void listItemClicked(int position) {
        String deviceName;
        String deviceAddress;
        if (deviceList != null && macAddressList != null){
            deviceName = deviceList.get(position);
            deviceAddress = macAddressList.get(position);
            connectToMetawearDevice(deviceAddress, deviceName);
        }
        //TODO check what kind of device is being clicked
        //TODO check if the device is already connected
        //TODO connect to the device if the above are false
    }

    public void blinkLed(Led.Color color, int count) {
        Led led;
        if ((led= board.getModule(Led.class)) != null) {
            led.editPattern(color, Led.PatternPreset.BLINK)
                    .repeatCount((byte) count)
                    .commit();
            led.play();
        }
    }

    public void beginDiscovery() {
        //if were already discovering, cancel it first
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        mBluetoothAdapter.startDiscovery();
    }

    public void connectToMetawearDevice(String macAddress, final String deviceName) {
        //makeToast("Trying to connect with " + deviceName);
        retrieveBoard(macAddress);
        boolean connected;

        //Establishes a Bluetooth Low Energy connection to the MetaWear board
        board.connectAsync().continueWith(new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) throws Exception {
                if (task.isFaulted()) {
                    Log.i("MainActivity", "Failed to connect");
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            makeToast("Failed to connect to " + deviceName);
                        }
                    });
                } else {
                    Log.i("MainActivity", "Connected");
                    blinkLed(Led.Color.GREEN, 10);
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            makeToast("Connected to " + deviceName);
                            mMetawearCheckBox.setChecked(true);
                        }
                    });
                }
                return null;
            }
        });
    }

    //TODO make a function that checks if a device has been d/c'd.. Metawear uses onClientConnParamsChanged()

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
                if (!deviceList.contains(deviceName)) {
                    deviceList.add(deviceName);
                    macAddressList.add(deviceHardwareAddress);
                    arrayAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    public void makeToast(String text){
        if (myToast != null) {
            myToast.cancel();
        }
        myToast = Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT);
        myToast.show();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        // Typecast the binder to the service's LocalBinder class
        serviceBinder = (BtleService.LocalBinder) service;
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) { }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // unregister the ACTION_FOUND receiver.
        unregisterReceiver(mReceiver);
        // Unbind the service when the activity is destroyed
        getApplicationContext().unbindService(this);
    }
}
