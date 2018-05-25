package com.example.lakethomason.bluetoothinteractiontest;

import android.app.DialogFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class SubjectListActivity extends AppCompatActivity
        implements AddSubjectDialogFragment.NoticeDialogListener
        , SubjectClickedDialogFragment.SubjectClickedDialogListener
        , KeepTestFragment.KeepTestListener
    {

    private ArrayAdapter<String> arrayAdapter;
    private ListView mSubjectListView;
    private TestSubjectList mSubjectList;
    private EasyToast easyToast;
    private AddSubjectDialogFragment AddSubjectDF;
    private DialogFragment subjectClickedFragment;
    private DialogFragment keepTestFragment;
    private Metawear metawearDevice;
    private PolarH7 polarH7;
    private Button mStopButton;
    private TextView mTimeRemainingText;
    private CountDownTimer timer;
    private int currentSubject;
    private int testBeingLogged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject_list);

        metawearDevice = Metawear.getInstance();
        metawearDevice.setActivity(this);

        polarH7 = PolarH7.getInstance();
        polarH7.setActivity(this);

        mSubjectList = TestSubjectList.getInstance();

        easyToast = new EasyToast(SubjectListActivity.this);
        mSubjectListView = findViewById(R.id.subjectListView);
        mStopButton = findViewById(R.id.stopButton);
        mStopButton.setVisibility(View.INVISIBLE);
        mTimeRemainingText = findViewById(R.id.timeRemainingText);
        timer = null;

        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mSubjectList.mNameList);
        mSubjectListView.setAdapter(arrayAdapter);
        AddSubjectDF = new AddSubjectDialogFragment();
        keepTestFragment = new KeepTestFragment();

        mSubjectListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listItemClicked(position);
            }
        });
        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keepTestFragment.show(getFragmentManager(), "KeepTest");
                stopTimer();
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddSubjectDF.show(getFragmentManager(), "SubjectInput");
            }
        });
    }

    private void listItemClicked(int index) {
        currentSubject = index;
        subjectClickedFragment = SubjectClickedDialogFragment.newInstance(mSubjectList.getSubject(index));
        subjectClickedFragment.show(getFragmentManager(), "SubjectClickedDialog");
    }

    private void startTimer(int time) {
        timer = new CountDownTimer(time, 1000) {

            public void onTick(long millisUntilFinished) {
                mTimeRemainingText.setText( "Time: "
                        + String.format("%d: %02d",
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))
                ));
            }

            public void onFinish() {
                saveTest();
            }
        }.start();
    }

    private void stopTimer(){
        if (timer != null)
            timer.cancel();
        mTimeRemainingText.setText("Time: 0:00");
    }

    private void saveTest() {
        if (testBeingLogged == 1 || testBeingLogged == 3) {
            metawearDevice.stopLogging(new Runnable() {
                @Override
                public void run() {
                    mSubjectList.addTestToSubject(currentSubject, metawearDevice.getFile(), getArrayTestNum(true, metawearDevice.getTestNum()));
                }
            });
        }
        polarH7.stopLogging(new Runnable() {
            @Override
            public void run() {
                mSubjectList.addTestToSubject(currentSubject, polarH7.getFile(), getArrayTestNum(false, testBeingLogged));
            }
        });
        mStopButton.setVisibility(View.INVISIBLE);
    }

    private void tossTest(){
        if (testBeingLogged == 1 || testBeingLogged == 3){
            metawearDevice.stopLoggingAndDestroy();
        }
        polarH7.stopLoggingAndDestroy();
        mStopButton.setVisibility(View.INVISIBLE);
        easyToast.makeToast("The log was deleted");
        stopTimer();
    }

    private int getArrayTestNum(boolean isMetawear, int testNum) {
        if (isMetawear) {
            switch (testNum) {
                case 1:
                    return 1;
                case 3:
                    return 4;
            }
        }
        else
            switch (testNum) {
                case 0:
                    return 0;
                case 1:
                    return 2;
                case 2:
                    return 3;
                case 3:
                    return 5;
                case 4:
                    return 6;
            }
        return -1;
    }

    /*
     * AddSubjectDialogListener Inheritance
     */

    @Override
    public void onDialogPositiveClick(String age, String weight, String identifier) {
        if (age.equals("") || weight.equals("")) {
            easyToast.makeToast("Do not leave required fields blank");
            AddSubjectDF.dismiss();
            AddSubjectDF.show(getFragmentManager(), "SubjectInput");
            return;
        }
        String id = identifier;
        if (id.equals("")) {
            id = null;
        }
        mSubjectList.addSubject(id, Integer.valueOf(weight), Integer.valueOf(age));
        arrayAdapter.notifyDataSetChanged();
        easyToast.makeToast("Test Subject Added successfully");
    }

    @Override
    public void onDialogNegativeClick() {
        easyToast.makeToast("Cancelled");
    }

    /*
     * SubjectClickedListener inheritance
     */

    @Override
    public void onRemoveSubjectClick() {
        easyToast.makeToast(mSubjectList.getSubject(currentSubject).getIdentifier() + " was removed");
        mSubjectList.removeSubject(currentSubject);
        arrayAdapter.notifyDataSetChanged();
        tossTest();
        subjectClickedFragment.dismiss();
    }

    @Override
    public void onRemoveTestClick(int testNum) {
        TestSubject subject = mSubjectList.getSubject(currentSubject);
        switch (testNum) {
            case 0:
                subject.removeTest(0);
                break;
            case 1:
                subject.removeTest(1);
                subject.removeTest(2);
                break;
            case 2:
                subject.removeTest(3);
                break;
            case 3:
                subject.removeTest(4);
                subject.removeTest(5);
                break;
            case 4:
                subject.removeTest(6);
                break;
        }
        easyToast.makeToast("Test " + (testNum + 1) + " was removed");
    }

    @Override
    public void onLogClicked(int testNum){
        TestSubject subject = mSubjectList.getSubject(currentSubject);
        if (testNum == 0 || testNum == 2 || testNum == 4 || metawearDevice.beginLogging(subject.getIdentifier(), testNum)) {
            polarH7.beginLogging(subject.getIdentifier(), testNum);
            mStopButton.setVisibility(View.VISIBLE);
            easyToast.makeToast("Logging has started");
            testBeingLogged = testNum;
            startTimer((testBeingLogged == 2) ? 420000 : 300000);
        }
        subjectClickedFragment.dismiss();
    }

    @Override
    public void onDownloadClicked(){
        subjectClickedFragment.dismiss();
        File[] fileList = mSubjectList.getSubject(currentSubject).getTests();
        Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        // set the type to 'email'
        emailIntent .setType("text/plain");
        // add email(s) here to whom you want to send email
        String to = "mcguinnea@sou.edu";
        emailIntent .putExtra(Intent.EXTRA_EMAIL, to);
        // create array to store files
        ArrayList<Uri> uris = new ArrayList<Uri>(); // URI: Uniform Resource Identifier
        for (File file : fileList){
            // convert file to uri
            if (file != null) {
                uris.add(Uri.fromFile(file));
            }
        }
        // add the attachment
        emailIntent .putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        // add mail subject
        emailIntent .putExtra(Intent.EXTRA_SUBJECT, "Saved files");
        // create mail service chooser
        this.startActivity(Intent.createChooser(emailIntent, "Save results"));
    }

        @Override
        public void onKeepTestClick() {
            saveTest();
            timer.cancel();
        }

        @Override
        public void onTossItClick() {
            tossTest();
        }
    }


