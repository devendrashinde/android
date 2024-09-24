package com.example.dshinde.myapplication_xmlpref.activities.listviewbased;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dshinde.myapplication_xmlpref.R;
import com.example.dshinde.myapplication_xmlpref.activities.AudioVideoActivity;
import com.example.dshinde.myapplication_xmlpref.activities.BaseActivity;
import com.example.dshinde.myapplication_xmlpref.activities.RandomButtonActivity;
import com.example.dshinde.myapplication_xmlpref.common.Constants;
import com.example.dshinde.myapplication_xmlpref.common.ControlType;
import com.example.dshinde.myapplication_xmlpref.helper.DynamicControls;
import com.example.dshinde.myapplication_xmlpref.helper.Factory;
import com.example.dshinde.myapplication_xmlpref.helper.JsonHelper;
import com.example.dshinde.myapplication_xmlpref.helper.StorageSelectionResult;
import com.example.dshinde.myapplication_xmlpref.helper.StorageUtil;
import com.example.dshinde.myapplication_xmlpref.listners.DataStorageListener;
import com.example.dshinde.myapplication_xmlpref.listners.ListviewActions;
import com.example.dshinde.myapplication_xmlpref.model.KeyValue;
import com.example.dshinde.myapplication_xmlpref.model.ScreenControl;
import com.example.dshinde.myapplication_xmlpref.services.DataStorage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShabdaKoshActivity extends BaseActivity implements ListviewActions {

    EditText keyField;
    EditText valueField;
    EditText searchText;
    EditText editText;
    RecyclerView listView;
    Button divider;
    DataStorage dataStorageManager;
    Map<String, Object> data = new HashMap<>();
    String collectionName = Constants.SHABDA_KOSH;
    LinearLayout editViewLayout;
    LinearLayout searchViewLayout;
    Menu myMenu;
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
            editText.setText(HtmlCompat.fromHtml(getShabdaKoshText(),HtmlCompat.FROM_HTML_MODE_LEGACY));
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
        editText = DynamicControls.getMultiLineEditText(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(10, 0, 10, 0);
        editViewLayout.addView(editText, layoutParams);
    }

    protected void setFindButtonAction(EditText editTextField) {
        editTextField.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (editTextField.getRight() - editTextField.getCompoundDrawables()[Constants.DRAWABLE_RIGHT].getBounds().width())) {
                        initDataStorageAndLoadData(editTextField.getText().toString());
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private void showEditView(boolean show) {
        editViewLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        searchViewLayout.setVisibility(show ? View.GONE : View.VISIBLE);
        MenuItem menuItem = myMenu.findItem(R.id.menu_edit);
        Drawable icon = getDrawable(show ? R.drawable.ic_format_line_spacing_black_24dp : R.drawable.ic_action_edit);
        menuItem.setIcon(icon);
        if (!show) {
            hideKeyboard(editViewLayout);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.navigation, menu);
        myMenu = menu;
        myMenu.removeItem(R.id.menu_add_to_dictionary);
        myMenu.removeItem(R.id.menu_backup);
        myMenu.removeItem(R.id.menu_design_screen);
        myMenu.removeItem(R.id.menu_test);
        myMenu.removeItem(R.id.menu_view);
        myMenu.removeItem(R.id.menu_import);
        myMenu.removeItem(R.id.menu_export);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_add:
                clear();
                showEditView(true);
                return true;
            case R.id.menu_save:
                save();
                return true;
            case R.id.menu_clear:
                clear();
                return true;
            case R.id.menu_copy:
                return true;
            case R.id.menu_edit:
                showEditView(editViewLayout.getVisibility() == View.GONE);
                return true;
            case R.id.menu_remove:
                remove();
                return true;
            case R.id.menu_share:
                share();
                return true;
            case R.id.menu_export:
                export();
                return true;
            case R.id.menu_daylight:
                setTheme(editViewLayout, Constants.DAY_MODE);
                setTheme(searchViewLayout, Constants.DAY_MODE);
                return true;
            case R.id.menu_nightlight:
                setTheme(editViewLayout, Constants.NIGHT_MODE);
                setTheme(searchViewLayout, Constants.NIGHT_MODE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setEditView(String key, String value) {
        keyField.setText(key);
        valueField.setText(value);
    }

    private void displayRandomButtonActivity(String value) {
        Intent intent = new Intent(this, RandomButtonActivity.class);
        intent.putExtra("text", value);
        startActivity(intent);
    }

    @Override
    public void save(View view) {
        save();
    }

    public void save() {
        String key = keyField.getText().toString().trim();
        String value = valueField.getText().toString().trim();
        dataStorageManager.save(key, value);
        showEditView(false);
        clear();
    }

    @Override
    public void remove(View view) {
        remove();
    }

    public void remove() {
        String key = keyField.getText().toString();
        clear();
        dataStorageManager.remove(key);
    }

    public void clear(View view) {
        clear();
    }

    public void clear() {
        String key = keyField.getText().toString(); // keep last key for ease of editing
        setEditView(key, "");
        searchText.setText("");
        keyField.requestFocus();
    }

    public void share() {
        String key = keyField.getText().toString();
        String value = valueField.getText().toString();
        if (!key.isEmpty() && !value.isEmpty()) {
            shareText(key + Constants.CR_LF + value);
        }
    }

    public void export() {
        startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), StorageUtil.PICK_DOCUMENT_FOLDER_FOR_EXPORT);
    }

    private void export(DocumentFile dir) {
        String path = StorageUtil.saveAsTextToDocumentFile(this, dir, collectionName, dataStorageManager.getDataString());
        if (path != null) {
            Toast.makeText(this, getResources().getString(R.string.save_to) + " " + path,
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        StorageSelectionResult result = StorageUtil.getStorageSelectionResult(this, requestCode, resultCode, data);
        if (result.getRequestCode() == StorageUtil.PICK_DOCUMENT_FOLDER_FOR_EXPORT) {
            export(result.getDir());
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        dataStorageManager.removeDataStorageListeners();
    }


    private void showPopup(String key, String value) {
        selectOption(Constants.SELECT_ACTION_FOR_NOTE, R.string.what_you_want_to_do,
                R.array.actions_on_note_item,
                null, key, value);
    }

    @Override
    protected void processSelectedOption(@NonNull String id, @NonNull String selectedOption, String key, String value) {
        switch (selectedOption) {
            case Constants.NAAMASMRAN:
                displayRandomButtonActivity(value);
                break;
            case Constants.AUDIO_NOTE:
                displayAudioVideoActivity(key, value);
                break;
            default:
                super.processSelectedOption(id, selectedOption, key, value);
                break;
        }
    }

    private void displayAudioVideoActivity(String key, String value) {
        Intent intent = new Intent(this, AudioVideoActivity.class);
        intent.putExtra(Constants.USERID, userId);
        intent.putExtra(Constants.PARAM_NOTE, collectionName);
        intent.putExtra(Constants.KEY, key);
        intent.putExtra(Constants.VALUE, value);
        startActivity(intent);
    }

    private String getShabdaKoshText()
    {
        int index=0;
        StringBuilder text = new StringBuilder("<html><body>");
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if(entry.getKey() != null) {
                text.append("<p>").append(entry.getKey()).append(":</p><br>");
            }
            if(entry.getValue() != null){
                if(entry.getValue() instanceof String) {
                    text.append("<p>").append(entry.getValue()).append("</p><br>");
                } else if (entry.getValue() instanceof List) {
                    text.append("<ul>");
                    for(Object obj : (List) entry.getValue()) {
                        text.append("<li>").append(obj).append("</li>");
                    }
                    text.append("</ul>");
                }
            }
        }
        return text + "</body></html>";
    }
}
