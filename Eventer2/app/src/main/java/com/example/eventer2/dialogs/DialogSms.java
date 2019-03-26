package com.example.eventer2.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.example.eventer2.R;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialogFragment;

public class DialogSms extends AppCompatDialogFragment {

    private DialogListener mListener;
    private Button mEnable;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.activity_invite, null);

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.dialog_sms)
                .setPositiveButton("Confirm", (dialog, id) -> {
                    mListener.onConfirm(true);
                })
                .setNegativeButton("Cancel", (dialog, id) -> {
                    mListener.onConfirm(false);
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            mListener = (DialogListener)context;
        }catch (ClassCastException e){
            throw  new ClassCastException(context.toString());
        }
    }

    public interface DialogListener{
        void onConfirm(boolean change);
    }
}
