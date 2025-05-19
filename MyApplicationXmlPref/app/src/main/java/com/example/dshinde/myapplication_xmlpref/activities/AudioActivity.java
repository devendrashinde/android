package com.example.dshinde.myapplication_xmlpref.activities;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.dshinde.myapplication_xmlpref.R;
import com.example.dshinde.myapplication_xmlpref.common.Constants;
import com.example.dshinde.myapplication_xmlpref.helper.Factory;
import com.example.dshinde.myapplication_xmlpref.helper.StorageUtil;
import com.example.dshinde.myapplication_xmlpref.listners.AudioServiceListener;
import com.example.dshinde.myapplication_xmlpref.listners.DataStorageListener;
import com.example.dshinde.myapplication_xmlpref.listners.OnSwipeTouchListener;
import com.example.dshinde.myapplication_xmlpref.model.KeyValue;
import com.example.dshinde.myapplication_xmlpref.services.AudioService;
import com.example.dshinde.myapplication_xmlpref.services.DataStorage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class AudioActivity extends BaseActivity implements AudioServiceListener {
    public static final String MEDIA_URI = "mediaUri";
    public static final String MEDIA_NAME = "mediaName";
    static final int PLAYBACK_MODE_SELECT = 1;
    static final int PLAYBACK_MODE_PLAY = 2;
    private static final String CLASS_TAG = "AudioActivity";
    public static final String PLAY = "PLAY";
    public static final String PAUSE = "PAUSE";
    EditText editTextFileUri;
    ImageButton buttonPlay;
    ImageButton buttonPrevious;
    ImageButton buttonNext;
    TextView textViewNote;
    DataStorage dataStorageManager;
    String collectionName = null;
    String key;
    String noteText;
    Uri mediaUri;
    private Intent audioServiceIntent;
    private AudioService audioService;
    Gson gson = new GsonBuilder().create();
    SeekBar seekBar;
    private final Handler handler = new Handler();
    private boolean isBound = false;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AudioService.LocalBinder binder = (AudioService.LocalBinder) service;
            audioService = binder.getService();
            audioService.addListener(AudioActivity.this);
            isBound = true;
            updateSeekBar();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }

    };

    int mode, currentNote, totalNotes;
    List<KeyValue> notes;
    List<KeyValue> audioNotes;
    boolean playingNoteSubject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.audiovideo_activity_layout);
        // get parameters
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            userId = bundle.getString(Constants.USERID);
            collectionName = bundle.getString("note");
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

    private void updateSeekBar() {
        if (isBound) {
            seekBar.setProgress(audioService.getCurrentPosition());
            handler.postDelayed(this::updateSeekBar, 1000);
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
        if(!isBound || mediaUri == null){
            return;
        }
        if (audioService.isPlaying()) {
            setAudioServiceAction(PAUSE);
        } else {
            setAudioServiceAction(PLAY);
        }
        setPlayPauseButton();
    }

    private void loadUI() {
        //startService(audioServiceIntent);
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
                if (fromTouch && isBound) {
                    audioService.seekTo(progress);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void setFileSelectorListener() {
        /*
        if (mode == PLAY) {
            // remove file select icon
            editTextFileUri.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        } else {

         */
            editTextFileUri.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (editTextFileUri.getRight() - editTextFileUri.getCompoundDrawables()[Constants.DRAWABLE_RIGHT].getBounds().width())) {
                        selectFile(Constants.AUDIO_FILE, Constants.SELECT_AUDIO);
                        return true;
                    }
                }
                return false;
            });
            /*
        }*/
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
                    setMediaPlayerSource();
                }
            } else {
                key = keyValue.getKey();
                noteText = keyValue.getValue();
                setTitleDescription();
                parseAndDisplayText(noteText);
            }
        } else if(audioNote){
            if(isBound && audioService.isPlaying()) {
                setAudioServiceAction("STOP");
            }
            setPlayPauseButton();
            seekBar.setProgress(0);
            mediaUri = null;
            editTextFileUri.setText(R.string.no_audio_note);
        }
    }

    private void save() {
        String dbKey = (playingNoteSubject ? collectionName : key);
        if (dbKey != null && mediaUri != null) {
            Map<String, String> data = new HashMap<>();
            data.put(MEDIA_URI, mediaUri.toString());
            data.put(MEDIA_NAME, editTextFileUri.getText().toString());
            dataStorageManager.save((playingNoteSubject ? collectionName : dbKey), gson.toJson(data));
        }
    }

    private void remove() {
        String dbKey = (playingNoteSubject ? collectionName : key);
        if (dbKey != null) {
            dataStorageManager.remove(dbKey);
        }
    }

    private void selectFile(String fileType, int actionCode) {
        Log.d(CLASS_TAG, "SelectFile");
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(fileType);
        startActivityForResult(intent, actionCode);
    }

    @SuppressLint("WrongConstant")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && data.getData() != null) {
            mediaUri = data.getData();
            final int takeFlags = data.getFlags()
                    & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            // Check for the freshest data.
            getContentResolver().takePersistableUriPermission(mediaUri, takeFlags);
            editTextFileUri.setText(StorageUtil.getFileName(this, mediaUri));
            save();
            setMediaPlayerSource();
        }
    }

    private void setMediaPlayerSource() {
        if (mediaUri != null) {
            audioServiceIntent.setAction(PLAY);
            audioServiceIntent.setData(mediaUri);
            seekBar.setProgress(0);
            startService(audioServiceIntent);
        }
    }

    private void setAudioServiceAction(String command) {
        audioServiceIntent.setAction(command);
        startService(audioServiceIntent);
    }


    private void setPlayPauseButton() {
        if (audioService.isPlaying()) {
            buttonPlay.setImageResource(R.drawable.ic_action_pause);
        } else {
            buttonPlay.setImageResource(R.drawable.ic_action_play);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        audioServiceIntent = new Intent(this, AudioService.class);
        bindService(audioServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(CLASS_TAG, "onStop");
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        dataStorageManager.removeDataStorageListeners();
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }
    }

    @Override
    public void playStarted() {
        seekBar.setMax(audioService.getDuration());
    }

    @Override
    public void playCompleted() {
        setPlayPauseButton();
        seekBar.setProgress(audioService.getDuration());
        if(mode == PLAYBACK_MODE_PLAY) {
            next();
        }
    }
}
