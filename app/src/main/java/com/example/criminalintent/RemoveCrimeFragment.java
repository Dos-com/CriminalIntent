package com.example.criminalintent;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;


public class RemoveCrimeFragment extends DialogFragment {
    private static final String EXTRA_REMOVE = "com.example.criminalintent.TimePickerFragment.time";
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_remove_crime,null);
        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle(R.string.delete_crime)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendResult(Activity.RESULT_OK, true);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendResult(Activity.RESULT_OK, false);
                    }
                })
                .create();
    }


    private void sendResult(int resultCode, boolean toRemove){
        if (getTargetFragment() == null)
            return;
        Intent intent = new Intent();
        intent.putExtra(EXTRA_REMOVE,toRemove);
        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode,intent);
    }
    public static boolean getResult(Intent data){
        return data.getBooleanExtra(EXTRA_REMOVE,false);
    }
}
