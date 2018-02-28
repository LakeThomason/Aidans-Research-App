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

    TestSubjectList() {
        mTestSubjects = new ArrayList<TestSubject>();
        mNameList = new ArrayList<String>();
        generateTestSubjects(50);
    }

    private void generateTestSubjects(int numSubjects) {
        for (int i = 0; i < numSubjects; i++){
            TestSubject testSubject = new TestSubject();
            mTestSubjects.add(testSubject);
            mNameList.add(testSubject.getIdentifier());
        }
    }

    public TestSubject getSubject(int pos) {
        return mTestSubjects.get(pos);
    }

    public void addSubject(String identifier, int weight, int age) {
        TestSubject testSubject = new TestSubject(identifier, age, weight);
        mTestSubjects.add(testSubject);
        mNameList.add(testSubject.getIdentifier());
    }

    public void addSubjects(List<TestSubject> subjects) {
        mTestSubjects.addAll(subjects);
    }

    public void removeSubject(int pos)
    {
        mTestSubjects.get(pos).removeAllTests();
        mTestSubjects.remove(pos);
        mNameList.remove(pos);
    }

    public void addTestToSubject(int pos, File file, int testNum) {
        mTestSubjects.get(pos).addTest(file, testNum);
    }
}
