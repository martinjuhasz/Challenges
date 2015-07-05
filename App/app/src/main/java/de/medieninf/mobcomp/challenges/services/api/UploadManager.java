package de.medieninf.mobcomp.challenges.services.api;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.webkit.MimeTypeMap;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import de.medieninf.mobcomp.challenges.database.Database;
import de.medieninf.mobcomp.challenges.database.DatabaseProviderFascade;
import de.medieninf.mobcomp.challenges.external.HttpRequest;

public class UploadManager extends HandlerThread{

    final static String TAG = UploadManager.class.getSimpleName();

    private static final int UPLOAD_FILE = 0;
    private static final int LINK_FILE = 1;
    private static final int DOWNLOAD_FILE = 3;
    private static final String NAME = "UploadManager";

    private ContentResolver contentResolver;
    private String serverUrl;
    private String authToken;
    private File storageDir;

    private boolean moreToUpload;
    private boolean moreToLink;

    private Handler workerHandler;
    private Handler.Callback handlerCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            boolean handled = false;
            switch (msg.what){
                case UPLOAD_FILE:
                    uploadFile();
                    sendLinkFile();
                    handled = true;
                    break;
                case LINK_FILE:
                    linkFile();
                    if(moreToUpload){
                        sendUploadFile();
                    } else if(moreToLink) {
                        sendLinkFile();
                    }
                    handled = true;
                    break;
                case DOWNLOAD_FILE:
                    downloadFile();
                    break;
            }
            return handled;
        }
    };

    public UploadManager(int priority, String serverUrl, String authToken, File storageDir, ContentResolver contentResolver) {
        super(NAME, priority);
        this.serverUrl = serverUrl;
        this.authToken = authToken;
        this.contentResolver = contentResolver;
        this.storageDir = storageDir;
    }

    @Override
    protected void onLooperPrepared() {
        workerHandler = new Handler(getLooper(), handlerCallback);
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public void notifySubmissionUpload(){
        sendUploadFile();
    }

    public void notifySubmissionDownload(){
        sendDownloadFile();
    }

    private boolean sendUploadFile(){
        return workerHandler.sendEmptyMessage(UPLOAD_FILE);
    }

    private boolean sendLinkFile(){
        return workerHandler.sendEmptyMessage(LINK_FILE);
    }

    private boolean sendDownloadFile(){
        return workerHandler.sendEmptyMessage(DOWNLOAD_FILE);
    }

    private void downloadFile(){
        Cursor cursor = DatabaseProviderFascade.getNotDownloadedSubmissions(contentResolver);

        if(cursor != null){
            int submissionId = cursor.getInt(cursor.getColumnIndex(Database.Submission.ID));
            long oid = cursor.getLong(cursor.getColumnIndex(Database.Submission.OID));
            String mimetype = cursor.getString(cursor.getColumnIndex(Database.Submission.MIMETYPE));

            String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimetype);

            try {
                File file = File.createTempFile(
                        String.valueOf(oid)+"_",  /* prefix */
                        "."+extension,         /* suffix */
                        storageDir      /* directory */
                );

                String binaryUrl = serverUrl + "/" + ApiHandler.BINARY_RESSOURCE + "/" + oid;
                HttpRequest.get(binaryUrl).receive(file);

                Uri contentUri = Uri.fromFile(file);
                //Content uri in db sichern
                DatabaseProviderFascade.setSubmissionContentUri(submissionId, contentUri, contentResolver);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
                //TODO exponential backoff
                e.printStackTrace();
                moreToUpload = false;
            } catch (JSONException e){
                e.printStackTrace();
                moreToUpload = false;
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
                moreToLink = false;
            } catch (JSONException e){
                e.printStackTrace();
                moreToLink = false;
            }
        } else {
            moreToLink = false;
        }
    }
}
