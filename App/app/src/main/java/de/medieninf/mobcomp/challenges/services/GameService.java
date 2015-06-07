package de.medieninf.mobcomp.challenges.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;

import org.json.JSONException;
import org.json.JSONObject;

import de.medieninf.mobcomp.challenges.R;

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
    private IBinder binder;
    private String userToken;

    @Override
    public void onCreate() {
        super.onCreate();

        this.binder = new GameServiceBinder();
        this.userToken = getUserTokenFromPreferences();
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
        // TODO: do webrequest async and broadcast result
        // sending failed broadcast here for example
        String url = getString(R.string.constant_server_url) + "/users";

        SubmitUserTask submitUserTask = new SubmitUserTask(username, new WebRequestListener() {
            @Override
            public void requestFinished(JSONObject jsonObject) {
                boolean saveSuccessful = setUserTokenFromJson(jsonObject);
                Intent broadcastIntent = new Intent(BROADCAST_USER_REGISTERED);
                broadcastIntent.putExtra(BROADCAST_USER_REGISTERED_SUCCESSFULLY_EXTRA, saveSuccessful);
                LocalBroadcastManager.getInstance(GameService.this).sendBroadcast(broadcastIntent);
            }
        });
        submitUserTask.execute(url);
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
        try {
            String userToken = jsonObject.getString("token");
            if (userToken.trim().isEmpty()) {
                return false;
            }

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            sharedPreferences.edit().putString(getString(R.string.constant_usertoken), userToken).apply();
            this.userToken = userToken;
            return true;
        } catch (JSONException e) {
            return false;
        }
    }



}
