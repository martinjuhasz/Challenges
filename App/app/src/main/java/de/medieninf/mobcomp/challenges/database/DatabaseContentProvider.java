package de.medieninf.mobcomp.challenges.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Created by Martin Juhasz on 09/06/15.
 */
public class DatabaseContentProvider extends ContentProvider {

    final static String TAG = DatabaseContentProvider.class.getSimpleName();

    // General Statics
    public static final String AUTHORITY = "de.medieninf.mobcomp.challenges.provider.content.database";
    public static final String CONTENT_URI_STRING = "content://" + AUTHORITY;
    public static final Uri CONTENT_URI = Uri.parse(CONTENT_URI_STRING);
    public static final String VND_HIGHSCORE_ITEM = "vnd.android.cursor.item/vnd.de.medieninf.mobcomp.challenges.database";
    public static final String VND_HIGHSCORE_DIR = "vnd.android.cursor.dir/vnd.de.medieninf.mobcomp.challenges.database";

    // games
    public static final String GAME_STRING = "games";
    private static final int GAMES_ID = 1;
    private static final int GAME_ID = 2;

    // URI Matcher
    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(AUTHORITY, GAME_STRING, GAMES_ID);
        uriMatcher.addURI(AUTHORITY, GAME_STRING+"/#", GAME_ID);
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
                return VND_HIGHSCORE_ITEM;
            case GAMES_ID:
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
                Integer id = extractID(uri);
                selection = addId(selection, id, Database.Game.ID);
                return database.getDatabase().query(Database.Game.TABLE, projection, selection, selectionArgs, null, null, sortOrder);
            case GAMES_ID:
                return database.getDatabase().query(Database.Game.TABLE, projection, selection, selectionArgs, null, null, sortOrder);
        }
        Log.e(TAG, "query, no matching uri " + uri);
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        switch (uriMatcher.match(uri)) {
            case GAME_ID:
                // we generate the id
            case GAMES_ID:
                long id = database.getDatabase().insert(Database.Game.TABLE, null, values);
                if (id == -1) {
                    Log.e(TAG, "insert, coundnt insert");
                    return null;
                }
                return CONTENT_URI.buildUpon().appendPath(GAME_STRING).appendPath(String.valueOf(id)).build();
        }
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        switch (uriMatcher.match(uri)) {
            case GAME_ID:
                Integer id = extractID(uri);
                selection = addId(selection, id, Database.Game.ID);
                return database.getDatabase().delete(Database.Game.TABLE, selection, selectionArgs);
            case GAMES_ID:
                return database.getDatabase().delete(Database.Game.TABLE, selection, selectionArgs);
            default:
                Log.e(TAG, "delete, no matching URI " + uri);
                return 0;
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        switch (uriMatcher.match(uri)) {
            case GAME_ID:
                Integer id = extractID(uri);
                selection = addId(selection, id, Database.Game.ID);
                return database.getDatabase().update(Database.Game.TABLE, values, selection, selectionArgs);
            case GAMES_ID:
                return database.getDatabase().update(Database.Game.TABLE, values, selection, selectionArgs);
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
