package com.example.lakethomason.bluetoothinteractiontest;

import android.os.Environment;
import android.util.Log;

import com.mbientlab.metawear.Data;
import com.mbientlab.metawear.data.EulerAngles;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

/**
 * Created by lakethomason on 1/31/2018.
 */

public class FileCreator {
    private File dir;
    private FileWriter writer;
    private File file;

    public FileCreator(String directoryName) {
        dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                , "MetawearFiles");
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.d("MainActivity", "Directory creation has failed");
            }
        }
        //prepare the datafile to write to
        try {
            file = new File(dir, "MetawearCSV__" + //TODO: convert MetawearCSV to test patient id
                    DateFormat.getDateTimeInstance().format(new Date()) +
                    "__.csv");
            file.createNewFile();
            writer = new FileWriter(file);
            appendLineToCSV("Elapsed Time(s),x-axis(deg/s),y-axis(deg/s),z-axis(deg/s)");
        }
        catch (IOException e) {
            Log.d("MainActivity", "IOException");
            e.getMessage();
            e.printStackTrace();
        }
    }

    public void appendLineToCSV(String data) {
        try {
            writer.append(data);
            writer.flush();
        }
        catch (java.io.IOException e) {
            Log.d("IOException", "File write failed: ");
            e.printStackTrace();
        }
    }

    public boolean closeFile() {
        try {
            writer.close();
            return true;
        }
        catch (IOException e) {
            Log.d("IOException", "File write failed: ");
            e.printStackTrace();
            return false;
        }
    }

    public File getFile(){
        return this.file;
    }

}
