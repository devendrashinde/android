package com.example.dshinde.myapplication_xmlpref.helper;

import static android.speech.tts.TextToSpeech.LANG_MISSING_DATA;
import static android.speech.tts.TextToSpeech.LANG_NOT_SUPPORTED;

import android.content.Context;
import android.os.DeadObjectException;
import android.speech.tts.TextToSpeech;
import android.text.Spanned;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.dshinde.myapplication_xmlpref.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.noties.markwon.Markwon;

public class ReadAloud {
    private static final int MAX_TTS_CHARS = 3000;

    private final Context context;
    private final Markwon markwon;

    private final Object ttsLock = new Object();
    private TextToSpeech textToSpeech;
    private volatile boolean isTtsReady = false;
    private volatile boolean isInitializing = false;

    public ReadAloud(@NonNull Context context) {
        this(context, Markwon.create(context.getApplicationContext()));
    }

    public ReadAloud(@NonNull Context context, @NonNull Markwon markwon) {
        this.context = context.getApplicationContext();
        this.markwon = markwon;
        initTts();
    }

    private void initTts() {
        synchronized (ttsLock) {
            if (isInitializing) return;
            isInitializing = true;
            shutdownLocked();
            textToSpeech = new TextToSpeech(context, status -> {
                synchronized (ttsLock) {
                    isTtsReady = (status == TextToSpeech.SUCCESS);
                    isInitializing = false;
                }
            });
        }
    }

    public void readNoteText(@NonNull String language, @Nullable String key, @Nullable String value) {
        TextToSpeech ttsLocal;
        synchronized (ttsLock) {
            if (textToSpeech == null) {
                initTts();
                return;
            }
            if (!isTtsReady) return;
            ttsLocal = textToSpeech;
        }

        try {
            Locale preferred = LanguageHelper.getLanguage(language);
            if (preferred == null) preferred = Locale.UK;

            int setResult = ttsLocal.setLanguage(preferred);
            Locale usedLocale = preferred;

            if (setResult == LANG_MISSING_DATA || setResult == LANG_NOT_SUPPORTED) {
                usedLocale = Locale.UK;
                int fallback = ttsLocal.setLanguage(usedLocale);
                if (fallback == LANG_MISSING_DATA || fallback == LANG_NOT_SUPPORTED) return;
            }

            String plainValue = markdownToPlainText(value);
            String fullText = ((key != null && !key.isEmpty()) ? key + ", " : "") + plainValue;
            if (fullText.trim().isEmpty()) return;

            Toast.makeText(
                    context,
                    context.getString(R.string.reading_text_in) + " " + usedLocale.getDisplayLanguage(),
                    Toast.LENGTH_SHORT).show();

            List<String> chunks = splitForTts(fullText, MAX_TTS_CHARS);
            String baseId = (key == null || key.isEmpty()) ? "read_aloud" : key;

            for (int i = 0; i < chunks.size(); i++) {
                int mode = (i == 0) ? TextToSpeech.QUEUE_FLUSH : TextToSpeech.QUEUE_ADD;
                int result = ttsLocal.speak(chunks.get(i), mode, null, baseId + "_" + i);
                if (result == TextToSpeech.ERROR) break;
            }
        } catch (RuntimeException e) {
            if (isDeadObject(e)) {
                initTts(); // recover only, no immediate recursive retry
            }
        }
    }

    private boolean isDeadObject(@NonNull Throwable t) {
        if (t instanceof DeadObjectException) return true;
        Throwable c = t.getCause();
        while (c != null) {
            if (c instanceof DeadObjectException) return true;
            c = c.getCause();
        }
        return false;
    }

    private String markdownToPlainText(@Nullable String markdown) {
        if (markdown == null || markdown.isEmpty()) return "";
        Spanned spanned = markwon.toMarkdown(markdown);
        return spanned == null ? "" : spanned.toString();
    }

    private List<String> splitForTts(@NonNull String text, int maxChars) {
        List<String> out = new ArrayList<>();
        int start = 0;
        int len = text.length();

        while (start < len) {
            int end = Math.min(start + maxChars, len);
            if (end < len) {
                int lastSpace = text.lastIndexOf(' ', end);
                if (lastSpace > start + (maxChars / 2)) end = lastSpace;
            }
            String chunk = text.substring(start, end).trim();
            if (!chunk.isEmpty()) out.add(chunk);
            start = end;
            while (start < len && text.charAt(start) == ' ') start++;
        }
        return out;
    }

    public void clearTTS() {
        synchronized (ttsLock) {
            shutdownLocked();
        }
    }

    private void shutdownLocked() {
        isTtsReady = false;
        isInitializing = false;
        if (textToSpeech != null) {
            try {
                textToSpeech.stop();
                textToSpeech.shutdown();
            } catch (RuntimeException ignored) {
            }
            textToSpeech = null;
        }
    }
}