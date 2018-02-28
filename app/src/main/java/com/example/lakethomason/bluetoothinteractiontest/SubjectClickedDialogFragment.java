package com.example.lakethomason.bluetoothinteractiontest;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import java.util.ArrayList;

public class SubjectClickedDialogFragment extends DialogFragment implements View.OnClickListener {

    private TestSubject mTestSubject;
    SubjectClickedDialogListener mListener;

    public interface SubjectClickedDialogListener {
        void onRemoveSubjectClick();
        void onRemoveTestClick(int pos);
        void onLogClicked(int pos);
        void onDownloadClicked();

    }

    static SubjectClickedDialogFragment newInstance(TestSubject subject) {
        SubjectClickedDialogFragment SubjectClickedDF = new SubjectClickedDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable("TestSubject", subject);
        SubjectClickedDF.setArguments(args);
        return SubjectClickedDF;
    }

    public void setCheckBoxes(View dialog) {
        ArrayList<Boolean> checkList = mTestSubject.getTestsAvailability();
        final CheckBox checkBox1 = dialog.findViewById(R.id.checkBox1);
        checkBox1.setChecked(checkList.get(0));
        final CheckBox checkBox2 = dialog.findViewById(R.id.checkBox2);
        checkBox2.setChecked(checkList.get(1) && checkList.get(2));
        final CheckBox checkBox3 = dialog.findViewById(R.id.checkBox3);
        checkBox3.setChecked(checkList.get(3));
        final CheckBox checkBox4 = dialog.findViewById(R.id.checkBox4);
        checkBox4.setChecked(checkList.get(4) && checkList.get(5));
        final CheckBox checkBox5 = dialog.findViewById(R.id.checkBox5);
        checkBox5.setChecked(checkList.get(6));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTestSubject = getArguments().getParcelable("TestSubject");

    }

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (SubjectClickedDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement SubjectClickedDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialog = inflater.inflate(R.layout.subject_clicked_fragment, null);

        //Remove Subject Button
        final Button removeSubjectButton = dialog.findViewById(R.id.removeSubjectButton);
        removeSubjectButton.setOnClickListener(this);

        //Remove Test Buttons
        final Button removeButton1 = dialog.findViewById(R.id.removeButton1);
        removeButton1.setOnClickListener(this);
        final Button removeButton2 = dialog.findViewById(R.id.removeButton2);
        removeButton2.setOnClickListener(this);
        final Button removeButton3 = dialog.findViewById(R.id.removeButton3);
        removeButton3.setOnClickListener(this);
        final Button removeButton4 = dialog.findViewById(R.id.removeButton4);
        removeButton4.setOnClickListener(this);
        final Button removeButton5 = dialog.findViewById(R.id.removeButton5);
        removeButton5.setOnClickListener(this);

        //Log buttons
        final Button logButton1 = dialog.findViewById(R.id.logButton1);
        logButton1.setOnClickListener(this);
        final Button logButton2 = dialog.findViewById(R.id.logButton2);
        logButton2.setOnClickListener(this);
        final Button logButton3 = dialog.findViewById(R.id.logButton3);
        logButton3.setOnClickListener(this);
        final Button logButton4 = dialog.findViewById(R.id.logButton4);
        logButton4.setOnClickListener(this);
        final Button logButton5 = dialog.findViewById(R.id.logButton5);
        logButton5.setOnClickListener(this);

        //Download Button
        final Button downloadButton = dialog.findViewById(R.id.downloadButton);
        downloadButton.setOnClickListener(this);

        //Set CheckBoxes
        setCheckBoxes(dialog);

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(mTestSubject.getIdentifier())
                .setView(dialog);
        // Create the AlertDialog object and return it
        return builder.create();
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.removeSubjectButton:
                mListener.onRemoveSubjectClick();
                break;
            case R.id.removeButton1:
                mListener.onRemoveTestClick(1);
                break;
            case R.id.removeButton2:
                mListener.onRemoveTestClick(2);
                break;
            case R.id.removeButton3:
                mListener.onRemoveTestClick(3);
                break;
            case R.id.removeButton4:
                mListener.onRemoveTestClick(4);
                break;
            case R.id.removeButton5:
                mListener.onRemoveTestClick(5);
                break;
            case R.id.logButton1:
                mListener.onLogClicked(1);
                break;
            case R.id.logButton2:
                mListener.onLogClicked(2);
                break;
            case R.id.logButton3:
                mListener.onLogClicked(3);
                break;
            case R.id.logButton4:
                mListener.onLogClicked(4);
                break;
            case R.id.logButton5:
                mListener.onLogClicked(5);
                break;
            case R.id.downloadButton:
                mListener.onDownloadClicked();
                break;
        }
    }
}