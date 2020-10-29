package com.example.dshinde.myapplication_xmlpref.activities;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.dshinde.myapplication_xmlpref.R;
import com.example.dshinde.myapplication_xmlpref.helper.Factory;
import com.example.dshinde.myapplication_xmlpref.helper.StorageUtil;
import com.example.dshinde.myapplication_xmlpref.listners.FireStorageListener;
import com.example.dshinde.myapplication_xmlpref.model.MediaFields;
import com.example.dshinde.myapplication_xmlpref.services.FileStorage;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.github.chrisbanes.photoview.PhotoView;
import com.shockwave.pdfium.PdfDocument;

import java.util.List;

public class PhotoViewFragment extends Fragment {

    private static final String TAG = PhotoViewFragment.class.getSimpleName();
    PhotoView photoView;
    FileStorage fileStorage;
    String collectionName;
    MediaFields mediaFields;

    public PhotoViewFragment(String collectionName, MediaFields mediaFields){
        this.collectionName = collectionName;
        this.mediaFields = mediaFields;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        return inflater.inflate(R.layout.activity_photoview, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        photoView = getActivity().findViewById(R.id.photoView);
        photoView.setAdjustViewBounds(true);
        //photoView.setScaleType(ImageView.ScaleType.FIT_XY);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fileStorage = Factory.getFileStorageInstance(getContext(), collectionName, new FireStorageListener() {
            @Override
            public void downloadUriReceived(Uri fileUri) {
                load(fileUri);
            }

            @Override
            public void downloadFileBytesReceived(byte[] bytes) {

            }

            @Override
            public void uploadedUriReceived(Uri fileUri) {

            }
        });
        loadNextPhoto();
    }

    private void load(Uri uri) {
        Glide.with(getContext()).load(uri).into(photoView);
    }

    private void loadNextPhoto(){
        String mediaFieldId = mediaFields.getNextPhotoMediaField();
        if(mediaFieldId != null) {
            fileStorage.getDownloadUrl(mediaFieldId, mediaFields.getMediaFieldValue(mediaFieldId));
        }
    }

    private void loadPrevPhoto(){
        String mediaFieldId = mediaFields.getPrevPhotoMediaField();
        if(mediaFieldId != null) {
            fileStorage.getDownloadUrl(mediaFieldId, mediaFields.getMediaFieldValue(mediaFieldId));
        }
    }

    public boolean onOptionsItemSelected(MenuItem item){
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_prev:
                loadNextPhoto();
                return true;
            case R.id.menu_next:
                loadPrevPhoto();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
