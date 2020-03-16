package com.example.dshinde.myapplication_xmlpref.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dshinde.myapplication_xmlpref.R;
import com.example.dshinde.myapplication_xmlpref.adapters.RecyclerViewKeyValueAdapter;
import com.example.dshinde.myapplication_xmlpref.common.Constants;
import com.example.dshinde.myapplication_xmlpref.services.DataStorage;
import com.example.dshinde.myapplication_xmlpref.services.ReadOnceDataStorage;

public class MainFragment extends Fragment {
    String key;
    EditText valueField;
    RecyclerView listView;
    DataStorage dataStorageManager;
    ReadOnceDataStorage readOnceDataStorage;
    RecyclerViewKeyValueAdapter listAdapter;
    String sharedPreferenceName = Constants.DATABASE_PATH_NOTES;
    private static final String CLASS_TAG = "MainActivityRV";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_main_recycler_view, container, false);
    }

}
