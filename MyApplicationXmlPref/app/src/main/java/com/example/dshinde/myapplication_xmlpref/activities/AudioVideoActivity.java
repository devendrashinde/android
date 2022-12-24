package com.example.dshinde.myapplication_xmlpref.activities;

import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
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

import java.security.Key;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AudioVideoActivity extends BaseActivity {
    private static final String CLASS_TAG = "AudioVideoActivity";
    public static final String MEDIA_URI = "mediaUri";
    public static final String MEDIA_NAME = "mediaName";
    EditText editTextFileUri;
    Button buttonPlay;
    Button buttonPause;
    Button buttonStop;
    TextView textViewNote;
    DataStorage dataStorageManager;
    String collectionName = null;
    String key;
    Uri mediaUri;
    MediaPlayer mp = new MediaPlayer();
    Gson gson = new GsonBuilder().create();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.audiovideo_activity_layout);

        loadUI();
        // get parameters
        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            userId = bundle.getString("userId");
            collectionName = bundle.getString("note");
            key = bundle.getString("key");
            textViewNote.setText(bundle.getString("value"));
            setTitle(key);
        }
        setMediaPlayerActionListeners();
        initDataStorageAndLoadData(this);
    }

    private void setMediaPlayerActionListeners() {
        buttonPlay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mp.start();
            }
        });
        buttonPause.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mp.pause();
            }
        });
        buttonStop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mp.stop();
            }
        });
    }

    private void loadUI(){
        editTextFileUri = (EditText) findViewById(R.id.fileUri);
        buttonPlay = (Button) findViewById(R.id.buttonPlay);
        buttonPause = (Button) findViewById(R.id.buttonPause);
        buttonStop = (Button) findViewById(R.id.buttonStop);
        textViewNote = (TextView)  findViewById(R.id.textNote);
        editTextFileUri.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (editTextFileUri.getRight() - editTextFileUri.getCompoundDrawables()[Constants.DRAWABLE_RIGHT].getBounds().width())) {
                        selectFile(Constants.AUDIO_FILE, Constants.SELECT_AUDIO);
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private void initDataStorageAndLoadData(Context context) {

        Log.d(CLASS_TAG, "initDataStorageAndLoadData->getDataStorageInstance");
        dataStorageManager = Factory.getDataStorageIntsance(context,
                getDataStorageType(),
                Constants.MEDIA_NOTE + ":" + collectionName,
                false, false, new DataStorageListener() {
            @Override
            public void dataChanged(String key, String value) {
                Log.d(CLASS_TAG, "dataChanged key: " + key + ", value: " + value);
            }

            @Override
            public void dataLoaded(List<KeyValue> data) {
                Log.d(CLASS_TAG, "dataLoaded");
                Optional<KeyValue> kv = data.stream().filter(keyValue -> keyValue.getKey().equalsIgnoreCase(key)).findFirst();
                if(kv.isPresent()) {
                    Map<String, String> value = gson.fromJson(kv.get().getValue(), Map.class);
                    mediaUri = Uri.parse(value.get(MEDIA_URI));
                    runOnUiThread(new Runnable() {
                        public void run() {
                            editTextFileUri.setText(value.get(MEDIA_NAME));
                        }
                    });
                    setMediaPlayerSource();
                }
            }
        });
        dataStorageManager.loadData();
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
        if(key != null && mediaUri != null){
            Map<String, String> data = new HashMap<>();
            data.put(MEDIA_URI, mediaUri.toString());
            data.put(MEDIA_NAME, editTextFileUri.getText().toString());
            dataStorageManager.save(key, gson.toJson(data));
        }
    }

    private void remove() {
        if(key != null){
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
        if( data != null && data.getData() != null) {
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

    private void setMediaPlayerSource(){
        try{
            ParcelFileDescriptor parcelFileDescriptor =
                    getContentResolver().openFileDescriptor(mediaUri, "r");
            mp.setAudioAttributes(new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA).build());
            mp.setDataSource(parcelFileDescriptor.getFileDescriptor());
            mp.prepare();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(CLASS_TAG, "onStop");
        dataStorageManager.removeDataStorageListeners();
    }

}
