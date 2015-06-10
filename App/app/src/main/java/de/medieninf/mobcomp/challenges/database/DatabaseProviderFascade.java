package de.medieninf.mobcomp.challenges.database;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;

/**
 * Created by Martin Juhasz on 10/06/15.
 */
public class DatabaseProviderFascade {


    public static Uri saveGame(int server_id, String title, int game_grounds, boolean submitted, ContentResolver contentResolver) {
        Uri scoreUri = DatabaseProvider.CONTENT_URI.buildUpon().appendPath(DatabaseProvider.GAME_STRING).build();
        ContentValues values = new ContentValues();
        values.put(Database.Game.SERVER_ID, server_id);
        values.put(Database.Game.TITLE, title);
        values.put(Database.Game.ROUNDS, game_grounds);
        values.put(Database.Game.SUBMITTED, submitted);
        return contentResolver.insert(scoreUri, values);
    }

}
