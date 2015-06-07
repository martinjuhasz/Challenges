package de.medieninf.mobcomp.challenges.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import de.medieninf.mobcomp.challenges.R;
import de.medieninf.mobcomp.challenges.services.api.ApiService;
import de.medieninf.mobcomp.challenges.services.api.ApiServiceCallback;
import de.medieninf.mobcomp.challenges.services.api.tasks.SubmitApiRequestTask;

/**
 * Created by Martin Juhasz on 06/06/15.
 */
public class GameService extends Service {

    // inner classes
    public class GameServiceBinder extends Binder {
        public GameService getService() {
            return GameService.this;
        }
    }

    // broadcasts
    public static final String BROADCAST_USER_REGISTERED = "de.medieninf.mobcomp.challenges.broadcast.user_registered";
    public static final String BROADCAST_USER_REGISTERED_SUCCESSFULLY_EXTRA = "BROADCAST_USER_REGISTERED_SUCCESSFULLY_EXTRA";

    // instance variables
    final static String TAG = GameService.class.getSimpleName();
    private IBinder binder;
    private String userToken;
    private ApiService apiService;

    @Override
    public void onCreate() {
        super.onCreate();

        this.binder = new GameServiceBinder();
        this.userToken = getUserTokenFromPreferences();
        this.apiService = new ApiService(getString(R.string.constant_server_url));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return this.binder;
    }

    public boolean isUserLoggedIn() {
        return this.userToken != null;
    }

    public String getUserToken() {
        return userToken;
    }

    public void submitUserRegistration(String username) {

        apiService.createUser(username, new ApiServiceCallback<JSONObject>() {
            @Override
            public void requestFinished(JSONObject returnBody, ApiService.ErrorCode errorCode) {
                Intent broadcastIntent = new Intent(BROADCAST_USER_REGISTERED);

                if (errorCode != null) {
                    Log.d(TAG, "Request failed. Error Code: " + errorCode);
                    broadcastIntent.putExtra(BROADCAST_USER_REGISTERED_SUCCESSFULLY_EXTRA, false);
                    LocalBroadcastManager.getInstance(GameService.this).sendBroadcast(broadcastIntent);
                    return;
                }

                boolean saveSuccessful = setUserTokenFromJson(returnBody);
                broadcastIntent.putExtra(BROADCAST_USER_REGISTERED_SUCCESSFULLY_EXTRA, saveSuccessful);
                LocalBroadcastManager.getInstance(GameService.this).sendBroadcast(broadcastIntent);
            }
        });
    }

    private String getUserTokenFromPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String prefUserToken = sharedPreferences.getString(getString(R.string.constant_usertoken), null);
        return prefUserToken;
    }

    private boolean setUserTokenFromJson(JSONObject jsonObject) {
        if (jsonObject == null) {
            return false;
        }

        if (!jsonObject.has("token")) {
            return false;
        }

        String userToken = null;
        try {
            userToken = jsonObject.getString("token");
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            sharedPreferences.edit().putString(getString(R.string.constant_usertoken), userToken).apply();
            this.userToken = userToken;
            return true;
        } catch (JSONException e) {
            return false;
        }
    }



}
