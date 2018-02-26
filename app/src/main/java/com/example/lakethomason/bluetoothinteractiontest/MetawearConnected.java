package com.example.lakethomason.bluetoothinteractiontest;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.mbientlab.metawear.Data;
import com.mbientlab.metawear.DeviceInformation;
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
import com.mbientlab.metawear.module.Settings;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;


/**
 * Created by lakethomason on 1/11/2018.
 */

public class MetawearConnected implements ServiceConnection {
    private Logging logging;
    private SensorFusionBosch sensorFusion;
    private MetaWearBoard board;
    private BtleService.LocalBinder serviceBinder;

    private CheckBox mMetawearCheckBox;
    private Button mLogButton;
    private TextView mBattery;
    private TextView mSignal;

    private long startTime;

    private Activity activity;
    private EasyToast easyToast;
    private FileCreator fileCreator;

    public MetawearConnected(Activity _activity, EasyToast _easyToast) {
        activity = _activity;
        easyToast = _easyToast;
        mMetawearCheckBox =  activity.findViewById(R.id.metawearCheckBox);
        mLogButton  = activity.findViewById(R.id.logMetaWearButton);
        mSignal = activity.findViewById(R.id.signalMetaWear);
        startTime = -1;
        activity.getApplicationContext().bindService(new Intent(activity, BtleService.class), this, Context.BIND_AUTO_CREATE);
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
                    //state = State.MetaConnected; TODO: incorporate state
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            mMetawearCheckBox.setChecked(true);
                            mLogButton.setVisibility(View.VISIBLE);
                            easyToast.makeToast("Connected to " + deviceName);
                        }
                    });
                    blinkLed(Led.Color.GREEN, 10);
                    logging = board.getModule(Logging.class);
                    sensorFusion = board.getModule(SensorFusionBosch.class);
                    new updateRSSI().execute();
                    if (logging == null || sensorFusion == null) {
                        Log.d("Metawear", "A module was null");
                    }
                }
                return null;
            }
        });

        //set an unexpected disconnect listener
        board.onUnexpectedDisconnect(new MetaWearBoard.UnexpectedDisconnectHandler() {
            @Override
            public void disconnected(int status) {
                Log.d("MainActivity", "Unexpectedly lost connection: " + status);
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

    public void beginLogging() {
        logging.clearEntries();
        blinkLed(Led.Color.BLUE, 10);
        mLogButton.setText("Stop");
        fileCreator = new FileCreator("Lake", "Metawear");//TODO: introduce subject names
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
    }

    public void stopLogging(final Runnable sendCSV) {
        //logging.stop();
        sensorFusion.stop();
        sensorFusion.eulerAngles().stop();
        blinkLed(Led.Color.BLUE, 5);
        mLogButton.setText("Log");
        // state = MainActivity.State.ReadyToLog; TODO:state
        easyToast.makeToast("Starting Download");

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
                easyToast.makeToastOnUIThread("Download Complete");
                blinkLed(Led.Color.GREEN, 10);
                logging.clearEntries();
                fileCreator.closeFile();
                sendCSV.run();
                return null;
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

        //Battery level reading example 1
        //Uses settings module to attempt to read battery level
        //Always outputs 0

//        final Settings settings = board.getModule(Settings.class);
//        settings.battery().addRouteAsync(new RouteBuilder() {
//            @Override
//            public void configure(RouteComponent source) {
//                source.stream(new Subscriber() {
//                    @Override
//                    public void apply(Data data, Object ... env) {
//                        Log.i("MainActivity", "battery state = " + data.value(Settings.BatteryState.class));
//                        mBattery.setText(
//                                "Battery: "
//                                + data.value(Settings.BatteryState.class).charge
//                                + "%" );
//                    }
//                });
//            }
//        }).continueWith(new Continuation<Route, Void>() {
//            @Override
//            public Void then(Task<Route> task) throws Exception {
//                settings.battery().read();
//                return null;
//            }
//        });
//

        //Second battery level read attempt
        //Utilizes onboard operations to attempt to read battery levels
        //Always outputs 0

//        board.readBatteryLevelAsync()
//                .continueWith(new Continuation<Byte, Void>() {
//                    @Override
//                    public Void then(Task<Byte> task) throws Exception {
//                        Log.i("MainActivity", "Battery Charge: " + task.getResult());
//                        final Task<Byte> mTask = task;
////                        activity.runOnUiThread(new Runnable() {
////                            @Override
////                            public void run() {
////                                mBattery.setText("Battery: " + mTask.getResult() + "%" );
////                            }
////                        });
//                        return null;
//                    }}
//                );
    }

    private void emailCSV() {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        // set the type to 'email'
        emailIntent .setType("text/plain");
        // add email(s) here to whom you want to send email
        String to[] = {"lakesainthomason@gmail.com"}; //TODO email input
        emailIntent .putExtra(Intent.EXTRA_EMAIL, to);
        // convert file to uri
        Uri uri = Uri.fromFile(fileCreator.getDir());
        // add the attachment
        emailIntent .putExtra(Intent.EXTRA_STREAM, uri);
        // add mail subject
        emailIntent .putExtra(Intent.EXTRA_SUBJECT, fileCreator.getDir().getName());
        // create mail service chooser
        activity.startActivity(Intent.createChooser(emailIntent, "Save results"));
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
