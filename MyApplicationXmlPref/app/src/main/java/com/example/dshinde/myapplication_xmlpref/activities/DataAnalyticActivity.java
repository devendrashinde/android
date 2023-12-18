package com.example.dshinde.myapplication_xmlpref.activities;

import android.os.Bundle;
import android.widget.LinearLayout;

import com.example.dshinde.myapplication_xmlpref.R;
import com.example.dshinde.myapplication_xmlpref.common.Constants;
import com.example.dshinde.myapplication_xmlpref.helper.Converter;
import com.example.dshinde.myapplication_xmlpref.helper.DynamicControls;
import com.example.dshinde.myapplication_xmlpref.helper.Factory;
import com.example.dshinde.myapplication_xmlpref.helper.Utils;
import com.example.dshinde.myapplication_xmlpref.listners.DataStorageListener;
import com.example.dshinde.myapplication_xmlpref.model.KeyValue;
import com.example.dshinde.myapplication_xmlpref.model.ScreenControl;
import com.example.dshinde.myapplication_xmlpref.services.ReadWriteOnceDataStorage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

public class DataAnalyticActivity extends BaseActivity {
    private static final String CLASS_TAG = "DataAnalyticActivity";
    private LinearLayout linearLayout;
    String collectionName = null;
    int totalNotes;
    List<KeyValue> notes;
    List<ScreenControl> screenDesignData;
    Map<String, KeyValue> summary = new HashMap<>();
    ReadWriteOnceDataStorage readWriteOnceDataStorage;
    private final Gson gson = new GsonBuilder().create();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dynamic_linear_layout);
        linearLayout = findViewById(R.id.linear_layout);
        // get parameters
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            userId = bundle.getString(Constants.USERID);
            collectionName = bundle.getString(Constants.PARAM_NOTE);
            notes = (List<KeyValue>) getIntent().getSerializableExtra(Constants.PARAM_DATA);
            totalNotes = notes.size();
            setTitle(collectionName);
            getScreenDesignData(collectionName);
        }
    }

    private void getScreenDesignData(String collection) {
        readWriteOnceDataStorage = Factory.getReadOnceFireDataStorageInstance(
            Constants.SCREEN_DESIGN_NOTE_PREFIX + collection,
            new DataStorageListener() {
                @Override
                public void dataChanged(String key, String value) {
                }

                @Override
                public void dataLoaded(List<KeyValue> data) {
                    screenDesignData = Utils.parseScreenConfig(Converter.getValuesJsonString(data));
                    readWriteOnceDataStorage.removeDataStorageListeners();
                    collectAnalyticalData();
                }
            });
    }

    private void collectAnalyticalData() {
        for(KeyValue keyValue : notes){
            Map<String, String> data = gson.fromJson(keyValue.getValue(), Map.class);
            for(Entry entry: data.entrySet()) {
                Optional<ScreenControl> control = screenDesignData.stream().filter(c -> c.getControlId().equals(entry.getKey())).findFirst();
                if(control.isPresent()) {
                    ScreenControl screenControl = control.get();
                    switch (screenControl.getControlType()) {
                        case Text:
                            break;
                        case CheckBox:
                            break;
                        case EditText:
                            break;
                        case EditNumber:
                            collectNumericData(screenControl.getTextLabel(), entry.getKey().toString(), entry.getValue().toString());
                            break;
                        case Expression:
                            break;
                        case MultiLineEditText:
                            break;
                        case DatePicker:
                            break;
                        case TimePicker:
                            break;
                        case RadioButton:
                            break;
                        case DropDownList:
                            break;
                        default:
                            break;
                    }
                }
            }
        }

        for(Entry entry: summary.entrySet()) {
            KeyValue keyValue = (KeyValue) entry.getValue();
            linearLayout.addView(DynamicControls.getTextView(this, keyValue.getKey()));
            linearLayout.addView(DynamicControls.getEditText(this, keyValue.getValue()));
        }
    }

    private void collectNumericData(String label, String field, String value) {
        KeyValue keyValue = summary.get(field);
        if(keyValue == null) {
            keyValue = new KeyValue("Total " + label, value);
        } else {
            float f = Float.parseFloat(keyValue.getValue()) + Float.parseFloat(value);
            keyValue.setValue(String.valueOf(f));
        }
        summary.put(field, keyValue);
    }

}
