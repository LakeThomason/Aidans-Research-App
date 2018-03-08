package com.example.lakethomason.bluetoothinteractiontest;

import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mbientlab.metawear.Data;
import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.Route;
import com.mbientlab.metawear.Subscriber;
import com.mbientlab.metawear.android.BtleService;
import com.mbientlab.metawear.builder.RouteBuilder;
import com.mbientlab.metawear.builder.RouteComponent;
import com.mbientlab.metawear.data.EulerAngles;
import com.mbientlab.metawear.module.Led;
import com.mbientlab.metawear.module.Logging;
import com.mbientlab.metawear.module.SensorFusionBosch;

import java.io.File;

import bolts.Continuation;
import bolts.Task;

import com.google.gson.Gson;



/**
 * Created by lakethomason on 1/11/2018.
 */

public class Metawear implements ServiceConnection {
    private Logging logging;
    private SensorFusionBosch sensorFusion;
    private MetaWearBoard board;
    private BtleService.LocalBinder serviceBinder;

    private TextView mSignal;
    private CheckBox mMetawearCheckBox;
    private ProgressBar mDownloadBar;

    private long startTime;
    private Boolean canLog;

    private Activity activity;
    private EasyToast easyToast;
    private FileCreator fileCreator;

    private static final Metawear instance = new Metawear();

    public static Metawear getInstance() {
        return instance;
    }

    private Metawear() {
        canLog = false;
    }

    public void setActivity(Activity _activity) {
        activity = _activity;
        easyToast = new EasyToast(activity);

        mSignal = activity.findViewById(R.id.signalMetawear);
        mMetawearCheckBox =  activity.findViewById(R.id.metawearCheckBox);
        mDownloadBar = activity.findViewById(R.id.downloadBar);

        activity.getApplicationContext().bindService(new Intent(activity, BtleService.class), this, Context.BIND_AUTO_CREATE);
        if (board != null && board.isConnected()){
            mMetawearCheckBox.setChecked(true);
        }
    }

    public void connectToMetawearDevice(final String macAddress, final String deviceName) {
        final BluetoothManager btManager=
                (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothDevice remoteDevice=
                btManager.getAdapter().getRemoteDevice(macAddress);
        board = serviceBinder.getMetaWearBoard(remoteDevice);

        //Establishes a Bluetooth Low Energy connection to the MetaWear board
        board.connectAsync().continueWith(new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) throws Exception {
                if (task.isFaulted()) {
                    easyToast.makeToastOnUIThread("Failed to connect to " + deviceName);
                } else {
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            mMetawearCheckBox.setChecked(true);
                            easyToast.makeToast("Connected to " + deviceName);
                        }
                    });
                    blinkLed(Led.Color.GREEN, 10);
                    canLog = true;
                    logging = board.getModule(Logging.class);
                    sensorFusion = board.getModule(SensorFusionBosch.class);
                    new updateRSSI().execute();
                    if (logging == null || sensorFusion == null) {
                        Log.d("Metawear", "A module was null");
                    }
                }
                setOnDisconnectListener();
                return null;
            }
        });
    }

    public boolean beginLogging(String subjectName, int testNumber) {
        if (board == null || !board.isConnected()) {
            easyToast.makeToast("The MetaWear board seems to be disconnected");
            return false;
        }
        else if (!canLog) {
            easyToast.makeToast("You cannot log while another process is logging or downloading");
            return false;
        }
        mDownloadBar.setProgress(0);
        startTime = -1;
        canLog = false;
        logging.clearEntries();
        blinkLed(Led.Color.BLUE, 10);
        fileCreator = new FileCreator(subjectName, "Metawear", testNumber);
        fileCreator.appendLineToCSV("Elapsed Time(s),x-axis(deg),y-axis(deg),z-axis(deg)");

        //setup the sensor
        sensorFusion.configure().mode(SensorFusionBosch.Mode.IMU_PLUS).commit();
        sensorFusion.eulerAngles().addRouteAsync(new RouteBuilder() {
            @Override
            public void configure(RouteComponent source) {
                source.log(new Subscriber() {
                    @Override
                    public void apply(Data data, Object... env) {
                        Log.i("MainActivity", "Euler Angles = " + data.value(EulerAngles.class).toString());
                        fileCreator.appendLineToCSV(formatDataToCSV(data));
                    }
                });
            }
        }).continueWith(new Continuation<Route, Void>() {
            @Override
            public Void then(Task<Route> task) throws Exception {
                sensorFusion.eulerAngles().start();
                sensorFusion.start();
                //logging.start(true);
                return null;
            }
        });
        return true;
    }

    public boolean stopLogging(final Runnable addFileToSubject) {
        if (board == null || !board.isConnected()) {
            easyToast.makeToast("The Metawear board has become disconnected");
            return false;
        }
        sensorFusion.stop();
        sensorFusion.eulerAngles().stop();
        blinkLed(Led.Color.BLUE, 5);
        easyToast.makeToast("Starting Download");

        // download log data and send 100 progress updates during the download
        logging.downloadAsync(100, new Logging.LogDownloadUpdateHandler() {
            @Override
            public void receivedUpdate(long nEntriesLeft, long totalEntries) {
                mDownloadBar.setMax(Integer.valueOf(String.valueOf(totalEntries)));
                Log.i("MainActivity", "Progress Update = " + nEntriesLeft + "/" + totalEntries);
                mDownloadBar.setProgress(Integer.valueOf(String.valueOf(totalEntries- nEntriesLeft)));
            }
        }).continueWithTask(new Continuation<Void, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Void> task) throws Exception {
                Log.i("MainActivity", "Download completed");
                easyToast.makeToastOnUIThread("Download Complete");
                canLog = true;
                blinkLed(Led.Color.GREEN, 10);
                logging.clearEntries();
                fileCreator.closeFile();
                addFileToSubject.run();
                mDownloadBar.setProgress(0);
                return null;
            }
        });
        return true;
    }

    public boolean stopLoggingAndDestroy() {
        if (board == null || !board.isConnected()) {
            easyToast.makeToast("The Metawear board has become disconnected");
            return false;
        }
        sensorFusion.stop();
        sensorFusion.eulerAngles().stop();
        blinkLed(Led.Color.RED, 3);
        logging.clearEntries();
        fileCreator.closeFile();
        fileCreator.deleteFile();
        canLog = true;
        return true;
    }

    private void setOnDisconnectListener() {
        if (board == null) {
            return;
        }
        //set an unexpected disconnect listener
        board.onUnexpectedDisconnect(new MetaWearBoard.UnexpectedDisconnectHandler() {
            @Override
            public void disconnected(int status) {
                Log.d("MainActivity", "Unexpectedly lost connection: " + status);
                canLog = false;
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        mMetawearCheckBox.setChecked(false);
                        mSignal.setText("Signal: ");
                        easyToast.makeToast("Lost connection to the MetaWear Device");
                    }
                });
            }
        });
    }

    private String formatDataToCSV(Data data) {
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

    private class updateRSSI extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... arg0) {
            while (board.isConnected()) {
                try {
                    displayBatteryAndSignal();
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }

    private void displayBatteryAndSignal() {
        //get RSSI and some GATT characteristics (battery level, device information

        board.readRssiAsync()
                .continueWith(new Continuation<Integer, Void>() {
                    @Override
                    public Void then(Task<Integer> task) throws Exception {
                        Log.i("MainActivity", "Signal Strength: " + task.getResult());
                        final Task<Integer> mTask = task;
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mSignal.setText("Signal: " + mTask.getResult());
                            }
                        });
                        return null;
                    }}
                );
    }

    public void blinkLed(Led.Color color, int count) {
        Led led;
        if ((led = board.getModule(Led.class)) != null) {
            led.editPattern(color, Led.PatternPreset.BLINK)
                    .repeatCount((byte) count)
                    .commit();
            led.play();
        }
    }

    public MetaWearBoard getBoard() {
        return board;
    }

    public File getFile() {
        return fileCreator.getFile();
    }

    public void destroyService() {
        // Unbind the service when the activity is destroyed
        activity.getApplicationContext().unbindService(this);
    }

    public void destroy() {
        board.tearDown();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        // Typecast the binder to the service's LocalBinder class
        serviceBinder = (BtleService.LocalBinder) service;
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) { }
}
