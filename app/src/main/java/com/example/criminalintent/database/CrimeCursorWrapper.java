package com.example.criminalintent.database;

import android.database.Cursor;
import android.database.CursorWrapper;
import android.util.Log;

import com.example.criminalintent.Crime;
import com.example.criminalintent.database.CrimeDbSchema.CrimeTable;

import java.util.Date;
import java.util.UUID;

public class CrimeCursorWrapper extends CursorWrapper{
    /**
     * Creates a cursor wrapper.
     *
     * @param cursor The underlying cursor to wrap.
     */
    public CrimeCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Crime getCrime(){
        String uuid = getString(getColumnIndex(CrimeTable.Cols.UUID));
        String title = getString(getColumnIndex(CrimeTable.Cols.TITLE));
        long date = getLong(getColumnIndex(CrimeTable.Cols.DATE));
        int solved = getInt(getColumnIndex(CrimeTable.Cols.SOLVED));
        int required_police = getInt(getColumnIndex(CrimeTable.Cols.REQUIRED_POLICE));
        String suspect = getString(getColumnIndex(CrimeTable.Cols.SUSPECT));
        String suspectId = getString(getColumnIndex(CrimeTable.Cols.SUSPECT_ID));


        Crime crime = new Crime(UUID.fromString(uuid));
        crime.setTitle(title);
        crime.setDate(new Date(date));
        crime.setSolved(solved != 0);
        crime.setRequiresPolice(required_police != 0);
        crime.setSuspect(suspect);
        crime.setSuspectId(suspectId);

        Log.d("TAG", "getCrime: "+crime.getTitle()+" date: "+date);
        return crime;
    }
}
