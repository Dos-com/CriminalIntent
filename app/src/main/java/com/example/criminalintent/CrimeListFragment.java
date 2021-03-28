package com.example.criminalintent;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class CrimeListFragment extends Fragment {
    private static final String SAVED_SUBTITLE_VISIBLE = "subtitle";

    private RecyclerView mRecyclerView;
    private CrimeAdapter mCrimeAdapter;
    private Button mAddNewCrimeButton;
    private LinearLayout mAddNewCrimeLinearLayout;
    private static final int REQUEST_CRIME = 1;

    private boolean mSubtitleVisible;

    private CallBacks mCallBacks;

    public interface CallBacks {
        void onCrimeSelected(Crime crime);
        void onCrimeRemoved(Crime crime);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime_list, container, false);
        mRecyclerView = v.findViewById(R.id.crime_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAddNewCrimeButton = v.findViewById(R.id.add_new_crime_button);
        mAddNewCrimeLinearLayout = v.findViewById(R.id.add_new_crime_linear_layout);
        updateUI();
        mAddNewCrimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Crime crime = new Crime();
                CrimeLab.get(getActivity()).addCrime(crime);
                Intent intent = CrimePagerActivity.newIntent(getActivity(), crime.getId());


                startActivityForResult(intent, REQUEST_CRIME);
            }
        });

        if (savedInstanceState != null) {
            mSubtitleVisible = savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE);
        }

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CRIME) {
            boolean found = false;
            if (CrimeFragment.getCrimeId(data) != null) {
                UUID crimeId = CrimeFragment.getCrimeId(data);
                List<Crime> crimes = CrimeLab.get(getActivity()).getCrimes();
                for (int i = 0; i < crimes.size(); i++) {
                    if (crimes.get(i).getId().equals(crimeId)) {
                        mCrimeAdapter.notifyItemChanged(i);
                        found = true;
                    }
                }
                if (!found) {
                    updateUI();
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
        updateSubtitle();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        updateSubtitle();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime_list, menu);

        MenuItem subtitleItem = menu.findItem(R.id.show_subtitle);
        if (mSubtitleVisible) {
            subtitleItem.setTitle(R.string.hide_subtitle);
        } else {
            subtitleItem.setTitle(R.string.show_subtitle);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_crime:
                Crime crime = new Crime();
                CrimeLab.get(getActivity()).addCrime(crime);
                updateUI();
                mCallBacks.onCrimeSelected(crime);
                return true;
            case R.id.show_subtitle:
                mSubtitleVisible = !mSubtitleVisible;
                getActivity().invalidateOptionsMenu();
                updateSubtitle();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_SUBTITLE_VISIBLE, mSubtitleVisible);
    }

    public void updateUI() {
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        List<Crime> crimes = crimeLab.getCrimes();

        if (mCrimeAdapter == null) {
            mCrimeAdapter = new CrimeAdapter(crimes);
            mRecyclerView.setAdapter(mCrimeAdapter);

            ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mCrimeAdapter);
            ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
            touchHelper.attachToRecyclerView(mRecyclerView);
        } else {
            mCrimeAdapter.setCrimes(crimes);
            mCrimeAdapter.notifyDataSetChanged();
        }
        if (crimes.size() == 0) {
            mAddNewCrimeLinearLayout.setVisibility(View.VISIBLE);
            mAddNewCrimeButton.setEnabled(true);
        } else {
            mAddNewCrimeLinearLayout.setVisibility(View.INVISIBLE);
            mAddNewCrimeButton.setEnabled(false);
        }
        updateSubtitle();
    }


    private void updateSubtitle() {
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        int crimeCount = crimeLab.getCrimes().size();
//        String subtitle = getResources().getQuantityString(R.plurals.subtitle_plural, crimeCount, crimeCount);
        String subtitle = getString(R.string.subtitle_format,crimeCount);
        if (!mSubtitleVisible) {
            subtitle = null;
        }
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setSubtitle(subtitle);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mCallBacks = (CallBacks) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallBacks = null;
    }

    private class CrimeHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mCrimeTitleTextView, mCrimeDateTextView;
        private Button mCallPoliceButton;
        private ImageView mSolvedImageView;

        private Crime mCrime;

        public CrimeHolder(LayoutInflater inflater, ViewGroup parent, int layout) {
            super(inflater.inflate(layout, parent, false));

            mCrimeTitleTextView = itemView.findViewById(R.id.crime_title);
            mCrimeDateTextView = itemView.findViewById(R.id.crime_date);

            if (layout == R.layout.list_item_crime) {
                mSolvedImageView = itemView.findViewById(R.id.crime_solved);
            }

            if (layout == R.layout.list_item_requires_police) {
                mCallPoliceButton = itemView.findViewById(R.id.call_police);
                mCallPoliceButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getActivity(), "Call police for arrest " + mCrime.getTitle(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
            ConstraintLayout constraintLayout = itemView.findViewById(R.id.holderLayout);
            constraintLayout.setContentDescription(getCrimeReport());


            itemView.setOnClickListener(this::onClick);
        }

        public void bind(Crime crime) {
            mCrime = crime;
            String format = "EEEE, MMM d, yyyy, HH:mm:ss";
//            String date = DateFormat.format(format, mCrime.getDate()).toString();
            String date = DateFormat.getDateInstance().format(crime.getDate());
            Log.d("TAG", "bind: "+date);
            mCrimeTitleTextView.setText(mCrime.getTitle());
            mCrimeDateTextView.setText(date);

            if (mSolvedImageView != null) {
                mSolvedImageView.setVisibility(crime.isSolved() ? View.VISIBLE : View.INVISIBLE);
            }
        }

        @Override
        public void onClick(View v) {
            mCallBacks.onCrimeSelected(mCrime);
        }

        private String getCrimeReport() {
            String solvedString = null;
            Log.d("TAG", "getCrimeReport: ");
            if (mCrime !=null){
                Log.d("TAG", "getCrimeReport: not null");

                if (mCrime.isSolved()) {
                    solvedString = getString(R.string.crime_report_solved);
                } else {
                    solvedString = getString(R.string.crime_report_unsolved);
                }

                String dateFormat = "EEEE, MMM d, yyyy";
                String dateString = android.text.format.DateFormat.format(dateFormat, mCrime.getDate()).toString();

                String suspect = mCrime.getSuspect();
                if (suspect == null) {
                    suspect = getString(R.string.crime_report_no_suspect);
                } else {
                    suspect = getString(R.string.crime_report_suspect, suspect);
                }

                String report = getString(R.string.crime_report, mCrime.getTitle(), dateString, solvedString, suspect);
                return report;
            }
            return "";
        }
    }

    private class CrimeAdapter extends RecyclerView.Adapter<CrimeHolder> implements ItemTouchHelperAdapter {
        private List<Crime> mCrimes;

        public CrimeAdapter(List<Crime> crimes) {
            mCrimes = crimes;
        }

        @NonNull
        @Override
        public CrimeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            int layout = R.layout.list_item_crime;
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            if (viewType == 1) {
                layout = R.layout.list_item_requires_police;
            }
            return new CrimeHolder(inflater, parent, layout);
        }

        @Override
        public void onBindViewHolder(@NonNull CrimeHolder holder, int position) {
            holder.bind(mCrimes.get(position));
        }

        @Override
        public int getItemCount() {
            return mCrimes.size();
        }

        @Override
        public int getItemViewType(int position) {
            return mCrimes.get(position).isRequiresPolice() && !mCrimes.get(position).isSolved() ? 1 : 0;
        }

        public void setCrimes(List<Crime> crimes) {
            mCrimes = crimes;
        }


        @Override
        public boolean onItemMove(int fromPosition, int toPosition) {
            return false;
        }

        @Override
        public void onItemDismiss(int position) {
            mCallBacks.onCrimeRemoved(mCrimes.get(position));
            CrimeLab.get(getActivity()).deleteCrime(mCrimes.get(position));
            mCrimes.remove(position);
            notifyItemRemoved(position);

        }
    }

    public interface ItemTouchHelperAdapter {
        boolean onItemMove(int fromPosition, int toPosition);

        void onItemDismiss(int position);
    }

    private class SimpleItemTouchHelperCallback extends ItemTouchHelper.Callback{
        private final ItemTouchHelperAdapter mAdapter;

        public SimpleItemTouchHelperCallback(ItemTouchHelperAdapter adapter) {
            mAdapter = adapter;
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return true;
        }

        @Override
        public boolean isItemViewSwipeEnabled() {
            return true;
        }

        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            int dragFlags = 0;
            int swipeFlags = ItemTouchHelper.START;
            return makeMovementFlags(dragFlags, swipeFlags);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            mAdapter.onItemDismiss(viewHolder.getLayoutPosition());
        }
    }
}
