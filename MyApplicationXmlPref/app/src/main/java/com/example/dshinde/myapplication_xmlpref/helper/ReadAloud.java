package com.example.dshinde.myapplication_xmlpref.helper;

import static android.speech.tts.TextToSpeech.LANG_MISSING_DATA;
import static android.speech.tts.TextToSpeech.LANG_NOT_SUPPORTED;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.dshinde.myapplication_xmlpref.R;

import java.util.Locale;

public class ReadAloud {
    Context context;
    protected TextToSpeech textToSpeech;

    public ReadAloud(Context context) {
        this.context = context;
        if(textToSpeech == null) {
            textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status != TextToSpeech.ERROR) {
                        // tts engine is connected
                    }
                }
            });
        }
    }

    public void readNoteText(@NonNull String language, String key, String value) {
        if(textToSpeech != null) {
            Locale locale = LanguageHelper.getLanguage(language);
            int langAvailable = textToSpeech.isLanguageAvailable(locale);
            if( langAvailable ==  LANG_MISSING_DATA && langAvailable == LANG_NOT_SUPPORTED) {
                locale = Locale.UK;
            }
            textToSpeech.setLanguage(locale);
            Toast.makeText(context, new StringBuilder()
                    .append(context.getResources()
                            .getString(R.string.reading_text_in))
                    .append(" ")
                    .append(locale.getDisplayLanguage()).toString(),
                    Toast.LENGTH_SHORT).show();
            final String text = (key != null ? key + ", " : "") + value;
            if (!textToSpeech.isSpeaking()) {
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, key);
            } else {
                textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null, key);
            }
        }
    }

    public void clearTTS() {
        if(textToSpeech !=null){
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

}
