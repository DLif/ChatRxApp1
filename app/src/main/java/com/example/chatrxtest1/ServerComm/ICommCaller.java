package com.example.chatrxtest1.ServerComm;

public interface ICommCaller {
    String calledToAsynced(String... params);
    void processFinish(String output);
}
