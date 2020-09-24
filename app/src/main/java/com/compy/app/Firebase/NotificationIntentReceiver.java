package com.compy.app.Firebase;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.compy.app.MessagesMVC.ChatMessage;
import com.compy.app.MessagesMVC.MemberData;
import com.compy.app.R;

import java.io.File;

public class NotificationIntentReceiver extends BroadcastReceiver {

    private static final String TAG = "Chat_rx";

    private static final String audioFilesDir= "/AudioFiles";

    private IChatReceiver _chatMsgHandler;

    public NotificationIntentReceiver(IChatReceiver chatMsgHandler) {
        _chatMsgHandler = chatMsgHandler;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            Bundle extras = intent.getExtras();
            String memberName = extras.getString("SourceId");
            String chatText = extras.getString("Content");
            int audioId = Integer.parseInt(extras.getString("MessageId"));
            Log.d(TAG, String.format("Parsed notification to %s,%s,%d", memberName, chatText, audioId));
            boolean hasAudio = (audioId != -1);

            //Make a ChatMessage object out of that
            MemberData data = new MemberData(memberName);
            ChatMessage chatMsg;

            if (hasAudio) {

                File internalFile = new File(audiIdToInternalFilePath(context.getFilesDir()+audioFilesDir, audioId));
                if (internalFile.exists()) {
                    Log.d(TAG, "Found " + internalFile.getAbsolutePath());
                    chatMsg = new ChatMessage(chatText, data, internalFile.getAbsolutePath());
                } else {
                    Log.d(TAG, "No found " + internalFile.getAbsolutePath());
                    chatMsg = new ChatMessage(chatText, data, audiIdToResourceId(audioId));
                }
            } else {
                chatMsg = new ChatMessage(chatText, data);
            }

            //Call activity from here by the field
            _chatMsgHandler.OnNewChatMsg(chatMsg);
        }
        catch (Exception exp) {
            Log.e(TAG, "Exception on receiving or parsing the firebase cht message.", exp);
        }
    }

    public static int audiIdToResourceId(int audioId) {
        switch (audioId) {
            case 0:
                return R.raw.eye_of_the_storm;
            default:
                return R.raw.eye_of_the_storm;
        }
    }

    public static String audiIdToInternalFilePath(String internalFilesDir, int audioId) {
        return new File(internalFilesDir, String.format("%d.mp3", audioId)).toString();
    }

}
