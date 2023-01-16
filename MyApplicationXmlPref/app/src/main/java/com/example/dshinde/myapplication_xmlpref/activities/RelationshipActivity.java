package com.example.dshinde.myapplication_xmlpref.activities;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;

import com.example.dshinde.myapplication_xmlpref.R;
import com.example.dshinde.myapplication_xmlpref.activities.drawables.RelationshipView;
import com.example.dshinde.myapplication_xmlpref.helper.BitmapUtil;
import com.example.dshinde.myapplication_xmlpref.helper.Converter;
import com.example.dshinde.myapplication_xmlpref.helper.DynamicControls;
import com.example.dshinde.myapplication_xmlpref.helper.Factory;
import com.example.dshinde.myapplication_xmlpref.listners.DataStorageListener;
import com.example.dshinde.myapplication_xmlpref.model.KeyValue;
import com.example.dshinde.myapplication_xmlpref.services.ReadOnceDataStorage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RelationshipActivity extends BaseActivity {

    private static final String CLASS_TAG = "RelationshipActivity";
    RelationshipView relationshipView;
    public ScrollView scroll_view;
    public HorizontalScrollView h_scroll_view;
    public LinearLayout lin_layout;
    String collectionName;
    ReadOnceDataStorage readOnceDataStorage;
    Map<String, Set<String>> relationShips;
    String parentNode;
    String[] parents;
    Spinner parentsList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Bundle bundle = getIntent().getExtras();
        collectionName = bundle.getString("filename");
        userId = bundle.getString("userId");
        setTitle(collectionName);
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
        createRelationshipView();
        h_scroll_view.addView(scroll_view);
        lin_layout.addView(parentsList);
        lin_layout.addView(getExportButton());
        lin_layout.addView(h_scroll_view);
        setContentView(lin_layout);

    }

    private View getExportButton() {
        Button export = DynamicControls.getButton(this, getString(R.string.export));
        export.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                BitmapUtil.saveBitmap(getApplicationContext(),
                        Converter.viewToBitmap(relationshipView,
                                relationshipView.getViewWidth(), relationshipView.getViewHeight()),
                        parentNode);
            }
        });
        return export;
    }

    private void createRelationshipView() {
        if(relationshipView != null) {
            scroll_view.removeView(relationshipView);
        }
        relationshipView = new RelationshipView(this, parentNode, relationShips);
        relationshipView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        scroll_view.addView(relationshipView);
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
            addToMap(parts[0],parts[1] + "," + (parts.length == 3 ? parts[2] : ""));
            if(!childs.contains(parts[0])) {
                parent.add(parts[0]);
            }
            childs.add(parts[1]);
            parent.remove(parts[1]);
        }
        parentNode = parent.iterator().next();
        parents = parent.toArray(new String[parent.size()]);;
        createParentListControl();
    }
    private void createParentListControl() {
        parentsList = DynamicControls.getDropDownList(this, parents, parentNode);
        parentsList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                parentNode = parentsList.getItemAtPosition(i).toString();
                createRelationshipView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void addToMap(String parentName, String childNameAndRelation){
        relationShips.computeIfAbsent(parentName, k -> new HashSet<>());
        relationShips.get(parentName).add(childNameAndRelation);
    }
}
