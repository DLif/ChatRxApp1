package com.example.chatrxtest1.Firebase;

import com.example.chatrxtest1.MessagesMVC.ChatMessage;

public interface IChatReceiver {
    void OnNewChatMsg(ChatMessage newMsg);
}
