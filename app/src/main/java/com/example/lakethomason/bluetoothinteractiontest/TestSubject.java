package com.example.lakethomason.bluetoothinteractiontest;

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
    public ArrayList<File> mFileList;

    private String[] Beginning = { "Kr", "Ca", "Ra", "Mrok", "Cru", "Ray", "Bre", "Zed", "Drak", "Mor", "Jag", "Mer", "Jar", "Mjol", "Zork", "Mad", "Cry", "Zur", "Creo", "Azak", "Azur", "Rei", "Cro", "Mar", "Luk" };
    private String[] Middle = { "air", "ir", "mi", "sor", "mee", "clo","red", "cra", "ark", "arc", "miri", "lori", "cres", "mur", "zer","marac", "zoir", "slamar", "salmar", "urak" };
    private String[] End = { "d", "ed", "ark", "arc", "es", "er", "der","tron", "med", "ure", "zur", "cred", "mur" };

    private Random rand = new Random();

    TestSubject(String identifier, int age, int weight) {
        mAge = age;
        mWeight = weight;
        mIdentifier = identifier != null ? identifier : generateName();
        mFileList = new ArrayList<File>(5);
    }

    TestSubject(Parcel in) {
        this.mAge = in.readInt();
        this.mWeight = in.readInt();
        this.mIdentifier = in.readString();
        this.mFileList = in.readArrayList(TestSubject.class.getClassLoader());
    }

    TestSubject() {
        mAge = new Random().nextInt(100);
        mWeight = new Random().nextInt(500);
        mIdentifier = generateName();
        mFileList = new ArrayList<File>(5);
    }

    public String generateName() {
        return Beginning[rand.nextInt(Beginning.length)] +
                Middle[rand.nextInt(Middle.length)] +
                End[rand.nextInt(End.length)];
    }

    public void addTest(File file, int pos) {
        mFileList.add(pos, file);
    }

    public String getIdentifier() {
        return mIdentifier;
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
        out.writeList(this.mFileList);
    }
}