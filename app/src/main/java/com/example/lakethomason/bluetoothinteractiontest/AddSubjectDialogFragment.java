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
    NoticeDialogListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (NoticeDialogListener) context;
        } catch (ClassCastException e) {
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
        return builder.create();
    }
}