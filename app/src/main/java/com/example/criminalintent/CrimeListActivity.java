package com.example.criminalintent;

import android.content.Intent;

import androidx.fragment.app.Fragment;

public class CrimeListActivity extends SingleFragmentActivity implements CrimeListFragment.CallBacks, CrimeFragment.Callbacks {

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_masterdetail;
    }

    @Override
    protected Fragment createFragment() {
        return new CrimeListFragment();
    }

    @Override
    public void onCrimeSelected(Crime crime) {
        if (findViewById(R.id.detail_fragment_container) == null) {
            Intent intent = CrimePagerActivity.newIntent(CrimeListActivity.this, crime.getId());
            startActivity(intent);
        } else {
            Fragment newFragment = CrimeFragment.newInstance(crime.getId());

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_fragment_container, newFragment)
                    .commit();
        }
    }

    @Override
    public void onCrimeRemoved(Crime crime) {
        if (findViewById(R.id.detail_fragment_container) != null) {
            CrimeFragment crimeFragment = (CrimeFragment) getSupportFragmentManager().findFragmentById(R.id.detail_fragment_container);
            if (crimeFragment != null){

                if (crime.getId().equals(crimeFragment.getCrimeId())) {
                    getSupportFragmentManager().beginTransaction()
                            .remove(crimeFragment)
                            .commit();
                }
            }
        }
    }

    @Override
    public void onCrimeUpdated(Crime crime) {
        CrimeListFragment listFragment = (CrimeListFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        listFragment.updateUI();
    }

    @Override
    public boolean onUpdateDate() {
        // when started from the tablet
        return true;
    }
}
