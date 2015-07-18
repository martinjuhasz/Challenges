package de.medieninf.mobcomp.challenges.services;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.*;
import android.os.Process;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.medieninf.mobcomp.challenges.R;
import de.medieninf.mobcomp.challenges.activities.PhotoChallengeActivity;
import de.medieninf.mobcomp.challenges.activities.RateActivity;
import de.medieninf.mobcomp.challenges.activities.WaitingActivity;
import de.medieninf.mobcomp.challenges.database.Database;
import de.medieninf.mobcomp.challenges.database.DatabaseProviderFascade;
import de.medieninf.mobcomp.challenges.services.api.ApiHandler;
import de.medieninf.mobcomp.challenges.services.api.ApiHandlerCallback;
import de.medieninf.mobcomp.challenges.services.api.UploadManager;

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
    private static final int STATUS_PLAYING = 1;
    private static final int STATUS_RATING = 2;
    private static final int STATUS_FINISHED = 3;

    // instance variables
    final static String TAG = GameService.class.getSimpleName();
    private IBinder binder;
    private String userToken;
    private int userId;
    private UploadManager uploadManager;
    private ApiHandler apiHandler;
    private ContentResolver contentResolver;
    private List<WeakReference<GameServiceListener>> listeners;

    @Override
    public void onCreate() {
        super.onCreate();

        this.binder = new GameServiceBinder();
        this.contentResolver = getContentResolver();
        String serverUrl = getString(R.string.constant_server_url);

        setUserTokenFromPreferences();
        setUserIdFromPreferences();

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        this.uploadManager = new UploadManager(Process.THREAD_PRIORITY_BACKGROUND, serverUrl, this.userToken, storageDir, this.contentResolver);
        this.apiHandler = new ApiHandler(serverUrl, this, this.userToken, this.userId, uploadManager, this.contentResolver);
        this.listeners = new ArrayList<>();

        this.gameController = new GameController(this.apiHandler, this, this.contentResolver);

        uploadManager.start();
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

    private void callListenerRatingSent(boolean successfully) {
        for (Iterator<WeakReference<GameServiceListener>> iterator = this.listeners.iterator(); iterator.hasNext(); ) {
            WeakReference<GameServiceListener> weakRef = iterator.next();
            // clean empty refs when already iterating
            if (weakRef.get() == null) {
                iterator.remove();
                continue;
            }
            weakRef.get().ratingSent(successfully);
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

    @Override
    public void onDestroy() {
        uploadManager.quit();
    }

    public Intent getIntentForChallengeActivity(int gameID) {
        Cursor challengeCursor = DatabaseProviderFascade.getChallengeForGame(gameID, this.contentResolver);

        if (challengeCursor == null) {
            return null;
        }

        int challengeID = challengeCursor.getInt(challengeCursor.getColumnIndex(Database.Challenge.ID));
        int challengeStatus = challengeCursor.getInt(challengeCursor.getColumnIndex(Database.Challenge.STATUS));
        int challengeType = challengeCursor.getInt(challengeCursor.getColumnIndex(Database.Challenge.TYPE));
        challengeCursor.close();

        Cursor submissionCursor = DatabaseProviderFascade.getSubmissionForChallenge(challengeID, this.contentResolver);
        if(submissionCursor != null) {
            submissionCursor.close();
        }

        switch (challengeStatus){
            case STATUS_PLAYING:
                if(submissionCursor != null){
                    Intent waitIntent = new Intent(this, WaitingActivity.class);
                    return waitIntent;
                }else{
                    // switch challenge type
                    switch (challengeType) {
                        case 1:
                            Intent challengeIntent = new Intent(this, PhotoChallengeActivity.class);
                            challengeIntent.putExtra(EXTRA_KEY_CHALLENGE_ID, challengeID);
                            return challengeIntent;
                        default:
                            throw new RuntimeException("invalid challenge type");
                    }
                }
            case STATUS_RATING:
                Intent rateIntent = new Intent(this, RateActivity.class);
                rateIntent.putExtra(EXTRA_KEY_CHALLENGE_ID, challengeID);
                return rateIntent;
            case STATUS_FINISHED:
                break;
            default:
                throw new RuntimeException("invalid Challenge Status type");
        }
        return null;
    }

    public boolean isUserLoggedIn() {
        return this.userToken != null;
    }

    public String getUserToken() {
        return userToken;
    }

    public int getUserId() {
        return userId;
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
        apiHandler.uploadBinary(new ApiHandlerCallback() {
            @Override
            public void requestFinished() {
                Log.i(TAG, "save submission request finished: ");
            }

            @Override
            public void requestFailed(ApiHandler.ErrorCode errorCode) {
                Log.i(TAG, "save submission request failed: " + errorCode);
            }
        }, challengeId, this.userId, location);
    }

    public void submitChallengeRating(int challengeId) {
        apiHandler.submitChallengeRating(challengeId, this.userId, new ApiHandlerCallback() {
            @Override
            public void requestFinished() {
                callListenerRatingSent(true);
            }

            @Override
            public void requestFailed(ApiHandler.ErrorCode errorCode) {
                callListenerRatingSent(false);
            }
        });
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
            if(this.uploadManager != null) {
                this.uploadManager.setAuthToken(this.userToken);
            }
            if (this.apiHandler != null) {
                this.apiHandler.setAuthToken(this.userToken);
            }
        }
    }

    private void setUserIdFromPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        this.userId = sharedPreferences.getInt(USER_ID_KEY, -1);
        if (this.apiHandler != null) {
            this.apiHandler.setUserID(this.userId);
        }
    }

}