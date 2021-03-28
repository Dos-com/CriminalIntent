package com.example.criminalintent;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DatePickerFragment extends DialogFragment {
    private static final String ARG_DATE = "date";
    private static final String ARG_IS_DIALOG = "isDialog";
    private static final String EXTRA_DATE = "com.example.criminalintent.DatePickerFragment.date";
    private DatePicker mDatePicker;

    private Button mPositiveButton;

    private boolean isDialog;

    public static DatePickerFragment newInstance(Date date, boolean isDialog){
        Bundle args = new Bundle();
        args.putSerializable(ARG_DATE, date);
        args.putBoolean(ARG_IS_DIALOG,isDialog);

        DatePickerFragment fragment = new DatePickerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isDialog = getArguments().getBoolean(ARG_IS_DIALOG);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (!isDialog){

            View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_date,null);
            Date date = (Date) getArguments().getSerializable(ARG_DATE);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);


            mDatePicker = view.findViewById(R.id.dialog_date_picker);
            mDatePicker.init(year,month,day,null);

            mPositiveButton = view.findViewById(R.id.positive_button);
            mPositiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int year = mDatePicker.getYear();
                    int month = mDatePicker.getMonth();
                    int day = mDatePicker.getDayOfMonth();
                    int hour = calendar.get(Calendar.HOUR_OF_DAY);
                    int minute = calendar.get(Calendar.MINUTE);
                    int sec = calendar.get(Calendar.SECOND);
                    Date date1 = new GregorianCalendar(year,month,day,hour,minute,sec).getTime();

                    sendResult(Activity.RESULT_OK, date1);
                    getActivity().finish();
                }
            });

            return view;
        }


        return super.onCreateView(inflater, container, savedInstanceState);
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if (isDialog) {
            Date date = (Date) getArguments().getSerializable(ARG_DATE);
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_date, null);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);


            mDatePicker = view.findViewById(R.id.dialog_date_picker);
            mDatePicker.init(year, month, day, null);

            mPositiveButton = view.findViewById(R.id.positive_button);
            mPositiveButton.setVisibility(View.INVISIBLE);

            return new AlertDialog.Builder(getActivity())
                    .setView(view)
                    .setTitle(R.string.date_picker_title)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            int year = mDatePicker.getYear();
                            int month = mDatePicker.getMonth();
                            int day = mDatePicker.getDayOfMonth();
                            int hour = calendar.get(Calendar.HOUR_OF_DAY);
                            int minute = calendar.get(Calendar.MINUTE);
                            int sec = calendar.get(Calendar.SECOND);
                            Date date1 = new GregorianCalendar(year, month, day,hour,minute,sec).getTime();
                            sendResult(Activity.RESULT_OK, date1);
                        }
                    })
                    .create();
        }
        return super.onCreateDialog(savedInstanceState);
    }

    private void sendResult(int resultCode, Date date){
        Intent intent = new Intent();
        intent.putExtra(EXTRA_DATE,date);
        if (getTargetFragment() == null){
            getActivity().setResult(Activity.RESULT_OK,intent);
            return;
        }
        getTargetFragment().onActivityResult(getTargetRequestCode(),resultCode,intent);
    }



    public static Date getResultData(Intent data){
        Date date = (Date) data.getSerializableExtra(EXTRA_DATE);
        return date;
    }
}
