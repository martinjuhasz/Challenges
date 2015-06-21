package de.medieninf.mobcomp.challenges.services.api;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.medieninf.mobcomp.challenges.database.DatabaseProviderFascade;
import de.medieninf.mobcomp.challenges.external.HttpRequest;
import de.medieninf.mobcomp.challenges.services.GameService;
import de.medieninf.mobcomp.challenges.services.api.tasks.ApiHandlerAsyncTask;

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
    public static final String USER_RESSOURCE = "users";
    public static final String GAME_RESSOURCE = "games";
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
    public static final String KEY_STATUS = "status";
    public static final String KEY_TEXT_HINT = "text_hint";
    public static final String KEY_TEXT_TASK = "text_task";
    public static final String KEY_TYPE = "type";
    public static final String HEADER_TOKEN = "challenge_user_token";

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

        ApiHandlerAsyncTask asyncTask = new ApiHandlerAsyncTask(callback, this.authToken) {
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

                return HttpRequest.post(url).send(payloadObject.toString());
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

    public void getGames(final ApiHandlerCallback callback) {
        ApiHandlerAsyncTask asyncTask = new ApiHandlerAsyncTask(callback, this.authToken) {
            @Override
            protected HttpRequest onPrepareRequest() {
                String url = serverUrl + "/" + GAME_RESSOURCE;
                return HttpRequest.get(url);
            }

            @Override
            protected boolean onDataReceived(JSONObject returnObject) {
                try {
                    JSONArray games = returnObject.getJSONArray(KEY_DATA);
                    for (int i = 0; i < games.length(); ++i) {

                        // Add Game
                        JSONObject gameObject = games.getJSONObject(i);
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
                    }
                    return true;
                } catch (JSONException e) {
                    return false;
                }
            }
        };
        asyncTask.execute();
    }
}
