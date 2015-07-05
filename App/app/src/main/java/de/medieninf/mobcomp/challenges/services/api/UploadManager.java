package de.medieninf.mobcomp.challenges.services.api;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import de.medieninf.mobcomp.challenges.database.Database;
import de.medieninf.mobcomp.challenges.database.DatabaseProviderFascade;
import de.medieninf.mobcomp.challenges.external.HttpRequest;

public class UploadManager extends HandlerThread{

    final static String TAG = UploadManager.class.getSimpleName();

    private static final int UPLOAD_FILE = 0;
    private static final int LINK_FILE = 1;

    private ContentResolver contentResolver;
    private String serverUrl;

    private String authToken;
    private boolean moreToUpload;
    private boolean moreToLink;

    private Handler workerHandler;
    private Handler.Callback handlerCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case UPLOAD_FILE:
                    uploadFile();
                    sendLinkFile();
                    break;
                case LINK_FILE:
                    linkFile();
                    if(moreToUpload){
                        sendUploadFile();
                    } else if(moreToLink) {
                        sendLinkFile();
                    }
                    break;
            }
            return false;
        }
    };

    public UploadManager(String name, int priority, String serverUrl, String authToken, ContentResolver contentResolver) {
        super(name, priority);
        this.serverUrl = serverUrl;
        this.authToken = authToken;
        this.contentResolver = contentResolver;
    }

    @Override
    protected void onLooperPrepared() {
        workerHandler = new Handler(getLooper(), handlerCallback);
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public void notifySubmission(){
        sendUploadFile();
    }

    private boolean sendUploadFile(){
        return workerHandler.sendEmptyMessage(UPLOAD_FILE);
    }

    private boolean sendLinkFile(){
        return workerHandler.sendEmptyMessage(LINK_FILE);
    }

    private void uploadFile(){

        Cursor cursor = DatabaseProviderFascade.getNotUploadedSubmissions(contentResolver);

        if(cursor != null) {
            moreToUpload = cursor.getCount() > 1;

            int submissionId = cursor.getInt(cursor.getColumnIndex(Database.Submission.ID));
            String contentUriPath = cursor.getString(cursor.getColumnIndex(Database.Submission.CONTENT_URI));
            Uri contentUri = Uri.parse(contentUriPath);
            cursor.close();

            File file = new File(contentUri.getPath());

            try {
                //upload file to server
                String binaryUrl = serverUrl + "/" + ApiHandler.BINARY_RESSOURCE;
                int contentLength = (int) file.length();
                HttpRequest request = HttpRequest.post(binaryUrl).header(ApiHandler.HEADER_TOKEN, authToken).contentLength(contentLength).send(file);

                if (request.ok()) {

                    String response = request.body();
                    if (!response.trim().isEmpty()) {
                        JSONObject jsonObject = new JSONObject(response);
                        //save oid in local DB
                        long oid = jsonObject.getLong(ApiHandler.KEY_OID);
                        DatabaseProviderFascade.setSubmissionOID(submissionId, oid, contentResolver);
                    }
                }
            } catch (HttpRequest.HttpRequestException e){
                e.printStackTrace();
            } catch (JSONException e){
                e.printStackTrace();
            }
        } else {
            moreToUpload = false;
        }
    }

    private void linkFile(){
        //link oid an file on server
        Cursor cursor = DatabaseProviderFascade.getUnlinkedSubmissions(contentResolver);

        if(cursor != null){

            moreToLink = cursor.getCount() > 1;

            int submissionId = cursor.getInt(cursor.getColumnIndex(Database.Submission.ID));
            int challengeId = cursor.getInt(cursor.getColumnIndex(Database.Submission.CHALLENGE_ID));
            long oid = cursor.getLong(cursor.getColumnIndex(Database.Submission.OID));
            String filename = cursor.getString(cursor.getColumnIndex(Database.Submission.FILENAME));
            String mimetype = cursor.getString(cursor.getColumnIndex(Database.Submission.MIMETYPE));
            cursor.close();

            try {
                String linkUrl = serverUrl + "/" + ApiHandler.CHALLENGE_RESSOURCE + "/" + String.valueOf(challengeId) + "/" + ApiHandler.CHALLENGE_SUBMISSION_RESSOURCE;
                Log.i(TAG, linkUrl);
                JSONObject payloadObject = new JSONObject();
                payloadObject.put(ApiHandler.KEY_OID, oid);
                payloadObject.put(ApiHandler.KEY_FILENAME, filename);
                payloadObject.put(ApiHandler.KEY_MIMETYPE, mimetype);

                HttpRequest request = HttpRequest.post(linkUrl).header(ApiHandler.HEADER_TOKEN, authToken).send(payloadObject.toString());

                if (request.ok()) {
                    DatabaseProviderFascade.setSubmissionLinked(submissionId, contentResolver);
                }
            } catch (HttpRequest.HttpRequestException e){
                e.printStackTrace();
            } catch (JSONException e){
                e.printStackTrace();
            }
        } else {
            moreToLink = false;
        }
    }
}
