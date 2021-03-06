package com.example.lakethomason.bluetoothinteractiontest;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHealth;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
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

import com.mbientlab.metawear.AsyncDataProducer;
import com.mbientlab.metawear.Data;
import com.mbientlab.metawear.DataProducer;
import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.Route;
import com.mbientlab.metawear.Subscriber;
import com.mbientlab.metawear.android.BtleService;
import com.mbientlab.metawear.builder.RouteBuilder;
import com.mbientlab.metawear.builder.RouteComponent;
import com.mbientlab.metawear.data.Acceleration;
import com.mbientlab.metawear.data.EulerAngles;
import com.mbientlab.metawear.data.Quaternion;
import com.mbientlab.metawear.module.Accelerometer;
import com.mbientlab.metawear.module.Led;
import com.mbientlab.metawear.module.Logging;
import com.mbientlab.metawear.MetaWearBoard.Module;
import com.mbientlab.metawear.module.SensorFusionBosch;
import com.mbientlab.metawear.module.Switch;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import bolts.Continuation;
import bolts.Task;


public class MainActivity extends AppCompatActivity implements ServiceConnection {
    private TextView mHelpText;
    private ListView mBluetoothList;
    private Button mContinueButton;
    private Button mLogButton;
    private CheckBox mMetawearCheckBox;
    private CheckBox mPolarH7;
    private MetaWearBoard board;
    private TextView mRefreshButton;
    private BluetoothAdapter mBluetoothAdapter;
    private Toast myToast;
    private final static int REQUEST_ENABLE_BT = 2;
    private final static int REQUEST_ENABLE_LOC = 3;
    private final static int REQUEST_ENABLE_BTADMIN = 4;
    private final static int REQUEST_WRITE_EXTERNAL_STORAGE = 5;
    private final static int REQUEST_READ_EXTERNAL_STORAGE = 6;
    private List<String> macAddressList;
    private List<String> deviceList;
    private ArrayAdapter<String> arrayAdapter;
    private BtleService.LocalBinder serviceBinder;
    private BluetoothHealth mPolarDevice;
    private Logging loggingCtrllr;
    private Logging logging;
    private SensorFusionBosch sensorFusion;
    private State state;
    private File file;
    private File csvDirectory;
    private FileWriter writer;
    private long startTime;

    private MetawearConnected metawearConnectedState;
    private UIInterface uiInterface;

    /***********************************************************************************************
     * Enums
     **********************************************************************************************/
    public enum State {
        Startup,
        MetaConnected,
        PolarConnected,
        ReadyToLog,
        Logging
    }

    /***********************************************************************************************
     * Android life cycle methods
     **********************************************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        uiInterface = new UIInterface(MainActivity.this);
        metawearConnectedState = new MetawearConnected(uiInterface);

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
                makeToast("Refreshing bluetooth list..");
            }
        });
        mLogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logButtonClicked();
            }
        });
        // Bind the service when the activity is created (metawear)
        getApplicationContext().bindService(new Intent(this, BtleService.class), this, Context.BIND_AUTO_CREATE);

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
            makeToast("Bluetooth not supported, closing app...");
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
        // Unbind the service when the activity is destroyed
        getApplicationContext().unbindService(this);
    }

    /***********************************************************************************************
     * Helper methods
     **********************************************************************************************/
    public void prepareDataMembers() {
        mHelpText =  (TextView) findViewById(R.id.HelpText);
        mRefreshButton = (TextView) findViewById(R.id.refreshText);
        mMetawearCheckBox = (CheckBox) findViewById(R.id.metawearCheckBox);
        mPolarH7 = (CheckBox) findViewById(R.id.polarh7CheckBox);
        mContinueButton = (Button) findViewById(R.id.continueButton);
        mLogButton  = (Button) findViewById(R.id.logButton);
        deviceList = new ArrayList<String>();
        macAddressList = new ArrayList<String>();
        mBluetoothList = (ListView) findViewById(R.id.LIbluetoothList);
        arrayAdapter = new ArrayAdapter<String> (this, android.R.layout.simple_list_item_1, deviceList);
        mBluetoothList.setAdapter(arrayAdapter);
        state = State.Startup;
        startTime = -1;
        csvDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "MetawearFiles");
        if (!csvDirectory.exists())
            if (!csvDirectory.mkdirs())
                Log.d("MainActivity", "Directory creation has failed");
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

    public void connectToMetawearDevice(final String macAddress, final String deviceName) {
        final BluetoothManager btManager=
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothDevice remoteDevice=
                btManager.getAdapter().getRemoteDevice(macAddress);
        board = serviceBinder.getMetaWearBoard(remoteDevice);

        //Establishes a Bluetooth Low Energy connection to the MetaWear board
        board.connectAsync().continueWith(new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) throws Exception {
                if (task.isFaulted()) {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            makeToast("Failed to connect to " + deviceName);
                        }
                    });
                } else {
                    state = State.MetaConnected;
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            blinkLed(Led.Color.GREEN, 10);
                            makeToast("Connected to " + deviceName);
                            mMetawearCheckBox.setChecked(true);
                            mLogButton.setVisibility(View.VISIBLE);
                            logging = board.getModule(Logging.class);
                            sensorFusion = board.getModule(SensorFusionBosch.class);
                        }
                    });
                }
                return null;
            }
        });
        //set an unexpected disconnect listener
        board.onUnexpectedDisconnect(new MetaWearBoard.UnexpectedDisconnectHandler() {
            @Override
            public void disconnected(int status) {
                Log.i("MainActivity", "Unexpectedly lost connection: " + status);
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        makeToast("Lost connection to the MetaWear Device");
                        mMetawearCheckBox.setChecked(false);
                    }
                });
                if (state == State.ReadyToLog || state == State.Logging) {
                    state = State.PolarConnected;
                }
                else {
                    state = State.Startup;
                }
            }
        });
    }

    public void makeToast(String text){
        if (myToast != null) {
            myToast.cancel();
        }
        myToast = Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT);
        myToast.show();
    }

    public void blinkLed(Led.Color color, int count) {
        if (state == State.MetaConnected || state == State.ReadyToLog) {
            Led led;
            if ((led = board.getModule(Led.class)) != null) {
                led.editPattern(color, Led.PatternPreset.BLINK)
                        .repeatCount((byte) count)
                        .commit();
                led.play();
            }
        }
    }

    public void emailCSV(String fileLocation) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        // set the type to 'email'
        emailIntent .setType("text/plain");
        // add email(s) here to whom you want to send email
        String to[] = {"lakesainthomason@gmail.com"};
        emailIntent .putExtra(Intent.EXTRA_EMAIL, to);
        // convert file to uri
        Uri uri = Uri.fromFile(file);
        // add the attachment
        emailIntent .putExtra(Intent.EXTRA_STREAM, uri);
        // add mail subject
        emailIntent .putExtra(Intent.EXTRA_SUBJECT, file.getName());
        // create mail service chooser
        startActivity(Intent.createChooser(emailIntent, "Send email..."));
    }

    String formatDataToCSV(Data data) {
        if (startTime == -1)
            startTime = System.currentTimeMillis();

        String line =
                "\n"
                        + String.valueOf((System.currentTimeMillis() - startTime)/1000f)
                        + "," + String.format("%6f", data.value(EulerAngles.class).pitch())
                        + "," + String.format("%6f",data.value(EulerAngles.class).roll())
                        + "," + String.format("%6f",data.value(EulerAngles.class).yaw());
        return line;
    }

    /***********************************************************************************************
     * Interaction methods
     **********************************************************************************************/
    public void listItemClicked(int position) {
        String deviceName, deviceAddress;
        if (deviceList != null && macAddressList != null){
            deviceName = deviceList.get(position);
            deviceAddress = macAddressList.get(position);
            makeToast("Trying to connect with " + deviceName);
            connectToMetawearDevice(deviceAddress, deviceName);
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

        if (state == State.Logging) {
            stopLogging();
        }
        else if (state == State.ReadyToLog || state == State.MetaConnected){ //TODO remove second param once polar is setup
            beginLogging();
        }
    }

    public void beginLogging() {
        blinkLed(Led.Color.BLUE, 10);
        logging.clearEntries();
        mLogButton.setText("Stop");
        state = State.Logging;
        //prepare the datafile to write to
        try {
            file = new File(csvDirectory, "MetawearCSV__" + //TODO: convert MetawearCSV to test patient id
                    DateFormat.getDateTimeInstance().format(new Date()) +
                    "__.csv");
            file.createNewFile();
            writer = new FileWriter(file);
            appendToCSV("Elapsed Time(s),x-axis(deg/s),y-axis(deg/s),z-axis(deg/s)");
        }
        catch (IOException e) {
            Log.d("MainActivity", "IOException");
            e.getMessage();
            e.printStackTrace();
        }

        //setup the sensor
        sensorFusion.configure().mode(SensorFusionBosch.Mode.IMU_PLUS).commit();
        sensorFusion.eulerAngles().addRouteAsync(new RouteBuilder() {
            @Override
            public void configure(RouteComponent source) {
                source.log(new Subscriber() {
                    @Override
                    public void apply(Data data, Object... env) {
                        Log.i("MainActivity", "Euler Angles = " + data.value(EulerAngles.class).toString());
                        appendToCSV(formatDataToCSV(data));
                    }
                });
            }
        }).continueWith(new Continuation<Route, Void>() {
            @Override
            public Void then(Task<Route> task) throws Exception {
                sensorFusion.eulerAngles().start();
                sensorFusion.start();
                logging.start(true);
                return null;
            }
        });
    }

    public void stopLogging() {
        logging.stop();
        sensorFusion.stop();
        sensorFusion.eulerAngles().stop();
        blinkLed(Led.Color.RED, 5);
        mLogButton.setText("Log");
        state = State.ReadyToLog;
        makeToast("Starting Download");

        // download log data and send 100 progress updates during the download
        logging.downloadAsync(100, new Logging.LogDownloadUpdateHandler() {
            @Override
            public void receivedUpdate(long nEntriesLeft, long totalEntries) {
                Log.i("MainActivity", "Progress Update = " + nEntriesLeft + "/" + totalEntries);
            }
        }).continueWithTask(new Continuation<Void, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Void> task) throws Exception {
                Log.i("MainActivity", "Download completed");
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        makeToast("Download complete");
                    }
                });
                blinkLed(Led.Color.GREEN, 10);
                logging.clearEntries();
                writer.close();
                emailCSV(file.getAbsolutePath());
                return null;
            }
        });
    }


    private void appendToCSV(String data) {
        try {
            writer.append(data);
            writer.flush();
        }
        catch (java.io.IOException e) {
            Log.d("IOException", "File write failed: ");
            e.printStackTrace();
        }
    }

    /***********************************************************************************************
     * Interface methods
     **********************************************************************************************/
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        // Typecast the binder to the service's LocalBinder class
        serviceBinder = (BtleService.LocalBinder) service;
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) { }
}