package com.example.lakethomason.bluetoothinteractiontest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lakethomason on 2/8/2018.
 */

public class TestSubjectList {
    public ArrayList<String> mNameList;
    private ArrayList<TestSubject> mTestSubjects;

    private static final TestSubjectList instance = new TestSubjectList();

    public static TestSubjectList getInstance() {
        return instance;
    }

    private TestSubjectList() {
        mTestSubjects = new ArrayList<TestSubject>();
        mNameList = new ArrayList<String>();
    }

    public TestSubject getSubject(int pos) {
        return mTestSubjects.get(pos);
    }

    public TestSubject addSubject(String identifier, int weight, int age) {
        TestSubject testSubject = new TestSubject(identifier, age, weight);
        if (!mNameList.contains(testSubject.getIdentifier())) {
            mNameList.add(testSubject.getIdentifier());
            mTestSubjects.add(testSubject);
        }
        return testSubject;
    }

    public void addSubject(String identifier, int weight, int age, File[] files) {
        if (identifier == null || files == null){
            return;
        }
        TestSubject testSubject = addSubject(identifier, weight, age);
        testSubject.addTests(files);
    }

    public void removeSubject(int pos)
    {
        mTestSubjects.get(pos).removeAllTests();
        mTestSubjects.remove(pos);
        mNameList.remove(pos);
    }

    public void addTestToSubject(int index, File file, int testNum) {
        mTestSubjects.get(index).addTest(file, testNum);
    }
}
