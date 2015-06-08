package de.medieninf.mobcomp.challenges.services.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import de.medieninf.mobcomp.challenges.external.HttpRequest;
import de.medieninf.mobcomp.challenges.services.GameService;
import de.medieninf.mobcomp.challenges.services.api.tasks.ApiServiceAsyncTask;

/**
 * Created by Martin Juhasz on 07/06/15.
 */
public class ApiService {

    public enum ErrorCode {
        INVALID_PAYLOAD,
        FAILED_REQUEST,
        UNKNOWN

    }

    private final String serverUrl;
    private static final String USER_RESSOURCE = "users";
    private final Context context;


    public ApiService(String serverUrl, Context context) {
        this.serverUrl = serverUrl;
        this.context = context;
    }

    public void createUser(final String username, final ApiServiceCallback callback) {

        ApiServiceAsyncTask asyncTask = new ApiServiceAsyncTask(callback) {
            @Override
            protected HttpRequest onPrepareRequest() {
                // build url
                String url = serverUrl + "/" + USER_RESSOURCE;

                // build json payload
                JSONObject payloadObject = new JSONObject();
                try {
                    payloadObject.put("username", username);
                } catch (JSONException e) {
                    return null;
                }

                return HttpRequest.post(url).send(payloadObject.toString());
            }

            @Override
            protected boolean onDataReceived(JSONObject returnObject) {
                if (!returnObject.has("token")) {
                    return false;
                }

                String userToken = null;
                try {
                    userToken = returnObject.getString("token");
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
}
