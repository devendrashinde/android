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
import com.example.dshinde.myapplication_xmlpref.model.MediaFields;
import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Map;

public class PhotoViewActivity extends AppCompatActivity {

    PhotoView photoView;
    StorageReference storageReference;
    StorageReference storageFileRef;
    String userId;
    String noteId;
    private static final String TAG = PhotoViewActivity.class.getSimpleName();

    MediaFields mediaFields;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photoview);
        photoView = findViewById(R.id.photoView);
        photoView.setAdjustViewBounds(true);
        //photoView.setScaleType(ImageView.ScaleType.FIT_XY);
        setPhotoViewListener();

        Bundle bundle = getIntent().getExtras();
        userId = bundle.getString("userId");
        noteId = bundle.getString("noteId");
        Gson gson = new GsonBuilder().create();

        String mediaValues = bundle.getString("mediaFields");
        mediaFields = gson.fromJson(mediaValues, MediaFields.class);
        mediaFields.init();
        if(mediaFields.hasMedia()) {
            mediaFields.setValues(gson.fromJson(mediaValues, Map.class));
            String mediaFieldId = mediaFields.getNextPhotoMediaField();
            downloadFile(mediaFieldId, mediaFields.getMediaFieldValue(mediaFieldId), photoView);
        } else {
            finish();
        }
    }
    private void setPhotoViewListener(){
        photoView.setOnPhotoTapListener(new OnPhotoTapListener(){
            public void onPhotoTap(ImageView view, float x, float y) {
                float xPercentage = x * 100f;
                if(xPercentage < 30) {
                    //left side tapped
                    String mediaFieldId = mediaFields.getNextPhotoMediaField();
                    if(mediaFieldId != null) {
                        downloadFile(mediaFieldId, mediaFields.getMediaFieldValue(mediaFieldId), photoView);
                    }
                }
                if(xPercentage > 70) {
                    //right side tapped
                    String mediaFieldId = mediaFields.getPrevPhotoMediaField();
                    if(mediaFieldId != null) {
                        downloadFile(mediaFieldId, mediaFields.getMediaFieldValue(mediaFieldId), photoView);
                    }
                }
            }
        });

    }

    private void downloadFile(String mediaFieldId, String mediaFieldValue, PhotoView imageView) {
        //getting the storage reference
        try{
            if(storageReference == null) {
                storageReference = FirebaseStorage.getInstance().getReference();
            }
            storageFileRef = storageReference.child(Constants.STORAGE_PATH_NOTES + userId + "/" + noteId + "/" + mediaFieldId + "/" + mediaFieldValue);
            //adding the file to reference
            storageFileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(getApplicationContext()).load(uri).into(imageView);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(getApplicationContext(), "download failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Throwable throwable) {
            Toast.makeText(getApplicationContext(), "download failed: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
