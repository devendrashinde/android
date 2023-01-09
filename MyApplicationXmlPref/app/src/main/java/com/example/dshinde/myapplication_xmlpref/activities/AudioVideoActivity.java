package com.example.dshinde.myapplication_xmlpref.activities;

import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.dshinde.myapplication_xmlpref.R;
import com.example.dshinde.myapplication_xmlpref.common.Constants;
import com.example.dshinde.myapplication_xmlpref.helper.Factory;
import com.example.dshinde.myapplication_xmlpref.helper.StorageUtil;
import com.example.dshinde.myapplication_xmlpref.listners.DataStorageListener;
import com.example.dshinde.myapplication_xmlpref.model.KeyValue;
import com.example.dshinde.myapplication_xmlpref.services.DataStorage;
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
    static final int SELECT = 1;
    static final int PLAY = 2;
    private static final String CLASS_TAG = "AudioVideoActivity";
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
    MediaPlayer mp = new MediaPlayer();
    Gson gson = new GsonBuilder().create();
    SeekBar seekBar;
    public final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            seekBar.setProgress(msg.arg1);
        }
    };
    Timer timer;
    int mode, currentNote, totalNotes;
    List<KeyValue> notes;
    List<KeyValue> audioNotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.audiovideo_activity_layout);
        timer = new Timer();
        // get parameters
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            userId = bundle.getString("userId");
            collectionName = bundle.getString("note");
            key = bundle.getString("key");
            if (key != null && !key.isEmpty()) {
                noteText = bundle.getString("value");
                mode = SELECT;
                setTitleDescription();
            } else {
                mode = PLAY;
                notes = (List<KeyValue>) getIntent().getSerializableExtra("data");
                totalNotes = notes.size();
                if (totalNotes > 0) {
                    currentNote = 0;
                    key = notes.get(currentNote).getKey();
                    noteText = notes.get(currentNote).getValue();
                }
                setTitleDescription();
            }
            loadUI();
            initDataStorageAndLoadData(this);
        }
    }

    private void setTitleDescription() {
        setTitle(key + " - " + collectionName);
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

    private void next() {
        if (currentNote + 1 < totalNotes) {
            loadTextOrAudioNote(notes.get(++currentNote), false);
            loadAudioNote();
        }
    }

    private void setPreviousButtonListener() {
        buttonPrevious.setOnClickListener(v -> previous());
    }

    private void previous() {
        if (currentNote - 1 >= 0) {
            loadTextOrAudioNote(notes.get(--currentNote),false);
            loadAudioNote();
        }
    }

    private void playOrPause() {
        if(mediaUri == null){
            return;
        }
        if (mp.isPlaying()) {
            mp.pause();
        } else {
            if (mp.getDuration() == mp.getCurrentPosition()) {
                seekBar.setProgress(0);
            }
            mp.start();
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
        if (mode == PLAY) {
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

    private void setFileSelectorListener() {
        if (mode == PLAY) {
            editTextFileUri.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        } else {
            editTextFileUri.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (editTextFileUri.getRight() - editTextFileUri.getCompoundDrawables()[Constants.DRAWABLE_RIGHT].getBounds().width())) {
                        selectFile(Constants.AUDIO_FILE, Constants.SELECT_AUDIO);
                        return true;
                    }
                }
                return false;
            });
        }
    }

    private void initDataStorageAndLoadData(Context context) {

        Log.d(CLASS_TAG, "initDataStorageAndLoadData->getDataStorageInstance");
        dataStorageManager = Factory.getDataStorageIntsance(context,
                getDataStorageType(),
                Constants.MEDIA_NOTE_PREFIX + collectionName,
                false, false, new DataStorageListener() {
                    @Override
                    public void dataChanged(String key, String value) {
                        Log.d(CLASS_TAG, "dataChanged key: " + key + ", value: " + value);
                        showInShortToast("Saved!");
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
                Map<String, String> value = gson.fromJson(keyValue.getValue(), Map.class);
                editTextFileUri.setText(value.get(MEDIA_NAME));
                mediaUri = Uri.parse(value.get(MEDIA_URI));
                setMediaPlayerSource();
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
            editTextFileUri.setText("no audio note");
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.navigation, menu);
        removeUnwantedMenuItems(menu);
        return true;
    }

    private void removeUnwantedMenuItems(Menu menu) {
        menu.removeItem(R.id.menu_add_to_shadba_kosh);
        menu.removeItem(R.id.menu_backup);
        menu.removeItem(R.id.menu_remove);
        menu.removeItem(R.id.menu_sell);
        menu.removeItem(R.id.menu_settings);
        menu.removeItem(R.id.menu_clear);
        menu.removeItem(R.id.menu_design_screen);
        menu.removeItem(R.id.menu_export);
        menu.removeItem(R.id.menu_copy);
        menu.removeItem(R.id.menu_import);
        menu.removeItem(R.id.menu_edit);
        menu.removeItem(R.id.menu_pay);
        menu.removeItem(R.id.menu_add);
        menu.removeItem(R.id.menu_view);
        menu.removeItem(R.id.menu_share);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_save:
                save();
                return true;
            case R.id.menu_remove:
                remove();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void save() {
        if (key != null && mediaUri != null) {
            Map<String, String> data = new HashMap<>();
            data.put(MEDIA_URI, mediaUri.toString());
            data.put(MEDIA_NAME, editTextFileUri.getText().toString());
            dataStorageManager.save(key, gson.toJson(data));
        }
    }

    private void remove() {
        if (key != null) {
            dataStorageManager.remove(key);
        }
    }

    private void selectFile(String fileType, int actionCode) {
        Log.d(CLASS_TAG, "SelectFile");
        //Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(fileType);
        startActivityForResult(intent, actionCode);
    }

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
            setMediaPlayerSource();
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
                if(mode == PLAY) {
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
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (mp != null && mp.isPlaying()) {
                        int currentPosition = mp.getCurrentPosition();
                        Message msg = new Message();
                        msg.arg1 = currentPosition;
                        mHandler.dispatchMessage(msg);
                    }
                }
            });
        }
    }
}
