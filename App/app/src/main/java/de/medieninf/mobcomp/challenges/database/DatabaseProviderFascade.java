package de.medieninf.mobcomp.challenges.database;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
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

        Uri existsUri = DatabaseProvider.CONTENT_URI.buildUpon().appendPath(DatabaseProvider.GAME_STRING).build();
        Cursor existsCursor = contentResolver.query(existsUri, new String[]{Database.Game.ID}, Database.Game.SERVER_ID + " = ?", new String[]{String.valueOf(server_id)}, null);
        existsCursor.moveToFirst();
        Uri savedUri = null;

        if(existsCursor.getCount() <= 0) {
            ContentValues values = new ContentValues();
            values.put(Database.Game.SERVER_ID, server_id);
            values.put(Database.Game.TITLE, title);
            values.put(Database.Game.ROUNDS, game_grounds);
            values.put(Database.Game.SUBMITTED, submitted);
            savedUri = contentResolver.insert(existsUri, values);
        } else {
            Log.i(TAG, "Game already exists, updating. Game: " + title);
            int updateID = existsCursor.getInt(existsCursor.getColumnIndex(Database.Game.ID));
            Uri updateUri = DatabaseProvider.CONTENT_URI.buildUpon().appendPath(DatabaseProvider.GAME_STRING).appendPath(String.valueOf(updateID)).build();
            ContentValues values = new ContentValues();
            values.put(Database.Game.TITLE, title);
            values.put(Database.Game.ROUNDS, game_grounds);
            values.put(Database.Game.SUBMITTED, submitted);
            int updated = contentResolver.update(updateUri, values, null, null);
            savedUri = updateUri;
        }

        return savedUri;
    }

    public static Uri saveOrUpdateUser(int server_id, String username, String image, ContentResolver contentResolver) {
        Uri existsUri = DatabaseProvider.CONTENT_URI.buildUpon().appendPath(DatabaseProvider.USER_STRING).build();
        Cursor existsCursor = contentResolver.query(existsUri, new String[]{Database.User.ID}, Database.User.SERVER_ID + " = ?", new String[]{String.valueOf(server_id)}, null);
        existsCursor.moveToFirst();
        Uri savedUri = null;

        if(existsCursor.getCount() <= 0) {
            ContentValues values = new ContentValues();
            values.put(Database.User.SERVER_ID, server_id);
            values.put(Database.User.USERNAME, username);
            values.put(Database.User.IMAGE, image);
            savedUri = contentResolver.insert(existsUri, values);
        } else {
            Log.i(TAG, "User already exists, updating. User: " + username);
            int updateID = existsCursor.getInt(existsCursor.getColumnIndex(Database.User.ID));
            Uri updateUri = DatabaseProvider.CONTENT_URI.buildUpon().appendPath(DatabaseProvider.USER_STRING).appendPath(String.valueOf(updateID)).build();
            ContentValues values = new ContentValues();
            values.put(Database.User.USERNAME, username);
            values.put(Database.User.IMAGE, image);
            int updated = contentResolver.update(updateUri, values, null, null);
            savedUri = updateUri;
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

    public static Uri saveOrUpdateChallenge(int server_id, int status, String hintText, String taskText, int type, int gameID, ContentResolver contentResolver) {

        Uri existsUri = DatabaseProvider.CONTENT_URI.buildUpon().appendPath(DatabaseProvider.CHALLENGE_STRING).build();
        Cursor existsCursor = contentResolver.query(existsUri, new String[]{Database.Challenge.ID}, Database.Challenge.SERVER_ID + " = ?", new String[]{String.valueOf(server_id)}, null);
        existsCursor.moveToFirst();
        Uri savedUri = null;

        if(existsCursor.getCount() <= 0) {
            ContentValues values = new ContentValues();
            values.put(Database.Challenge.SERVER_ID, server_id);
            values.put(Database.Challenge.STATUS, status);
            values.put(Database.Challenge.TEXT_HINT, hintText);
            values.put(Database.Challenge.TEXT_TASK, taskText);
            values.put(Database.Challenge.TYPE, type);
            values.put(Database.Challenge.GAME_ID, gameID);
            savedUri = contentResolver.insert(existsUri, values);
        } else {
            Log.i(TAG, "Challenge already exists, updating. Challenge: " + server_id);
            int updateID = existsCursor.getInt(existsCursor.getColumnIndex(Database.Challenge.ID));
            Uri updateUri = DatabaseProvider.CONTENT_URI.buildUpon().appendPath(DatabaseProvider.CHALLENGE_STRING).appendPath(String.valueOf(updateID)).build();
            ContentValues values = new ContentValues();
            values.put(Database.Challenge.STATUS, status);
            int updated = contentResolver.update(updateUri, values, null, null);
            savedUri = updateUri;
        }

        return savedUri;
    }
}
