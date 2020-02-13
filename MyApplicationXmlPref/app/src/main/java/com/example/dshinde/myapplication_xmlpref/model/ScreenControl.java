package com.example.dshinde.myapplication_xmlpref.model;

import android.view.View;

import com.example.dshinde.myapplication_xmlpref.common.ControlType;

public class ScreenControl {
    private int id;
    private String textLabel;
    private String controlId;
    private String indexField;
    private ControlType controlType;
    private String options; // \n separated values checkbox, radio buttons and dropdowns
    private transient String[] optionValues; // values parsed into array for checkbox, radio buttons, dropdowns
    private transient View labelControl;
    private transient View valueControl;
    private transient View[] optionControls;

    public ScreenControl(){
    }

    public String getIndexField() {
        return indexField;
    }

    public void setIndexField(String indexField) {
        this.indexField = indexField;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String[] getOptionValues() {
        return optionValues;
    }

    public void setOptionValues(String[] optionValues) {
        this.optionValues = optionValues;
    }

    public View getLabelControl() {
        return labelControl;
    }
    public void setLabelControl(View labelControl) {
        this.labelControl = labelControl;
    }

    public View getValueControl() {
        return valueControl;
    }

    public void setValueControl(View valueControl) {
        this.valueControl = valueControl;
    }

    public String getTextLabel() {
        return textLabel;
    }
    public void setTextLabel(String textLabel) {
        this.textLabel = textLabel;
    }

    public ControlType getControlType() {
        return controlType;
    }
    public void setControlType(ControlType controlType) {
        this.controlType = controlType;
    }

    public View[] getOptionControls() {
        return optionControls;
    }

    public void setOptionControls(View[] optionControls) {
        this.optionControls = optionControls;
    }

    public String getControlId() {
        return controlId;
    }

    public void setControlId(String controlId) {
        this.controlId = controlId;
    }
}
