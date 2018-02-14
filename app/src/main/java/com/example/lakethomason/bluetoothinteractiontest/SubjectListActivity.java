package com.example.lakethomason.bluetoothinteractiontest;

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
import android.widget.ListView;
import android.widget.TextView;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.List;

public class SubjectListActivity extends AppCompatActivity {

    private ArrayAdapter<String> arrayAdapter;
    private ListView mSubjectListView;
    private TestSubjectList mSubjectList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject_list);

        mSubjectList = new TestSubjectList();

        mSubjectListView = findViewById(R.id.subjectListView);
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mSubjectList.getSubjectList());
        mSubjectListView.setAdapter(arrayAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FireMissilesDialogFragment f = new FireMissilesDialogFragment();
                f.show(getFragmentManager(), "SubjectInput");
            }
        });
    }
    public static class FireMissilesDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Enter the required attributes *")
                    .setPositiveButton("Add Subject", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // FIRE ZE MISSILES!
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    })
                    .setView(R.layout.addsubjectview); //left off here :D
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }
}


