package com.example.dshinde.myapplication_xmlpref.activities;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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
import com.shockwave.pdfium.PdfDocument;

import java.util.List;
import java.util.Objects;

public class PdfViewFragment extends Fragment implements OnPageChangeListener, OnLoadCompleteListener,
        OnPageErrorListener {

    private static final String TAG = PdfViewFragment.class.getSimpleName();
    PDFView pdfView;

    Integer pageNumber = 0;
    String pdfFileName;

    FileStorage fileStorage;
    String collectionName;
    MediaFields mediaFields;

    public PdfViewFragment(String collectionName, MediaFields mediaFields){
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
        return inflater.inflate(R.layout.activity_pdfview, container, false);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        pdfView = getActivity().findViewById(R.id.pdfView);
        pdfView.setBackgroundColor(Color.LTGRAY);
    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        pageNumber = page;
        requireActivity().setTitle(String.format("%s %s / %s", pdfFileName, page + 1, pageCount));
    }

    private void displayFromUri(Uri uri) {
        pdfFileName = StorageUtil.getFileName(getContext(), uri);

        pdfView.fromUri(uri)
                .defaultPage(pageNumber)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .scrollHandle(new DefaultScrollHandle(getContext()))
                .spacing(10) // in dp
                .onPageError(this)
                .load();
    }

    private void displayFromBytes(byte[] bytes, int pageNumber) {
        pdfView.fromBytes(bytes)
                .defaultPage(pageNumber)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .scrollHandle(new DefaultScrollHandle(getContext()))
                .spacing(10) // in dp
                .onPageError(this)
                .load();
    }

    @Override
    public void loadComplete(int nbPages) {
        PdfDocument.Meta meta = pdfView.getDocumentMeta();
        Log.e(TAG, "title = " + meta.getTitle());
        Log.e(TAG, "author = " + meta.getAuthor());
        Log.e(TAG, "subject = " + meta.getSubject());
        Log.e(TAG, "keywords = " + meta.getKeywords());
        Log.e(TAG, "creator = " + meta.getCreator());
        Log.e(TAG, "producer = " + meta.getProducer());
        Log.e(TAG, "creationDate = " + meta.getCreationDate());
        Log.e(TAG, "modDate = " + meta.getModDate());

        printBookmarksTree(pdfView.getTableOfContents(), "-");

    }

    public void printBookmarksTree(List<PdfDocument.Bookmark> tree, String sep) {
        for (PdfDocument.Bookmark b : tree) {

            Log.e(TAG, String.format("%s %s, p %d", sep, b.getTitle(), b.getPageIdx()));

            if (b.hasChildren()) {
                printBookmarksTree(b.getChildren(), sep + "-");
            }
        }
    }

    @Override
    public void onPageError(int page, Throwable t) {
        Log.e(TAG, "Cannot load page " + page);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fileStorage = Factory.getFileStorageInstance(getContext(), collectionName, new FireStorageListener() {
            @Override
            public void downloadUriReceived(Uri fileUri) {
            }

            @Override
            public void downloadFileBytesReceived(byte[] bytes) {
                displayFromBytes(bytes, pageNumber);
            }

            @Override
            public void uploadedUriReceived(Uri fileUri) {

            }
        });
        loadNext();
    }

    private void load(Uri uri, int pageNumber) {
        this.pageNumber = pageNumber;
        displayFromUri(uri);
        getActivity().setTitle(pdfFileName);
    }

    private void loadNext(){
        String mediaFieldId = mediaFields.getNextDocumentMediaField();
        if(mediaFieldId != null) {
            pdfFileName = mediaFields.getMediaFieldValue(mediaFieldId);
            fileStorage.downloadFileAsBytes(mediaFieldId, pdfFileName);
        }
    }

    private void loadPrev(){
        String mediaFieldId = mediaFields.getPrevDocumentMediaField();
        if(mediaFieldId != null) {
            pdfFileName = mediaFields.getMediaFieldValue(mediaFieldId);
            fileStorage.downloadFileAsBytes(mediaFieldId, mediaFields.getMediaFieldValue(mediaFieldId));
        }
    }

    public boolean onOptionsItemSelected(MenuItem item){
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_prev:
                loadNext();
                return true;
            case R.id.menu_next:
                loadPrev();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
