package de.medieninf.mobcomp.challenges.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import de.medieninf.mobcomp.challenges.R;
import de.medieninf.mobcomp.challenges.services.api.ApiService;
import de.medieninf.mobcomp.challenges.services.api.ApiServiceCallback;

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

    // statics
    public static final String BROADCAST_USER_REGISTERED = "de.medieninf.mobcomp.challenges.broadcast.user_registered";
    public static final String BROADCAST_USER_REGISTERED_SUCCESSFULLY_EXTRA = "BROADCAST_USER_REGISTERED_SUCCESSFULLY_EXTRA";
    public static final String TOKEN_KEY = "constant_usertoken";

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
        this.apiService = new ApiService(getString(R.string.constant_server_url), this);
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
        /*
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
        */
        apiService.createUser(username, new ApiServiceCallback() {
            @Override
            public void requestFinished() {
                GameService.this.userToken = getUserTokenFromPreferences();

                Intent broadcastIntent = new Intent(BROADCAST_USER_REGISTERED);
                broadcastIntent.putExtra(BROADCAST_USER_REGISTERED_SUCCESSFULLY_EXTRA, true);
                LocalBroadcastManager.getInstance(GameService.this).sendBroadcast(broadcastIntent);
            }

            @Override
            public void requestFailed(ApiService.ErrorCode errorCode) {
                Log.i(TAG, "request failed: " + errorCode);

                Intent broadcastIntent = new Intent(BROADCAST_USER_REGISTERED);
                broadcastIntent.putExtra(BROADCAST_USER_REGISTERED_SUCCESSFULLY_EXTRA, false);
                LocalBroadcastManager.getInstance(GameService.this).sendBroadcast(broadcastIntent);
            }
        });
    }

    private String getUserTokenFromPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String prefUserToken = sharedPreferences.getString(TOKEN_KEY, null);
        return prefUserToken;
    }



}
