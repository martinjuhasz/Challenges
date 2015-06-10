package de.medieninf.mobcomp.challenges.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.medieninf.mobcomp.challenges.R;
import de.medieninf.mobcomp.challenges.services.api.ApiHandler;
import de.medieninf.mobcomp.challenges.services.api.ApiHandlerCallback;

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
    public static final String TOKEN_KEY = "constant_usertoken";

    // instance variables
    final static String TAG = GameService.class.getSimpleName();
    private IBinder binder;
    private String userToken;
    private ApiHandler apiHandler;
    private List<WeakReference<GameServiceListener>> listeners;

    @Override
    public void onCreate() {
        super.onCreate();

        this.binder = new GameServiceBinder();
        setUserTokenFromPreferences();
        this.apiHandler = new ApiHandler(getString(R.string.constant_server_url), this, this.userToken, getContentResolver());
        this.listeners = new ArrayList<>();
    }

    public void addListener(GameServiceListener listener) {
        // test if listener already in list
        for (Iterator<WeakReference<GameServiceListener>> iterator = this.listeners.iterator(); iterator.hasNext();) {
            WeakReference<GameServiceListener> weakRef = iterator.next();
            if (weakRef.get() == listener) {
                return;
            }
        }

        this.listeners.add(new WeakReference<GameServiceListener>(listener));
    }


    public void removeListener(GameServiceListener listener) {
        // test if listener is in list
        for (Iterator<WeakReference<GameServiceListener>> iterator = this.listeners.iterator(); iterator.hasNext();) {
            WeakReference<GameServiceListener> weakRef = iterator.next();
            // clean empty refs when already iterating
            if (weakRef.get() == null) {
                iterator.remove();
            }
            if (weakRef.get() == listener) {
                iterator.remove();
                return;
            }
        }
    }

    private void callListenerUserRegistrationUpdated(boolean successfully) {
        for (Iterator<WeakReference<GameServiceListener>> iterator = this.listeners.iterator(); iterator.hasNext();) {
            WeakReference<GameServiceListener> weakRef = iterator.next();
            // clean empty refs when already iterating
            if (weakRef.get() == null) {
                iterator.remove();
                continue;
            }
            weakRef.get().userRegistrationUpdated(successfully);
        }
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

        apiHandler.createUser(username, new ApiHandlerCallback() {
            @Override
            public void requestFinished() {
                setUserTokenFromPreferences();
                callListenerUserRegistrationUpdated(true);
            }

            @Override
            public void requestFailed(ApiHandler.ErrorCode errorCode) {
                Log.i(TAG, "request failed: " + errorCode);
                callListenerUserRegistrationUpdated(false);
            }
        });

    }

    public void updateGames() {
        apiHandler.getGames(new ApiHandlerCallback() {
            @Override
            public void requestFinished() {

            }

            @Override
            public void requestFailed(ApiHandler.ErrorCode errorCode) {

            }
        });
    }

    private void setUserTokenFromPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String prefUserToken = sharedPreferences.getString(TOKEN_KEY, null);
        if (prefUserToken != null) {
            this.userToken = prefUserToken;
            if (this.apiHandler != null) {
                this.apiHandler.setAuthToken(this.userToken);
            }
        }
    }



}
