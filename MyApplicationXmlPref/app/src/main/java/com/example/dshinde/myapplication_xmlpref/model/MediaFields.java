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
    private int counterNext=0;
    private int counterPrev=0;

    public MediaFields() {
    }

    public String getNextPhotoMediaField() {
        String media = null;
        if (counterNext >= 0 && counterNext < photoMedia.length) {
            counterPrev = counterNext - 1;
            media = photoMedia[counterNext++];
        }
        return media;
    }

    public String getPrevPhotoMediaField() {
        String media = null;
        if( counterPrev >= 0 ) {
            counterNext = counterPrev + 1;
            media = photoMedia[counterPrev--];
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
