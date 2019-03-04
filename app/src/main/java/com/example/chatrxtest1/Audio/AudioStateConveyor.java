package com.example.chatrxtest1.Audio;

import android.app.Activity;
import android.util.Log;

import com.example.chatrxtest1.MessagesMVC.MessageViewHolder;

public class AudioStateConveyor {

    private MessageViewHolder _messageViewHolder;
    private Activity _activity;

    private static final String TAG = "Chat_rx_AudioStateConveyor";


    public AudioStateConveyor(MessageViewHolder messageViewHolder, Activity activity) {
        _messageViewHolder = messageViewHolder;
        _activity = activity;
    }

    public void onDurationChanged(final int duration) {
        _activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                _messageViewHolder.seekBar.setMax(duration);
            }
        });

        Log.d(TAG, String.format("Set seekBar proportions. Duration: %d", duration));
    }

    public void onPositionChanged(final int position) {
        _activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                _messageViewHolder.seekBar.setProgress(position, true);
            }
        });

        Log.d(TAG, String.format("Update audio position. Position: %d", position));
    }

    public void onPlay() {
        _messageViewHolder.playPauseButton.setSelected(true);
    }

    public void onPaused() {
        _messageViewHolder.playPauseButton.setSelected(false);
    }

    public void onFinished() {
        _messageViewHolder.playPauseButton.setSelected(false);
    }

    public void onReset() {
        _messageViewHolder.playPauseButton.setSelected(true);
    }

    public void onInvalid() {
        Log.e(TAG, "An error occurred on loading message");
    }
}
