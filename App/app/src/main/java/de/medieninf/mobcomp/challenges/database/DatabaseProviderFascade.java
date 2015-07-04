package de.medieninf.mobcomp.challenges.database;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
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
        existsCursor.close();

        return savedUri;
    }

    public static Uri setCurrentChallengeToGame(int challenge_id, int game_id, ContentResolver contentResolver) {
        // check if game exists
        Uri gameExistsUri = DatabaseProvider.CONTENT_URI.buildUpon().appendPath(DatabaseProvider.GAME_STRING).build();
        Cursor gameExistsCursor = contentResolver.query(gameExistsUri, new String[]{Database.Game.ID}, Database.Game.ID + " = ?", new String[]{String.valueOf(game_id)}, null);
        gameExistsCursor.moveToFirst();
        if(gameExistsCursor.getCount() <= 0) {
            gameExistsCursor.close();
            return null;
        }
        gameExistsCursor.close();

        // check if challenge exists
        Uri challengeExistsUri = DatabaseProvider.CONTENT_URI.buildUpon().appendPath(DatabaseProvider.CHALLENGE_STRING).build();
        Cursor challengeExistsCursor = contentResolver.query(challengeExistsUri, new String[]{Database.Challenge.ID}, Database.Challenge.ID + " = ?", new String[]{String.valueOf(challenge_id)}, null);
        challengeExistsCursor.moveToFirst();
        if(challengeExistsCursor.getCount() <= 0) {
            challengeExistsCursor.close();
            return null;
        }
        challengeExistsCursor.close();

        Uri updateUri = DatabaseProvider.CONTENT_URI.buildUpon().appendPath(DatabaseProvider.GAME_STRING).appendPath(String.valueOf(game_id)).build();
        ContentValues values = new ContentValues();
        values.put(Database.Game.CURRENT_CHALLENGE_ID, challenge_id);
        int updated = contentResolver.update(updateUri, values, null, null);
        return updateUri;
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
        existsCursor.close();

        return savedUri;
    }


    public static Uri saveSubmission(int challengeId, int userId, Uri contentUri, String filename, String mimetype, ContentResolver contentResolver) {
        Uri submissionUri = DatabaseProvider.CONTENT_URI.buildUpon().appendPath(DatabaseProvider.SUBMISSION_STRING).build();
        Uri savedUri = null;
        ContentValues values = new ContentValues();
        values.put(Database.Submission.CHALLENGE_ID, challengeId);
        values.put(Database.Submission.USER_ID, userId);
        values.put(Database.Submission.CONTENT_URI, contentUri.toString());
        values.put(Database.Submission.FILENAME, filename);
        values.put(Database.Submission.MIMETYPE, mimetype);
        savedUri = contentResolver.insert(submissionUri, values);
        return savedUri;
    }

    public static int setSubmissionLinked(int submissionId, ContentResolver contentResolver){
        Uri submissionUri = DatabaseProvider.CONTENT_URI.buildUpon().appendEncodedPath(DatabaseProvider.SUBMISSION_STRING).appendPath(String.valueOf(submissionId)).build();
        ContentValues values = new ContentValues();
        values.put(Database.Submission.LINKED, 1);
        return contentResolver.update(submissionUri, values, null, null);
    }

    public static int setSubmissionOID(int submissionId, long oid, ContentResolver contentResolver) {
        Uri submissionUri = DatabaseProvider.CONTENT_URI.buildUpon().appendEncodedPath(DatabaseProvider.SUBMISSION_STRING).appendPath(String.valueOf(submissionId)).build();
        ContentValues values = new ContentValues();
        values.put(Database.Submission.OID, oid);
        return contentResolver.update(submissionUri, values, null, null);
    }

    public static Cursor getNotUploadedSubmissions(ContentResolver contentResolver){
        Uri submissionUris = DatabaseProvider.CONTENT_URI.buildUpon().appendEncodedPath(DatabaseProvider.SUBMISSION_STRING).build();
        String selection = Database.Submission.OID + " is null";
        Cursor submissionCursor = contentResolver.query(submissionUris, new String[]{Database.Submission.ID, Database.Submission.CONTENT_URI}, selection, null, null);
        submissionCursor.moveToFirst();
        if(submissionCursor.getCount() <= 0){
            return null;
        }

        return submissionCursor;
    }

    public static Cursor getUnlinkedSubmissions(ContentResolver contentResolver){
        Uri submissionUris = DatabaseProvider.CONTENT_URI.buildUpon().appendEncodedPath(DatabaseProvider.SUBMISSION_STRING).build();
        String selection = Database.Submission.LINKED + " = 0";
        Cursor submissionCursor = contentResolver.query(submissionUris, new String[]{Database.Submission.CHALLENGE_ID, Database.Submission.OID, Database.Submission.FILENAME, Database.Submission.MIMETYPE, Database.Submission.LINKED}, selection, null, null);
        submissionCursor.moveToFirst();
        if(submissionCursor.getCount() <= 0){
            return null;
        }

        return submissionCursor;
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
        existsCursor.close();

        return savedUri;
    }

    public static Cursor getChallengeForGame(int gameID, ContentResolver contentResolver) {
        Uri gameUri = DatabaseProvider.CONTENT_URI.buildUpon().appendPath(DatabaseProvider.GAME_STRING).build();
        Cursor gameCursor = contentResolver.query(gameUri, new String[]{Database.Game.ID, Database.Game.CURRENT_CHALLENGE_ID}, Database.Game.SERVER_ID + " = ?", new String[]{String.valueOf(gameID)}, null);
        gameCursor.moveToFirst();

        // invalid gameID
        if (gameCursor.getCount() <= 0) {
            gameCursor.close();
            return null;
        }

        int challengeID = gameCursor.getInt(gameCursor.getColumnIndex(Database.Game.CURRENT_CHALLENGE_ID));
        gameCursor.close();

        Uri challengeUri = DatabaseProvider.CONTENT_URI.buildUpon().appendPath(DatabaseProvider.CHALLENGE_STRING).appendPath(String.valueOf(challengeID)).build();
        Cursor challengeCursor = contentResolver.query(challengeUri, new String[]{Database.Challenge.ID, Database.Challenge.TYPE, Database.Challenge.STATUS}, null, null, null);
        challengeCursor.moveToFirst();

        // no current challenge exists for game
        if (challengeCursor.getCount() <= 0) {
            challengeCursor.close();
            return null;
        }

        return challengeCursor;
    }

    public static Cursor getChallenge(int challengeID, ContentResolver contentResolver) {
        Uri challengeUri = DatabaseProvider.CONTENT_URI.buildUpon().appendPath(DatabaseProvider.CHALLENGE_STRING).appendPath(String.valueOf(challengeID)).build();
        Cursor challengeCursor = contentResolver.query(challengeUri, new String[]{Database.Challenge.ID, Database.Challenge.TEXT_HINT, Database.Challenge.TEXT_TASK}, null, null, null);
        challengeCursor.moveToFirst();

        // no current challenge exists for game
        if (challengeCursor.getCount() <= 0) {
            return null;
        }
        return challengeCursor;
    }


    //Submissions

    public static Cursor getSubmissionForChallenge(int challengeID, ContentResolver contentResolver){
        Uri submissionUri = DatabaseProvider.CONTENT_URI.buildUpon().appendPath(DatabaseProvider.SUBMISSION_STRING).build();

        String [] projection = new String[]{Database.Submission.CHALLENGE_ID,Database.Submission.SUBMITTED};
        String selection = Database.Submission.CHALLENGE_ID + " = ?";
        String [] selectionArgs = new String[]{String.valueOf(challengeID)};

        Cursor submissionCursor = contentResolver.query(submissionUri,projection,selection,selectionArgs,null);
        submissionCursor.moveToFirst();

        //if no submission found
        if(submissionCursor.getCount()<=0){
            submissionCursor.close();
            return null;
        }
        return submissionCursor;
    }

}
