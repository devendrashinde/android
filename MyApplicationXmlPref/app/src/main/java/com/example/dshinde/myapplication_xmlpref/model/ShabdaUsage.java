package com.example.dshinde.myapplication_xmlpref.model;

import com.example.dshinde.myapplication_xmlpref.common.ControlType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShabdaUsage {
    private String note;
    private List<String> reference = new ArrayList<>();

    public ShabdaUsage(){
    }

    public List<String> getReference() {
        return reference;
    }

    public void setReference(List<String> reference) {
        this.reference = reference;
    }

    public void addReference(String reference) {
        for(String ref : this.reference) {
            if(ref.equalsIgnoreCase(reference)) return;
        }
        this.reference.add(reference);
    }

    public String getNote() {
        return note;
    }
    public void setNote(String note) {
        this.note = note;
    }
}
