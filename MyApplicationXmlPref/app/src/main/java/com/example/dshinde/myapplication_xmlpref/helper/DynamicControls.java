package com.example.dshinde.myapplication_xmlpref.helper;

import android.content.Context;
import android.text.InputType;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Scroller;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.chrisbanes.photoview.PhotoView;

import java.util.List;

public class DynamicControls {

    public static TextView getTextView(Context context, String label) {
        TextView textView = new TextView(context);
        textView.setText(label);
        textView.setId(View.generateViewId());
        return textView;
    }

    public static EditText getEditText(Context context, String text) {
        EditText editText = new EditText(context);
        editText.setId(View.generateViewId());
        editText.setText(text);
        return editText;
    }

    public static EditText getMultiLineEditText(Context context, String text) {
        EditText editText = new EditText(context);
        editText.setId(View.generateViewId());
        editText.setText(text);
        addMultiLineEditText(context, editText);
        return editText;
    }

    public static EditText getEditText(Context context) {
        return getEditText(context, "");
    }

    public static EditText getMultiLineEditText(Context context) {
        return getMultiLineEditText(context, "");
    }


    public static Button getButton(Context context, String label) {
        Button btn = new Button(context);
        btn.setText(label);
        btn.setId(View.generateViewId());
        return btn;
    }

    public static RadioGroup getRadioGroupControl(Context context, String options[], List<String> values){
        RadioGroup rg = new RadioGroup(context);
        rg.setOrientation(options.length <= 2 ? RadioGroup.HORIZONTAL : RadioGroup.VERTICAL);
        rg.setId(View.generateViewId());
        int selectedId = -1;
        for (String option : options) {
            RadioButton rb = new RadioButton(context);
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

    public static View[] getCheckbox(Context context, String[] options, List<String> values) {
        View[] optionControls = new View[options.length];
        for (int i = 0; i < options.length; i++) {
            CheckBox cb = new CheckBox(context);
            cb.setText(options[i]);
            cb.setId(View.generateViewId());
            optionControls[i] = cb;
            if (values.contains(options[i])) {
                cb.setChecked(true);
            }
        }
        return optionControls;
    }

    public static Spinner getDropDownList(Context context, String[] options, String value) {
        Spinner spinner = new Spinner(context);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        if (value != null) {
            int spinnerPosition = adapter.getPosition(value);
            spinner.setSelection(spinnerPosition);
        }
        return spinner;
    }

    public static PhotoView getPhotoView(Context context, LinearLayout.LayoutParams params){
        PhotoView imageView = new PhotoView(context);
        imageView.setLayoutParams(params);
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.setId(View.generateViewId());
        return imageView;
    }

    public static PhotoView getPhotoView(Context context){
        return getPhotoView(context, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
    }

    private static void addMultiLineEditText(Context context, EditText editText) {
        editText.setSingleLine(false);
        editText.setLines(4);
        editText.setMinLines(4);
        editText.setMaxLines(8);
        editText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 100 * editText.getMinLines()));
        editText.setScroller(new Scroller(context));
        editText.setVerticalScrollBarEnabled(true);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        editText.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
    }

}
