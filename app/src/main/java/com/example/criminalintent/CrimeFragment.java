package com.example.criminalintent;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ShareCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static android.widget.CompoundButton.*;

public class CrimeFragment extends Fragment {
    private static final String ARG_CRIME_ID = "crime_id";
    private static final String EXTRA_CRIME_ID = "com.example.criminalintent.CrimeListActivity.crime_id";
    private static final String DIALOG_DATE = "DialogDate";
    private static final String DIALOG_Time = "DialogTime";
    private static final String DIALOG_REMOVE = "DialogRemove";
    private static final String DIALOG_Photo = "DialogPhoto";
    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_TIME = 1;
    private static final int REQUEST_REMOVE = 2;
    private static final int REQUEST_CONTACT = 3;
    private static final int REQUEST_PHOTO = 4;

    private Crime mCrime;
    private EditText mTitleField;
    private Button mDateButton, mTimeButton, mRemoveButton, mReportButton, mSuspectButton, mCallSuspectButton;
    private CheckBox mSolvedCheckBox, mRequiresPoliceCheckBox;
    private ImageView mPhotoView;
    private ImageButton mPhotoButton;


    private File mPhotoFile;

    private int photoWidth, photoHeight;

    private Callbacks mCallbacks;

    public interface Callbacks{
        void onCrimeUpdated(Crime crime);
        boolean onUpdateDate();
    }

    public static CrimeFragment newInstance(UUID crimeId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);

        CrimeFragment crimeFragment = new CrimeFragment();
        crimeFragment.setArguments(args);
        return crimeFragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
        mPhotoFile = CrimeLab.get(getActivity()).getPhotoFile(mCrime);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime, container, false);

        mTitleField = v.findViewById(R.id.crime_title);
        mTitleField.setText(mCrime.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCrime.setTitle(s.toString());
                updateCrime();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        mDateButton = v.findViewById(R.id.crime_date);
        updateDate();
        mDateButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
                if (mCallbacks.onUpdateDate()){
                    FragmentManager manager = getFragmentManager();
                    DatePickerFragment dialog = DatePickerFragment.newInstance(mCrime.getDate(),true);
                    dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                    dialog.show(manager, DIALOG_DATE);
                }else {
                    Intent intent = DatePickerActivity.newIntent(getActivity(), mCrime.getDate(),false);
                    startActivityForResult(intent, REQUEST_DATE);
                }
            }
        });

        mSolvedCheckBox = v.findViewById(R.id.crime_solved);
        mSolvedCheckBox.setChecked(mCrime.isSolved());
        mSolvedCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCrime.setSolved(isChecked);
                updateCrime();
            }
        });

        mRequiresPoliceCheckBox = v.findViewById(R.id.requires_police);
        mRequiresPoliceCheckBox.setChecked(mCrime.isRequiresPolice());
        mRequiresPoliceCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCrime.setRequiresPolice(isChecked);
                updateCrime();
            }
        });


        mTimeButton = v.findViewById(R.id.crime_time);
        updateTime();
        mTimeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                TimePickerFragment dialog = TimePickerFragment.newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_TIME);
                dialog.show(manager, DIALOG_Time);
            }
        });


        mRemoveButton = v.findViewById(R.id.delete_crime);
        mRemoveButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                RemoveCrimeFragment dialog = new RemoveCrimeFragment();
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_REMOVE);
                dialog.show(manager, DIALOG_REMOVE);
            }
        });

        mReportButton = v.findViewById(R.id.crime_report);
        mReportButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareIntent = ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setChooserTitle(getString(R.string.crime_report_subject))
                        .setText(getCrimeReport())
                        .createChooserIntent();
                startActivity(shareIntent);
            }
        });


        final Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        mSuspectButton = v.findViewById(R.id.crime_suspect);
        mSuspectButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(pickContact, REQUEST_CONTACT);
            }
        });

        if (mCrime.getSuspect() != null) {
            mSuspectButton.setText(mCrime.getSuspect());
        }
        mCallSuspectButton = v.findViewById(R.id.crime_call_suspend);
        mCallSuspectButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                // Query phone here. Covered next
                String tel = null;
                Cursor phones = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",new String[]{mCrime.getSuspectId()}, null);


                try {
                    phones.moveToFirst();
                    tel = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                } finally {
                    phones.close();
                }


                if (tel!=null){

                    Uri uri = Uri.parse("tel:"+tel);
                    Intent intent = new Intent(Intent.ACTION_DIAL,uri);

                    PackageManager pm = getActivity().getPackageManager();
                    ComponentName cn = intent.resolveActivity(pm);
                    startActivity(intent);
                    if (cn == null){
                        Log.e("TAG", "Intent could not resolve to an Activity.");
                    } else {
                        startActivity(intent);
                    }
                }
            }
        });
        if (mCrime.getSuspect() !=null){
            mCallSuspectButton.setText(getString(R.string.call_suspect,mCrime.getSuspect()));
        }
        else {
            mCallSuspectButton.setEnabled(false);
        }



        mPhotoView = v.findViewById(R.id.crime_photo);
        mPhotoView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                PhotoViewFragment dialog = PhotoViewFragment.newInstance(getActivity(),mPhotoFile.getPath());
                dialog.show(manager,DIALOG_Photo);
            }
        });
        mPhotoView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                photoWidth = mPhotoView.getWidth();
                photoHeight = mPhotoView.getHeight();
                updatePhotoView();
            }
        });

        mPhotoButton = v.findViewById(R.id.crime_camera);
        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);


        mPhotoButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = FileProvider.getUriForFile(getActivity(),"com.example.criminalintent.fileprovider",mPhotoFile);
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT,uri);
                List<ResolveInfo> cameraActivities = getActivity().getPackageManager().queryIntentActivities(captureImage, PackageManager.MATCH_DEFAULT_ONLY);

                for (ResolveInfo activity: cameraActivities){
                    getActivity().grantUriPermission(activity.activityInfo.packageName,uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }

                startActivityForResult(captureImage,REQUEST_PHOTO);
            }
        });

        permissionReadContacts();
        permissionCamera();
        returnResult();

        return v;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_CONTACT:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //granted
                    mSuspectButton.setEnabled(true);
                    mCallSuspectButton.setEnabled(true);
                } else {
                    mSuspectButton.setEnabled(false);
                    mCallSuspectButton.setEnabled(false);
                }
                break;
            case REQUEST_PHOTO:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //granted
                    mPhotoView.setEnabled(true);
                } else {
                    mPhotoView.setEnabled(false);
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        CrimeLab.get(getActivity()).updateCrime(mCrime);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_DATE) {
            Date date = DatePickerFragment.getResultData(data);
            mCrime.setDate(date);

            updateDate();
            if (mCallbacks.onUpdateDate()){
                updateCrime();
            }
        } else if (requestCode == REQUEST_TIME) {
            Date date = TimePickerFragment.getTimeResult(data);
            mCrime.setDate(date);


            String format = "EEEE, MMM d, yyyy, HH:mm:ss";
            String dateString = DateFormat.format(format, mCrime.getDate()).toString();
            updateTime();
            updateCrime();
        } else if (requestCode == REQUEST_REMOVE) {
            boolean toRemove = RemoveCrimeFragment.getResult(data);
            if (toRemove) {
                CrimeLab.get(getActivity()).deleteCrime(mCrime);
                getActivity().finish();
                updateCrime();
            }
        } else if (requestCode == REQUEST_CONTACT && data != null) {
            Uri contactUri = data.getData();
            // Определение полей, значения которых должны быть
            // возвращены запросом.


            String[] queryFields = new String[]{
                    ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.Contacts._ID
            };
            // Выполнение запроса - contactUri здесь выполняет функции
            // условия "where"
            Cursor c = getActivity().getContentResolver().query(contactUri,queryFields,null,null,null);
            try {
                // Проверка получения результатов
                if (c.getCount() == 0){
                    return;
                }
                // Извлечение первого столбца данных - имени подозреваемого.
                c.moveToFirst();
                String suspect = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String suspectId = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
                mCrime.setSuspect(suspect);
                mCrime.setSuspectId(suspectId);
                mSuspectButton.setText(mCrime.getSuspect());
                mCallSuspectButton.setText(getString(R.string.call_suspect,mCrime.getSuspect()));
                mCallSuspectButton.setEnabled(true);
                updateCrime();

            } finally {
                c.close();
            }
        } else if (requestCode == REQUEST_PHOTO){
            Uri uri = FileProvider.getUriForFile(getActivity(),"com.example.criminalintent.fileprovider",mPhotoFile);

            getActivity().revokeUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            updatePhotoView();
            updateCrime();
        }
    }

    private void updateDate() {
        String format = "EEEE, MMM d, yyyy";
        String dateString = DateFormat.format(format, mCrime.getDate()).toString();
        mDateButton.setText(dateString);
    }

    private void updateTime() {
        String format = "HH:mm:ss";
        String dateString = DateFormat.format(format, mCrime.getDate()).toString();
        mTimeButton.setText(dateString);
    }

    public void returnResult() {
        Intent data = new Intent();
        data.putExtra(EXTRA_CRIME_ID, mCrime.getId());
        getActivity().setResult(Activity.RESULT_OK, data);
    }

    public static UUID getCrimeId(Intent result) {

        return (UUID) result.getSerializableExtra(EXTRA_CRIME_ID);
    }

    private String getCrimeReport() {
        String solvedString = null;
        if (mCrime.isSolved()) {
            solvedString = getString(R.string.crime_report_solved);
        } else {
            solvedString = getString(R.string.crime_report_unsolved);
        }

        String dateFormat = "EEEE, MMM d, yyyy";
        String dateString = DateFormat.format(dateFormat, mCrime.getDate()).toString();

        String suspect = mCrime.getSuspect();
        if (suspect == null) {
            suspect = getString(R.string.crime_report_no_suspect);
        } else {
            suspect = getString(R.string.crime_report_suspect, suspect);
        }

        String report = getString(R.string.crime_report, mCrime.getTitle(), dateString, solvedString, suspect);
        return report;
    }

    private void updatePhotoView(){
        if (mPhotoFile == null || !mPhotoFile.exists()){
            mPhotoView.setImageDrawable(null);
            mPhotoView.setEnabled(false);
            mPhotoView.setContentDescription(getString(R.string.crime_photo_no_image_description));
            talkBackPhotoView(R.string.crime_photo_no_image_description);
        } else if (photoWidth == 0 || photoHeight ==0){
            Bitmap bitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(),getActivity());
            mPhotoView.setImageBitmap(bitmap);
            mPhotoView.setEnabled(true);
            mPhotoView.setContentDescription(getString(R.string.crime_photo_image_description));
            talkBackPhotoView(R.string.crime_photo_image_description);
        }
        else {
            Bitmap bitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(),photoWidth,photoHeight);
            mPhotoView.setImageBitmap(bitmap);
            mPhotoView.setEnabled(true);
            mPhotoView.setContentDescription(getString(R.string.crime_photo_image_description));
            talkBackPhotoView(R.string.crime_photo_image_description);
        }
    }

    private void talkBackPhotoView(int strId){
        mPhotoView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mPhotoView.announceForAccessibility(getString(strId));
            }
        },1000);
    }

    private void permissionCamera(){
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            mPhotoButton.setEnabled(true);
        } else {
            // You can directly ask for the permission.
            ActivityCompat.requestPermissions(getActivity(),
                    new String[] { Manifest.permission.CAMERA },
                    REQUEST_PHOTO);
        }
    }

    private void permissionReadContacts(){
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            mCallSuspectButton.setEnabled(true);
            mSuspectButton.setEnabled(true);
        } else {
            // You can directly ask for the permission.
            ActivityCompat.requestPermissions(getActivity(),
                    new String[] { Manifest.permission.READ_CONTACTS },
                    REQUEST_CONTACT);
        }
    }

    private void updateCrime(){
        CrimeLab.get(getActivity()).updateCrime(mCrime);
        mCallbacks.onCrimeUpdated(mCrime);
    }

    public UUID getCrimeId(){
        return mCrime.getId();
    }
}