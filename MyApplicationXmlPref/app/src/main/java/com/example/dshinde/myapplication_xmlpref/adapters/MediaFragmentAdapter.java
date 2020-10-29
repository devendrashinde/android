package com.example.dshinde.myapplication_xmlpref.adapters;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.dshinde.myapplication_xmlpref.activities.PdfViewFragment;
import com.example.dshinde.myapplication_xmlpref.activities.PhotoViewFragment;
import com.example.dshinde.myapplication_xmlpref.model.MediaFields;

public class MediaFragmentAdapter extends FragmentPagerAdapter {
    Context context;
    int totalTabs;
    String collectionName;
    MediaFields mediaFields;
    public MediaFragmentAdapter(Context c, FragmentManager fm, int totalTabs, String collectionName, MediaFields mediaFields) {
        super(fm);
        context = c;
        this.totalTabs = totalTabs;
        this.mediaFields = mediaFields;
        this.collectionName = collectionName;
    }
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new PhotoViewFragment(collectionName, mediaFields);
            case 1:
                return new PdfViewFragment(collectionName, mediaFields);
            default:
                return null;
        }
    }
    @Override
    public int getCount() {
        return totalTabs;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Photos";
            case 1:
                return "Documents";
        }
        return null;
    }
}
