package com.example.dshinde.myapplication_xmlpref;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Scroller;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dshinde.myapplication_xmlpref.common.Constants;
import com.example.dshinde.myapplication_xmlpref.common.ControlType;
import com.example.dshinde.myapplication_xmlpref.model.ScreenControl;
import com.example.dshinde.myapplication_xmlpref.pickers.DatePickerFragment;
import com.example.dshinde.myapplication_xmlpref.pickers.TimePickerFragment;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DynamicLinearLayoutActivity extends AppCompatActivity {

    LinearLayout linearLayout;
    List<ScreenControl> controls;
    Map<String, String> data = new HashMap<>();
    Gson gson = new GsonBuilder().create();
    int idCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamic_linear_layout);
        Bundle bundle = getIntent().getExtras();
        String screenConfig = bundle.getString("screenConfig");
        String screenData = bundle.getString("screenData");
        if(screenData != null && screenData.length() > 0) {
            data = gson.fromJson(screenData, Map.class);
        }
        parseControls(screenConfig);
        linearLayout = findViewById(R.id.linear_layout);
        addControls();
        setTitle("Add/Edit Screen Control");
    }

    private void addControls() {
        for(ScreenControl screenControl : controls){
            switch (screenControl.getControlType()){
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
                case SaveButton:
                    addSaveButton(screenControl);
                    break;
                case CancelButton:
                    addCancelButton(screenControl);
                    break;
                default:
                    break;
            }
        }
    }

    private void addText(ScreenControl screenControl) {
        screenControl.setLabelControl(getTextView(screenControl.getTextLabel()));
        linearLayout.addView(screenControl.getLabelControl());
    }

    private void addEditText(ScreenControl screenControl, boolean multiLine) {
        addText(screenControl);
        EditText editText = getEditText();
        if(multiLine) {
            addMultiLineEditText(editText);
        }
        screenControl.setValueControl(editText);
        linearLayout.addView(editText);
        setEditTextListener(screenControl);
        editText.setText(data.get(screenControl.getControlId()));
    }

    private void addMultiLineEditText(EditText editText) {
        editText.setSingleLine(false);
        editText.setLines(4);
        editText.setMinLines(4);
        editText.setMaxLines(8);
        editText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 100 * editText.getMinLines()));
        editText.setScroller(new Scroller(getApplicationContext()));
        editText.setVerticalScrollBarEnabled(true);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        editText.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
    }

    private EditText getEditText() {
        EditText editText = new EditText(getApplicationContext());
        editText.setId(View.generateViewId());
        return editText;
    }

    private void addSaveButton(ScreenControl screenControl){
        Button btn = new Button(this);
        btn.setText(screenControl.getTextLabel());
        btn.setId(View.generateViewId());
        screenControl.setValueControl(btn);
        setSaveButtonListener(btn);
        linearLayout.addView(btn);
    }

    private void addCancelButton(ScreenControl screenControl){
        Button btn = new Button(this);
        btn.setText(screenControl.getTextLabel());
        btn.setId(View.generateViewId());
        screenControl.setValueControl(btn);
        setCancelButtonListener(btn);
        linearLayout.addView(btn);
    }

    private void addRadioButton(ScreenControl screenControl) {
        addText(screenControl);
        String[] options = screenControl.getOptionValues();
        RadioGroup rg = new RadioGroup(getApplicationContext());
        rg.setOrientation(RadioGroup.HORIZONTAL);//or RadioGroup.VERTICAL
        rg.setId(View.generateViewId());
        String value = data.get(screenControl.getControlId());
        int selectedId = -1;
        for(int i=0; i<options.length; i++){
            RadioButton rb = new RadioButton(getApplicationContext());
            rb.setText(options[i]);
            rb.setId(View.generateViewId());
            rg.addView(rb);
            if(value != null && options[i].equals(value)){
                selectedId = rb.getId();
            }
        }
        linearLayout.addView(rg);
        screenControl.setValueControl(rg);
        setRadioGroupChangeListner(screenControl);
        if(selectedId != -1) rg.check(selectedId);
    }

    private void addCheckbox(ScreenControl screenControl) {
        addText(screenControl);
        String[] options = screenControl.getOptionValues();
        View[] optionControls = new View[options.length];
        String value = data.get(screenControl.getControlId());
        for(int i=0; i<options.length; i++){
            CheckBox cb = new CheckBox(getApplicationContext());
            cb.setText(options[i]);
            cb.setId(View.generateViewId());
            linearLayout.addView(cb);
            optionControls[i] = cb;
            if(value != null && options[i].equals(value)){
                cb.setChecked(true);
            }
        }
        screenControl.setOptionControls(optionControls);
        setCheckBoxChangeListner(screenControl);
    }

    private void addDropDownList(ScreenControl screenControl){
        addText(screenControl);
        Spinner spin = new Spinner(getApplicationContext());
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, screenControl.getOptionValues());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(adapter);
        linearLayout.addView(spin);
        screenControl.setValueControl(spin);
        setDropDownListListener(screenControl);
        String value = data.get(screenControl.getControlId());
        if (value != null) {
            int spinnerPosition = adapter.getPosition(value);
            spin.setSelection(spinnerPosition);
        }
    }

    private void addTimePicker(ScreenControl screenControl){
        addEditText(screenControl, false);
        EditText valueControl = (EditText) screenControl.getValueControl();
        valueControl.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_dashboard_black_24dp, 0);
        setTimePicker(valueControl);
    }

    private void addDatePicker(ScreenControl screenControl){
        addEditText(screenControl, false);
        EditText valueControl = (EditText) screenControl.getValueControl();
        valueControl.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_dashboard_black_24dp, 0);
        setDatePicker(valueControl);
    }

    private TextView getTextView(String label){
        TextView textView = new TextView(getApplicationContext());
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

    private void setEditTextListener(ScreenControl screenControl){
        EditText editText =(EditText) screenControl.getValueControl();
        editText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                data.put(screenControl.getControlId(), s.toString());
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }

    private void setSaveButtonListener(Button btn){
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                closeActivityWithReturnValues();
            }
        });
    }

    private void setCancelButtonListener(Button btn){
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                closeActivityWithoutReturnValues();
            }
        });
    }

    private void closeActivityWithReturnValues() {
        Intent intent = new Intent();
        intent.putExtra("data", gson.toJson(data));
        setResult(Constants.RESULT_CODE_OK, intent);
        Toast.makeText(this, "You have entered\n" + data.toString(),
                Toast.LENGTH_LONG).show();
        finish();
    }

    private void closeActivityWithoutReturnValues() {
        Intent intent = new Intent();
        setResult(Constants.RESULT_CODE_CANCEL, intent);
        finish();
    }


    private void setDropDownListListener(ScreenControl screenControl){
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
                        data.put(screenControl.getControlId(), selectedButton.getText().toString());
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

    private void parseControls(String screenConfigJson){
        controls = new ArrayList<>();

        try {
            JSONArray arr = new JSONArray(screenConfigJson);
            for(int i=0;i<arr.length();i++){
                JSONObject obj= arr.getJSONObject(i);
                ScreenControl item = gson.fromJson(obj.toString(), ScreenControl.class);
                if(isMultiOptionControl(item.getControlType()) &&
                        item.getOptions() != null &&
                        item.getOptions().length() > 0) {
                    item.setOptionValues(item.getOptions().split("\\n"));
                }
                controls.add(item);
            }

        } catch (JSONException e) {
            System.out.print(e);
        }
    }

    private boolean isMultiOptionControl(ControlType type) {
        return type == ControlType.CheckBox || type == ControlType.RadioButton || type == ControlType.DropDownList;
    }

}