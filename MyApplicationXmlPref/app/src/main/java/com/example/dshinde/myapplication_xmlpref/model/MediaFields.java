package com.example.dshinde.myapplication_xmlpref.model;

import com.example.dshinde.myapplication_xmlpref.common.Constants;

import java.util.HashMap;
import java.util.Map;

public class MediaFields {

    public static String PHOTO_MEDIA="photoMediaFields";
    public static String AUDIO_MEDIA="audioMediaFields";
    public static String VIDEO_MEDIA="videoMediaFields";
    public static String DOCS_MEDIA="documentMediaFields";

    public String photoMediaFields;
    public String audioMediaFields;
    public String videoMediaFields;
    public String documentMediaFields;

    private String[] photoMedia;
    private String[] audioMedia;
    private String[] videoMedia;
    private String[] documentMedia;

    private Map<String, String> values = new HashMap<>();
    private int counterNextPhoto =0;
    private int counterPrevPhoto =0;
    private int counterNextDoc=0;
    private int counterPrevDoc=0;

    public MediaFields() {
    }

    public String getNextPhotoMediaField() {
        String media = null;
        if (counterNextPhoto >= 0 && counterNextPhoto < photoMedia.length) {
            counterPrevPhoto = counterNextPhoto - 1;
            media = photoMedia[counterNextPhoto++];
        }
        return media;
    }

    public String getPrevPhotoMediaField() {
        String media = null;
        if( counterPrevPhoto >= 0 ) {
            counterNextPhoto = counterPrevPhoto + 1;
            media = photoMedia[counterPrevPhoto--];
        }
        return media;
    }

    public String getNextDocumentMediaField() {
        String media = null;
        if (counterNextDoc >= 0 && counterNextDoc < documentMedia.length) {
            counterPrevDoc = counterNextDoc - 1;
            media = documentMedia[counterNextDoc++];
        }
        return media;
    }

    public String getPrevDocumentMediaField() {
        String media = null;
        if( counterPrevDoc >= 0 ) {
            counterNextDoc = counterPrevDoc + 1;
            media = documentMedia[counterPrevDoc--];
        }
        return media;
    }

    public String[] getPhotoMedia() {
        return photoMedia;
    }

    public String[] getAudioMedia() {
        return audioMedia;
    }

    public String[] getVideoMedia() {
        return videoMedia;
    }

    public String[] getDocumentMedia() {
        return documentMedia;
    }

    public void init(){
        setPhotoMedia();
        setDocumentMedia();
        setAudioMedia();
    }
    private void setPhotoMedia() {
        photoMedia = photoMediaFields != null ? photoMediaFields.split(",") : Constants.EMPTY_ARRAY;;
    }

    private void setAudioMedia() {
        this.audioMedia = audioMediaFields != null ? audioMediaFields.split(",") : Constants.EMPTY_ARRAY;
    }

    private void setVideoMedia() {
        this.videoMedia = videoMediaFields != null ? videoMediaFields.split(",") : Constants.EMPTY_ARRAY;
    }

    private void setDocumentMedia() {
        this.documentMedia = documentMediaFields != null ? documentMediaFields.split(",") : Constants.EMPTY_ARRAY;
    }

    public String getMediaFieldValue(String mediaField){
        return values.get(mediaField);
    }

    public void setValues(Map<String, String> values) {
        this.values = values;
    }

    public boolean hasMedia(){
        return photoMedia != null || audioMedia != null || videoMedia != null || documentMedia != null;
    }
}
