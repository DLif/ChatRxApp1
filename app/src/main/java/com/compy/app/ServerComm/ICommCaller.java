package com.compy.app.ServerComm;

public interface ICommCaller {
    String calledToAsynced(String... params);
    void processFinish(String output);
}
