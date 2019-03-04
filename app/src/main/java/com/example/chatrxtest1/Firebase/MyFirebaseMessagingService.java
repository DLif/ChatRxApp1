package com.example.chatrxtest1.Firebase;

import android.content.Intent;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "Chat_rx";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated.
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "To: " + remoteMessage.getTo());
        Log.d(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());

        Intent intent = new Intent();
        Map<String, String> msgMetaData =  remoteMessage.getData();
        if (msgMetaData.containsKey("SourceId")) {
            intent.putExtra("SourceId", msgMetaData.get("SourceId"));
        }
        if (msgMetaData.containsKey("Content")) {
            intent.putExtra("Content", msgMetaData.get("Content"));
        }
        if (msgMetaData.containsKey("MessageId")) {
            intent.putExtra("MessageId", msgMetaData.get("MessageId"));
        }

        intent.setAction("com.example.chatrxtest1.onMessageReceived");
        sendStickyBroadcast(intent); //sendStickyBroadcast use sticky broadcast so we can receive the intent even if the application is in the background
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.d(TAG,String.format(""));

    }
}
