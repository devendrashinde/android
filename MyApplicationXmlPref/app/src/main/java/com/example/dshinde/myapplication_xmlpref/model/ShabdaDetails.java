package com.example.dshinde.myapplication_xmlpref.model;

import java.util.ArrayList;
import java.util.List;

public class ShabdaDetails {
    private String meaning;
    private List<ShabdaUsage> usage = new ArrayList<>();

    public ShabdaDetails(){
        this.meaning = "";
    }

    public List<ShabdaUsage> getUsage() {
        return usage;
    }

    public ShabdaUsage getUsage(String note) {
        for(ShabdaUsage ref : this.usage) {
            if(ref.getNote().equalsIgnoreCase(note)) return ref;
        }
        return new ShabdaUsage();
    }

    public void setUsage(List<ShabdaUsage> usage) {
        this.usage = usage;
    }

    public void addUsage(ShabdaUsage shabdaUsage) {
        for(ShabdaUsage ref : this.usage) {
            if(ref.getNote().equalsIgnoreCase(shabdaUsage.getNote())) return;
        }
        this.usage.add(shabdaUsage);
    }

    public String getMeaning() {
        return meaning;
    }
    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }
}
