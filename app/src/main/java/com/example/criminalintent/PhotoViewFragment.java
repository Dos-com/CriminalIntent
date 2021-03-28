package com.example.criminalintent;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class PhotoViewFragment extends DialogFragment {
    private static final String ARG_PHOTO_PATH = "photoPath";
    private String photoPath;
    private ImageView mPhotoView;

    public static PhotoViewFragment newInstance(Context packageContext, String photoPath){
        Bundle args = new Bundle();
        args.putString(ARG_PHOTO_PATH, photoPath);

        PhotoViewFragment fragment = new PhotoViewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        photoPath = getArguments().getString(ARG_PHOTO_PATH);
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_photo,null);
        mPhotoView = view.findViewById(R.id.crime_photo);

        Bitmap bitmap = PictureUtils.getScaledBitmap(photoPath,getActivity());
        mPhotoView.setImageBitmap(bitmap);

        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle(R.string.crime_photo)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create();
    }
}
