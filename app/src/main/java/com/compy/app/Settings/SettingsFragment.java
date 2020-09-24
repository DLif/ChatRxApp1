package com.compy.app.Settings;


import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;
import android.widget.Toast;

import com.compy.app.Firebase.NotificationIntentReceiver;
import com.compy.app.R;
import com.compy.app.SettingsActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SettingsFragment extends PreferenceFragmentCompat {

    private static final String TAG = "Chat_rx";

    private static final String audioFilesDir= "/AudioFiles";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        findPreference(SettingsActivity.RESET_AUDIO_PREFERENCE_KEY)
                .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                ResetAudioEvent();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast toast = Toast.makeText(getContext(),R.string.audio_default_reset_toast,Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });
                return true;
            }
        });

        findPreference(SettingsActivity.UPDATE_AUDIO_PREFERENCE_KEY)
                .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    public boolean onPreferenceClick(Preference preference) {
                        UpdateAudioEvent();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast toast = Toast.makeText(getContext(),R.string.audio_updated_toast,Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        });
                        return true;
                    }
                });

        File audioDir = new File(getContext().getFilesDir().toString()+audioFilesDir);
        if (!audioDir.exists()) {
            audioDir.mkdir();
        }
    }

    private void UpdateAudioEvent() {
        try {
            File internalFile =
                    new File(NotificationIntentReceiver.audiIdToInternalFilePath(getContext().getFilesDir().toString()+audioFilesDir, 1));
            if (!internalFile.exists()) {
                InputStream inputStream = getResources().openRawResource(R.raw.we_will_rock_you);
                copy(inputStream, internalFile);
                inputStream.close();
            }
            Log.d(TAG, "Written to " + internalFile.getAbsolutePath());
        }
        catch (IOException exp) {
            Log.w(TAG, exp);
        }
    }

    private static void copy(InputStream inputStream, File dst) throws IOException {
        try (OutputStream out = new FileOutputStream(dst)) {
            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        }
    }

    private void ResetAudioEvent() {
        try {
            File audioDir = new File(getContext().getFilesDir().toString()+ audioFilesDir);
            File[] directoryListing = audioDir.listFiles();
            for (File audioFile : directoryListing) {
                if (audioFile.exists()) {
                    boolean isDeleted = audioFile.delete();
                    if (!isDeleted) {
                        Log.w(TAG, String.format("Internal file %s could not be deleted", audioFile.getAbsolutePath()));
                    } else {
                        Log.d(TAG, String.format("Deleted the file %s", audioFile.getAbsolutePath()));
                    }
                }
            }
        } catch (Exception exp) {
            Log.e(TAG, "Reset audio files failed with exception", exp);
        }
    }
}
