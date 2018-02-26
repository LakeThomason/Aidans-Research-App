package com.example.lakethomason.bluetoothinteractiontest;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SubjectClickedDialogFragment extends DialogFragment {

    private TestSubject testSubject;
    private int index;

    public interface SubjectClickedDialogListener {
        void onDialogRemoveSubjectClick(int pos);
    }
    static SubjectClickedDialogFragment newInstance(TestSubject subject, int pos) {
        SubjectClickedDialogFragment SubjectClickedDF = new SubjectClickedDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable("TestSubject", subject);
        args.putInt("Position", pos);
        SubjectClickedDF.setArguments(args);
        return SubjectClickedDF;
    }


    // Use this instance of the interface to deliver action events
    SubjectClickedDialogListener mListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        testSubject = getArguments().getParcelable("TestSubject");
        index = getArguments().getInt("Position");
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
        final Button removeSubjectButton = dialog.findViewById(R.id.removeSubjectButton);

        removeSubjectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onDialogRemoveSubjectClick(index);
            }
        });
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(testSubject.getIdentifier())
                .setView(dialog);
        // Create the AlertDialog object and return it
        return builder.create();
    }
}