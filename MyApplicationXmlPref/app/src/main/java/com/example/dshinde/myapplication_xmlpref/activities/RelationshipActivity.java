package com.example.dshinde.myapplication_xmlpref.activities;

import android.os.Bundle;
import android.view.Window;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.example.dshinde.myapplication_xmlpref.activities.BaseActivity;
import com.example.dshinde.myapplication_xmlpref.activities.drawables.RelationshipView;
import com.example.dshinde.myapplication_xmlpref.helper.Converter;
import com.example.dshinde.myapplication_xmlpref.helper.Factory;
import com.example.dshinde.myapplication_xmlpref.listners.DataStorageListener;
import com.example.dshinde.myapplication_xmlpref.model.KeyValue;
import com.example.dshinde.myapplication_xmlpref.services.ReadOnceDataStorage;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RelationshipActivity extends BaseActivity {

    RelationshipView customDrawableView;
    public ScrollView scroll_view;
    public HorizontalScrollView h_scroll_view;
    public LinearLayout lin_layout;
    PhotoView photoView;
    String collectionName;
    private static final String CLASS_TAG = "DrawableActivity";
    ReadOnceDataStorage readOnceDataStorage;
    Map<String, Set<String>> relationShips;
    String parentNode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Bundle bundle = getIntent().getExtras();
        collectionName = bundle.getString("filename");
        userId = bundle.getString("userId");
        initDataStorageAndLoadData();

        lin_layout = new LinearLayout(this);
        scroll_view = new ScrollView(this);
        h_scroll_view = new HorizontalScrollView(this);

        scroll_view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        h_scroll_view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        lin_layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        lin_layout.setOrientation(LinearLayout.VERTICAL);

        initDataStorageAndLoadData();
    }

    private void setView() {
        customDrawableView = new RelationshipView(this, parentNode, relationShips);
        customDrawableView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        scroll_view.addView(customDrawableView);
        h_scroll_view.addView(scroll_view);
        setContentView(h_scroll_view);
    }



    private void initDataStorageAndLoadData() {

        readOnceDataStorage = Factory.getReadOnceDataStorageIntsance(this,
            getDataStorageType(),
            collectionName,
            new DataStorageListener() {
                @Override
                public void dataChanged(String key, String value) {
                }

                @Override
                public void dataLoaded(List<KeyValue> data) {
                    String jsonString = Converter.getValuesJsonString(data);
                    readOnceDataStorage.removeDataStorageListeners();
                    parse(data);
                    setView();
                }
            });
    }

    private void parse(List<KeyValue> keyValues){
        relationShips = new HashMap<>();
        Set<String> parent = new HashSet<>();
        Set<String> childs = new HashSet<>();
        for(KeyValue key : keyValues){
            String[] parts = key.getKey().split(",");
            addToMap(parts[0],parts[1] + "," + parts[2]);
            if(!childs.contains(parts[0])) {
                parent.add(parts[0]);
            }
            childs.add(parts[1]);
            parent.remove(parts[1]);
        }
        parentNode = parent.iterator().next();
    }

    private void addToMap(String key, String value){
        relationShips.computeIfAbsent(key, k -> new HashSet<>());
        relationShips.get(key).add(value);
    }
}
