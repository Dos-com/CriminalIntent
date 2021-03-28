package com.example.criminalintent;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;
import java.util.UUID;

public class CrimePagerActivity extends AppCompatActivity implements CrimeFragment.Callbacks {
    private ViewPager mViewPager;
    private List<Crime> mCrimes;
    private Button mToBeginningButton, mToEndButton;

    private static final String EXTRA_CRIME_ID = "com.example.criminalintent.CrimePagerActivity.crime_id";

    public static Intent newIntent(Context packageContext, UUID crimeId) {
        Intent intent = new Intent(packageContext, CrimePagerActivity.class);
        intent.putExtra(EXTRA_CRIME_ID, crimeId);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crime_pager);

        mViewPager = findViewById(R.id.crime_view_pager);
        mToBeginningButton = findViewById(R.id.view_pager_beginning_button);
        mToEndButton = findViewById(R.id.view_pager_end_button);

        mCrimes = CrimeLab.get(CrimePagerActivity.this).getCrimes();

        FragmentManager fragmentManager = getSupportFragmentManager();

        UUID crimeId = (UUID) getIntent().getSerializableExtra(EXTRA_CRIME_ID);
        int currentPosition = 0;

        for (int i = 0; i < mCrimes.size(); i++) {
            if (mCrimes.get(i).getId().equals(crimeId)) {
                currentPosition = i;
                break;
            }
        }
        mViewPager.setAdapter(new CrimePagerAdapter(fragmentManager));
        mViewPager.setPageTransformer(true, new ZoomOutPageTransformer());
        mViewPager.setCurrentItem(currentPosition);


        mToBeginningButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewPager.setCurrentItem(0);
                blockButton();
            }
        });

        mToEndButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewPager.setCurrentItem(mCrimes.size()-1);
                blockButton();
            }
        });
        blockButton();
    }

    private void blockButton(){
        if (mViewPager.getCurrentItem() == 0 && mViewPager.getCurrentItem() == mCrimes.size()-1){
            mToBeginningButton.setEnabled(false);
            mToEndButton.setEnabled(false);
        }
        else if (mViewPager.getCurrentItem() == 0){
            mToBeginningButton.setEnabled(false);
            mToEndButton.setEnabled(true);
        }
        else if (mViewPager.getCurrentItem() == mCrimes.size()-1){
            mToEndButton.setEnabled(false);
            mToBeginningButton.setEnabled(true);
        }
    }

    @Override
    public void onCrimeUpdated(Crime crime) {

    }

    @Override
    public boolean onUpdateDate() {
        // when started from the phone
        return false;
    }


    private class CrimePagerAdapter extends FragmentStatePagerAdapter {

        public CrimePagerAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return CrimeFragment.newInstance(mCrimes.get(position).getId());
        }

        @Override
        public int getCount() {
            return mCrimes.size();
        }


    }

    private class ZoomOutPageTransformer implements ViewPager.PageTransformer{
        private static final float MIN_SCALE = 0.85f;
        private static final float MIN_ALPHA = 0.5f;

        @Override
        public void transformPage(@NonNull View view, float position) {
            int pageWidth = view.getWidth();
            int pageHeight = view.getHeight();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0f);

            } else if (position <= 1) { // [-1,1]
                // Modify the default slide transition to shrink the page as well
                float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                float vertMargin = pageHeight * (1 - scaleFactor) / 2;
                float horzMargin = pageWidth * (1 - scaleFactor) / 2;
                if (position < 0) {
                    view.setTranslationX(horzMargin - vertMargin / 2);
                } else {
                    view.setTranslationX(-horzMargin + vertMargin / 2);
                }

                // Scale the page down (between MIN_SCALE and 1)
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

                // Fade the page relative to its size.
                view.setAlpha(MIN_ALPHA +
                        (scaleFactor - MIN_SCALE) /
                                (1 - MIN_SCALE) * (1 - MIN_ALPHA));

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0f);
            }
            blockButton();
        }
    }
}