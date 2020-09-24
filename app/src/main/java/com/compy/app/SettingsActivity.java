package com.compy.app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.compy.app.Settings.SettingsFragment;

public class SettingsActivity extends AppCompatActivity {

    public static final String
            AUTO_PLAY_PREFERENCE_KEY = "auto_play_preference";
    public static final String
            SERVER_NAME_PREFERENCE_KEY = "server_name_preference";
    public static final String
            RESET_AUDIO_PREFERENCE_KEY = "reset_audio_preference";
    public static final String
            UPDATE_AUDIO_PREFERENCE_KEY = "update_audio_preference";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    @Override
    public boolean onNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
