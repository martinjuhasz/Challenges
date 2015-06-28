package de.medieninf.mobcomp.challenges.services;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
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
import de.medieninf.mobcomp.challenges.activities.PhotoChallengeActivity;
import de.medieninf.mobcomp.challenges.database.Database;
import de.medieninf.mobcomp.challenges.database.DatabaseProviderFascade;
import de.medieninf.mobcomp.challenges.services.api.ApiHandler;
import de.medieninf.mobcomp.challenges.services.api.ApiHandlerCallback;

/**
 * Created by Martin Juhasz on 06/06/15.
 */
public class GameService extends Service {

    private GameController gameController;


    // inner classes
    public class GameServiceBinder extends Binder {
        public GameService getService() {
            return GameService.this;
        }
    }

    // statics
    public static final String TOKEN_KEY = "constant_usertoken";
    public static final String USER_ID_KEY = "constant_user_id";
    public static final String EXTRA_KEY_CHALLENGE_ID = "EXTRA_KEY_CHALLENGE_ID";

    // instance variables
    final static String TAG = GameService.class.getSimpleName();
    private IBinder binder;
    private String userToken;
    private int userId;
    private ApiHandler apiHandler;
    private ContentResolver contentResolver;
    private List<WeakReference<GameServiceListener>> listeners;

    @Override
    public void onCreate() {
        super.onCreate();

        this.binder = new GameServiceBinder();
        this.contentResolver = getContentResolver();
        this.apiHandler = new ApiHandler(getString(R.string.constant_server_url), this, this.userToken, this.contentResolver);
        this.listeners = new ArrayList<>();

        this.gameController = new GameController(this.apiHandler, this, this.contentResolver);

        setUserTokenFromPreferences();
        setUserIdFromPreferences();
    }

    public GameController getGameController() {
        return gameController;
    }

    public void addListener(GameServiceListener listener) {
        // test if listener already in list
        for (Iterator<WeakReference<GameServiceListener>> iterator = this.listeners.iterator(); iterator.hasNext(); ) {
            WeakReference<GameServiceListener> weakRef = iterator.next();
            if (weakRef.get() == listener) {
                return;
            }
        }

        this.listeners.add(new WeakReference<GameServiceListener>(listener));
    }


    public void removeListener(GameServiceListener listener) {
        // test if listener is in list
        for (Iterator<WeakReference<GameServiceListener>> iterator = this.listeners.iterator(); iterator.hasNext(); ) {
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
        for (Iterator<WeakReference<GameServiceListener>> iterator = this.listeners.iterator(); iterator.hasNext(); ) {
            WeakReference<GameServiceListener> weakRef = iterator.next();
            // clean empty refs when already iterating
            if (weakRef.get() == null) {
                iterator.remove();
                continue;
            }
            weakRef.get().userRegistrationUpdated(successfully);
        }
    }

    private void callListenerGamesUpdated(boolean successfully) {
        for (Iterator<WeakReference<GameServiceListener>> iterator = this.listeners.iterator(); iterator.hasNext(); ) {
            WeakReference<GameServiceListener> weakRef = iterator.next();
            // clean empty refs when already iterating
            if (weakRef.get() == null) {
                iterator.remove();
                continue;
            }
            weakRef.get().gamesUpdated(successfully);
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return this.binder;
    }

    public Intent getIntentForChallengeActivity(int gameID) {
        Cursor challengeCursor = DatabaseProviderFascade.getChallengeForGame(gameID, this.contentResolver);

        if (challengeCursor == null) {
            return null;
        }

        int challengeID = challengeCursor.getInt(challengeCursor.getColumnIndex(Database.Challenge.ID));

        // TODO: implement Submissions

        // switch challenge type
        int challengeType = challengeCursor.getInt(challengeCursor.getColumnIndex(Database.Challenge.TYPE));
        challengeCursor.close();
        Intent challengeIntent = null;
        switch (challengeType) {
            case 1:
                challengeIntent = new Intent(this, PhotoChallengeActivity.class);
                break;
            default:
                throw new RuntimeException("invalid challenge type");
        }


        challengeIntent.putExtra(EXTRA_KEY_CHALLENGE_ID, challengeID);
        return challengeIntent;
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
                setUserIdFromPreferences();
                callListenerUserRegistrationUpdated(true);
            }

            @Override
            public void requestFailed(ApiHandler.ErrorCode errorCode) {
                Log.i(TAG, "request failed: " + errorCode);
                callListenerUserRegistrationUpdated(false);
            }
        });
    }

    public void saveChallengeSubmission(int challengeId, Uri location) {
        DatabaseProviderFascade.saveSubmission(challengeId, this.userId, location, this.contentResolver);

        apiHandler.uploadBinary(new ApiHandlerCallback() {
            @Override
            public void requestFinished() {
                Log.i(TAG, "save submission request finished: ");
            }

            @Override
            public void requestFailed(ApiHandler.ErrorCode errorCode) {
                Log.i(TAG, "save submission request failed: " + errorCode);
            }
        }, challengeId, location, getContentResolver());
    }

    public void updateGames() {
        apiHandler.getGames(new ApiHandlerCallback() {
            @Override
            public void requestFinished() {
                callListenerGamesUpdated(true);
            }

            @Override
            public void requestFailed(ApiHandler.ErrorCode errorCode) {
                Log.i(TAG, "request failed: " + errorCode);
                callListenerGamesUpdated(false);
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

    private void setUserIdFromPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        this.userId = sharedPreferences.getInt(USER_ID_KEY, 0);
    }
}