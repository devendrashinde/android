package com.example.dshinde.myapplication_xmlpref.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.dshinde.myapplication_xmlpref.R;
import com.example.dshinde.myapplication_xmlpref.common.Constants;
import com.example.dshinde.myapplication_xmlpref.helper.Factory;
import com.example.dshinde.myapplication_xmlpref.helper.StorageUtil;
import com.example.dshinde.myapplication_xmlpref.listners.DataStorageListener;
import com.example.dshinde.myapplication_xmlpref.listners.FireStorageListener;
import com.example.dshinde.myapplication_xmlpref.listners.OnSwipeTouchListener;
import com.example.dshinde.myapplication_xmlpref.model.KeyValue;
import com.example.dshinde.myapplication_xmlpref.services.DataStorage;
import com.example.dshinde.myapplication_xmlpref.services.FileStorage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class AudioVideoActivity extends BaseActivity {
    public static final String MEDIA_URI = "mediaUri";
    public static final String MEDIA_NAME = "mediaName";
    static final int PLAYBACK_MODE_SELECT = 1;
    static final int PLAYBACK_MODE_PLAY = 2;
    private static final String CLASS_TAG = "AudioVideoActivity";
    EditText editTextFileUri;
    ImageButton buttonPlay;
    ImageButton buttonPrevious;
    ImageButton buttonNext;
    TextView textViewNote;
    DataStorage dataStorageManager;
    FileStorage mediaStorage;
    private String mediaStoragePath;
    String collectionName = null;
    String key;
    String noteText;
    Uri mediaUri;
    MediaPlayer mp = new MediaPlayer();
    Gson gson = new GsonBuilder().create();
    SeekBar seekBar;

    public final Handler mHandler = new Handler(Looper.getMainLooper(), msg -> {
        seekBar.setProgress(msg.arg1);
        return true;
    });

    Timer timer;
    int mode, currentNote, totalNotes;
    List<KeyValue> notes;
    List<KeyValue> audioNotes;
    boolean playingNoteSubject;
    private ActivityResultLauncher<String> filePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.audiovideo_activity_layout);
        timer = new Timer();

        // get parameters
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            userId = bundle.getString(Constants.USERID);
            collectionName = bundle.getString("note");
            mediaStoragePath = Constants.STORAGE_PATH_NOTES +
                    userId + "/" +
                    collectionName;
            key = bundle.getString("key");
            if (key != null && !key.isEmpty()) {
                mode = PLAYBACK_MODE_SELECT;
                if(collectionName.equals(key)) {
                    playingNoteSubject = true;
                    getNoteItems();
                    initialiseNoteItemToDisplay();
                } else {
                    noteText = bundle.getString("value");
                }
            } else {
                mode = PLAYBACK_MODE_PLAY;
                getNoteItems();
                initialiseNoteItemToDisplay();
            }
            setTitleDescription();
            loadUI();
            initialiseFilePickerLauncher();
            initDataStorageAndLoadData(this);
        }
    }

    private void initialiseNoteItemToDisplay() {
        if (totalNotes > 0) {
            currentNote = 0;
            if(!playingNoteSubject) {
                key = notes.get(currentNote).getKey();
            }
            noteText = notes.get(currentNote).getValue();
        }
    }

    private void getNoteItems() {
        notes = (List<KeyValue>) getIntent().getSerializableExtra("data");
        totalNotes = notes.size();
    }

    private void setTitleDescription() {
        setTitle((!playingNoteSubject ? key + " - " : "") + collectionName);
    }

    private void parseAndDisplayText(String text) {
        final String finalText = (text != null && !text.isEmpty()) ? text : "";
        runOnUiThread(() -> textViewNote.setText(finalText));
    }

    private void setPlayButtonListener() {
        buttonPlay.setOnClickListener(v -> playOrPause());
    }

    private void setNextButtonListener() {
        buttonNext.setOnClickListener(v -> next());
    }

    private void setPreviousButtonListener() {
        buttonPrevious.setOnClickListener(v -> previous());
    }

    private void next() {
        if (currentNote + 1 < totalNotes) {
            loadTextOrAudioNote(notes.get(++currentNote), false);
            if (!playingNoteSubject) loadAudioNote();
        }
    }

    private void previous() {
        if (currentNote - 1 >= 0) {
            loadTextOrAudioNote(notes.get(--currentNote),false);
            if (!playingNoteSubject) loadAudioNote();
        }
    }

    private void playOrPause() {
        if(mediaUri == null){
            return;
        }
        if (mp.isPlaying()) {
            mp.pause();
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            if (mp.getDuration() == mp.getCurrentPosition()) {
                seekBar.setProgress(0);
            }
            mp.start();
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            timer.schedule(new ProgressUpdate(), 0, 1000); // using handler/timer task
        }
        setPlayPauseButton();
    }

    private void loadUI() {
        editTextFileUri = findViewById(R.id.fileUri);
        buttonPlay = findViewById(R.id.buttonPlay);
        setPlayButtonListener();
        buttonNext = findViewById(R.id.buttonNext);
        buttonPrevious = findViewById(R.id.buttonPrevious);
        if (mode == PLAYBACK_MODE_PLAY || playingNoteSubject) {
            setNextButtonListener();
            setPreviousButtonListener();
        } else {
            buttonNext.setVisibility(View.GONE);
            buttonPrevious.setVisibility(View.GONE);
        }
        textViewNote = findViewById(R.id.textNote);
        textViewNote.setMovementMethod(new ScrollingMovementMethod());
        seekBar = findViewById(R.id.seekbar);
        parseAndDisplayText(noteText);
        setFileSelectorListener();
        setSeekBarListener();
        setSwipeListener();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setSwipeListener() {
        if(mode == PLAYBACK_MODE_SELECT) {
            textViewNote.setOnTouchListener(new OnSwipeTouchListener(this) {
                @Override
                public void onSwipeLeft() {
                    super.onSwipeLeft();
                    next();
                }

                @Override
                public void onSwipeRight() {
                    super.onSwipeRight();
                    previous();
                }
            });
        }
    }

    private void setSeekBarListener() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                if (fromTouch && mp != null) {
                    mp.seekTo(progress);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setFileSelectorListener() {
        editTextFileUri.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (editTextFileUri.getRight() - editTextFileUri.getCompoundDrawables()[Constants.DRAWABLE_RIGHT].getBounds().width())) {
                    selectAudioFile();
                    return true;
                }
            }
            return false;
        });
    }

    private void initDataStorageAndLoadData(Context context) {
        Log.d(CLASS_TAG, "initDataStorageAndLoadData->getDataStorageInstance");
        dataStorageManager = Factory.getDataStorageInstance(context,
                getDataStorageType(),
                Constants.MEDIA_NOTE_PREFIX + collectionName +
                        (mode == PLAYBACK_MODE_SELECT ? "/" + key : ""),
                false, false, new DataStorageListener() {
                    @Override
                    public void dataChanged(String key, String value) {
                        Log.d(CLASS_TAG, "dataChanged key: " + key + ", value: " + value);
                        showInShortToast(getResources().getString(R.string.saved));
                    }

                    @Override
                    public void dataLoaded(List<KeyValue> data) {
                        Log.d(CLASS_TAG, "dataLoaded");
                        audioNotes = data;
                        loadAudioNote();
                    }
                });
        dataStorageManager.loadData();
    }

    private void loadAudioNote() {
        KeyValue keyValue = audioNotes.stream().filter(kv -> kv.getKey().equalsIgnoreCase(key)).findFirst().orElse(null);
        loadTextOrAudioNote(keyValue, true);
    }

    private void loadTextOrAudioNote(KeyValue keyValue, boolean audioNote) {
        if (keyValue != null) {
            if(audioNote) {
                if(keyValue.getValue() != null) {
                    Map<String, String> value = gson.fromJson(keyValue.getValue(), Map.class);
                    editTextFileUri.setText(value.get(MEDIA_NAME));
                    mediaUri = Uri.parse(value.get(MEDIA_URI));
                    getMediaUri(value.get(MEDIA_NAME));
                }
            } else {
                key = keyValue.getKey();
                noteText = keyValue.getValue();
                setTitleDescription();
                parseAndDisplayText(noteText);
            }
        } else if(audioNote){
            if(mp != null && mp.isPlaying()) {
                mp.stop();
                mp.reset();
            }
            setPlayPauseButton();
            seekBar.setProgress(0);
            mediaUri = null;
            editTextFileUri.setText(R.string.no_audio_note);
        }
    }

    private void getMediaUri(String mediaName) {
        /*
        if(mediaUri == null) {
            setMediaPlayerSource();
            return;
        }
         */
        if(mediaStorage == null) {
            mediaStorage = Factory.getFileStorageInstance(this, mediaStoragePath, new FireStorageListener() {
                @Override
                public void downloadUriReceived(Uri fileUri) {
                    mediaUri = fileUri;
                    setMediaPlayerSource();
                }

                @Override
                public void downloadFileBytesReceived(byte[] bytes) {

                }

                @Override
                public void uploadedUriReceived(Uri fileUri) {

                }
            });
        }
        mediaStorage.downloadFile(mediaName);
    }

    private void save() {
        String dbKey = (playingNoteSubject ? collectionName : key);
        if (dbKey != null && mediaUri != null) {
            Map<String, String> data = new HashMap<>();
            data.put(MEDIA_URI, mediaUri.toString());
            data.put(MEDIA_NAME, editTextFileUri.getText().toString());
            dataStorageManager.save((playingNoteSubject ? collectionName : dbKey), gson.toJson(data));
            mediaStorage.uploadMedia(mediaUri);
        }
    }

    private void remove() {
        String dbKey = (playingNoteSubject ? collectionName : key);
        if (dbKey != null) {
            dataStorageManager.remove(dbKey);
        }
    }

    private void setMediaPlayerSource() {
        if (mediaUri == null) return;
        try {
            if (mp == null) {
                mp = new MediaPlayer();
            }
            mp.reset();
            ParcelFileDescriptor parcelFileDescriptor =
                    getContentResolver().openFileDescriptor(mediaUri, "r");
            mp.setAudioAttributes(new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA).build());
            mp.setDataSource(parcelFileDescriptor.getFileDescriptor());
            seekBar.setProgress(0);
            mp.setOnPreparedListener(mediaPlayer -> {
                seekBar.setMax(mp.getDuration());
                playOrPause();
            });
            mp.setOnCompletionListener(mediaPlayer -> {
                setPlayPauseButton();
                seekBar.setProgress(mp.getDuration());
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                if(mode == PLAYBACK_MODE_PLAY) {
                    next();
                }
            });
            mp.prepareAsync();
            parcelFileDescriptor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setPlayPauseButton() {
        if (mp.isPlaying()) {
            buttonPlay.setImageResource(R.drawable.ic_action_pause);
        } else {
            buttonPlay.setImageResource(R.drawable.ic_action_play);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(CLASS_TAG, "onStop");
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        dataStorageManager.removeDataStorageListeners();
        if (mp != null) {
            if (mp.isPlaying()) mp.stop();
            mp.release();
            mp = null;
        }
    }

    private class ProgressUpdate extends TimerTask {

        public ProgressUpdate() {
            super();
        }

        @Override
        public void run() {
            runOnUiThread(() -> {
                if (mp != null && mp.isPlaying()) {
                    int currentPosition = mp.getCurrentPosition();
                    Message msg = new Message();
                    msg.arg1 = currentPosition;
                    mHandler.dispatchMessage(msg);
                }
            });
        }
    }

    private void selectAudioFile() {
        Log.d(CLASS_TAG, "selectAudioFile");
        filePickerLauncher.launch(Constants.AUDIO_FILE);
    }

    private void initialiseFilePickerLauncher() {
        // Initialize the ActivityResultLauncher
        // The contract is GetContent(), which takes a String (MIME type) as input
        // and returns a Uri as output.
        filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    // Handle the returned Uri here
                    if (uri != null) {
                        mediaUri = uri;
                        final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
                        // Check for the freshest data.
                        getContentResolver().takePersistableUriPermission(mediaUri, takeFlags);
                        editTextFileUri.setText(StorageUtil.getFileName(getApplicationContext(), mediaUri));
                        save();
                        setMediaPlayerSource();
                    } else {
                        Log.d(CLASS_TAG, "No file selected");
                    }
                }
            });
    }

}
