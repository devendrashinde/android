package com.example.dshinde.myapplication_xmlpref.activities.listviewbased;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;

import com.bumptech.glide.Glide;
import com.example.dshinde.myapplication_xmlpref.R;
import com.example.dshinde.myapplication_xmlpref.activities.BaseActivity;
import com.example.dshinde.myapplication_xmlpref.activities.DynamicLinearLayoutActivity;
import com.example.dshinde.myapplication_xmlpref.activities.MediaViewActivity;
import com.example.dshinde.myapplication_xmlpref.adapters.ListviewKeyValueObjectAdapter;
import com.example.dshinde.myapplication_xmlpref.common.Constants;
import com.example.dshinde.myapplication_xmlpref.helper.Converter;
import com.example.dshinde.myapplication_xmlpref.helper.Factory;
import com.example.dshinde.myapplication_xmlpref.helper.JsonHelper;
import com.example.dshinde.myapplication_xmlpref.helper.StorageUtil;
import com.example.dshinde.myapplication_xmlpref.listners.DataStorageListener;
import com.example.dshinde.myapplication_xmlpref.listners.OnSwipeTouchListener;
import com.example.dshinde.myapplication_xmlpref.model.KeyValue;
import com.example.dshinde.myapplication_xmlpref.model.MediaFields;
import com.example.dshinde.myapplication_xmlpref.model.ScreenControl;
import com.example.dshinde.myapplication_xmlpref.services.DataStorage;
import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Collections;
import java.util.List;

public class ScreenDesignActivity extends BaseActivity {

    String keyField;
    String valueField;
    ListView listView;
    Button addButton;
    Button editButton;
    Button delButton;
    Button screenPreview;
    EditText searchText;
    ListviewKeyValueObjectAdapter listAdapter;
    DataStorage dataStorageManager;
    DataStorageListener dataStorageListener;
    StorageReference storageReference;
    StorageReference storageFileRef;
    String collectionName = null;
    Integer requestMode = null;
    String screenConfig = null;
    LinearLayout editViewLayout;
    Gson gson = new GsonBuilder().create();
    Menu myMenu;
    private static final String CLASS_TAG = "ScreenDesignActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // get parameters
        Bundle bundle = getIntent().getExtras();
        collectionName = bundle.getString("screenName");
        userId = bundle.getString("userId");
        requestMode = bundle.getInt("requestMode", Constants.REQUEST_CODE_SCREEN_DESIGN);
        loadUI(bundle);
        initDataStorageAndLoadData(this);
    }

    private void loadUI(Bundle bundle) {
        if (isDesignMode()) {
            setContentView(R.layout.screen_design_activity);
            setTitle(collectionName + ": Design");
            screenConfig = screenDesign();
        } else {
            setContentView(R.layout.screen_capture_activity);
            setTitle(collectionName + ": Edit");
            screenConfig = bundle.getString("screenConfig");
        }
        listView = (ListView) findViewById(R.id.list);
        addButton = (Button) findViewById(R.id.btnAdd);
        editButton = (Button) findViewById(R.id.btnEdit);
        delButton = (Button) findViewById(R.id.btnDel);
        editViewLayout = (LinearLayout) findViewById(R.id.editView);
        searchText = (EditText) findViewById(R.id.searchText);

        populateListView();
        if (isDesignMode()) {
            setAddActionListener();
            setDeleteActionListener();
            setEditActionListener();
            setPreviewActionListener();
        }
        setSearchFieldWatcher();
        setValueFieldClearButtonAction();
    }

    private void setValueFieldClearButtonAction() {
        searchText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (searchText.getRight() - searchText.getCompoundDrawables()[Constants.DRAWABLE_RIGHT].getBounds().width())) {
                        searchText.setText("");
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private void setSearchFieldWatcher() {
        searchText.addTextChangedListener(new TextWatcher() {

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

    private void initDataStorageAndLoadData(Context context) {
        Log.d(CLASS_TAG, "initDataStorageAndLoadData->getDataStorageIntsance");
        dataStorageManager = Factory.getDataStorageIntsance(context, getDataStorageType(),
                (isDesignMode() ? Constants.SCREEN_DESIGN_NOTE_PREFIX : "") + collectionName,
                false, false, getDataStorageListener());
        Log.d(CLASS_TAG, "initDataStorageAndLoadData->loadData");
        dataStorageManager.loadData();
    }

    private DataStorageListener getDataStorageListener(){
        dataStorageListener = new DataStorageListener() {
            @Override
            public void dataChanged(String key, String value) {
                Log.d(CLASS_TAG, "dataChanged->loadDataInListView");
                loadDataInListView(dataStorageManager.getValues());
            }

            @Override
            public void dataLoaded(List<KeyValue> data) {
                Log.d(CLASS_TAG, "dataLoaded->loadDataInListView");
                loadDataInListView(data);
            }
        };
        return dataStorageListener;
    }

    private void loadDataInListView(List<KeyValue> data) {
        Log.d(CLASS_TAG, "loadDataInListView");
        runOnUiThread(() -> listAdapter.setData(data));
    }

    private boolean isDesignMode() {
        return requestMode == Constants.REQUEST_CODE_SCREEN_DESIGN;
    }

    private void setAddActionListener() {
        addButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startDyanmicScreenDesignActivity(false);
            }
        });
    }

    private void startDyanmicScreenDesignActivity(boolean edit) {
        if (edit && (valueField == null || valueField.length() == 0)) {
            Toast.makeText(this, "Please select record to Edit it.",
                    Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(this, DynamicLinearLayoutActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("noteId", collectionName);
            intent.putExtra("screenConfig", screenConfig);
            intent.putExtra("requestMode", requestMode);
            if (edit) {
                intent.putExtra("screenData", valueField);
            }
            startActivityForResult(intent, requestMode);
        }
    }

    private void startDyanmicScreenPreviewActivity() {
        if (dataStorageManager.count() > 0) {
            Intent intent = new Intent(this, DynamicLinearLayoutActivity.class);
            intent.putExtra("screenConfig", Converter.getValuesJsonString(dataStorageManager.getValues()));
            intent.putExtra("requestMode", Constants.REQUEST_CODE_SCREEN_PREVIEW);
            startActivityForResult(intent, Constants.REQUEST_CODE_SCREEN_PREVIEW);
        }
    }

    private void setEditActionListener() {
        editButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startDyanmicScreenDesignActivity(true);
            }
        });
    }

    private void setDeleteActionListener() {
        delButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                remove();
            }
        });
    }

    private void setPreviewActionListener() {
        screenPreview = (Button) findViewById(R.id.btnPreview);
        screenPreview.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startDyanmicScreenPreviewActivity();
            }
        });
    }

    private void showEditView(boolean show) {
        editViewLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        MenuItem menuItem = myMenu.findItem(R.id.menu_edit);
        Drawable icon = getDrawable(show ? R.drawable.ic_format_line_spacing_black_24dp : R.drawable.ic_edit_black);
        menuItem.setIcon(icon);
        if (!show) {
            hideKeyboard(editViewLayout);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.navigation, menu);
        removeUnwantedMenuItems(menu);
        myMenu = menu;
        showEditView(isDesignMode());
        return true;
    }

    private void removeUnwantedMenuItems(Menu menu) {
        menu.removeItem(R.id.menu_add_to_shadba_kosh);
        menu.removeItem(R.id.menu_backup);
        menu.removeItem(R.id.menu_sell);
        menu.removeItem(R.id.menu_settings);
        menu.removeItem(R.id.menu_save);
        menu.removeItem(R.id.menu_clear);
        menu.removeItem(R.id.menu_design_screen);
        menu.removeItem(R.id.menu_export);
        menu.removeItem(R.id.menu_pay);
        if (isDesignMode()) {
            menu.removeItem(R.id.menu_add);
            menu.removeItem(R.id.menu_remove);
            menu.removeItem(R.id.menu_view);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_add:
                startDyanmicScreenDesignActivity(false);
                return true;
            case R.id.menu_edit:
                if (isDesignMode()) {
                    showEditView(!(editViewLayout.getVisibility() == View.VISIBLE));
                } else {
                    startDyanmicScreenDesignActivity(true);
                }
                return true;
            case R.id.menu_remove:
                remove();
                return true;
            case R.id.menu_view:
                startDyanmicScreenPreviewActivity();
                return true;
            case R.id.menu_share:
                share();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setEditView(String key, String value) {
        keyField = key;
        valueField = value;
    }

    private void populateListView() {
        listAdapter = new ListviewKeyValueObjectAdapter(Collections.emptyList(), this, R.layout.list_view_items_flexbox);
        listView.setAdapter(listAdapter);
        setOnItemClickListenerToListView();
    }

    private void setOnItemClickListenerToListView() {
        AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                KeyValue kv = listAdapter.getItem(position);
                setEditView(kv.getKey(), kv.getValue());
                view.setSelected(true);
            }
        };
        listView.setOnItemClickListener(listener);

        AdapterView.OnItemLongClickListener listener2 = new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                KeyValue kv = listAdapter.getItem(position);
                MediaFields mediaFields = gson.fromJson(kv.getValue(), MediaFields.class);
                mediaFields.init();
                if(mediaFields.hasMedia()){
                    //mediaFields.setValues(gson.fromJson(kv.getValue(), Map.class));
                    //showPhotoMedia(mediaFields);
                    startMediaActvity(kv.getValue());
                }
                return true;
            }
        };
        listView.setOnItemLongClickListener(listener2);

    }

    private void startMediaActvity(String mediaFields){
        Intent intent = new Intent(getApplicationContext(), MediaViewActivity.class);
        intent.putExtra("userId", userId);
        intent.putExtra("noteId", collectionName);
        intent.putExtra("mediaFields", mediaFields);
        startActivity(intent);

    }

    public void save() {
        if (keyField != null && keyField.length() > 0) {
            dataStorageManager.save(keyField, valueField);
            showEditView(false);
            clear();
        }
    }

    public void remove() {
        if (keyField != null && keyField.length() > 0) {
            dataStorageManager.remove(keyField);
            clear();
        }
    }

    public void clear() {
        setEditView("", "");
    }

    public void share() {
        shareText(JsonHelper.formatAsString(valueField));
    }

    @Override
    protected  void doExport(DocumentFile dir) {
        String path = StorageUtil.saveAsTextToDocumentFile(this, dir, collectionName, dataStorageManager.getDataString());
        if (path != null) {
            Toast.makeText(this, "Saved to " + path,
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_CODE_SCREEN_DESIGN && resultCode == Constants.RESULT_CODE_OK) {
            Log.d(CLASS_TAG, "onActivityResult design");
            valueField = data.getExtras().getString("data");
            ScreenControl screenControl = gson.fromJson(valueField, ScreenControl.class);
            keyField = screenControl.getControlId();
            save();
        }

        if (requestCode == Constants.REQUEST_CODE_SCREEN_CAPTURE && resultCode == Constants.RESULT_CODE_OK) {
            Log.d(CLASS_TAG, "onActivityResult capture");
            valueField = data.getExtras().getString("data");
            keyField = data.getExtras().getString("key");
            save();
        }
    }

    @Override
    public void onStop() {
        Log.d(CLASS_TAG, "onStop");
        super.onStop();
        dataStorageManager.removeDataStorageListeners();
    }

    private String screenDesign() {
        return "[\n" +
                "    {\n" +
                "        \"controlId\": \"controlType\",\n" +
                "        \"positionId\": \"1\",\n" +
                "        \"controlType\": \"DropDownList\",\n" +
                "        \"textLabel\": \"Field Type:\",\n" +
                "        \"options\": \"Text\\nEditText\\nEditNumber\\nExpression\\nCheckBox\\nRadioButton\\nDropDownList\\nDatePicker\\nTimePicker\\nPhoto\\nDocument\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"controlId\": \"positionId\",\n" +
                "        \"positionId\": \"2\",\n" +
                "        \"controlType\": \"EditNumber\",\n" +
                "        \"textLabel\": \"Field position on screen:\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"controlId\": \"controlId\",\n" +
                "        \"positionId\": \"3\",\n" +
                "        \"controlType\": \"EditText\",\n" +
                "        \"textLabel\": \"Field Id:\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"controlId\": \"defaultValue\",\n" +
                "        \"controlType\": \"EditText\",\n" +
                "        \"positionId\": \"4\",\n" +
                "        \"textLabel\": \"Default value:\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"controlId\": \"indexField\",\n" +
                "        \"positionId\": \"5\",\n" +
                "        \"controlType\": \"RadioButton\",\n" +
                "        \"textLabel\": \"Is this field part of index?\",\n" +
                "        \"defaultValue\": \"No\",\n" +
                "        \"options\": \"Yes\\nNo\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"controlId\": \"textLabel\",\n" +
                "        \"positionId\": \"6\",\n" +
                "        \"controlType\": \"EditText\",\n" +
                "        \"textLabel\": \"Label Text:\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"controlId\": \"noteData\",\n" +
                "        \"positionId\": \"7\",\n" +
                "        \"controlType\": \"RadioButton\",\n" +
                "        \"textLabel\": \"Pick value from MyNote?\",\n" +
                "        \"defaultValue\": \"No\",\n" +
                "        \"options\": \"Yes\\nNo\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"controlId\": \"options\",\n" +
                "        \"positionId\": \"8\",\n" +
                "        \"controlType\": \"MultiLineEditText\",\n" +
                "        \"textLabel\": \"Enter values for DropdownList/Checkbox/RadioButtons/Expression/MyNote on separate lines:\"\n" +
                "    }\n" +
                "]";
    }

    private void showPhotoMediaUsingPopup(MediaFields mediaFields) {
        Dialog builder = new Dialog(this);
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                //nothing;
            }
        });
        PhotoView imageView = new PhotoView(this);
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.setOnPhotoTapListener(new OnPhotoTapListener(){
            public void onPhotoTap(ImageView view, float x, float y) {
                float xPercentage = x * 100f;
                if(xPercentage < 30) {
                    //left side tapped
                    String mediaFieldId = mediaFields.getNextPhotoMediaField();
                    if(mediaFieldId != null) {
                        downloadFile(mediaFieldId, mediaFields.getMediaFieldValue(mediaFieldId), imageView, builder);
                    }
                }
                if(xPercentage > 70) {
                    //right side tapped
                    String mediaFieldId = mediaFields.getPrevPhotoMediaField();
                    if(mediaFieldId != null) {
                        downloadFile(mediaFieldId, mediaFields.getMediaFieldValue(mediaFieldId), imageView, builder);
                    }
                }
            }
        });
        builder.addContentView(imageView, new RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT));
        String mediaFieldId = mediaFields.getNextPhotoMediaField();
        downloadFile(mediaFieldId, mediaFields.getMediaFieldValue(mediaFieldId), imageView, builder);
    };

    private void setImageOnTouchListener(ImageView imageView, MediaFields mediaFields, Dialog  builder){
        imageView.setOnTouchListener(new OnSwipeTouchListener(this){
            @Override
            public void onSwipeLeft() {
                String mediaFieldId = mediaFields.getNextPhotoMediaField();
                if(mediaFieldId != null) {
                    downloadFile(mediaFieldId, mediaFields.getMediaFieldValue(mediaFieldId), imageView, builder);
                }
            }
            @Override
            public void onSwipeRight() {
                String mediaFieldId = mediaFields.getPrevPhotoMediaField();
                if(mediaFieldId != null) {
                    downloadFile(mediaFieldId, mediaFields.getMediaFieldValue(mediaFieldId), imageView, builder);
                }
            }
        });
    }

    private void downloadFile(String mediaFieldId, String mediaFieldValue, ImageView imageView, Dialog builder) {
        //getting the storage reference
        try{
            if(storageReference == null) {
                storageReference = FirebaseStorage.getInstance().getReference();
            }
            storageFileRef = storageReference.child(Constants.STORAGE_PATH_NOTES + userId + "/" + collectionName + "/" + mediaFieldId + "/" + mediaFieldValue);
            //adding the file to reference
            storageFileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(getApplicationContext()).load(uri).into(imageView);
                    builder.show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(getApplicationContext(), "download failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Throwable throwable) {
            Toast.makeText(getApplicationContext(), "download failed: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

}
