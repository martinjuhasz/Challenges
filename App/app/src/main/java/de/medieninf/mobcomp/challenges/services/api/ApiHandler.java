package de.medieninf.mobcomp.challenges.services.api;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.MimeTypeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

import de.medieninf.mobcomp.challenges.database.Database;
import de.medieninf.mobcomp.challenges.database.DatabaseProviderFascade;
import de.medieninf.mobcomp.challenges.external.HttpRequest;
import de.medieninf.mobcomp.challenges.services.GameService;
import de.medieninf.mobcomp.challenges.services.api.tasks.ApiHandlerAsyncTask;
import de.medieninf.mobcomp.challenges.services.api.tasks.SimpleAsyncTask;

/**
 * Created by Martin Juhasz on 07/06/15.
 */
public class ApiHandler {

    public enum ErrorCode {
        INVALID_PAYLOAD,
        FAILED_REQUEST,
        UNKNOWN

    }

    // API Constants
    public static final String FINDUSER_RESSOURCE = "users/find";
    public static final String USER_RESSOURCE = "users";
    public static final String GAME_RESSOURCE = "games";
    public static final String BINARY_RESSOURCE = "binary";
    public static final String CHALLENGE_RESSOURCE = "challenge";
    public static final String CHALLENGE_SUBMISSION_RESSOURCE = "submission";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_TOKEN = "token";
    public static final String KEY_USER_ID = "id";
    public static final String KEY_DATA = "data";
    public static final String KEY_ID = "id";
    public static final String KEY_TITLE = "title";
    public static final String KEY_GAME_ROUNDS = "game_rounds";
    public static final String KEY_USERS = "users";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_CURRENT_CHALLENGE = "current_challenge";
    public static final String KEY_SUBMISSIONS = "submissions";
    public static final String KEY_OID = "oid";
    public static final String KEY_STATUS = "status";
    public static final String KEY_FILENAME = "filename";
    public static final String KEY_MIMETYPE = "mimetype";
    public static final String KEY_TEXT_HINT = "text_hint";
    public static final String KEY_TEXT_TASK = "text_task";
    public static final String KEY_TYPE = "type";
    public static final String HEADER_TOKEN = "CHALLENGEUSERTOKEN";

    final static String TAG = ApiHandler.class.getSimpleName();
    private final String serverUrl;
    private final Context context;
    private String authToken;
    private ContentResolver contentResolver;


    public ApiHandler(String serverUrl, Context context, String authToken, ContentResolver contentResolver) {
        this.serverUrl = serverUrl;
        this.context = context;
        this.authToken = authToken;
        this.contentResolver = contentResolver;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public void createUser(final String username, final ApiHandlerCallback callback) {

        ApiHandlerAsyncTask asyncTask = new ApiHandlerAsyncTask(callback) {
            @Override
            protected HttpRequest onPrepareRequest() {
                // build url
                String url = serverUrl + "/" + USER_RESSOURCE;

                // build json payload
                JSONObject payloadObject = new JSONObject();
                try {
                    payloadObject.put(KEY_USERNAME, username);
                } catch (JSONException e) {
                    return null;
                }

                return HttpRequest.post(url).header(HEADER_TOKEN, ApiHandler.this.authToken).send(payloadObject.toString());
            }

            @Override
            protected boolean onDataReceived(JSONObject returnObject) {
                if (!returnObject.has(KEY_TOKEN) && !returnObject.has(KEY_USER_ID)) {
                    return false;
                }

                String userToken = null;
                int userId;
                try {
                    userToken = returnObject.getString(KEY_TOKEN);
                    userId = returnObject.getInt(KEY_USER_ID);
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                    sharedPreferences.edit()
                            .putString(GameService.TOKEN_KEY, userToken)
                            .putInt(GameService.USER_ID_KEY, userId)
                            .apply();
                    return true;
                } catch (JSONException e) {
                    return false;
                }
            }
        };
        asyncTask.execute();
    }

    public void createGame(final String title, final List<Integer> users, final ApiHandlerCallback callback) {

        ApiHandlerAsyncTask asyncTask = new ApiHandlerAsyncTask(callback) {
            @Override
            protected HttpRequest onPrepareRequest() {
                // build url
                String url = serverUrl + "/" + GAME_RESSOURCE;

                // build json payload
                JSONObject payloadObject = new JSONObject();
                try {
                    payloadObject.put(KEY_TITLE, title);
                    payloadObject.put(KEY_USERS, new JSONArray(users));
                } catch (JSONException e) {
                    return null;
                }

                return HttpRequest.post(url).header(HEADER_TOKEN, ApiHandler.this.authToken).send(payloadObject.toString());
            }

            @Override
            protected boolean onDataReceived(JSONObject returnObject) {
                try {
                    saveGame(returnObject);
                    return true;
                } catch (JSONException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        };
        asyncTask.execute();
    }

    public void userExists(final String username, final ApiHandlerCallback callback) {

        ApiHandlerAsyncTask asyncTask = new ApiHandlerAsyncTask(callback) {
            @Override
            protected HttpRequest onPrepareRequest() {
                // build url
                String url = serverUrl + "/" + FINDUSER_RESSOURCE;

                // build json payload
                JSONObject payloadObject = new JSONObject();
                try {
                    payloadObject.put(KEY_USERNAME, username);
                } catch (JSONException e) {
                    return null;
                }

                return HttpRequest.post(url).header(HEADER_TOKEN, ApiHandler.this.authToken).send(payloadObject.toString());
            }

            @Override
            protected boolean onDataReceived(JSONObject user) {
                Log.i(TAG, user.toString());
                try {
                    int user_server_id = user.getInt(KEY_ID);
                    String username = user.getString(KEY_USERNAME);
                    String image = user.getString(KEY_IMAGE);
                    Uri userUri = DatabaseProviderFascade.saveOrUpdateUser(user_server_id, username, image, ApiHandler.this.contentResolver);
                    return true;
                } catch (JSONException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        };
        asyncTask.execute();
    }

    public void getGames(final ApiHandlerCallback callback) {
        ApiHandlerAsyncTask asyncTask = new ApiHandlerAsyncTask(callback) {
            @Override
            protected HttpRequest onPrepareRequest() {
                String url = serverUrl + "/" + GAME_RESSOURCE;
                HttpRequest request = HttpRequest.get(url);
                request.header(HEADER_TOKEN, ApiHandler.this.authToken);
                return request;
            }

            @Override
            protected boolean onDataReceived(JSONObject returnObject) {
                try {
                    JSONArray games = returnObject.getJSONArray(KEY_DATA);
                    for (int i = 0; i < games.length(); ++i) {

                        // Add Game
                        JSONObject gameObject = games.getJSONObject(i);
                        saveGame(gameObject);
                    }
                    return true;
                } catch (JSONException e) {
                    return false;
                }
            }
        };
        asyncTask.execute();
    }

//    public void uploadBinary(final ApiHandlerCallback callback, int challengeId, final Uri location, final ContentResolver contentResolver){
//        ApiHandlerAsyncTask asyncTask = new ApiHandlerAsyncTask(callback) {
//            @Override
//            protected HttpRequest onPrepareRequest() {
//                String url = serverUrl + "/" + BINARY_RESSOURCE;
//
//                File file = new File(location.getPath());
//
//
//                String testPart = "{test:'test'}";
//                String mimeType = "image/jpg";
//                Log.i(TAG, "file size: " + file.length() + ", name size: " + file.getName().getBytes().length);
//                long contentLength = file.length();
//                contentLength += file.getName().getBytes().length;
//                contentLength += mimeType.getBytes().length;
//                contentLength += 128;
//                return HttpRequest.post(url).header(HEADER_TOKEN, ApiHandler.this.authToken).contentLength((int) contentLength).part("file", file.getName(), "image/jpg", file);
//            }
//
//            @Override
//            protected boolean onDataReceived(JSONObject returnObject) {
//                return super.onDataReceived(returnObject);
//            }
//        };
//        asyncTask.execute();
//    }

    public void uploadBinary(final ApiHandlerCallback callback, final int challengeId, final int userId, final Uri location, final ContentResolver contentResolver){
        SimpleAsyncTask asyncTask = new SimpleAsyncTask(callback){
            @Override
            protected void doInBackground() throws ApiHandlerException, JSONException {

                //save submission in local DB
                File file = new File(location.getPath());
                String filename = file.getName();
                String extension = filename.substring(filename.lastIndexOf('.') + 1);
                String mimetype = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

                Uri submissionUri = DatabaseProviderFascade.saveSubmission(challengeId, userId, location, filename, mimetype, contentResolver);

                uploadFile();
                linkFile();
            }
        };
        asyncTask.execute();
    }

    private void uploadFile() throws ApiHandlerException, JSONException {
        Cursor cursor = DatabaseProviderFascade.getNotUploadedSubmissions(contentResolver);

        if(cursor != null) {
            int submissionId = cursor.getInt(cursor.getColumnIndex(Database.Submission.ID));
            String contentUriPath = cursor.getString(cursor.getColumnIndex(Database.Submission.CONTENT_URI));
            Uri contentUri = Uri.parse(contentUriPath);
            cursor.close();

            File file = new File(contentUri.getPath());
            String filename = file.getName();
            String extension = filename.substring(filename.lastIndexOf('.') + 1);
            String mimetype = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);


            //upload file to server
            String binaryUrl = serverUrl + "/" + BINARY_RESSOURCE;
            int contentLength = (int) file.length();
            HttpRequest request = HttpRequest.post(binaryUrl).header(HEADER_TOKEN, ApiHandler.this.authToken).contentLength(contentLength).send(file);
            JSONObject jsonObject = requestJSON(request);

            //link oid and file
            if (jsonObject != null) {
                //save oid in local DB
                long oid = jsonObject.getLong(KEY_OID);
                DatabaseProviderFascade.setSubmissionOID(submissionId, oid, contentResolver);
            }
        }
    }

    private void linkFile() throws JSONException, ApiHandlerException {
        //link oid an file on server
        Cursor cursor = DatabaseProviderFascade.getUnlinkedSubmissions(contentResolver);

        if(cursor != null){
            int challengeId = cursor.getInt(cursor.getColumnIndex(Database.Submission.CHALLENGE_ID));
            long oid = cursor.getLong(cursor.getColumnIndex(Database.Submission.OID));
            String filename = cursor.getString(cursor.getColumnIndex(Database.Submission.FILENAME));
            String mimetype = cursor.getString(cursor.getColumnIndex(Database.Submission.MIMETYPE));
            cursor.close();

            String linkUrl = serverUrl + "/" + CHALLENGE_RESSOURCE + "/" + String.valueOf(challengeId) + "/" + CHALLENGE_SUBMISSION_RESSOURCE;
            Log.i(TAG, linkUrl);
            JSONObject payloadObject = new JSONObject();
            payloadObject.put(KEY_OID, oid);
            payloadObject.put(KEY_FILENAME, filename);
            payloadObject.put(KEY_MIMETYPE, mimetype);
            HttpRequest request = HttpRequest.post(linkUrl).header(HEADER_TOKEN, ApiHandler.this.authToken).send(payloadObject.toString());
            requestJSON(request);
        }
    }

    private void saveGame(JSONObject gameObject) throws JSONException {
        int server_id = gameObject.getInt(KEY_ID);
        String title = gameObject.getString(KEY_TITLE);
        int game_rounds = gameObject.getInt(KEY_GAME_ROUNDS);
        Uri gameUri = DatabaseProviderFascade.saveOrUpdateGame(server_id, title, game_rounds, true, ApiHandler.this.contentResolver);
        int gameId = Integer.valueOf(gameUri.getLastPathSegment());

        // Add Users
        JSONArray users = gameObject.getJSONArray(KEY_USERS);
        for (int j = 0; j < users.length(); ++j) {
            JSONObject user = users.getJSONObject(j);
            int user_server_id = user.getInt(KEY_ID);
            String username = user.getString(KEY_USERNAME);
            String image = user.getString(KEY_IMAGE);
            Uri userUri = DatabaseProviderFascade.saveOrUpdateUser(user_server_id, username, image, ApiHandler.this.contentResolver);
            int userId = Integer.valueOf(userUri.getLastPathSegment());
            DatabaseProviderFascade.addUserToGame(userId, gameId, ApiHandler.this.contentResolver);
        }

        // Add Challenge
        JSONObject challenge = gameObject.getJSONObject(KEY_CURRENT_CHALLENGE);
        if (challenge != null) {
            int challenge_server_id = challenge.getInt(KEY_ID);
            int status = challenge.getInt(KEY_STATUS);
            String hintText = challenge.getString(KEY_TEXT_HINT);
            String taskText = challenge.getString(KEY_TEXT_TASK);
            int type = challenge.getInt(KEY_TYPE);
            Uri challengeUri = DatabaseProviderFascade.saveOrUpdateChallenge(challenge_server_id, status, hintText, taskText, type, gameId, ApiHandler.this.contentResolver);
            int challengeId = Integer.valueOf(challengeUri.getLastPathSegment());
            DatabaseProviderFascade.setCurrentChallengeToGame(challengeId, gameId, ApiHandler.this.contentResolver);
        }

        // Add Submissions
//        JSONArray submissions = gameObject.getJSONArray(KEY_SUBMISSIONS);
//        JSONObject submission;
//        int oid;
//        String filename;
//        String mimetype;
//
//        for (int i = 0; i < submissions.length(); i++){
//            submission = submissions.getJSONObject(i);
//
//        }

//        "binsize": 126485,
//                "filename": "IMG_20150629_143356_1658813437.jpg",
//                "mimetype": "image/jpg",
//                "oid": 17738,
//                "user_id": 2
    }

    private JSONObject requestJSON(HttpRequest request) throws ApiHandlerException{
        try {
            if (request != null && request.ok()) {

                // extract response json object if needed
                String response = request.body();
                if (response.trim().isEmpty()) {
                    return null;
                }

                JSONObject jsonObject = new JSONObject(response);

                ErrorCode errorCode = getErrorCode(jsonObject);

                if(errorCode != null){
                    throw new ApiHandlerException(errorCode);
                }


                return jsonObject;
            } else {
                throw new ApiHandlerException(ErrorCode.FAILED_REQUEST);
            }

        } catch (HttpRequest.HttpRequestException exception) {
            exception.printStackTrace();
            throw new ApiHandlerException(ErrorCode.FAILED_REQUEST);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private ErrorCode getErrorCode(JSONObject jsonObject) {
        // response body is empty or not an jsonobject
        if (jsonObject == null) {
            return null;
        }

        if(!jsonObject.has("error_code")) {
            return null;
        }

        try {
            int errorCode = jsonObject.getInt("error_code");
            // TODO: implement conversion from web error codes into enum values
            return null;
        } catch (JSONException e) {
            return null;
        }
    }
}
