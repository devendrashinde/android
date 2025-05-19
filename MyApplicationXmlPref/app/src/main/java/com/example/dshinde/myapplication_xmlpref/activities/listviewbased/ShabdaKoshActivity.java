package com.example.dshinde.myapplication_xmlpref.activities.listviewbased;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.text.HtmlCompat;

import com.example.dshinde.myapplication_xmlpref.R;
import com.example.dshinde.myapplication_xmlpref.activities.BaseActivity;
import com.example.dshinde.myapplication_xmlpref.common.Constants;
import com.example.dshinde.myapplication_xmlpref.helper.Factory;
import com.example.dshinde.myapplication_xmlpref.listners.DataStorageListener;
import com.example.dshinde.myapplication_xmlpref.model.KeyValue;
import com.example.dshinde.myapplication_xmlpref.services.DataStorage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShabdaKoshActivity extends BaseActivity {

    EditText searchText;
    TextView textView;
    DataStorage dataStorageManager;
    Map<String, Object> data = new HashMap<>();
    String collectionName = Constants.SHABDA_KOSH;
    LinearLayout editViewLayout;
    LinearLayout searchViewLayout;
    private final Gson gson = new GsonBuilder().create();
    private static final String CLASS_TAG = "ShabdaKoshActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get parameters
        Bundle bundle = getIntent().getExtras();
        userId = bundle.getString(Constants.USERID);
        loadUI();
    }

    private void initDataStorageAndLoadData(String noteItem) {
        Log.d(CLASS_TAG, "initDataStorageAndLoadData->getDataStorageInstance");
        dataStorageManager = Factory.getDataStorageInstance(this,
                getDataStorageType(),
                collectionName + "/" + noteItem,
                false, false,
                new DataStorageListener() {
                    @Override
                    public void dataChanged(String key, String value) {
                        Log.d(CLASS_TAG, "dataChanged key: " + key + ", value: " + value);
                        loadDataInEditView(dataStorageManager.getValues());
                    }

                    @Override
                    public void dataLoaded(List<KeyValue> data) {
                        Log.d(CLASS_TAG, "dataLoaded");
                        loadDataInEditView(data);
                    }
        });
        dataStorageManager.loadData();
    }

    private void loadDataInEditView(List<KeyValue> keyValues) {
        Log.d(CLASS_TAG, "loadDataInEditView");
        if(!keyValues.isEmpty() && keyValues.get(0).getValue() != null ){
            data = gson.fromJson(keyValues.get(0).getValue(), Map.class);
            textView.setText(HtmlCompat.fromHtml(getShabdaKoshText(),HtmlCompat.FROM_HTML_MODE_LEGACY));
        } else{
            showInShortToast("Not found");
        }
    }

    private void loadUI() {
        setContentView(R.layout.shabdkosh_layout);
        editViewLayout = (LinearLayout) findViewById(R.id.editView);
        searchViewLayout = (LinearLayout) findViewById(R.id.searchView);
        searchText = (EditText) findViewById(R.id.searchText);
        setTitle(collectionName);
        setFindButtonAction(searchText);
        textView = editViewLayout.findViewById(R.id.dictionaryItem);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.shabdkosh, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_clear:
                clear();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void setFindButtonAction(EditText searchTextField) {
        searchTextField.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (searchTextField.getRight() - searchTextField.getCompoundDrawables()[Constants.DRAWABLE_RIGHT].getBounds().width())) {
                        if(!searchTextField.getText().toString().isEmpty()) {
                            initDataStorageAndLoadData(searchTextField.getText().toString().trim());
                        }
                        return true;
                    }
                }
                return false;
            }
        });
    }

    public void clear() {
        searchText.setText("");
        textView.setText("");
        searchText.requestFocus();
    }

    @Override
    public void onStop() {
        super.onStop();
        dataStorageManager.removeDataStorageListeners();
    }

    private String getShabdaKoshText()
    {
        int index=0;
        StringBuilder text = new StringBuilder("<html><body>");
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if(entry.getKey() != null) {
                text.append("<p><h1>").append(entry.getKey()).append("</h1></p><br>");
            }
            if(entry.getValue() != null){
                if(entry.getValue() instanceof String) {
                    text.append("<p>").append(entry.getValue()).append("</p><br>");
                } else if (entry.getValue() instanceof List) {
                    text.append("<ul>");
                    for(Object obj : (List) entry.getValue()) {
                        text.append("<li><h3>").append(obj).append("</h3></li>");
                    }
                    text.append("</ul>");
                }
            }
        }
        return text + "</body></html>";
    }
}
