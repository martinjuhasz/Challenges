package de.medieninf.mobcomp.challenges.services;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.medieninf.mobcomp.challenges.database.Database;
import de.medieninf.mobcomp.challenges.database.DatabaseProvider;
import de.medieninf.mobcomp.challenges.services.api.ApiHandler;
import de.medieninf.mobcomp.challenges.services.api.ApiHandlerCallback;

/**
 * Created by Martin Juhasz on 27/06/15.
 */
public class GameController {

    final static String TAG = GameController.class.getSimpleName();
    private final ApiHandler apiHandler;
    private final GameService gameService;
    private final ContentResolver contentResolver;

    private List<Integer> newGameUsers;

    public GameController(ApiHandler apiHandler, GameService gameService, ContentResolver contentResolver) {
        this.apiHandler = apiHandler;
        this.gameService = gameService;
        this.contentResolver = contentResolver;
        this.newGameUsers = new ArrayList<>();
    }

    public List<Integer> getNewGameUsers() {
        return newGameUsers;
    }

    public void addUserToNewGame(final String username, final GameControllerCallback callback) {
        apiHandler.userExists(username, new ApiHandlerCallback() {
            @Override
            public void requestFinished() {
                addUserFromDatabase(username);
                callback.userAdded(true);
            }

            @Override
            public void requestFailed(ApiHandler.ErrorCode errorCode) {
                Log.i(TAG, "request failed: " + errorCode);
                callback.userAdded(false);
            }
        });
    }

    public void startNewGame(String title, final GameControllerCallback callback) {
        apiHandler.createGame(title, this.newGameUsers, new ApiHandlerCallback(){
            @Override
            public void requestFinished() {
                GameController.this.newGameUsers = new ArrayList<>();
                callback.gameCreated(true);
            }

            @Override
            public void requestFailed(ApiHandler.ErrorCode errorCode) {
                callback.gameCreated(false);
            }
        });
    }

    private void addUserFromDatabase(String username) {
        Uri userUri = DatabaseProvider.CONTENT_URI.buildUpon().appendPath(DatabaseProvider.USER_STRING).build();
        ContentValues values = new ContentValues();
        String[] projection = {Database.User.ID, Database.User.SERVER_ID};
        String selection = Database.User.USERNAME + " = ?";
        String[] selectionArgs= {username};


        Cursor userCursor = contentResolver.query(userUri, projection, selection, selectionArgs, null);
        userCursor.moveToFirst();

        int userid = userCursor.getInt(userCursor.getColumnIndex(Database.User.SERVER_ID));

        if (!newGameUsers.contains(userid)) {
            newGameUsers.add(new Integer(userid));
        }
    }
}
