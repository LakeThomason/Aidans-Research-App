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


    //creating log files
    public FileCreator(String subjectName, String deviceName, int testNumber) {
        dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                , "Aidans Research App/" + subjectName + "/Test" + (testNumber + 1));
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.d("MainActivity", "Directory creation has failed");
            }
        }
        //prepare the datafile to write to
        try {
            file = new File(dir, deviceName + "_" +
                    DateFormat.getDateTimeInstance().format(new Date()) +
                    "_.csv");
            file.createNewFile();
            writer = new FileWriter(file);
        }
        catch (IOException e) {
            Log.d("MainActivity", "IOException");
            e.getMessage();
            e.printStackTrace();
        }
    }

    //creating details file
    public FileCreator(String subjectName) {
        dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                , "Aidans Research App/" + subjectName + "/");
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.d("MainActivity", "Directory creation has failed");
            }
        }
        try {
            file = new File(dir, "details.csv");
            file.createNewFile();
            writer = new FileWriter(file);
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
            Log.d("IOException", "File close failed: ");
            e.printStackTrace();
            return false;
        }
    }

    public void deleteFile() {
        if (file.exists()){
            file.delete();
        }
    }

    public File getFile(){
        return this.file;
    }

    public File getDir() {
        return this.dir;
    }

}
