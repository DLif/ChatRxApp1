package com.example.chatrxtest1;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.example.chatrxtest1.Firebase.IChatReceiver;
import com.example.chatrxtest1.Firebase.NotificationIntentReceiver;
import com.example.chatrxtest1.MessagesMVC.ChatMessage;
import com.example.chatrxtest1.MessagesMVC.MemberData;
import com.example.chatrxtest1.MessagesMVC.MessageAdapter;
import com.example.chatrxtest1.ServerComm.Registration;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements IChatReceiver {

    private static final String TAG = "Chat_rx";

    private static final String audioFilesDir= "/AudioFiles";

    private String _serverName;

    private MessageAdapter _messageAdapter;
    private ListView _messagesView;
    private boolean _autoStartPreference;

    private NotificationIntentReceiver _pushNotificationReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "The onCreate has been called on the main activity.");

        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        _autoStartPreference = PreferenceManager.getDefaultSharedPreferences(this).getBoolean
                (SettingsActivity.AUTO_PLAY_PREFERENCE_KEY, true);
        _serverName = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(SettingsActivity.SERVER_NAME_PREFERENCE_KEY, "");
        //_serverName="192.168.43.226";

        Registration reg = new Registration(this, _serverName);
        reg.GetFirebaseTokenAndRegister();

        _pushNotificationReceiver = new NotificationIntentReceiver(this);

        if (_messageAdapter == null) {
            _messageAdapter = new MessageAdapter(this, this, _autoStartPreference);
        } else {
            _messageAdapter.set_autoStartPreference(_autoStartPreference);
        }

        _messagesView = findViewById(R.id.messages_view);
        _messagesView.setAdapter(_messageAdapter);

        if (getIntent() != null && getIntent().getExtras() != null ) {
            if (getIntent().getExtras().containsKey("SourceId")
                && getIntent().getExtras().containsKey("Content")
                && getIntent().getExtras().containsKey("MessageId")) {
                Log.d(TAG, "Launching intent has extras data of notification");
                _pushNotificationReceiver.onReceive(this, getIntent());
            }
        }
    }

    @Override
    protected void onRestoreInstanceState (Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "The onRestoreInstanceState has been called on the main activity");

        if (_messageAdapter != null) {
            _messageAdapter.release();
            _messageAdapter.get_messages().clear();
        } else {
            _messageAdapter = new MessageAdapter(this, this, _autoStartPreference);
            _messagesView.setAdapter(_messageAdapter);
        }

        _messagesView = findViewById(R.id.messages_view);
        if (savedInstanceState != null && savedInstanceState.containsKey("messagesList")) {
            ArrayList<ChatMessage> messages;
            messages = savedInstanceState.getParcelableArrayList("messagesList");
            if (messages != null) {
                for (ChatMessage msg: messages) {
                    OnNewChatMsg(msg);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "The onResume has been called on the main activity");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.example.chatrxtest1.onMessageReceived");

        Intent stickyIntent = registerReceiver(_pushNotificationReceiver, intentFilter);

        //update preference for auto-play if needed
        _autoStartPreference = PreferenceManager.getDefaultSharedPreferences(this).getBoolean
                (SettingsActivity.AUTO_PLAY_PREFERENCE_KEY, true);

        if (stickyIntent != null) {
            Log.d(TAG, "Got the sticky bastard!");
            removeStickyBroadcast(stickyIntent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.simulate_new_msg) {

            MemberData data = new MemberData(MemberData.getRandomName());

            final ChatMessage newChatMsg;
            File internalFile = new File(getApplicationContext().getFilesDir()+audioFilesDir, "1.mp3");
            if (internalFile.exists()) { //tells us if the file is overloaded
                newChatMsg = new ChatMessage(ChatMessage.getRandomText(), data, internalFile.getAbsolutePath());
            } else {
                newChatMsg = new ChatMessage(ChatMessage.getRandomText(), data, R.raw.eye_of_the_storm);
            }

            OnNewChatMsg(newChatMsg);

            return true;
        } else if (id == R.id.simulate_msg_no_audio) {

            MemberData data = new MemberData(MemberData.getRandomName());
            final ChatMessage newChatMsg = new ChatMessage(ChatMessage.getRandomText(), data);

            OnNewChatMsg(newChatMsg);

        } else if (id == R.id.get_token) {
            final Context con = this;

            FirebaseInstanceId.getInstance().getInstanceId()
                    .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                        @Override
                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                            if (!task.isSuccessful()) {
                                Log.w(TAG, "Firebase get instance ID failed", task.getException());
                                return;
                            }

                            final String token;
                            try {
                                token = task.getResult().getToken();
                                Log.d(TAG, token);
                            } catch (Exception exp) {
                                Log.e(TAG, "Failed to get get firebase token at GetFirebaseTokenAndRegister", exp);
                                return;
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast toast
                                            = Toast.makeText(
                                            con
                                            ,token
                                            ,Toast.LENGTH_LONG);
                                    toast.show();
                                }
                            });
                        }
                    });
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void OnNewChatMsg(final ChatMessage newMsg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                _messageAdapter.add(newMsg);
                // scroll the ListView to the last added element
                _messagesView.setSelection(_messagesView.getCount() - 1);
            }
        });
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "The onPause has been called on the main activity");
        unregisterReceiver(_pushNotificationReceiver);
        _messageAdapter.pauseAll();
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "The onSaveInstanceState has been called on the main activity");
        _messageAdapter.pauseAll();
        outState.putParcelableArrayList("messagesList", _messageAdapter.get_messages());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "The onDestroy has been called on the main activity. Is finishing: "+ isFinishing());
        if (isFinishing()) _messageAdapter.release();
        super.onDestroy();
    }
}
