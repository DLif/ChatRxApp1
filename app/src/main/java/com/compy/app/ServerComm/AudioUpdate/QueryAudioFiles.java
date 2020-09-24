package com.compy.app.ServerComm.AudioUpdate;

import android.app.Activity;
import android.util.Log;

import com.compy.app.ServerComm.AsyncHandlerWrapper;
import com.compy.app.ServerComm.ICommCaller;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

public class QueryAudioFiles {

    private static final String TAG = "Chat_rx";
    private static final String _getAudioUpdateUrl_base = "http://%s/AudioUpdate/%s";
    private static final String audioFilesDir= "/AudioFiles";

    private final String _serverName;
    private final String _username;
    private final Activity _callerActivity;

    private Hashtable<String, String> _audioFilesMeta;

    public QueryAudioFiles(String serverName, String username, Activity callerActivity) {
        _serverName = serverName;
        _username = username;
        _callerActivity = callerActivity;
    }

    public void QueryAndUpdateAudioFiles() {
        _audioFilesMeta = new Hashtable<>();

        File audioDir = new File(_callerActivity.getFilesDir().toString()+ audioFilesDir);
        File[] directoryListing = audioDir.listFiles();
        for (File audioFile : directoryListing) {
            byte[] fileMd5 = Hash.MD5.checksum(audioFile);
            _audioFilesMeta.put(audioFile.getName(), encodeHexString(fileMd5));
        }

        ICommCaller aCaller = new ICommCaller() {
            @Override
            public String calledToAsynced(String... params) {
                String serverName = params[0];

                String response = GetAudioUpdateMsg(serverName);

                List<String> toUpdateFilesLst = new ArrayList<>();
                List<String> toUpdateFiles_serverIds = new ArrayList<>();

                ParseResponse_handleDeletes_getUpdatesLst(response, toUpdateFilesLst, toUpdateFiles_serverIds);

                for(int i =0; i < toUpdateFilesLst.size(); i++) {
                    String fileName = toUpdateFilesLst.get(i);
                    String fileServerId = toUpdateFiles_serverIds.get(i);

                    DownloadAudioFile(fileName, fileServerId);
                }

                return null;
            }

            @Override
            public void processFinish(String output) {

            }
        };

        //Get from sever the files update on another thread
        AsyncHandlerWrapper registrationCall = new AsyncHandlerWrapper(aCaller);
        registrationCall.execute(_serverName);
    }

    private String GetAudioUpdateMsg(String serverName) {
        String onAppStartRegistrationUrl = String.format(_getAudioUpdateUrl_base, serverName, _username);

        HttpURLConnection httpConn = null;
        try {
            URL registrationUrl = new URL(onAppStartRegistrationUrl);
            httpConn = (HttpURLConnection) registrationUrl.openConnection();

            // Set http request method to get.
            httpConn.setDoOutput(true);
            httpConn.setDoInput(false);
            httpConn.setRequestMethod("GET");
            // Set connection timeout and read timeout value.
            httpConn.setConnectTimeout(10000);
            httpConn.setReadTimeout(10000);
            //Set request content-type
            httpConn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            httpConn.setRequestProperty("Accept", "application/json; charset=utf-8");

            //Get Response
            httpConn.connect();
            int responseCode = httpConn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK ) {

                Log.d(TAG, String.format("Got the updated audio files list with %d.", responseCode));

                //Read the server response to string, so it will be handled in "OnPostCall"
                BufferedReader br = new BufferedReader(new InputStreamReader(httpConn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                br.close();
                return sb.toString();
            } else {
                //Handle registration exception failure
                Log.e(TAG, String.format("Failed to get audio files list :%d.", responseCode));
                return null;
            }
        } catch (Exception exp) {
            Log.e(TAG, "Exception on sending registration to server. Get connection and restart the app. " + exp.getMessage(), exp);
            return null;
        } finally {
            if (httpConn != null) httpConn.disconnect();
        }
    }

    private void ParseResponse_handleDeletes_getUpdatesLst(String output, List<String> toUpdateFilesLst, List<String> toUpdateFiles_serverIds) {
        try {
            HashMap<String, JSONObject> serverFilesMap = new HashMap<>();

            //Parse response as json
            JSONObject response = new JSONObject(output);
            JSONArray jArr = response.getJSONArray("UpdatedAudioList");
            for (int i=0; i < jArr.length(); i++) {
                JSONObject fileJsonObj = jArr.getJSONObject(i);
                String fileName = fileJsonObj.getString("FileName");
                serverFilesMap.put(fileName, fileJsonObj);
            }

            //Delete all files not on the server list, and get all files with hash diff
            for (String audioFileName : _audioFilesMeta.keySet()) {
                File currAudioFile = new File(_callerActivity.getFilesDir().toString()+ audioFilesDir, audioFileName);
                String audioFilePath = currAudioFile.getAbsolutePath();

                if (!serverFilesMap.containsKey(audioFileName)) {
                    boolean isDeleted = currAudioFile.delete();
                    if (!isDeleted) { Log.w(TAG, String.format("Internal file %s could not be deleted. Removed by server list", audioFilePath));
                    } else { Log.d(TAG, String.format("Deleted the file %s. Removed by server list", audioFilePath)); }
                }

                JSONObject fileServerObj = serverFilesMap.get(audioFileName);
                String fileServerHash = fileServerObj.getString("Hash");
                if (!fileServerHash.equals(_audioFilesMeta.get(audioFilePath))) {
                    Log.d(TAG, String.format("Detected hash change in server for the file %s", audioFilePath));
                    toUpdateFilesLst.add(audioFileName);

                    boolean isDeleted = currAudioFile.delete();
                    if (!isDeleted) { Log.w(TAG, String.format("Internal file %s could not be deleted for update. Removed by server list", audioFilePath));
                    } else { Log.d(TAG, String.format("Deleted the file %s for update. Removed by server list ", audioFilePath)); }
                }
            }

            //Get all files with no current equivalent
            for (String serverFileName : serverFilesMap.keySet()) {
                if (!_audioFilesMeta.containsKey(serverFileName)) {
                    Log.d(TAG, String.format("Detected new file from server - %s", serverFileName));
                    toUpdateFilesLst.add(serverFileName);
                }
            }

            for (String fileToDownload : toUpdateFilesLst) {
                toUpdateFiles_serverIds.add(serverFilesMap.get(fileToDownload).getString("FileServerId"));
            }
        } catch (JSONException e) {
            Log.e(TAG, "Failed on response parse and handle", e);
        }
    }

    private void DownloadAudioFile(String fileName, String fileServerId) {
        //TODO: download file on the same thread
    }


    private String encodeHexString(byte[] byteArray) {
        StringBuffer hexStringBuffer = new StringBuffer();
        for (int i = 0; i < byteArray.length; i++) {
            hexStringBuffer.append(byteToHex(byteArray[i]));
        }
        return hexStringBuffer.toString();
    }

    private String byteToHex(byte num) {
        char[] hexDigits = new char[2];
        hexDigits[0] = Character.forDigit((num >> 4) & 0xF, 16);
        hexDigits[1] = Character.forDigit((num & 0xF), 16);
        return new String(hexDigits);
    }
}
