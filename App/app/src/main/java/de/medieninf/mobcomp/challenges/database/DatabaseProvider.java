package de.medieninf.mobcomp.challenges.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

/**
 * Created by Martin Juhasz on 09/06/15.
 */
public class DatabaseProvider extends ContentProvider {

    final static String TAG = DatabaseProvider.class.getSimpleName();

    // General Statics
    public static final String AUTHORITY = "de.medieninf.mobcomp.challenges.provider.content.database";
    public static final String CONTENT_URI_STRING = "content://" + AUTHORITY;
    public static final Uri CONTENT_URI = Uri.parse(CONTENT_URI_STRING);
    public static final String VND_HIGHSCORE_ITEM = "vnd.android.cursor.item/vnd.de.medieninf.mobcomp.challenges.database";
    public static final String VND_HIGHSCORE_DIR = "vnd.android.cursor.dir/vnd.de.medieninf.mobcomp.challenges.database";

    // games
    public static final String GAME_STRING = "games";
    public static final String USER_STRING = "users";
    public static final String USERGAMES_STRING = "usergames";
    private static final int GAMES_ID = 1;
    private static final int GAME_ID = 2;
    private static final int USER_ID = 3;
    private static final int USERS_ID = 4;
    private static final int USERGAMES_ID = 5;


    // URI Matcher
    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(AUTHORITY, GAME_STRING, GAMES_ID);
        uriMatcher.addURI(AUTHORITY, GAME_STRING+"/#", GAME_ID);

        uriMatcher.addURI(AUTHORITY, USER_STRING, USERS_ID);
        uriMatcher.addURI(AUTHORITY, USER_STRING+"/#", USER_ID);

        uriMatcher.addURI(AUTHORITY, USERGAMES_STRING, USERGAMES_ID);
    }

    private Database database;


    @Override
    public boolean onCreate() {
        database = new Database(this.getContext());
        database.open();
        return true;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case GAME_ID:
            case USER_ID:
                return VND_HIGHSCORE_ITEM;
            case GAMES_ID:
            case USERS_ID:
            case USERGAMES_ID:
                return VND_HIGHSCORE_DIR;
            default:
                Log.e(TAG, "getType, uri not supported " + uri);
                throw new IllegalArgumentException("Unsupported Uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        switch (uriMatcher.match(uri)) {
            case GAME_ID:
                Integer game_id = extractID(uri);
                selection = addId(selection, game_id, Database.Game.ID);
                return database.getDatabase().query(Database.Game.TABLE, projection, selection, selectionArgs, null, null, sortOrder);
            case GAMES_ID:
                return database.getDatabase().query(Database.Game.TABLE, projection, selection, selectionArgs, null, null, sortOrder);
            case USER_ID:
                Integer user_id = extractID(uri);
                selection = addId(selection, user_id, Database.User.ID);
                return database.getDatabase().query(Database.User.TABLE, projection, selection, selectionArgs, null, null, sortOrder);
            case USERS_ID:
                return database.getDatabase().query(Database.User.TABLE, projection, selection, selectionArgs, null, null, sortOrder);
        }
        Log.e(TAG, "query, no matching uri " + uri);
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        switch (uriMatcher.match(uri)) {
            case GAME_ID:
            case GAMES_ID:
                long game_id = database.getDatabase().insertOrThrow(Database.Game.TABLE, null, values);
                if (game_id == -1) {
                    Log.e(TAG, "insert, coundnt insert");
                    return null;
                }
                return CONTENT_URI.buildUpon().appendPath(GAME_STRING).appendPath(String.valueOf(game_id)).build();
            case USER_ID:
            case USERS_ID:
                long user_id = database.getDatabase().insertOrThrow(Database.User.TABLE, null, values);
                if (user_id == -1) {
                    Log.e(TAG, "insert, coundnt insert");
                    return null;
                }
                return CONTENT_URI.buildUpon().appendPath(USER_STRING).appendPath(String.valueOf(user_id)).build();
            case USERGAMES_ID:
                long usergames_id = database.getDatabase().insertOrThrow(Database.UserGames.TABLE, null, values);
                if (usergames_id == -1) {
                    Log.e(TAG, "insert, coundnt insert");
                    return null;
                }
                return CONTENT_URI.buildUpon().appendPath(USERGAMES_STRING).appendPath(String.valueOf(usergames_id)).build();
        }
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        switch (uriMatcher.match(uri)) {
            case GAME_ID:
                Integer game_id = extractID(uri);
                selection = addId(selection, game_id, Database.Game.ID);
                return database.getDatabase().delete(Database.Game.TABLE, selection, selectionArgs);
            case GAMES_ID:
                return database.getDatabase().delete(Database.Game.TABLE, selection, selectionArgs);
            case USER_ID:
                Integer user_id = extractID(uri);
                selection = addId(selection, user_id, Database.User.ID);
                return database.getDatabase().delete(Database.User.TABLE, selection, selectionArgs);
            case USERS_ID:
                return database.getDatabase().delete(Database.User.TABLE, selection, selectionArgs);
            default:
                Log.e(TAG, "delete, no matching URI " + uri);
                return 0;
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        switch (uriMatcher.match(uri)) {
            case GAME_ID:
                Integer game_id = extractID(uri);
                selection = addId(selection, game_id, Database.Game.ID);
                return database.getDatabase().update(Database.Game.TABLE, values, selection, selectionArgs);
            case GAMES_ID:
                return database.getDatabase().update(Database.Game.TABLE, values, selection, selectionArgs);
            case USER_ID:
                Integer user_id = extractID(uri);
                selection = addId(selection, user_id, Database.User.ID);
                return database.getDatabase().update(Database.User.TABLE, values, selection, selectionArgs);
            case USERS_ID:
                return database.getDatabase().update(Database.User.TABLE, values, selection, selectionArgs);
            default:
                return 0;
        }
    }

    private Integer extractID(Uri uri) {
        String idString = uri.getLastPathSegment();
        Integer id = null;
        try {
            id = new Integer(idString);
        } catch (NumberFormatException e) {
            Log.e(TAG, "could not extract id " + uri.toString());
            return null;
        }
        return id;
    }

    private String addId(String selection, long id, String idColumn) {
        selection = (selection == null) ? "" : selection.trim();
        String sand = selection.isEmpty() ? "" : " AND ";
        return String.format("%s%s%s=%d", selection, sand, idColumn, id);
    }
}
