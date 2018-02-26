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
    public ArrayList<String> nameList;
    private ArrayList<TestSubject> testSubjects;

    TestSubjectList() {
        testSubjects = new ArrayList<TestSubject>();
        nameList = new ArrayList<String>();
        generateTestSubjects(50);
    }

    private void generateTestSubjects(int numSubjects) {
        for (int i = 0; i < numSubjects; i++){
            TestSubject testSubject = new TestSubject();
            testSubjects.add(testSubject);
            nameList.add(testSubject.getIdentifier());
        }
    }

    public TestSubject getSubject(int pos) {
        return testSubjects.get(pos);
    }

    public void addSubject(String identifier, int weight, int age) {
        TestSubject testSubject = new TestSubject(identifier, age, weight);
        testSubjects.add(testSubject);
        nameList.add(testSubject.getIdentifier());
    }

    public void addSubjects(List<TestSubject> subjects) {
        testSubjects.addAll(subjects);
    }

    public void removeSubject(int pos)
    {
        testSubjects.remove(pos);
        nameList.remove(pos);
    }

    public void addTestToSubject(int pos, File file, int testNum) {
        testSubjects.get(pos).addTest(file, testNum);
    }

    public ArrayList<String> getSubjectList() {
        ArrayList<String> names = new ArrayList<String>();
        for (TestSubject testSubject : testSubjects) {
            names.add(testSubject.mIdentifier);
        }
        return names;
    }
}
