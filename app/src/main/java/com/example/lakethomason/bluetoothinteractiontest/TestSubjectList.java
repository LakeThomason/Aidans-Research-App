package com.example.lakethomason.bluetoothinteractiontest;

import junit.framework.Test;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by lakethomason on 2/8/2018.
 */

public class TestSubjectList {
    private ArrayList<TestSubject> testSubjects;

    TestSubjectList() {
        testSubjects = new ArrayList<TestSubject>();
        generateTestSubjects(50);
    }

    private void generateTestSubjects(int numSubjects) {
        for (int i = 0; i < numSubjects; i++){
            testSubjects.add(new TestSubject());
        }
    }

    public void addSubject(String identifier, int weight, int age) {
        testSubjects.add(new TestSubject(identifier, age, weight));
    }

    public void removeSubject(int pos) {
        testSubjects.remove(pos);
    }

    public void addPolarTestToSubject(int pos, File polarFile) {
        testSubjects.get(pos).addPolarTest(polarFile);
    }

    public void addMetaWearTestToSubject(int pos, File metaWearFile) {
        testSubjects.get(pos).addMetaWearTest(metaWearFile);
    }

    public ArrayList<String> getSubjectList() {
        ArrayList<String> names = new ArrayList<String>();
        for (TestSubject testSubject : testSubjects) {
            names.add(testSubject.mIdentifier);
        }
        return names;
    }
}
