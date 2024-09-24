package com.example.dshinde.myapplication_xmlpref.activities.recyclerviewbased;

import static com.example.dshinde.myapplication_xmlpref.common.Constants.DRAWABLE_RIGHT;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.documentfile.provider.DocumentFile;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.example.dshinde.myapplication_xmlpref.R;
import com.example.dshinde.myapplication_xmlpref.activities.AudioVideoActivity;
import com.example.dshinde.myapplication_xmlpref.activities.BaseActivity;
import com.example.dshinde.myapplication_xmlpref.activities.GraphViewActivity;
import com.example.dshinde.myapplication_xmlpref.activities.PhotoGalleryActivity;
import com.example.dshinde.myapplication_xmlpref.activities.RelationshipActivity;
import com.example.dshinde.myapplication_xmlpref.activities.ScrollingTextViewActivity;
import com.example.dshinde.myapplication_xmlpref.activities.listviewbased.ShabdaKoshActivity;
import com.example.dshinde.myapplication_xmlpref.adapters.MarginItemDecoration;
import com.example.dshinde.myapplication_xmlpref.adapters.RecyclerViewKeyValueAdapter;
import com.example.dshinde.myapplication_xmlpref.common.Constants;
import com.example.dshinde.myapplication_xmlpref.helper.Converter;
import com.example.dshinde.myapplication_xmlpref.helper.Factory;
import com.example.dshinde.myapplication_xmlpref.helper.StorageUtil;
import com.example.dshinde.myapplication_xmlpref.listners.DataStorageListener;
import com.example.dshinde.myapplication_xmlpref.listners.RecyclerViewKeyValueItemListener;
import com.example.dshinde.myapplication_xmlpref.model.KeyValue;
import com.example.dshinde.myapplication_xmlpref.services.AddNoteToDictionaryWorker;
import com.example.dshinde.myapplication_xmlpref.services.BackupBackgroundService;
import com.example.dshinde.myapplication_xmlpref.services.BackupWorker;
import com.example.dshinde.myapplication_xmlpref.services.DataStorage;
import com.example.dshinde.myapplication_xmlpref.services.ReadWriteOnceDataStorage;
import com.example.dshinde.myapplication_xmlpref.services.SharedPrefManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MainActivityRecyclerView extends BaseActivity  {
    private static final int REQ_CODE = 0;
    EditText valueField;
    RecyclerView listView;
    RecyclerViewKeyValueAdapter listAdapter;
    DataStorage dataStorageManager;
    DocumentFile selectedDir;
    ReadWriteOnceDataStorage readWriteOnceDataStorage;
    String key;
    String databasePathNotes = Constants.DATABASE_PATH_NOTES;
    private static final String CLASS_TAG = "MainActivityRV";
    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(CLASS_TAG, "onCreate");
        Bundle bundle = getIntent().getExtras();
        userId = bundle.getString(Constants.USERID);
        // Check if user is signed in (non-null) and update UI accordingly.
        loadUI();
        initDataStorageAndLoadData(this);
    }

    private void loadUI() {
        Log.d(CLASS_TAG, "loadUI");
        setContentView(R.layout.activity_main_recycler_view);
        valueField = findViewById(R.id.VALUE_1);
        listView = findViewById(R.id.list);
        populateListView();
        setValueFieldWatcher();
        setValueFieldClearButtonAction();
    }

    private void initDataStorageAndLoadData(Context context) {
        Log.d(CLASS_TAG, "initDataStorageAndLoadData->getDataStorageInstance");
        dataStorageManager = Factory.getDataStorageInstance(context,
                getDataStorageType(),
                databasePathNotes,
                true,
                false, new DataStorageListener() {
                    @Override
                    public void dataChanged(String key, String value) {
                        Log.d(CLASS_TAG, "dataChanged key: " + key + ", value: " + value);
                        loadDataInListView(dataStorageManager.getValues());
                    }

                    @Override
                    public void dataLoaded(List<KeyValue> data) {
                        Log.d(CLASS_TAG, "dataLoaded");
                        loadDataInListView(data);
                    }
                });
        Log.d(CLASS_TAG, "initDataStorageAndLoadData->loadData");
        dataStorageManager.loadData();
    }

    private void loadDataInListView(List<KeyValue> data) {
        Log.d(CLASS_TAG, "loadDataInListView");
        runOnUiThread(() -> listAdapter.setData(data));
    }

    private void setValueFieldClearButtonAction() {
        valueField.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (valueField.getRight() - valueField.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        clear();
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
                // as user is typing test, need to clear earlier key if any and user need to select the record from list
                key = null;
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

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.navigation, menu);
        menu.removeItem(R.id.menu_add);
        menu.removeItem(R.id.menu_share);
        menu.removeItem(R.id.menu_clear);
        menu.removeItem(R.id.menu_remove);
        menu.removeItem(R.id.menu_copy);
        menu.removeItem(R.id.menu_daylight);
        menu.removeItem(R.id.menu_nightlight);
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
            case R.id.menu_daylight:
                return true;
            case R.id.menu_test:
                showTestActivity();
                return true;
            case R.id.menu_design_screen:
                designScreen();
                return true;
            case R.id.menu_add_to_dictionary:
                addToDictionary();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showTestActivity() {
        String fileName = valueField.getText().toString();
        //Intent intent = new Intent(MainActivityRecyclerView.this, GraphViewActivity.class);
        Intent intent = new Intent(MainActivityRecyclerView.this, PhotoGalleryActivity.class);
        intent.putExtra(Constants.PARAM_FILENAME, fileName);
        intent.putExtra(Constants.USERID, userId);
        startActivity(intent);
    }

    private void startRelationshipActivity() {
        String fileName = valueField.getText().toString();
        Intent intent = new Intent(MainActivityRecyclerView.this, RelationshipActivity.class);
        intent.putExtra(Constants.PARAM_FILENAME, fileName);
        intent.putExtra(Constants.USERID, userId);
        startActivity(intent);
    }

    public void remove() {
    }

    public void save() {
        String value = valueField.getText().toString();
        value = value.replaceAll("/", "-");
        value = value.replaceAll("\\s+"," ").trim();
        int index = dataStorageManager.getKeyIndex(value);
        if(index < 0) {
            dataStorageManager.save(key, value);
            clear();
        }
    }

    public void share() {
        String fileName = valueField.getText().toString();
        if (!fileName.isEmpty()) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            String textToShare = fileName + Constants.CR_LF + SharedPrefManager.getDataString(this, fileName);
            sendIntent.putExtra(Intent.EXTRA_TEXT, textToShare);
            sendIntent.setType(Constants.TEXT_PLAIN);
            startActivity(sendIntent);
        }
    }

    public void edit() {
        String fileName = valueField.getText().toString();
        if (!fileName.isEmpty()) {
            if(fileName.equals(Constants.SHABDA_KOSH)){
                startShabdaKoshActivity(fileName);
            } else {

                startActivityForAction(fileName, "EDIT");
            }
        }
    }

    private void startShabdaKoshActivity(String fileName) {
        Intent intent = new Intent(MainActivityRecyclerView.this, ShabdaKoshActivity.class);
        intent.putExtra(Constants.USERID, userId);
        startActivity(intent);
    }

    private void startEditActivity(String fileName) {
        Intent intent = new Intent(MainActivityRecyclerView.this, Main2ActivityRecyclerView.class);
        intent.putExtra(Constants.PARAM_FILENAME, fileName);
        intent.putExtra(Constants.USERID, userId);
        startActivity(intent);
    }

    public void clear() {
        key = null;
        setEditView("");
        valueField.requestFocus();
    }

    private void populateListView() {
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        listAdapter = new RecyclerViewKeyValueAdapter(Collections.emptyList(), this,
                R.layout.list_view_items_recyclerview, getOnItemClickListenerToListView());
        listView.setLayoutManager(mLayoutManager);
        listView.addItemDecoration(new MarginItemDecoration(8));
        listView.setAdapter(listAdapter);

        listAdapter.registerAdapterDataObserver( new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                listView.scrollToPosition(dataStorageManager.getLastModifiedIndex());
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                listView.scrollToPosition(dataStorageManager.getLastModifiedIndex());
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                listView.scrollToPosition(dataStorageManager.getLastModifiedIndex());
            }
        });
    }

    private RecyclerViewKeyValueItemListener getOnItemClickListenerToListView() {
        RecyclerViewKeyValueItemListener listener = new RecyclerViewKeyValueItemListener() {
            @Override
            public void onItemClick(KeyValue kv) {
                key = kv.getKey();
                setEditView(kv.getValue());
            }

            @Override
            public boolean onItemLongClick(KeyValue kv) {
                String value = kv.getValue();
                setEditView(value);
                showPopup(kv.getValue());
                return true;
            }
        };
        return listener;
    }

    private void showPopup(String value) {
        if( value != null && !value.isEmpty()) {
            selectOption(Constants.SELECT_ACTION_FOR_NOTE, R.string.what_you_want_to_do, R.array.actions_on_note,
                    null, key, value);
        }
    }

    @Override
    protected void processSelectedOption(@NonNull String id, @NonNull String selectedOption, String key, String value) {
        switch (selectedOption) {
            case Constants.VIEW_NOTE:
                viewNote(value);
                break;
            case Constants.PLAY_NOTE:
                playNote(value);
                break;
            case Constants.PLAY_NOTE_ITEMS:
                playNoteItems(value);
                break;
            case Constants.SCREEN_DESIGN:
                designScreen();
                break;
            case Constants.VIEW_RELATIONSHIP:
                startRelationshipActivity();
                break;
            default:
                super.processSelectedOption(id, selectedOption, key, value);
                break;
        }
    }

    private void playNote(String fileName) {
        startActivityForAction(fileName, Constants.PLAY_NOTE);
    }

    private void playNoteItems(String fileName) {
        startActivityForAction(fileName, Constants.PLAY_NOTE_ITEMS);
    }

    private void viewNote(String fileName) {
        startActivityForAction(fileName, Constants.VIEW);
    }

    private void startAudioNoteActivity(String title, List<KeyValue> values, String action) {
        Intent intent = new Intent(MainActivityRecyclerView.this, AudioVideoActivity.class);
        intent.putExtra(Constants.USERID, userId);
        intent.putExtra("note", title);
        if(action.equals(Constants.PLAY_NOTE)) {
            intent.putExtra("key", title);
        }
        intent.putExtra("data", (Serializable) values);
        startActivity(intent);
    }

    private void startViewNoteActivity(String title, String text) {
        if (text != null && !text.isEmpty()) {
            Intent intent = new Intent(MainActivityRecyclerView.this,
                    ScrollingTextViewActivity.class);
            intent.putExtra("subject", title);
            intent.putExtra("text", text);
            startActivity(intent);
        }
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

    public void doSettings() {
        /*
        Intent intent = new Intent(MainActivityRecyclerView.this,
                CafeSettingsActivity.class);
        intent.putExtra(Constants.USERID, userId);
        startActivity(intent);
         */
    }

    private void designScreen() {
        String fileName = valueField.getText().toString();
        if (!fileName.isEmpty()) {
            startDesignOrEditActivity(fileName, Constants.REQUEST_CODE_SCREEN_DESIGN, null);
        }
    }

    private void startDesignOrEditActivity(String fileName, Integer requestMode, String screeConfig) {
        Intent intent = new Intent(this, ScreenDesignActivityRecyclerView.class);
        intent.putExtra(Constants.SCREEN_NAME, fileName);
        intent.putExtra(Constants.REQUEST_MODE, requestMode);
        intent.putExtra(Constants.USERID, userId);
        if (requestMode == Constants.REQUEST_CODE_SCREEN_CAPTURE) {
            intent.putExtra(Constants.SCREEN_CONFIG, screeConfig);
        }
        startActivity(intent);
    }

    /*
    this method will extract words from selected note and it will add to Sanskrit ShabdKosh note
     */
    private void addToDictionary() {
        String collectionName = valueField.getText().toString();
        if (collectionName != null && !collectionName.isEmpty()) {
            final Data data = new Data.Builder()
                    .putString(Constants.PARAM_NOTE, collectionName)
                    .putString(Constants.PARAM_DICTIONARY, Constants.SHABDA_KOSH)
                    .build();
            Constraints constraints = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresBatteryNotLow(true)
                    .build();
            final OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(AddNoteToDictionaryWorker.class)
                    .setConstraints(constraints)
                    .setInputData(data)
                    .build();

            WorkManager.getInstance(this).enqueue(workRequest);

            // Get the work status
            WorkManager.getInstance(getApplicationContext()).getWorkInfoByIdLiveData(workRequest.getId())
                    .observe(this, new Observer<WorkInfo>() {
                        @Override
                        public void onChanged(WorkInfo workInfo) {
                            showInLongToast("Request for adding " + collectionName + " is " + workInfo.getState().name());
                        }
                    });

        }
    }

    private void copy(String initialValue) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.new_subject);

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(initialValue);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
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
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void performCopy(String srcFile, String destFile) {
        // TODO implement copy
        if (SharedPrefManager.copy(this, srcFile, destFile)) {
            dataStorageManager.save(null, destFile);
        }
    }

    public void export() {
        startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), StorageUtil.PICK_DOCUMENT_FOLDER_FOR_EXPORT);
    }

    private void export(DocumentFile dir) {
        // TODO getDataString in dataStorageManager
        selectedDir = dir;
        String path = StorageUtil.saveAsTextToDocumentFile(this, dir, databasePathNotes, dataStorageManager.getDataString());
        if (path != null) {
            Toast.makeText(this, R.string.save_to + " " + path,
                    Toast.LENGTH_LONG).show();
        }
    }

    public void backup() {
        startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), StorageUtil.PICK_DOCUMENT_FOLDER_FOR_BACKUP);
    }

    private void backup(DocumentFile dir) {
        selectedDir = dir;
        if (dir != null && dataStorageManager.getValues().size() > 0) {
            final Data data = new Data.Builder()
                    .putString(Constants.PARAM_FOLDER, dir.getUri().toString())
                    .build();
            Constraints constraints = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresBatteryNotLow(true)
                    .build();
            final OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(BackupWorker.class)
                    .setConstraints(constraints)
                    .setInputData(data)
                    .build();

            WorkManager.getInstance(this).enqueue(workRequest);

            // Get the work status
            WorkManager.getInstance(getApplicationContext()).getWorkInfoByIdLiveData(workRequest.getId())
                    .observe(this, new Observer<WorkInfo>() {
                        @Override
                        public void onChanged(WorkInfo workInfo) {
                            showInLongToast("Backup status: " + workInfo.getState().name());
                        }
                    });

        }
    }
    /*
    private void backup2(DocumentFile dir) {
        selectedDir = dir;
        if (dir != null) {
            AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
            Intent backupIntent = new Intent(this, BackupBackgroundService.class);
            backupIntent.putExtra(Constants.PARAM_DATA, gson.toJson(dataStorageManager.getValues()));
            backupIntent.putExtra(Constants.PARAM_FOLDER, dir.getUri().toString());
            boolean backupInProgress = (PendingIntent.getBroadcast(this, REQ_CODE, backupIntent, PendingIntent.FLAG_NO_CREATE) != null);
            if(!backupInProgress) {
                PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), REQ_CODE, backupIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager.set(AlarmManager.RTC_WAKEUP, -1, pendingIntent);

            }
        }
    }
    */

    public void restore() {

    }

    public void viewFile() {
        selectFile(StorageUtil.PICK_FILE_FOR_VIEW, false);
    }

    public void importFile() {
        selectFile(StorageUtil.PICK_FILE_FOR_IMPORT, false);
    }

    private void importFile(String collectionName, String data) {
        String newCollectionName = getSubNoteCollectionName(collectionName);
        if(newCollectionName.equals(collectionName)) {
            Optional<KeyValue> keyValue = dataStorageManager.getValues().stream()
                    .filter(x -> x.getValue().equalsIgnoreCase(collectionName)).findFirst();
            if (!keyValue.isPresent()) {
                valueField.setText(collectionName);
                save();
            } else {
                showInShortToast(getResources().getString(R.string.note_already_exists_merging_changes));
            }
        }
        Intent intent = new Intent(MainActivityRecyclerView.this, Main2ActivityRecyclerView.class);
        intent.putExtra(Constants.PARAM_FILENAME, newCollectionName);
        intent.putExtra(Constants.USERID, userId);
        intent.putExtra(Constants.PARAM_NOTE_DATA, data);
        startActivity(intent);
    }

    private String getSubNoteCollectionName(String collectionName) {
        if(collectionName.startsWith(Constants.MEDIA_NOTE_FILE_PREFIX)) {
            return collectionName.replace(Constants.MEDIA_NOTE_FILE_PREFIX, Constants.MEDIA_NOTE_PREFIX);
        }
        if(collectionName.startsWith(Constants.SCREEN_DESIGN_NOTE_FILE_PREFIX)) {
            return collectionName.replace(Constants.SCREEN_DESIGN_NOTE_FILE_PREFIX, Constants.SCREEN_DESIGN_NOTE_PREFIX);
        }
        return collectionName;
    }

    @Override
    protected void doBackup(DocumentFile targetFolder) {
        backup(targetFolder);
    }

    @Override
    protected void doExport(DocumentFile targetFolder) {
        export(targetFolder);
    }

    @Override
    protected void doImport(String collectionName, String data) {
        importFile(collectionName, data);
    }

    @Override
    protected void doView(String collectionName, String data) {
        startViewNoteActivity(collectionName, data);
    }

    @Override
    public void onStop() {
        super.onStop();
        dataStorageManager.removeDataStorageListeners();
    }

    private void startActivityForAction(String collection, String action) {
        readWriteOnceDataStorage = Factory.getReadOnceFireDataStorageInstance(
            (action.equals("EDIT") ? Constants.SCREEN_DESIGN_NOTE_PREFIX : "") + collection,
            new DataStorageListener() {
                @Override
                public void dataChanged(String key, String value) {
                }

                @Override
                public void dataLoaded(List<KeyValue> data) {
                    switch (action) {
                        case Constants.EDIT:
                            /*
                            if screen config is found then start ScreenDesignActivity
                            otherwise start normal edit activity
                             */
                            if (data.size() > 0) {
                                String screenConfig = Converter.getValuesJsonString(data);
                                startDesignOrEditActivity(collection, Constants.REQUEST_CODE_SCREEN_CAPTURE, screenConfig);
                            } else {
                                startEditActivity(collection);
                            }
                            break;
                        case Constants.VIEW:
                            startViewNoteActivity(collection, Converter.getKeyValuesJsonString(data));
                            break;
                        case Constants.PLAY_NOTE_ITEMS:
                        case Constants.PLAY_NOTE:
                            startAudioNoteActivity(collection, data, action);
                            break;
                        case Constants.BACKUP:
                            if (data.size() > 0) {
                                String path = StorageUtil.saveAsObjectToDocumentFile(getApplicationContext(), selectedDir, collection, gson.toJson(data));
                                if(path != null ){
                                    runOnUiThread(()-> showInLongToast(getResources().getString(R.string.save_to) + " " + path ));
                                }
                            }
                            break;
                        default: break;
                    }
                    readWriteOnceDataStorage.removeDataStorageListeners();
                }
            });
    }

    private void selectAndCropPhoto() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    Uri selectedFile = result.getUri();
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                }
                break;
        }
    }

    private void saveImage(Uri imageUri) {
        String key = valueField.getText().toString();
        if (key != null && imageUri != null) {
            key = Constants.MEDIA_NOTE_PREFIX + key;
            Map<String, String> data = new HashMap<>();
            data.put(key, imageUri.toString());
            dataStorageManager.save(key, gson.toJson(data));
        }
    }
}
