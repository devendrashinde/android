package com.example.dshinde.myapplication_xmlpref;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class MainActivity extends AppCompatActivity implements ListviewActions {
    EditText valueField;
    ListView listView;
    //SimpleAdapter listAdapter;
    //ListviewKeyValueMapAdapter listAdapter;
    ListviewKeyValueObjectAdapter listAdapter;
    SharedPrefManager sharedPrefManager;
    String key;
    String sharedPreferenceName = "MyNotes";
    public static final int PICKFILE_RESULT_CODE = 42; // 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPrefManager = new SharedPrefManager(this, sharedPreferenceName, true);
        sharedPrefManager.add(new SharedPrefListener() {
            @Override
            public void sharedPrefChanged(String key, String value) {
                listAdapter.setData(sharedPrefManager.getValues());
            }
        });
        setContentView(R.layout.activity_main);
        valueField = (EditText) findViewById(R.id.VALUE_1);
        listView = (ListView) findViewById(R.id.list);
        populateListView();
        listView.setTextFilterEnabled(true);
        setValueFieldWatcher();
        setValueFieldClearButtonAction();

    }

    private void setValueFieldClearButtonAction() {
        valueField.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (valueField.getRight() - valueField.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        valueField.setText("");
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private void setValueFieldWatcher() {
        valueField.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count < before) {
                    // We're deleting char so we need to reset the adapter data
                    listAdapter.resetData();
                }
                listAdapter.getFilter().filter(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

    }

    public void onFindCheckboxClicked(View view) {
        boolean checked = ((CheckBox) view).isChecked();
        if(checked){
            valueField.setHint(R.string.searchText);
        } else{
            valueField.setHint(R.string.subject);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.navigation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_save:
                save();
                return true;
            case R.id.menu_clear:
                clear();
                return true;
            case R.id.menu_copy:
                copy();
                return true;
            case R.id.menu_edit:
                edit();
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
            case R.id.menu_backup:
                backup();
                return true;
            case R.id.menu_import:
                importFile();
                return true;
            case R.id.menu_view:
                viewFile();
                return true;
            case R.id.menu_sell:
                doSell();
                return true;
            case R.id.menu_settings:
                doSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void remove(View view) {
        remove();
    }

    public void remove() {
        if (key != null) {
            sharedPrefManager.remove(key);
        }
        clear();
    }

    @Override
    public void save(View view) {
        save();
    }

    public void save() {
        String value = valueField.getText().toString();
        sharedPrefManager.save(key, value);
        clear();
    }

    public void share(View view) {
        share();
    }

    public void share() {
        String fileName = valueField.getText().toString();
        if (!fileName.isEmpty()) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            String textToShare = fileName + "\n" + SharedPrefManager.getDataString(this, fileName);
            sendIntent.putExtra(Intent.EXTRA_TEXT, textToShare);
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        }
    }

    public void edit() {
        String fileName = valueField.getText().toString();
        if (!fileName.isEmpty()) {
            Intent intent = new Intent(MainActivity.this, Main2Activity.class);
            intent.putExtra("filename", fileName);
            startActivity(intent);
        }
    }

    public void edit(View view) {
        edit();
    }

    public void clear() {
        key = null;
        setEditView("");
        valueField.requestFocus();
    }

    public void clear(View view) {
        clear();
    }

    private void populateListView() {
        //listAdapter = new SimpleAdapter(this, sharedPrefManager.getValues(), R.layout.list_view_items, from, to);
        listAdapter = new ListviewKeyValueObjectAdapter(sharedPrefManager.getValues(),this, R.layout.list_view_items);
        //listAdapter = new ListviewKeyValueMapAdapter(sharedPrefManager.getValues(),this, R.layout.list_view_items);
        listView.setAdapter(listAdapter);
        setOnItemClickListenerToListView();
        setOnItemLongClickListenerToListView();
    }

    private void setOnItemClickListenerToListView() {
        AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // selected item
                key = ((TextView) view.findViewById(R.id.listKey)).getText().toString();
                String value = ((TextView) view.findViewById(R.id.listValue)).getText().toString();
                setEditView(value);
            }
        };
        listView.setOnItemClickListener(listener);
    }

    private void setOnItemLongClickListenerToListView() {
        AdapterView.OnItemLongClickListener listener = new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // selected item
                String value = ((TextView) view.findViewById(R.id.listValue)).getText().toString();
                setEditView(value);
                displayTextviewActivity(value);
                return true;
            }
        };
        listView.setOnItemLongClickListener(listener);
    }

    private void displayTextviewActivity(String sharedPreferenceName) {
        if(sharedPreferenceName != null && !sharedPreferenceName.isEmpty()) {
            displayTextviewActivity(sharedPreferenceName, SharedPrefManager.getDataString(this, sharedPreferenceName));
        }
    }

    private void displayTextviewActivity(String title, String text) {
        if(text != null && !text.isEmpty()) {
            Intent intent = new Intent(MainActivity.this, ScrollingTextViewActivity.class);
            intent.putExtra("subject", title);
            intent.putExtra("text", text);
            startActivity(intent);
        }
    }

    private void setSharedPrefManagerListener() {
        SharedPrefListener listener = new SharedPrefListener() {
            public void sharedPrefChanged(String changedKey, String changedValue) {
                listAdapter.notifyDataSetChanged();
                key = changedKey;
                setEditView(changedValue);
            }
        };
    }

    private void setEditView(String value) {
        valueField.setText(value);
    }

    public void copy() {
        String fileName = valueField.getText().toString();
        if (!fileName.isEmpty()) {
            copy(fileName);
        }
    }

    public void doSell() {
        String fileName = valueField.getText().toString();
        if (!fileName.isEmpty()) {
            Intent intent = new Intent(MainActivity.this, SellTeaActivity.class);
            intent.putExtra("filename", fileName);
            startActivity(intent);
        }
    }

    public void doSettings() {
        String fileName = valueField.getText().toString();
        if (!fileName.isEmpty()) {
            Intent intent = new Intent(MainActivity.this, CafeSettingsActivity.class);
            intent.putExtra("filename", fileName);
            startActivity(intent);
        }
    }

    private void copy(String initialValue) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Subject");

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(initialValue);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newFileName = input.getText().toString();
                String fileName = valueField.getText().toString();
                if (newFileName == null || newFileName.equalsIgnoreCase(fileName)) {
                    return;
                }
                performCopy(fileName, newFileName);

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void performCopy(String srcFile, String destFile) {
        if (SharedPrefManager.copy(this, srcFile, destFile)) {
            sharedPrefManager.save(null, destFile);
        }
    }

    public void export() {
        startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), StorageUtil.PICK_DOCUMENT_FOLDER_FOR_EXPORT);
    }

    private void export(DocumentFile dir) {
        String path = StorageUtil.saveAsTextToDocumentFile(this, dir, sharedPreferenceName, sharedPrefManager.getDataString());
        if (path != null) {
            Toast.makeText(this, "Saved to " + path,
                    Toast.LENGTH_LONG).show();
        }
    }

    public void backup() {
        startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), StorageUtil.PICK_DOCUMENT_FOLDER_FOR_BACKUP);
    }

    private void backup(DocumentFile dir) {
        Map<String, String> subjects = sharedPrefManager.getDataMap();
        String path = StorageUtil.saveAsObjectToDocumentFile(this, dir, sharedPreferenceName, new JSONObject(subjects));

        if (path != null) {
            Toast.makeText(this, "Saved to " + path,
                    Toast.LENGTH_SHORT).show();

            for (Map.Entry<String, String> entry : subjects.entrySet()) {
                String fileName = entry.getValue().trim();

                path = StorageUtil.saveAsObjectToDocumentFile(this, dir, fileName,
                        new JSONObject(SharedPrefManager.getDataMap(this, fileName)));
                if (path != null) {
                    Toast.makeText(this, "Saved to " + path,
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void restore() {

    }

    private String getFileNameWithOutExtension(String filename) {
        return filename.replaceFirst("[.][^.]+$", "");
    }

    public void viewFile() {
        selectFile(StorageUtil.PICK_FILE_FOR_VIEW, false);
    }

    private void selectFile(int actionCode, boolean selectMultiple) {
        Intent selectFile = new Intent(Intent.ACTION_GET_CONTENT);
        selectFile.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        selectFile.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, selectMultiple);
        selectFile.setType("*/*");
        selectFile = Intent.createChooser(selectFile, "Select File");
        startActivityForResult(selectFile, actionCode);
    }

    public void importFile() {
        selectFile(StorageUtil.PICK_FILE_FOR_IMPORT,false);
    }

    private void importFile(String sharedPreferenceName, JSONObject data) {
        try {
            if(SharedPrefManager.loadData(this, sharedPreferenceName, JsonHelper.toMap(data), true)){
                valueField.setText(sharedPreferenceName);
                save();
            }
        } catch (JSONException ex) {
            Toast.makeText(this, "Failed to import file, error\n" + ex.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Uri fileUri = data.getData();
            DocumentFile dir;
            switch (requestCode) {
                case StorageUtil.PICK_DOCUMENT_FOLDER_FOR_EXPORT:
                    dir = StorageUtil.getDocumentDir(this, fileUri);
                    export(dir);
                    break;
                case StorageUtil.PICK_DOCUMENT_FOLDER_FOR_BACKUP:
                    dir = StorageUtil.getDocumentDir(this, fileUri);
                    backup(dir);
                    break;
                case StorageUtil.PICK_FILE_FOR_IMPORT:
                case StorageUtil.PICK_FILE_FOR_VIEW:
                    String fileName = StorageUtil.getFileName(this, fileUri);
                    if(requestCode == StorageUtil.PICK_FILE_FOR_IMPORT) {
                        if (fileName.substring(fileName.lastIndexOf(".")).equalsIgnoreCase(".json")) {
                            JSONObject fileData = StorageUtil.getObjectFromDocumentFile(this, fileUri);
                            Toast.makeText(this, "Importing from file " + fileName,
                                    Toast.LENGTH_LONG).show();
                            importFile(getFileNameWithOutExtension(fileName), fileData);
                        } else {
                            Toast.makeText(this, "Only JSON files are supported",
                                    Toast.LENGTH_LONG).show();
                        }
                    } else{
                        String text = null;
                        if(fileName.substring(fileName.lastIndexOf(".")).equalsIgnoreCase(".json")){
                            JSONObject fileData = StorageUtil.getObjectFromDocumentFile(this, fileUri);
                            text = fileData.toString();
                        } else {
                            text = StorageUtil.getTextFromDocumentFile(this, fileUri);
                        }
                        displayTextviewActivity(fileName, text);
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
