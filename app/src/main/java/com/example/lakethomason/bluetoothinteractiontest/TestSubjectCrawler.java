package com.example.lakethomason.bluetoothinteractiontest;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lakethomason on 3/13/2018.
 */

public class TestSubjectCrawler {

    private File[] files;
    private File[] tests;
    private TestSubjectList testSubjectList;

    private File dir;
    private String[] details;
    private int currFileNum;
    private int layer;

    public TestSubjectCrawler() {
        dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                , "Aidans Research App/");
        files = dir.listFiles();
        details = null;
        tests = new File[7];
        currFileNum = -1;
        layer = 0;
        testSubjectList = TestSubjectList.getInstance();
    }

    public void populateTestSubjectList() {
        recurseFiles(files);
    }

    private void recurseFiles(File[] files) {
        layer++;
        for (File file : files) {
            if (file.isDirectory()) {
                currFileNum = checkTestNum(file);
                recurseFiles(file.listFiles());
            } else {
                if (file.getName().equals("details.csv")){
                    details = getTestSubjectDetails(file);
                }
                else {
                    tests[currFileNum] = file;
                    currFileNum++;
                }
            }
        }
        layer--;
        if (layer == 1 && details != null) {
            testSubjectList.addSubject(details[0]
                    , Integer.valueOf(details[1])
                    , Integer.valueOf(details[2])
                    , tests);
            details = null;
            tests = new File[7];
        }
    }

    private String[] getTestSubjectDetails(File file) {
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";

        try {

            br = new BufferedReader(new FileReader(file));
            br.readLine();
            if ((line = br.readLine()) != null) {
                return line.split(cvsSplitBy);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private int checkTestNum(File file) {
        switch (file.getName()) {
            case "Test1" :
                return 0;
            case "Test2" :
                if (tests[1] != null ){
                    return 2;
                }
                else {
                    return 1;
                }
            case "Test3" :
                return 3;
            case "Test4" :
                if (tests[4] != null ){
                    return 5;
                }
                else {
                    return 4;
                }
            case "Test5" :
                return 6;
        }
        return -1;
    }
}
