package com.example.lakethomason.bluetoothinteractiontest;

import java.io.File;
import java.util.Random;

/**
 * Created by lakethomason on 2/8/2018.
 */

public class TestSubject {
    public int mAge;
    public int mWeight;
    public String mIdentifier;
    public File mPolarFile;
    public File mMetaWearFile;

    private String[] Beginning = { "Kr", "Ca", "Ra", "Mrok", "Cru", "Ray", "Bre", "Zed", "Drak", "Mor", "Jag", "Mer", "Jar", "Mjol", "Zork", "Mad", "Cry", "Zur", "Creo", "Azak", "Azur", "Rei", "Cro", "Mar", "Luk" };
    private String[] Middle = { "air", "ir", "mi", "sor", "mee", "clo","red", "cra", "ark", "arc", "miri", "lori", "cres", "mur", "zer","marac", "zoir", "slamar", "salmar", "urak" };
    private String[] End = { "d", "ed", "ark", "arc", "es", "er", "der","tron", "med", "ure", "zur", "cred", "mur" };

    private Random rand = new Random();

    TestSubject(String identifier, int age, int weight) {
        mAge = age;
        mWeight = weight;
        mIdentifier = identifier != null ? identifier : generateName();
        mMetaWearFile = null;
        mPolarFile = null;
    }

    TestSubject() {
        mAge = new Random().nextInt(100);
        mWeight = new Random().nextInt(500);
        mIdentifier = generateName();
        mMetaWearFile = null;
        mPolarFile = null;
    }

    public String generateName() {
        return Beginning[rand.nextInt(Beginning.length)] +
                Middle[rand.nextInt(Middle.length)] +
                End[rand.nextInt(End.length)];
    }

    public void addPolarTest(File polarFile) {
        mPolarFile = polarFile;
    }

    public void addMetaWearTest(File metaWearFile) {
        mMetaWearFile = metaWearFile;
    }
}