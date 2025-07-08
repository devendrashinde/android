package com.example.dshinde.myapplication_xmlpref.activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
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
import android.webkit.WebView;
import android.widget.AdapterView;
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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.dshinde.myapplication_xmlpref.R;
import com.example.dshinde.myapplication_xmlpref.common.Constants;
import com.example.dshinde.myapplication_xmlpref.common.ControlType;
import com.example.dshinde.myapplication_xmlpref.common.YesNo;
import com.example.dshinde.myapplication_xmlpref.helper.DynamicControls;
import com.example.dshinde.myapplication_xmlpref.helper.ExpressionSolver;
import com.example.dshinde.myapplication_xmlpref.helper.Factory;
import com.example.dshinde.myapplication_xmlpref.helper.StorageUtil;
import com.example.dshinde.myapplication_xmlpref.helper.Utils;
import com.example.dshinde.myapplication_xmlpref.listners.FireStorageListener;
import com.example.dshinde.myapplication_xmlpref.model.MediaFields;
import com.example.dshinde.myapplication_xmlpref.model.ScreenControl;
import com.example.dshinde.myapplication_xmlpref.pickers.DatePickerFragment;
import com.example.dshinde.myapplication_xmlpref.pickers.TimePickerFragment;
import com.example.dshinde.myapplication_xmlpref.services.FileStorage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DynamicLinearLayoutActivity extends AppCompatActivity {

    private static final String CLASS_TAG = "DynamicActivity";
    private String mediaStoragePath;
    private LinearLayout linearLayout;
    private List<ScreenControl> controls;
    private Map<String, String> data = new HashMap<>();
    private final Gson gson = new GsonBuilder().create();
    private Integer requestMode = null;
    private ScreenControl currentScreenControl;
    // Launcher for camera permission request
    private final ActivityResultLauncher<String> requestCameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted. Continue the action that requires camera.
                    Log.d(CLASS_TAG, "Camera permission granted");
                } else {
                    // Permission denied. Explain to the user why the feature is unavailable.
                    Log.w(CLASS_TAG, "Camera permission denied");
                    Toast.makeText(this, "Camera permission is required to take photos.", Toast.LENGTH_LONG).show();
                }
            });

    private ActivityResultLauncher<Intent> takePictureLauncher;
    private ActivityResultLauncher<String> filePickerLauncher;
    private ActivityResultLauncher<Intent> imageCropperActivityResultLauncher;
    private ActivityResultLauncher<Intent> selectMyNoteActivityResultLauncher;
    private FileStorage mediaStorage;

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(CLASS_TAG, "onCreate");
        setContentView(R.layout.activity_dynamic_linear_layout);
        linearLayout = findViewById(R.id.linear_layout);

        Bundle bundle = getIntent().getExtras();
        userId = bundle.getString(Constants.USERID);
        mediaStoragePath = Constants.STORAGE_PATH_NOTES +
                userId + "/" +
                bundle.getString(Constants.NOTE_ID);
        String screenConfig = bundle.getString(Constants.SCREEN_CONFIG);
        String screenData = bundle.getString(Constants.SCREEN_DATA);
        if (screenData != null && !screenData.isEmpty()) {
            data = gson.fromJson(screenData, Map.class);
        }
        requestMode = bundle.getInt(Constants.REQUEST_MODE, Constants.REQUEST_CODE_SCREEN_DESIGN);

        switch (requestMode){
            case Constants.REQUEST_CODE_SCREEN_DESIGN:
                setTitle(getResources().getString(R.string.screen_design_define_field));
                break;
            case Constants.REQUEST_CODE_SCREEN_CAPTURE:
                setTitle(getResources().getString(R.string.add_or_edit_record));
                break;
            default:
                setTitle(getResources().getString(R.string.screen_design_preview));
                break;
        }

        initialiseTakePhotoLauncher();
        initialiseFilePickerLauncher();
        renderUI(screenConfig);
        registerImageCropperActivityForResults();
        registerPicklistActivityForResults();

    }

    private void initialiseFilePickerLauncher() {
        // Initialize the ActivityResultLauncher
        // The contract is GetContent(), which takes a String (MIME type) as input
        // and returns a Uri as output.
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        // Handle the returned Uri here
                        if (uri != null) {
                            Log.d(CLASS_TAG, "Selected file URI: " + uri.toString());
                            EditText editText = (EditText) currentScreenControl.getValueControl();
                            currentScreenControl.setMediaUri(uri);
                            editText.setText(StorageUtil.getFileName(getApplicationContext(), uri));
                        } else {
                            Log.d(CLASS_TAG, "No file selected");
                        }
                    }
                });
    }

    private void initialiseTakePhotoLauncher() {
        // Initialize your takePictureLauncher (if not already done)
        // This example assumes you are using a FileProvider to get a content URI
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // Image captured and saved to photoURI
                        // Process the image: display it, save path, etc.
                        if (currentScreenControl.getMediaUri() != null) {
                            Log.d(CLASS_TAG, "Photo taken successfully: " + currentScreenControl.getMediaUri().toString());
                            cropPhoto(currentScreenControl.getMediaUri());
                        }
                    } else {
                        Log.d(CLASS_TAG, "Photo taking cancelled or failed.");
                    }
                });
    }

    // Method that triggers taking a photo (e.g., called from a button click)
    private void takePhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            // Permission is already granted
            dispatchTakePictureIntent();
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            // Explain to the user why you need the permission
            // Then request the permission
            // You can show a dialog here
            Toast.makeText(this, "Camera access is required to take photos.", Toast.LENGTH_LONG).show();
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        } else {
            // Directly request the permission
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = createImageFile();
            if(photoFile != null) {
                Uri photoURI = StorageUtil.getUriForFile(this, photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                currentScreenControl.setMediaUri(photoURI);
                takePictureLauncher.launch(takePictureIntent);
            }
        } else {
            Toast.makeText(this, "No camera app found.", Toast.LENGTH_SHORT).show();
        }
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.dynamic_screen, menu);
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
        new Handler(Looper.getMainLooper()).post(() -> {
            parseConfig(screenConfig);
            createControls();
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(10, 0, 10, 0);

            try {
                for (ScreenControl screenControl : controls) {
                    new Handler(Looper.getMainLooper()).post(() -> addControlsToUI(screenControl, layoutParams));
                }
            } catch (final Exception ex) {
                Log.i("---","Exception in thread");
            }
        });
        Log.d(CLASS_TAG, "exit(renderUI)");
    }

    private void addControlsToUI(ScreenControl screenControl, LinearLayout.LayoutParams layoutParams) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(CLASS_TAG, "enter(addControlsToUI: " + screenControl.getControlId());
                if (screenControl.getLabelControl() != null) {
                    linearLayout.addView(screenControl.getLabelControl(), layoutParams);
                }
                if (screenControl.getControlType() == ControlType.CheckBox) {
                    for (View view : screenControl.getOptionControls()) {
                        linearLayout.addView(view, layoutParams);
                    }
                } else if (screenControl.getValueControl() != null) {
                    linearLayout.addView(screenControl.getValueControl(), layoutParams);
                }
                if (screenControl.getMediaControl() != null) {
                    linearLayout.addView(screenControl.getMediaControl(), layoutParams);
                }
                Log.d(CLASS_TAG, "exit(addControlsToUI)");
            }
        });
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
                case EditNumber:
                    addEditNumber(screenControl);
                    break;
                case Expression:
                    addExpression(screenControl);
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
                case Document:
                    addDocumentControl(screenControl);
                    break;
                case Photo:
                    if(mediaStorage == null) {
                        mediaStorage = Factory.getFileStorageInstance(this, mediaStoragePath);
                    }
                    addPhotoControl(screenControl);
                    break;
                default:
                    break;
            }

            switch (screenControl.getControlType()){
                case EditText:
                case EditNumber:
                case MultiLineEditText:
                    if(screenControl.getNoteData() != null) {
                        addPicker((EditText) screenControl.getValueControl(), R.drawable.ic_dashboard_black_24dp);
                        setPicklistListener(screenControl);
                    }
                default:
                    break;
            }
        }
        Log.d(CLASS_TAG, "exit(createControls)");
    }

    private void addExpression(ScreenControl screenControl) {
        addText(screenControl);
        EditText editText = DynamicControls.getEditText(this);
        screenControl.setValueControl(editText);
        editText.setEnabled(false);
    }

    private String evaluateExpression(ScreenControl screenControl) {
        String[] expression = screenControl.getOptionValues();
        boolean numericExp = expression.length > 1;
        if(!numericExp) {
            expression = screenControl.getOptions().split(" ");
        }
        return ExpressionSolver.evaluateExpression(expression, data);
    }

    private void setExpressionValue() {
        /*
        find all expression fields and try to resolve and set value of respective expression control
         */
        for (ScreenControl screenControl : controls) {
            switch (screenControl.getControlType()) {
                case Expression:
                    EditText editText = (EditText) screenControl.getValueControl();
                    if(editText != null) {
                        editText.setText(evaluateExpression(screenControl));
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void addText(ScreenControl screenControl) {
        screenControl.setLabelControl(DynamicControls.getTextView(this, screenControl.getTextLabel()));
    }

    private String getValue(ScreenControl screenControl){
        String value = data.get(screenControl.getControlId());
        return value != null ? value : screenControl.getDefaultValue();
    }

    private void addEditText(ScreenControl screenControl, boolean multiLine) {
        addText(screenControl);
        EditText editText;
        if (multiLine) {
            editText = DynamicControls.getMultiLineEditText(this);
        } else {
            editText = DynamicControls.getEditText(this);
        }
        screenControl.setValueControl(editText);
        setEditTextListener(screenControl);
        editText.setText(getValue(screenControl));
    }

    private void addEditNumber(ScreenControl screenControl) {
        addEditText(screenControl, false);
        EditText editText = (EditText) screenControl.getValueControl();
        editText.setRawInputType(InputType.TYPE_CLASS_NUMBER);
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

    private void addRadioButton(ScreenControl screenControl) {
        addText(screenControl);
        String[] options = screenControl.getOptionValues();
        List<String> values = getOptionValues(screenControl);
        RadioGroup rg = DynamicControls.getRadioGroupControl(this, options, values);
        screenControl.setValueControl(rg);
        setRadioGroupChangeListener(screenControl);
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
        List<String> values = getOptionValues(screenControl);
        View[] optionControls = DynamicControls.getCheckbox(this, options, values);
        screenControl.setOptionControls(optionControls);
        setCheckBoxChangeListener(screenControl);
    }

    private void addDropDownList(ScreenControl screenControl) {
        addText(screenControl);
        String value = data.get(screenControl.getControlId());
        Spinner spin = DynamicControls.getDropDownList(this, screenControl.getOptionValues(), value);
        screenControl.setValueControl(spin);
        setDropDownListListener(screenControl);
    }

    private void addTimePicker(ScreenControl screenControl) {
        addEditText(screenControl, false);
        EditText valueControl = (EditText) screenControl.getValueControl();
        addPicker(valueControl, R.drawable.ic_dashboard_black_24dp);
        setTimePicker(valueControl);
    }

    private void addDatePicker(ScreenControl screenControl) {
        addEditText(screenControl, false);
        EditText valueControl = (EditText) screenControl.getValueControl();
        addPicker(valueControl, R.drawable.ic_dashboard_black_24dp);
        setDatePicker(valueControl);
    }

    private void addPicker(EditText valueControl, int p) {
        valueControl.setCompoundDrawablesWithIntrinsicBounds(0, 0, p, 0);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setDatePicker(EditText control) {
        control.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (control.getRight() - control.getCompoundDrawables()[Constants.DRAWABLE_RIGHT].getBounds().width())) {
                    DialogFragment datePickerFragment = new DatePickerFragment(control);
                    datePickerFragment.show(getSupportFragmentManager(), "datePicker");
                    return true;
                }
            }
            return false;
        });
    }

    private void setTimePicker(EditText control) {
        control.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
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
        String[] options = {getResources().getString(R.string.take_photo),
                getResources().getString(R.string.select_photo)};
        RadioGroup rg = DynamicControls.getRadioGroupControl(this, options, Collections.EMPTY_LIST);
        RadioButton rbTakePhoto = (RadioButton) rg.getChildAt(0);
        rbTakePhoto.setButtonDrawable(R.drawable.ic_photo_camera_red_24dp);
        RadioButton rbSelectPhoto = (RadioButton) rg.getChildAt(1);
        rbSelectPhoto.setButtonDrawable(R.drawable.ic_photo_library_magenta_24dp);
        screenControl.setValueControl(rg);
        screenControl.setMediaControl(DynamicControls.getPhotoView(this));
        setPhotoControlListener(rg, screenControl);
        String photoFileName = getValue(screenControl);
        if(photoFileName != null && !photoFileName.trim().isEmpty()){
            mediaStorage.downloadImageFile(photoFileName,
                    new PhotoListener((ImageView)screenControl.getMediaControl(), screenControl));
        }
    }

    private void addDocumentControl(ScreenControl screenControl) {
        addEditText(screenControl, false);
        EditText valueControl = (EditText) screenControl.getValueControl();
        addPicker(valueControl, R.drawable.ic_pdf_file_yellow_24dp);
        setDocumentSelector(screenControl);
        Button btn = DynamicControls.getButton(this, "Open");
        screenControl.setMediaControl(btn);
        setOpenDocumentButtonListener(screenControl);

        String documentFileName = getValue(screenControl);
        if(documentFileName != null && !documentFileName.trim().isEmpty()){
            valueControl.setText(documentFileName);
        }
    }

    private void setOpenDocumentButtonListener(ScreenControl screenControl) {
        Button btn = (Button) screenControl.getMediaControl();
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(screenControl.getMediaUri() == null) {
                    mediaStorage.downloadDocumentFile(getValue(screenControl), new PdfListener((screenControl)));
                } else {
                    startPdfViewActivity(screenControl.getMediaUri().toString());
                }
                }
            }
        );
    }

    private void startPdfViewActivity(String fileName) {
        Intent intent = new Intent(getApplicationContext(), PdfViewActivity.class);
        intent.putExtra("fileUri", fileName);
        startActivity(intent);
    }

    private void startWebViewActivity(String fileName) {
        Intent intent = new Intent(getApplicationContext(), WebViewActivity.class);
        intent.putExtra(Constants.PARAM_URL, fileName);
        startActivity(intent);
    }

    private void startActionViewIntent(String fileName){
        Uri webpage = Uri.parse(fileName);
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void setPicklistListener(ScreenControl screenControl) {
        EditText control = (EditText) screenControl.getValueControl();
        control.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (control.getRight() - control.getCompoundDrawables()[Constants.DRAWABLE_RIGHT].getBounds().width())) {
                        currentScreenControl = screenControl;
                        pickFromMyNote(screenControl.getOptions());
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private void setDocumentSelector(ScreenControl screenControl) {
        EditText control = (EditText) screenControl.getValueControl();
        control.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (control.getRight() - control.getCompoundDrawables()[Constants.DRAWABLE_RIGHT].getBounds().width())) {
                        currentScreenControl = screenControl;
                        selectDocument();
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private WebView getWebView(){
        WebView webView = new WebView(this);
        LinearLayout.LayoutParams params = new LinearLayout
                .LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        webView.setLayoutParams(params);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setLoadWithOverviewMode(false);
        webView.getSettings().setUseWideViewPort(true);
        webView.setId(View.generateViewId());
        return webView;
    }

    private void setPhotoControlListener(RadioGroup radioGroup, ScreenControl screenControl) {
        radioGroup
            .setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    RadioButton selectedButton = (RadioButton) findViewById(checkedId);
                    currentScreenControl = screenControl;
                    if(selectedButton.getText() == getResources().getString(R.string.take_photo)) {
                        takePhoto();
                    }else {
                        selectAndCropPhoto();
                    }
                }
            });
    }

    private File createImageFile() {
        return StorageUtil.createTempImageFile(this);
    }

    private void selectDocument() {
        Log.d(CLASS_TAG, "selectDocument");
        filePickerLauncher.launch(Constants.PDF_FILE);
    }

    private void cropPhoto(Uri photoUri) {
            Intent intent = new Intent(this, ImageCropperActivity.class);
            intent.putExtra(Constants.PARAM_URL, photoUri.toString());
            imageCropperActivityResultLauncher.launch(intent);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if(resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode){
            case Constants.TAKE_PHOTO:
                if (currentScreenControl.getMediaUri() != null) {
                    cropPhoto(currentScreenControl.getMediaUri());
                }
                break;
            case Constants.SELECT_IMAGE:
                if( data != null && data.getData() != null) {
                    Uri selectedFile = data.getData();
                    currentScreenControl.setMediaUri(selectedFile);
                    ImageView currentView = (ImageView) currentScreenControl.getMediaControl();
                    currentView.setImageURI(selectedFile);
                }
                break;
            case Constants.SELECT_DOCUMENT:
                if( data != null && data.getData() != null) {
                    Uri selectedFile = data.getData();
                    EditText editText = (EditText) currentScreenControl.getValueControl();
                    currentScreenControl.setMediaUri(selectedFile);
                    editText.setText(StorageUtil.getFileName(this, selectedFile));
                }
                break;
            case Constants.SELECT_MYNOTE:
                if( data != null) {
                    EditText editText = (EditText) currentScreenControl.getValueControl();
                    editText.setText(Objects.requireNonNull(data.getExtras()).getString("data"));
                }
                break;
            default:
                break;
        }
    }

    private void setEditTextListener(ScreenControl screenControl) {
        EditText editText = (EditText) screenControl.getValueControl();
        editText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                data.put(screenControl.getControlId(), s.toString());
                setExpressionValue();
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

    private Map<String, String> trimData() {
        Map<String, String> map = new LinkedHashMap<>();

        for (Map.Entry<String,String> entry : data.entrySet()) {
            String value = entry.getValue();
            if( value != null && !value.trim().isEmpty()) {
                map.put(entry.getKey(), value.trim());
            }
        }
        return map;
    }

    private void closeActivityWithReturnValues() {
        if(!CheckForRequiredValues()) {
            checkMediaAndUploadToStorage();
            Intent intent = new Intent();
            data = trimData();
            intent.putExtra("data", gson.toJson(data));
            if (requestMode == Constants.REQUEST_CODE_SCREEN_CAPTURE) {
                intent.putExtra("key", getIndexValues());
            }
            setResult(Constants.RESULT_CODE_OK, intent);
            finish();
        }
    }

    private void checkMediaAndUploadToStorage(){
        for (ScreenControl screenControl : controls) {
            if(screenControl.getMediaControl() != null && screenControl.getMediaUri() != null){
                data.put(screenControl.getControlId(), StorageUtil.getFileName(this, screenControl.getMediaUri()));
                switch (screenControl.getControlType()){
                    case Photo:
                        data.put(MediaFields.PHOTO_MEDIA, getCommaSeparated(data.get(MediaFields.PHOTO_MEDIA), screenControl.getControlId()));
                        break;
                    case Document:
                        data.put(MediaFields.DOCS_MEDIA, getCommaSeparated(data.get(MediaFields.DOCS_MEDIA), screenControl.getControlId()));
                        break;
                    default:
                        break;
                }
                uploadFile(screenControl);
            }
        }
    }

    private String getCommaSeparated(String existingValue, String newValue){
        if(existingValue != null && !existingValue.isEmpty() && !existingValue.contains(newValue)){
            return existingValue + "," + newValue;
        }
        return newValue;
    }

    private boolean CheckForRequiredValues() {
        StringBuilder indexValues = new StringBuilder();
        boolean requiredValueMissing=false;
        for (ScreenControl screenControl : controls) {
            if (YesNo.YES.getValue().equals(screenControl.getIndexField())) {
                String value = data.get(screenControl.getControlId());
                if (value == null || value.isEmpty()) {
                    switch (screenControl.getControlType()) {
                        case DatePicker:
                        case EditNumber:
                        case MultiLineEditText:
                        case DropDownList:
                        case EditText:
                        case TimePicker:
                            EditText ctl = (EditText) screenControl.getValueControl();
                            ctl.setError(screenControl.getTextLabel() + " value required");
                            break;
                        default:
                            Toast.makeText(this,
                                    screenControl.getTextLabel() + " value required",
                                    Toast.LENGTH_SHORT).show();
                            break;

                    }
                    requiredValueMissing=true;
                }
            }
        }
        return requiredValueMissing;
    }

    private String getIndexValues() {
        StringBuilder indexValues = new StringBuilder();
        for (ScreenControl screenControl : controls) {
            if (YesNo.YES.getValue().equals(screenControl.getIndexField())) {
                String value = data.get(screenControl.getControlId());
                if (value != null) {
                    if(screenControl.getControlType() == ControlType.DatePicker) {
                        value = value.replaceAll("/", "-");
                    } else{
                        value = value.replaceAll(",", " ");
                    }
                    value = value.replaceAll("\\s+"," ").trim();
                    indexValues.append(indexValues.length() > 0 ? "," : "").append(value);
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

    private void setRadioGroupChangeListener(ScreenControl screenControl) {
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

    private void setCheckBoxChangeListener(ScreenControl screenControl) {
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
        StringBuilder checked = new StringBuilder();
        for (View view : screenControl.getOptionControls()) {
            CheckBox cb = (CheckBox) view;
            if (cb.isChecked()) {
                checked.append(checked.length() > 0 ? "\n" : "")
                        .append(cb.getText().toString());
            }
        }
        data.put(screenControl.getControlId(), checked.toString());
    }

    private void parseConfig(String screenConfigJson) {
        controls = Utils.parseScreenConfig(screenConfigJson);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void uploadFile(ScreenControl screenControl) {
        Uri filePath = screenControl.getMediaUri();
        mediaStorage.uploadMedia(filePath);
    }

    private class PhotoListener implements FireStorageListener {
        final ImageView imageView;
        ScreenControl screenControl;
        public PhotoListener(ImageView imageView, ScreenControl screenControl){
            this.imageView = imageView;
            this.screenControl = screenControl;
        }

        @Override
        public void downloadUriReceived(Uri fileUri) {
            Glide.with(getApplicationContext()).load(fileUri).into(imageView);
            screenControl.setMediaUri(fileUri);
        }

        @Override
        public void downloadFileBytesReceived(byte[] bytes) {

        }

        @Override
        public void uploadedUriReceived(Uri fileUri) {

        }

    }

    private class PdfListener implements FireStorageListener {
        ScreenControl screenControl;
        public PdfListener(ScreenControl screenControl) {
            this.screenControl = screenControl;
        }

        @Override
        public void downloadUriReceived(Uri fileUri) {
            screenControl.setMediaUri(fileUri);
            startPdfViewActivity(fileUri.toString());
        }

        @Override
        public void downloadFileBytesReceived(byte[] bytes) {

        }

        @Override
        public void uploadedUriReceived(Uri fileUri) {

        }

    }

    private void selectAndCropPhoto() {
        Intent intent = new Intent(this, ImageCropperActivity.class);
        imageCropperActivityResultLauncher.launch(intent);
    }

    private void registerImageCropperActivityForResults() {
        imageCropperActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Constants.RESULT_CODE_OK) {
                            // There are no request codes
                            Intent data = result.getData();
                            assert data != null;
                            Uri imageUri = data.getData();
                            currentScreenControl.setMediaUri(imageUri);
                            ImageView currentView = (ImageView) currentScreenControl.getMediaControl();
                            currentView.setImageURI(imageUri);
                        }
                    }
                });
    }

    private void pickFromMyNote(String options) {
        Intent intent = new Intent(this, ImageCropperActivity.class);
        intent.putExtra(Constants.PARAM_FILENAME, options.replaceAll("\\n", "/"));
        intent.putExtra(Constants.USERID, userId);
        selectMyNoteActivityResultLauncher.launch(intent);
    }

    private void registerPicklistActivityForResults() {
        selectMyNoteActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Constants.RESULT_CODE_OK) {
                            Intent data = result.getData();
                            if( data != null) {
                                EditText editText = (EditText) currentScreenControl.getValueControl();
                                editText.setText(Objects.requireNonNull(data.getExtras()).getString("data"));
                            }
                        }
                    }
                });
    }

}
