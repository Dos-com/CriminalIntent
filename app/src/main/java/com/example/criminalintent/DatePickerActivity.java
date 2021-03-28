package com.example.criminalintent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;

import java.util.Date;
import java.util.GregorianCalendar;

public class DatePickerActivity extends SingleFragmentActivity {
    private static final String EXTRA_DATE = "com.example.criminalintent.DatePickerActivity.date";
    private static final String EXTRA_IS_DIALOG = "com.example.criminalintent.DatePickerActivity.is_dialog";
    private Date mDate;

    private boolean isDialog;

    public static Intent newIntent(Context packageContext, Date date, boolean isDialog){
        Intent intent = new Intent(packageContext, DatePickerActivity.class);
        intent.putExtra(EXTRA_DATE, date);
        intent.putExtra(EXTRA_IS_DIALOG,isDialog);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        mDate = (Date) getIntent().getSerializableExtra(EXTRA_DATE);
        isDialog = getIntent().getBooleanExtra(EXTRA_IS_DIALOG,true);

        return DatePickerFragment.newInstance(mDate,isDialog);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
