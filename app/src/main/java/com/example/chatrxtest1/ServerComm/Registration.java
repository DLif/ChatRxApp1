package com.example.chatrxtest1.ServerComm;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.example.chatrxtest1.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Registration implements ICommCaller {
    private static final String TAG = "Chat_rx";
    private static final String _onAppStartRegistrationUrl_base = "http://%s/register-phone";

    private final Activity _callerActivity;
    private final String _serverName;

    public Registration(Activity activity, String serverName) {
        _callerActivity = activity;
        _serverName = serverName;
    }

    private static Boolean SendRegistrationMsg(String serverName, String registrationBody) {
        String onAppStartRegistrationUrl = String.format(_onAppStartRegistrationUrl_base, serverName);

        HttpURLConnection httpRegistrationConn = null;
        try {
            URL registrationUrl = new URL(onAppStartRegistrationUrl);
            httpRegistrationConn = (HttpURLConnection) registrationUrl.openConnection();

            // Set http request method to get.
            httpRegistrationConn.setDoOutput(false);
            httpRegistrationConn.setDoInput(true);
            httpRegistrationConn.setRequestMethod("PUT");
            // Set connection timeout and read timeout value.
            httpRegistrationConn.setConnectTimeout(10000);
            httpRegistrationConn.setReadTimeout(10000);
            //Set request content-type
            httpRegistrationConn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            httpRegistrationConn.setRequestProperty("Accept", "application/json; charset=utf-8");

            //set requestContent
            OutputStream os = httpRegistrationConn.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8);
            osw.write(registrationBody);
            osw.flush();
            osw.close();
            os.close();

            //Get Response
            httpRegistrationConn.connect();
            int responseCode = httpRegistrationConn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                //Handle server response
                Log.d(TAG, String.format("Successful registration request with :%d.", responseCode));
                return true;
            } else {
                //Handle registration exception failure
                Log.e(TAG, String.format("Failed registration request with :%d.", responseCode));
                return false;
            }
        } catch (Exception exp) {
            Log.e(TAG, "Exception on sending registration to server. Get connection and restart the app. " + exp.getMessage(), exp);
            return false;
        } finally {
            if (httpRegistrationConn != null) httpRegistrationConn.disconnect();
        }
    }

    public void GetFirebaseTokenAndRegister() {
        final ICommCaller commCaller = this;

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
                        } catch (Exception exp) {
                            Log.e(TAG, "Failed to get get Firebase token at GetFirebaseTokenAndRegister", exp);
                            return;
                        }

                        //Generate body for request
                        String phoneNumber = GetPhoneNumber(_callerActivity);
                        String deviceName = getDeviceName();
                        String registrationBody =
                                String.format("{ \"token\": \"%s\", \"phoneNumber\": \"%s\", \"deviceName\": \"%s\"}", token, phoneNumber, deviceName);

                        //Post the http registration on another thread
                        AsyncHandlerWrapper registrationCall = new AsyncHandlerWrapper(commCaller);
                        registrationCall.execute(_serverName, registrationBody);
                    }
                });
    }

    private static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return model;
        }
        return manufacturer + " " + model;
    }

    private static String GetPhoneNumber(Context context) {
        try {
            TelephonyManager tMgr = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
            String mPhoneNumber = tMgr.getLine1Number();
            if (mPhoneNumber != null && mPhoneNumber.equals("")) {
                Log.d(TAG, "Failed to get phone number because the phone sim doesn't support getLine1Number");
            } else if (mPhoneNumber != null) {
                Log.d(TAG, String.format("Managed to get phone number %s", mPhoneNumber));
                mPhoneNumber = mPhoneNumber.replace("-","");
            }
            return  mPhoneNumber;
        } catch (SecurityException exp) {
            Log.w(TAG, "Failed to get phone number with security exception. " + exp.getMessage());
            return null;
        } catch (Exception exp) {
            Log.w(TAG, "Failed to get phone number with non-security exception", exp);
            return null;
        }
    }

    @Override
    public String calledToAsynced(String... params) {
        String serverName = params[0];
        String callBody = params[1];
        return SendRegistrationMsg(serverName, callBody).toString();
    }

    @Override
    public void processFinish(String output) {
        boolean hasRegistered = Boolean.parseBoolean(output);
        final String regError =
                _callerActivity.getResources().getString(R.string.user_server_reg_failure_part1_toast)
                + " " +_serverName
                + _callerActivity.getResources().getString(R.string.user_server_reg_failure_part2_toast);

        if (!hasRegistered) {
            _callerActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast toast = Toast.makeText(_callerActivity, regError ,Toast.LENGTH_LONG);
                    toast.show();
                }
            });
            Log.w(TAG, regError);
        } else {
            Log.d(TAG, String.format("Registered to %s successfully", _serverName));
        }
    }
}
