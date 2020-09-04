package com.example.dshinde.myapplication_xmlpref.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Scroller;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.dshinde.myapplication_xmlpref.R;
import com.example.dshinde.myapplication_xmlpref.common.Constants;
import com.example.dshinde.myapplication_xmlpref.common.ControlType;
import com.example.dshinde.myapplication_xmlpref.common.YesNo;
import com.example.dshinde.myapplication_xmlpref.helper.StorageUtil;
import com.example.dshinde.myapplication_xmlpref.model.MediaFields;
import com.example.dshinde.myapplication_xmlpref.model.ScreenControl;
import com.example.dshinde.myapplication_xmlpref.pickers.DatePickerFragment;
import com.example.dshinde.myapplication_xmlpref.pickers.TimePickerFragment;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class DynamicLinearLayoutActivity extends AppCompatActivity {

    private static final String TAKE_PHOTO = "Take Photo";
    private static final String SELECT_PHOTO = "Select Photo";
    private static final String CLASS_TAG = "DynamicActivity";
    private String userId;
    private String noteId;
    private LinearLayout linearLayout;
    private List<ScreenControl> controls;
    private Map<String, String> data = new HashMap<>();
    private Gson gson = new GsonBuilder().create();
    private Integer requestMode = null;
    private ScreenControl currentScreenControl;
    private StorageReference storageReference;
    private StorageReference storageFileRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(CLASS_TAG, "onCreate");
        setContentView(R.layout.activity_dynamic_linear_layout);
        linearLayout = findViewById(R.id.linear_layout);

        Bundle bundle = getIntent().getExtras();
        userId = bundle.getString("userId");
        noteId = bundle.getString("noteId");
        String screenConfig = bundle.getString("screenConfig");
        String screenData = bundle.getString("screenData");
        if (screenData != null && screenData.length() > 0) {
            data = gson.fromJson(screenData, Map.class);
        }
        requestMode = bundle.getInt("requestMode", Constants.REQUEST_CODE_SCREEN_DESIGN);

        switch (requestMode){
            case Constants.REQUEST_CODE_SCREEN_DESIGN:
                setTitle("Screen Design: Define field");
                break;
            case Constants.REQUEST_CODE_SCREEN_CAPTURE:
                setTitle("Add/Edit Record");
                break;
            default:
                setTitle("Screen Design: Preview");
                break;
        }
        renderUI(screenConfig);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.navigation, menu);
        menu.removeItem(R.id.menu_add);
        menu.removeItem(R.id.menu_clear);
        menu.removeItem(R.id.menu_copy);
        menu.removeItem(R.id.menu_edit);
        menu.removeItem(R.id.menu_remove);
        menu.removeItem(R.id.menu_share);
        menu.removeItem(R.id.menu_export);
        menu.removeItem(R.id.menu_backup);
        menu.removeItem(R.id.menu_import);
        menu.removeItem(R.id.menu_view);
        menu.removeItem(R.id.menu_pay);
        menu.removeItem(R.id.menu_sell);
        menu.removeItem(R.id.menu_settings);
        menu.removeItem(R.id.menu_design_screen);
        menu.removeItem(R.id.menu_add_to_shadba_kosh);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_save:
                closeActivityWithReturnValues();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void renderUI(String screenConfig) {
        Log.d(CLASS_TAG, "enter(renderUI)");
        new Thread() {
            @Override
            public void run() {
                parseConfig(screenConfig);
                createControls();
                try {
                    for (ScreenControl screenControl : controls) {
                        new Handler(Looper.getMainLooper()).post(() -> addControlsToUI(screenControl));
                    }
                } catch (final Exception ex) {
                    Log.i("---","Exception in thread");
                }
            }
        }.start();
        Log.d(CLASS_TAG, "exit(renderUI)");
    }

    private void addControlsToUI(ScreenControl screenControl) {
        Log.d(CLASS_TAG, "enter(addControlsToUI: " + screenControl.getControlId());
        if(screenControl.getLabelControl() != null){
            linearLayout.addView(screenControl.getLabelControl());
        }
        if(screenControl.getControlType() == ControlType.CheckBox){
            for(View view : screenControl.getOptionControls()){
                linearLayout.addView(view);
            }
        } else if(screenControl.getValueControl() != null){
            linearLayout.addView(screenControl.getValueControl());
        }
        if(screenControl.getMediaControl() != null){
            linearLayout.addView(screenControl.getMediaControl());
        }
        Log.d(CLASS_TAG, "exit(addControlsToUI)");
    }

    private void createControls() {
        Log.d(CLASS_TAG, "enter(createControls)");
        for (ScreenControl screenControl : controls) {
            switch (screenControl.getControlType()) {
                case Text:
                    addText(screenControl);
                    break;
                case CheckBox:
                    addCheckbox(screenControl);
                    break;
                case EditText:
                    addEditText(screenControl, false);
                    break;
                case MultiLineEditText:
                    addEditText(screenControl, true);
                    break;
                case DatePicker:
                    addDatePicker(screenControl);
                    break;
                case TimePicker:
                    addTimePicker(screenControl);
                    break;
                case RadioButton:
                    addRadioButton(screenControl);
                    break;
                case DropDownList:
                    addDropDownList(screenControl);
                    break;
                case Photo:
                    if(storageReference == null) {
                        storageReference = FirebaseStorage.getInstance().getReference();
                    }
                    addPhotoControl(screenControl);
                    break;
                default:
                    break;
            }
        }
        Log.d(CLASS_TAG, "exit(createControls)");
    }

    private void addText(ScreenControl screenControl) {
        screenControl.setLabelControl(getTextView(screenControl.getTextLabel()));
    }

    private String getValue(ScreenControl screenControl){
        String value = data.get(screenControl.getControlId());
        return value != null ? value : screenControl.getDefaultValue();
    }

    private void addEditText(ScreenControl screenControl, boolean multiLine) {
        addText(screenControl);
        EditText editText = getEditText();
        if (multiLine) {
            addMultiLineEditText(editText);
        }
        screenControl.setValueControl(editText);
        setEditTextListener(screenControl);
        editText.setText(getValue(screenControl));
    }

    private void addMultiLineEditText(EditText editText) {
        editText.setSingleLine(false);
        editText.setLines(4);
        editText.setMinLines(4);
        editText.setMaxLines(8);
        editText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 100 * editText.getMinLines()));
        editText.setScroller(new Scroller(this));
        editText.setVerticalScrollBarEnabled(true);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        editText.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
    }

    private EditText getEditText() {
        EditText editText = new EditText(this);
        editText.setId(View.generateViewId());
        return editText;
    }

    private void addSaveButton(ScreenControl screenControl) {
        Button btn = getButton(screenControl);
        screenControl.setValueControl(btn);
        setSaveButtonListener(btn);
    }

    private Button getButton(ScreenControl screenControl) {
        Button btn = new Button(this);
        btn.setText(screenControl.getTextLabel());
        btn.setId(View.generateViewId());
        return btn;
    }

    private void addCancelButton(ScreenControl screenControl) {
        Button btn = getButton(screenControl);
        screenControl.setValueControl(btn);
        setCancelButtonListener(btn);
    }

    private void addRadioButton(ScreenControl screenControl) {
        addText(screenControl);
        String[] options = screenControl.getOptionValues();
        List<String> values = getOptionValues(screenControl);
        RadioGroup rg = addRadioGroupControl(options, values);
        screenControl.setValueControl(rg);
        setRadioGroupChangeListner(screenControl);
    }

    private RadioGroup addRadioGroupControl(String options[], List<String> values){
        RadioGroup rg = new RadioGroup(this);
        rg.setOrientation(options.length <= 2 ? RadioGroup.HORIZONTAL : RadioGroup.VERTICAL);
        rg.setId(View.generateViewId());
        int selectedId = -1;
        for (String option : options) {
            RadioButton rb = new RadioButton(this);
            rb.setText(option);
            rb.setId(View.generateViewId());
            rg.addView(rb);
            if (values.contains(option)) {
                selectedId = rb.getId();
            }
        }
        if (selectedId != -1) {
            rg.check(selectedId);
        }
        return rg;
    }

    private List<String> getOptionValues(ScreenControl screenControl){
        String value = data.get(screenControl.getControlId());
        List<String> values;
        if(value != null && value.length() >0) {
            values = Arrays.asList(value.split("\\n"));
        } else{
            values = new ArrayList<>();
            value = screenControl.getDefaultValue();
            if( value != null){
                values.add(value);
            }
        }
        return values;
    }

    private void addCheckbox(ScreenControl screenControl) {
        addText(screenControl);
        String[] options = screenControl.getOptionValues();
        View[] optionControls = new View[options.length];
        List<String> values = getOptionValues(screenControl);
        for (int i = 0; i < options.length; i++) {
            CheckBox cb = new CheckBox(this);
            cb.setText(options[i]);
            cb.setId(View.generateViewId());
            optionControls[i] = cb;
            if (values.contains(options[i])) {
                cb.setChecked(true);
            }
        }
        screenControl.setOptionControls(optionControls);
        setCheckBoxChangeListner(screenControl);
    }

    private void addDropDownList(ScreenControl screenControl) {
        addText(screenControl);
        Spinner spin = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, screenControl.getOptionValues());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(adapter);
        screenControl.setValueControl(spin);
        setDropDownListListener(screenControl);
        String value = data.get(screenControl.getControlId());
        if (value != null) {
            int spinnerPosition = adapter.getPosition(value);
            spin.setSelection(spinnerPosition);
        }
    }

    private void addTimePicker(ScreenControl screenControl) {
        addEditText(screenControl, false);
        EditText valueControl = (EditText) screenControl.getValueControl();
        valueControl.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_dashboard_black_24dp, 0);
        setTimePicker(valueControl);
    }

    private void addDatePicker(ScreenControl screenControl) {
        addEditText(screenControl, false);
        EditText valueControl = (EditText) screenControl.getValueControl();
        valueControl.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_dashboard_black_24dp, 0);
        setDatePicker(valueControl);
    }

    private TextView getTextView(String label) {
        TextView textView = new TextView(this);
        textView.setText(label);
        textView.setId(View.generateViewId());
        return textView;
    }

    private void setDatePicker(EditText control) {
        control.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (control.getRight() - control.getCompoundDrawables()[Constants.DRAWABLE_RIGHT].getBounds().width())) {
                        DialogFragment datePickerFragment = new DatePickerFragment(control);
                        datePickerFragment.show(getSupportFragmentManager(), "datePicker");
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private void setTimePicker(EditText control) {
        control.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (control.getRight() - control.getCompoundDrawables()[Constants.DRAWABLE_RIGHT].getBounds().width())) {
                        DialogFragment timePickerFragment = new TimePickerFragment(control);
                        timePickerFragment.show(getSupportFragmentManager(), "timePicker");
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private void addPhotoControl(ScreenControl screenControl) {
        addText(screenControl);
        String options[] = {TAKE_PHOTO, SELECT_PHOTO};
        RadioGroup rg = addRadioGroupControl(options, Collections.EMPTY_LIST);
        RadioButton rbTakePhoto = (RadioButton) rg.getChildAt(0);
        rbTakePhoto.setButtonDrawable(R.drawable.ic_photo_camera_red_24dp);
        RadioButton rbSelectPhoto = (RadioButton) rg.getChildAt(1);
        rbSelectPhoto.setButtonDrawable(R.drawable.ic_photo_library_magenta_24dp);
        screenControl.setValueControl(rg);
        screenControl.setMediaControl(getImageView());
        setPhotoControlListener(rg, screenControl);
        String value = getValue(screenControl);
        if(value != null){
            downloadFile(screenControl.getControlId(), value, (ImageView)screenControl.getMediaControl());
        }
    }

    private ImageView getImageView(){
        ImageView imageview = new ImageView(this);
        LinearLayout.LayoutParams params = new LinearLayout
                .LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        imageview.setLayoutParams(params);
        imageview.setAdjustViewBounds(true);
        imageview.setScaleType(ImageView.ScaleType.FIT_XY);
        imageview.setId(View.generateViewId());
        return imageview;
    }

    private void setPhotoControlListener(RadioGroup radioGroup, ScreenControl screenControl) {
        radioGroup
            .setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    RadioButton selectedButton = (RadioButton) findViewById(checkedId);
                    currentScreenControl = screenControl;
                    if(selectedButton.getText() == TAKE_PHOTO) {
                        takePhoto();
                    }else {
                        selectPhoto();
                    }
                }
            });
    }

    private void takePhoto() {
        Log.d(CLASS_TAG, "TakePhoto");
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {

            File photoFile = createImageFile();
            if(photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.dshinde.fileprovider",
                        photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                currentScreenControl.setMediaUri(photoURI);
            }

            startActivityForResult(cameraIntent, Constants.TAKE_PHOTO);
        }
    }

    private File createImageFile() {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMG" + timeStamp + "_";

        try {
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
            );
            return image;
        } catch(IOException e){
            return null;
        }
    }

    private void selectPhoto() {
        Log.d(CLASS_TAG, "SelectPhoto");
        // Pick an image from storage
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, Constants.SELECT_PHOTO);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == Constants.TAKE_PHOTO  && resultCode == RESULT_OK && currentScreenControl.getMediaUri() != null) {
            //Bitmap image = BitmapFactory.decodeFile(currentPhotoUri);;

            //Bitmap image = (Bitmap) data.getExtras().get("data"); // small image
            //currentImageView.setImageBitmap(image);

            ImageView currentImageView = (ImageView) currentScreenControl.getMediaControl();
            currentImageView.setImageURI(currentScreenControl.getMediaUri());
        } else {
            if (requestCode == Constants.SELECT_PHOTO && resultCode == RESULT_OK && data != null && data.getData() != null) {
                Uri selectedFile = data.getData();
                try {
                    //Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                    //currentImageView.setImageBitmap(bitmap);
                    currentScreenControl.setMediaUri(selectedFile);
                    ImageView currentImageView = (ImageView) currentScreenControl.getMediaControl();
                    currentImageView.setImageURI(selectedFile);
                //} catch (IOException e) {
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setEditTextListener(ScreenControl screenControl) {
        EditText editText = (EditText) screenControl.getValueControl();
        editText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                data.put(screenControl.getControlId(), s.toString());
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
    }

    private void setSaveButtonListener(Button btn) {
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                closeActivityWithReturnValues();
            }
        });
    }

    private void setCancelButtonListener(Button btn) {
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                closeActivityWithoutReturnValues();
            }
        });
    }

    private void closeActivityWithReturnValues() {
        checkMediaAndUploadToFireStorage();
        Intent intent = new Intent();
        intent.putExtra("data", gson.toJson(data));
        if (requestMode == Constants.REQUEST_CODE_SCREEN_CAPTURE) {
            intent.putExtra("key", getIndexValues());
        }
        setResult(Constants.RESULT_CODE_OK, intent);
        finish();
    }

    private void checkMediaAndUploadToFireStorage(){
        for (ScreenControl screenControl : controls) {
            if(screenControl.getMediaControl() != null && screenControl.getMediaUri() != null){
                data.put(screenControl.getControlId(), StorageUtil.getFileName(this, screenControl.getMediaUri()));
                data.put(MediaFields.PHOTO_MEDIA, getCommaSeparated(data.get(MediaFields.PHOTO_MEDIA), screenControl.getControlId()));
                uploadFile(screenControl);
            }
        }
    }

    private String getCommaSeparated(String existingValue, String newValue){
        if(existingValue != null && !existingValue.isEmpty()){
            return existingValue + "," + newValue;
        }
        return newValue;
    }

    private String getIndexValues() {
        StringBuilder indexValues = new StringBuilder("");
        for (ScreenControl screenControl : controls) {
            if (YesNo.YES.getValue().equals(screenControl.getIndexField())) {
                String value = data.get(screenControl.getControlId());
                if (value != null) {
                    if(screenControl.getControlType() == ControlType.DatePicker) {
                        value = value.replaceAll("/", "-");
                    }
                    indexValues.append((indexValues.length() > 0 ? "," : "") + value);
                }
            }
        }
        if (indexValues.length() == 0) {
            indexValues.append(data.values().hashCode());
        }
        return indexValues.toString();
    }

    private void closeActivityWithoutReturnValues() {
        Intent intent = new Intent();
        setResult(Constants.RESULT_CODE_CANCEL, intent);
        finish();
    }

    private void setDropDownListListener(ScreenControl screenControl) {
        Spinner spinner = (Spinner) screenControl.getValueControl();
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                data.put(screenControl.getControlId(), spinner.getItemAtPosition(i).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                data.put(screenControl.getControlId(), "");
            }
        });
    }

    private void setRadioGroupChangeListner(ScreenControl screenControl) {
        RadioGroup radioGroup = (RadioGroup) screenControl.getValueControl();
        radioGroup
                .setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        RadioButton selectedButton = (RadioButton) findViewById(checkedId);
                        if(selectedButton != null) {
                            data.put(screenControl.getControlId(), selectedButton.getText().toString());
                        }
                    }
                });
    }

    private void setCheckBoxChangeListner(ScreenControl screenControl) {
        for (View view : screenControl.getOptionControls()) {
            CheckBox checkBox = (CheckBox) view;
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        getCheckBoxValues(screenControl);
                    }
                }
            );
        }
    }

    private void getCheckBoxValues(ScreenControl screenControl) {
        StringBuilder checked = new StringBuilder("");
        for (View view : screenControl.getOptionControls()) {
            CheckBox cb = (CheckBox) view;
            if (cb.isChecked()) {
                checked.append((checked.length() > 0 ? "\n" : "") + cb.getText().toString());
            }
        }
        data.put(screenControl.getControlId(), checked.toString());
    }

    private void parseConfig(String screenConfigJson) {
        Log.d(CLASS_TAG, "parseConfig");
        controls = new ArrayList<>();

        try {
            JSONArray arr = new JSONArray(screenConfigJson);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                ScreenControl item = gson.fromJson(obj.toString(), ScreenControl.class);
                if(item.getPositionId() == null){
                    item.setPositionId(String.format("%03d", i+1));
                } else{
                    item.setPositionId(String.format("%03d", Integer.valueOf(item.getPositionId())));
                }
                if (isMultiOptionControl(item.getControlType()) &&
                        item.getOptions() != null &&
                        item.getOptions().length() > 0) {
                    item.setOptionValues(item.getOptions().split("\\n"));
                }
                controls.add(item);
            }
            Collections.sort(controls, positionIdComparator);

        } catch (JSONException e) {
            System.out.print(e);
        }
    }

    Comparator<ScreenControl> positionIdComparator = new Comparator<ScreenControl>() {
        public int compare(ScreenControl m1, ScreenControl m2) {
            return m1.getPositionId().compareTo(m2.getPositionId());
        }
    };

    private boolean isMultiOptionControl(ControlType type) {
        return type == ControlType.CheckBox || type == ControlType.RadioButton || type == ControlType.DropDownList;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void galleryAddPhoto() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(currentScreenControl.getMediaUri());
        this.sendBroadcast(mediaScanIntent);
    }

    private void uploadFile(ScreenControl screenControl) {
        Uri filePath = screenControl.getMediaUri();
        //getting the storage reference
        try{
            storageFileRef = storageReference.child(Constants.STORAGE_PATH_NOTES + userId + "/" + noteId + "/" + screenControl.getControlId() + "/" + StorageUtil.getFileName(this, filePath));
            //adding the file to reference
            storageFileRef
                    .putFile(filePath).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return storageFileRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        Toast.makeText(getApplicationContext(), "upload successful!!!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "upload failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (Throwable throwable) {
            Toast.makeText(getApplicationContext(), "upload failed: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void downloadFile(String mediaFieldId, String mediaFieldValue, ImageView imageView) {
        //getting the storage reference
        try{
            storageFileRef = storageReference.child(Constants.STORAGE_PATH_NOTES + userId + "/" + noteId + "/" + mediaFieldId + "/" + mediaFieldValue);
            //adding the file to reference
            storageFileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(getApplicationContext()).load(uri).into(imageView);
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
