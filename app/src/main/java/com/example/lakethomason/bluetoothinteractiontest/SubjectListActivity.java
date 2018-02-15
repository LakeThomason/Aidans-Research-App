package com.example.lakethomason.bluetoothinteractiontest;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.List;

public class SubjectListActivity extends AppCompatActivity implements AddSubjectDialogFragment.NoticeDialogListener {

    private ArrayAdapter<String> arrayAdapter;
    private ListView mSubjectListView;
    private TestSubjectList mSubjectList;
    private EasyToast easyToast;
    private AddSubjectDialogFragment AddSubjectDF;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject_list);

        easyToast = new EasyToast(SubjectListActivity.this);

        mSubjectList = new TestSubjectList();
        mSubjectListView = findViewById(R.id.subjectListView);
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mSubjectList.nameList);
        mSubjectListView.setAdapter(arrayAdapter);
        AddSubjectDF = new AddSubjectDialogFragment();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddSubjectDF.show(getFragmentManager(), "SubjectInput");
            }
        });
    }
    @Override
    public void onDialogPositiveClick(String age, String weight, String identifier) {
        String id = identifier;
        if (age.equals("") || weight.equals("")) {
            easyToast.makeToast("Do not leave required fields blank");
            AddSubjectDF.dismiss();
            AddSubjectDF.show(getFragmentManager(), "SubjectInput");
            return;
        }
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
}


