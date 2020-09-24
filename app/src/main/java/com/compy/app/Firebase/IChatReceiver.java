package com.compy.app.Firebase;

import com.compy.app.MessagesMVC.ChatMessage;

public interface IChatReceiver {
    void OnNewChatMsg(ChatMessage newMsg);
}
