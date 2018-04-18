package com.example.lakethomason.bluetoothinteractiontest;

import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by lakethomason on 2/8/2018.
 */

public class TestSubject implements Parcelable {
    public int mAge;
    public int mWeight;
    public String mIdentifier;
    public File[] mFileList;
    public File mDetails;
    /*
     * mFileList structure
     * 0 PolarH7 Test
     * 1 MetaWear Test
     * 2 PolarH7 Test
     * 3 PolarH7 Test
     * 4 MetaWear Test
     * 5 PolarH7 Test
     * 6 PolarH7 Test
     * Tests 1&2, 4&5 are performed at the same time
     */

    private String[] Beginning = { "Kr", "Ca", "Ra", "Mrok", "Cru", "Ray", "Bre", "Zed", "Drak", "Mor", "Jag", "Mer", "Jar", "Mjol", "Zork", "Mad", "Cry", "Zur", "Creo", "Azak", "Azur", "Rei", "Cro", "Mar", "Luk" };
    private String[] Middle = { "air", "ir", "mi", "sor", "mee", "clo","red", "cra", "ark", "arc", "miri", "lori", "cres", "mur", "zer","marac", "zoir", "slamar", "salmar", "urak" };
    private String[] End = { "d", "ed", "ark", "arc", "es", "er", "der","tron", "med", "ure", "zur", "cred", "mur" };

    private Random rand = new Random();

    TestSubject(String identifier, int age, int weight) {
        mAge = age;
        mWeight = weight;
        mIdentifier = identifier != null ? identifier : generateName();
        mFileList = new File[7];
        mDetails = makeDetails(mIdentifier, age, weight);
    }

    TestSubject(Parcel in) {
        this.mAge = in.readInt();
        this.mWeight = in.readInt();
        this.mIdentifier = in.readString();
        this.mFileList = (File[]) in.readArray(TestSubject.class.getClassLoader());
    }

    public String generateName() {
        return Beginning[rand.nextInt(Beginning.length)] +
                Middle[rand.nextInt(Middle.length)] +
                End[rand.nextInt(End.length)];
    }

    public void addTest(File file, int pos) {
        mFileList[pos] = file;
    }

    public void addTests(File[] files) {
        mFileList = files;
    }

    public File getTest(int test) {
        return mFileList[test];
    }

    public File[] getTests() {
        return mFileList;
    }

    public Boolean getTestAvailability(int index) {
        return mFileList[index] == null;
    }

    public ArrayList<Boolean> getTestsAvailability() {
        ArrayList<Boolean> fileExistsList = new ArrayList<Boolean>(mFileList.length);
        for (int i = 0; i < mFileList.length; i++) {
            if (mFileList[i] != null)
                fileExistsList.add(i, true);
            else
                fileExistsList.add(i, false);
        }
        return fileExistsList;
    }

    public void removeTest(int test) {
        if (mFileList[test] != null) {
            mFileList[test].delete();
        }
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                , "Aidans Research App/" + mIdentifier + "/Test" + (test + 1));
        dir.delete();
        mFileList[test] = null;
    }

    public void removeAllTests() {
        for (File file : mFileList) {
            if (file != null) {
                file.delete();
                file.getParentFile().delete();
            }
        }
        if (mDetails != null) {
            mDetails.delete();
        }
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                , "Aidans Research App/" + mIdentifier);
        dir.delete();
        mFileList = new File[7];
    }

    public String getIdentifier() {
        return mIdentifier;
    }

    private File makeDetails(String identifier, int age, int weight) {
        FileCreator fc = new FileCreator(identifier);
        fc.appendLineToCSV("Name,Age,Weight\n");
        fc.appendLineToCSV(identifier + "," + age + "," + weight);
        fc.closeFile();
        return fc.getFile();
    }

    public static final Parcelable.Creator<TestSubject> CREATOR
            = new Parcelable.Creator<TestSubject>() {
        public TestSubject createFromParcel(Parcel in) {
            return new TestSubject(in);
        }

        public TestSubject[] newArray(int size) {
            return new TestSubject[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mAge);
        out.writeInt(this.mWeight);
        out.writeString(this.mIdentifier);
        out.writeArray(this.mFileList);
    }
}