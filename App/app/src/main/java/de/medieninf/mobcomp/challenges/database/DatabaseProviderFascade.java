package de.medieninf.mobcomp.challenges.database;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.util.Log;

/**
 * Created by Martin Juhasz on 10/06/15.
 */
public class DatabaseProviderFascade {

    final static String TAG = DatabaseProviderFascade.class.getSimpleName();

    public static Uri saveOrUpdateGame(int server_id, String title, int game_grounds, boolean submitted, ContentResolver contentResolver) {
        Uri savedUri = null;
        try {
            Uri scoreUri = DatabaseProvider.CONTENT_URI.buildUpon().appendPath(DatabaseProvider.GAME_STRING).build();
            ContentValues values = new ContentValues();
            values.put(Database.Game.SERVER_ID, server_id);
            values.put(Database.Game.TITLE, title);
            values.put(Database.Game.ROUNDS, game_grounds);
            values.put(Database.Game.SUBMITTED, submitted);
            savedUri = contentResolver.insert(scoreUri, values);
        }
        // Game already exists, so update it
        catch (SQLiteConstraintException e) {
            Log.i(TAG, "Game already exists, updating. Game: " + title);
            Uri scoreUri = DatabaseProvider.CONTENT_URI.buildUpon().appendPath(DatabaseProvider.GAME_STRING).appendPath(String.valueOf(server_id)).build();
            ContentValues values = new ContentValues();
            values.put(Database.Game.TITLE, title);
            values.put(Database.Game.ROUNDS, game_grounds);
            values.put(Database.Game.SUBMITTED, submitted);
            int updated = contentResolver.update(scoreUri, values, null, null);
            savedUri = scoreUri;
        }
        return savedUri;
    }

    public static Uri saveOrUpdateUser(int server_id, String username, String image, ContentResolver contentResolver) {
        Uri savedUri = null;
        try {
            Uri userUri = DatabaseProvider.CONTENT_URI.buildUpon().appendPath(DatabaseProvider.USER_STRING).build();
            ContentValues values = new ContentValues();
            values.put(Database.User.SERVER_ID, server_id);
            values.put(Database.User.USERNAME, username);
            values.put(Database.User.IMAGE, image);
            savedUri = contentResolver.insert(userUri, values);
        }
        // User already exists, so update it
        catch (SQLiteConstraintException e) {
            Log.i(TAG, "User already exists, updating. User: " + username);
            Uri userUri = DatabaseProvider.CONTENT_URI.buildUpon().appendPath(DatabaseProvider.USER_STRING).appendPath(String.valueOf(server_id)).build();
            ContentValues values = new ContentValues();
            values.put(Database.User.USERNAME, username);
            values.put(Database.User.IMAGE, image);
            int updated = contentResolver.update(userUri, values, null, null);
            savedUri = userUri;
        }
        return savedUri;
    }

    public static Uri addUserToGame(int user_id, int game_id, ContentResolver contentResolver) {
        Uri savedUri = null;
        try {
            Uri userUri = DatabaseProvider.CONTENT_URI.buildUpon().appendPath(DatabaseProvider.USERGAMES_STRING).build();
            ContentValues values = new ContentValues();
            values.put(Database.UserGames.USERNAME_ID, user_id);
            values.put(Database.UserGames.GAME_ID, game_id);
            savedUri = contentResolver.insert(userUri, values);
        }
        // Relation already exists
        catch (SQLiteConstraintException e) {
            Log.i(TAG, "relation already exists, skipping. User/Game: " + user_id + "/" + game_id);
        }
        return savedUri;
    }
}
