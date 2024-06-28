package com.example.dshinde.myapplication_xmlpref.activities;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.dshinde.myapplication_xmlpref.R;
import com.example.dshinde.myapplication_xmlpref.common.Constants;
import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;

public class PhotoViewActivity extends AppCompatActivity {

    PhotoView photoView;
    String photoUrl;
    private static final String TAG = PhotoViewActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photoview);
        photoView = findViewById(R.id.photoView);
        photoView.setAdjustViewBounds(true);
        setPhotoViewListener();

        Bundle bundle = getIntent().getExtras();
        photoUrl = bundle.getString(Constants.PARAM_URL);
        photoView.setZoomable(true);
        //photoView.setImageURI(Uri.parse(photoUrl));
        Glide.with(getApplicationContext())
                .load(new File(photoUrl)) // Uri of the picture
                .into(photoView);
        //Glide.with(getApplicationContext()).load(Uri.parse(photoUrl)).into(photoView);
    }
    private void setPhotoViewListener(){
        photoView.setOnPhotoTapListener(new OnPhotoTapListener(){
            public void onPhotoTap(ImageView view, float x, float y) {
                float xPercentage = x * 100f;
                if(xPercentage < 30) {
                    //left side tapped
                }
                if(xPercentage > 70) {
                    //right side tapped
                }
            }
        });
    }
}
