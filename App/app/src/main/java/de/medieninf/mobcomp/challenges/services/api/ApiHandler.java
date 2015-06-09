package de.medieninf.mobcomp.challenges.services.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

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
    public static final String HEADER_TOKEN = "challenge_user_token";

    final static String TAG = ApiHandler.class.getSimpleName();
    private final String serverUrl;
    private final Context context;
    private String authToken;


    public ApiHandler(String serverUrl, Context context, String authToken) {
        this.serverUrl = serverUrl;
        this.context = context;
        this.authToken = authToken;
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
                if (!returnObject.has(KEY_TOKEN)) {
                    return false;
                }

                String userToken = null;
                try {
                    userToken = returnObject.getString(KEY_TOKEN);
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                    sharedPreferences.edit().putString(GameService.TOKEN_KEY, userToken).apply();
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
                    String test = returnObject.getString("test");
                    Log.i(TAG, test);
                    return true;
                } catch (JSONException e) {
                    return false;
                }
            }
        };
        asyncTask.execute();
    }
}