package com.example.dshinde.myapplication_xmlpref.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;
import com.example.dshinde.myapplication_xmlpref.R;
import com.example.dshinde.myapplication_xmlpref.activities.AudioActivity;
import com.example.dshinde.myapplication_xmlpref.listners.AudioServiceListener;

import java.io.IOException;

public class AudioService extends Service implements AudioManager.OnAudioFocusChangeListener {
    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;
    private AudioServiceListener audioServiceListener;
    private final IBinder binder = new LocalBinder();

    public void addListener(AudioServiceListener audioServiceListener) {
        this.audioServiceListener = audioServiceListener;
        mediaPlayer.setOnCompletionListener(mp -> {
            if (audioServiceListener != null) {
                audioServiceListener.playCompleted();
            }
        });
        mediaPlayer.setOnPreparedListener(mp -> {
            if (audioServiceListener != null) {
                audioServiceListener.playStarted();
            }
        });
    }

    public class LocalBinder extends Binder {
        public AudioService getService() {
            return AudioService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setLooping(false);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (action != null) {
            switch (action) {
                case "PLAY":
                    Uri audioUri = intent.getData();
                    if (audioUri != null) {
                        try {
                            mediaPlayer.reset();
                            mediaPlayer.setDataSource(getApplicationContext(), audioUri);
                            mediaPlayer.prepare();
                            if (requestAudioFocus()) {
                                mediaPlayer.start();
                                if (audioServiceListener != null) {
                                    audioServiceListener.playStarted();
                                }
                                //startForeground(1, createNotification());
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else if (!mediaPlayer.isPlaying() && requestAudioFocus()) {
                        mediaPlayer.start();
                        //startForeground(1, createNotification());
                    }
                    break;
                case "PAUSE":
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                        //stopForeground(false);
                    }
                    break;
                case "STOP":
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                        mediaPlayer.prepareAsync();
                        //stopForeground(true);
                    }
                    break;
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        abandonAudioFocus();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public int getCurrentPosition() {
        return mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0;
    }

    public int getDuration() {
        return mediaPlayer != null ? mediaPlayer.getDuration() : 0;
    }

    public void seekTo(int position) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(position);
        }
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }


    private Notification createNotification() {
        NotificationChannel channel = new NotificationChannel("AudioServiceChannel",
                "Audio Service Channel", NotificationManager.IMPORTANCE_LOW);
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }

        return new NotificationCompat.Builder(this, "AudioServiceChannel")
                .setContentTitle("Playing Audio")
                .setContentText("Your audio is playing in the background")
                .setSmallIcon(R.drawable.ic_action_audio_note)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private boolean requestAudioFocus() {
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    private void abandonAudioFocus() {
        audioManager.abandonAudioFocus(this);
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                if (mediaPlayer == null) {
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setLooping(false);
                } else if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                }
                mediaPlayer.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
                mediaPlayer = null;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.setVolume(0.2f, 0.2f);
                }
                break;
        }
    }
}
