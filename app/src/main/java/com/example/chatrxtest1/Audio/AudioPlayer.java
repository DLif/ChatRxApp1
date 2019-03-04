package com.example.chatrxtest1.Audio;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.util.Log;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AudioPlayer {

    private Context _appContext;
    private MediaPlayer _mp;
    private ScheduledExecutorService _taskExecutor;
    private Runnable _updateSeekBarTask;
    private String _audioFilePointer;

    private AudioStateConveyor _audioStateConveyor;

    private int _positionMemory;

    private static final String TAG = "Chat_rx";
    private static final int POSITION_REFRESH_MILLISECONDS = 500;

    public AudioPlayer(Context appContext) {
        _appContext = appContext;
        _positionMemory = 0;

        _updateSeekBarTask = new Runnable() {
            @Override
            public void run() {
                if (_mp != null && _mp.isPlaying()) {
                    int currentPosition = _mp.getCurrentPosition();
                    if (_audioStateConveyor != null) {
                        _audioStateConveyor.onPositionChanged(currentPosition);
                    }
                }
            }
        };
    }

    public void set_audioStateConveyor(AudioStateConveyor audioStateConveyor) {
        _audioStateConveyor = audioStateConveyor;
    }

    public int get_positionMemory() {
        return _positionMemory;
    }

    public boolean load_setPosition_play(int audioFilePointer) {
        loadMedia(audioFilePointer);
        return setPosition_play(_positionMemory);
    }

    public boolean load_setPosition_play(String innerStoragePath) {
        loadMedia(innerStoragePath);
        return setPosition_play(_positionMemory);
    }

    private boolean setPosition_play(int position) {
        if (seekTo(position)) {
            return play();
        } else {
            return false;
        }
    }

    private void loadMedia(int audioFilePointer) {
        setMediaPlayer(String.valueOf(audioFilePointer));

        AssetFileDescriptor assetFileDescriptor =
                _appContext.getResources().openRawResourceFd(audioFilePointer);
        try {
            _mp.setDataSource(assetFileDescriptor);
        } catch (Exception e) {
            Log.e(TAG, "Exception at setting audio data source", e);
            _audioStateConveyor.onInvalid();
            _mp.release();
            return;
        }

        try {
            _mp.prepare();
        } catch (Exception e) {
            Log.e(TAG, "Exception at mediaPlayer.prepare ", e);
            _audioStateConveyor.onInvalid();
            _mp.release();
            return;
        }

        initReporterOnNewData();
    }

    private void loadMedia(String innerStoragePath) {
        setMediaPlayer(innerStoragePath);

        try {
            _mp.setDataSource(innerStoragePath);
        } catch (Exception e) {
            Log.e(TAG, "Exception at setting audio data source", e);
            _audioStateConveyor.onInvalid();
            _mp.release();
            return;
        }

        try {
            _mp.prepare();
        } catch (Exception e) {
            Log.e(TAG, "Exception at mediaPlayer.prepare ", e);
            _audioStateConveyor.onInvalid();
            _mp.release();
            return;
        }

        initReporterOnNewData();
    }

    private void setMediaPlayer(String audioFilePointer) {
        if (_mp == null) {
            _audioFilePointer = audioFilePointer;
            _mp = new MediaPlayer();
            _mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    endPositionUpdate();
                    if (_audioStateConveyor != null) {
                        _audioStateConveyor.onFinished();
                    }
                    Log.d(TAG, "Finished audion playing. Audio number " + _audioFilePointer);
                }
            });
        }
    }

    private boolean play() {
        if (_mp != null && !_mp.isPlaying()) {
            Log.d(TAG, "Play has been called for the file " + _audioFilePointer + " on position :" + _positionMemory);
            _mp.start();
            if (_positionMemory != _mp.getCurrentPosition()) _mp.seekTo(_positionMemory);
            if (_audioStateConveyor != null) {
                _audioStateConveyor.onPlay();
            }
            beginPositionUpdate();
            return true;
        } else {
            return false;
        }
    }

    public void pause() {
        if (_mp != null && _mp.isPlaying()) {
            Log.d(TAG, "Pause has been called for the file " + _audioFilePointer);
            _mp.pause();
            _positionMemory = _mp.getCurrentPosition();

            if (_audioStateConveyor != null) {
                _audioStateConveyor.onPaused();
            }
            _mp.reset();
        }
    }

    public void reset() {
        if (_mp != null) {
            Log.d(TAG, "reset on " + _audioFilePointer);
            _positionMemory = 0;
            _mp.reset();
        }
        if (_audioStateConveyor != null) {
            _audioStateConveyor.onReset();
        }
        endPositionUpdate();
    }


    public boolean seekTo(int position) {
        if (_mp != null) {
            Log.d(TAG, String.format("seek %d ms in %s", position, _audioFilePointer));
            _positionMemory = position;
            _mp.seekTo(position);
            return true;
        } else {
            _positionMemory = position;
            return false;
        }
    }


    private void beginPositionUpdate() {
        if (_taskExecutor == null) {
            _taskExecutor = Executors.newSingleThreadScheduledExecutor();
        }
        _taskExecutor.scheduleAtFixedRate(_updateSeekBarTask, 0, POSITION_REFRESH_MILLISECONDS, TimeUnit.MILLISECONDS);
    }

    private void endPositionUpdate() {
        if (_taskExecutor != null) {
            _taskExecutor.shutdownNow();
            _taskExecutor = null;
            if (_audioStateConveyor != null) {
                _audioStateConveyor.onPositionChanged(0);
            }
        }
    }

    private void initReporterOnNewData() {
        final int duration = _mp.getDuration();
        if (_audioStateConveyor != null) {
            _audioStateConveyor.onDurationChanged(duration);
            _audioStateConveyor.onPositionChanged(0);
        }
    }

    public void release() {
        if (_mp != null) {
            _mp.release();
            _mp = null;
        }
    }

}
