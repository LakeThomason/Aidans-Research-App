package com.example.lakethomason.bluetoothinteractiontest;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class AddSubjectDialogFragment extends DialogFragment {
    public interface NoticeDialogListener {
        void onDialogPositiveClick(String age, String weight, String identifier);
        void onDialogNegativeClick();
    }
    // Use this instance of the interface to deliver action events
    NoticeDialogListener mListener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (NoticeDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialog = inflater.inflate(R.layout.add_subject_view, null);
        final EditText weightEditText = dialog.findViewById(R.id.weightEditText);
        final EditText ageEditText = dialog.findViewById(R.id.ageEditText);
        final EditText identifierEditText = dialog.findViewById(R.id.identifierEditText);
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Enter the required attributes *")
                .setPositiveButton("Add Subject", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String weight = weightEditText.getText().toString();
                        String age = ageEditText.getText().toString();
                        String identifier = identifierEditText.getText().toString();
                        mListener.onDialogPositiveClick(age, weight, identifier);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogNegativeClick();
                    }
                })
                .setView(dialog);
        // Create the AlertDialog object and return it
        return builder.create();
    }
}