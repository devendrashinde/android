package com.example.dshinde.myapplication_xmlpref.model;

import android.net.Uri;
import android.view.View;

import com.example.dshinde.myapplication_xmlpref.common.ControlType;

public class ScreenControl {
    private int id;
    private String textLabel;
    private String controlId;
    private String indexField;
    private String positionId;
    private String defaultValue;
    private ControlType controlType;
    private String noteData; // \n separated values checkbox, radio buttons and dropdowns
    private String options; // \n separated values checkbox, radio buttons and dropdowns
    private transient String[] optionValues; // values parsed into array for checkbox, radio buttons, dropdowns
    private transient View labelControl;
    private transient View valueControl;
    private transient View mediaControl;
    private transient View[] optionControls;
    private transient Uri mediaUri;

    public ScreenControl(){
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getPositionId() {
        return positionId;
    }

    public void setPositionId(String positionId) {
        this.positionId = positionId;
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

    public String getNoteData() {
        return noteData;
    }

    public void setNoteData(String noteData) {
        this.noteData= noteData;
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

    public View getMediaControl() {
        return mediaControl;
    }
    public void setMediaControl(View mediaControl) {
        this.mediaControl = mediaControl;
    }

    public Uri getMediaUri() {
        return mediaUri;
    }
    public void setMediaUri(Uri mediaUri) {
        this.mediaUri = mediaUri;
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
