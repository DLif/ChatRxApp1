package com.compy.app.ServerComm;

import android.os.AsyncTask;

public class AsyncHandlerWrapper extends AsyncTask<String, Void, String> {

    public ICommCaller delegate;

    public AsyncHandlerWrapper(ICommCaller delegate){
        this.delegate = delegate;
    }

    @Override
    protected String doInBackground(String... params) {
        return delegate.calledToAsynced(params);
    }

    @Override
    protected void onPostExecute(String result) {
        delegate.processFinish(result);
    }
}
