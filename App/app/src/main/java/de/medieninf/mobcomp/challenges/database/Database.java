package de.medieninf.mobcomp.challenges.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Martin Juhasz on 09/06/15.
 */
public class Database {

    private static final String NAME = "challenges.db";
    private static final int VERSION = 1;
    public SQLiteDatabase db;
    private final Context context;
    private DBHelper dbHelper;

    public class Game {

        public static final String TABLE = "games";
        public static final String ID = "_id";
        public static final String SERVER_ID = "server_id";
        public static final String ROUNDS = "rounds";
        public static final String TITLE = "title";
        public static final String SUBMITTED = "submitted";
        public static final String CURRENT_CHALLENGE_ID = "current_challenge_id";

        private static final String CREATE_TABLE =
                "CREATE TABLE " + TABLE + "( "
                        + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + SERVER_ID + " INTEGER NOT NULL UNIQUE, "
                        + ROUNDS + " INTEGER NOT NULL, "
                        + TITLE + " TEXT NOT NULL, "
                        + SUBMITTED + " INTEGER NOT NULL DEFAULT 0, "
                        + CURRENT_CHALLENGE_ID + " INTEGER, "
                        + "FOREIGN KEY(" + CURRENT_CHALLENGE_ID + ") REFERENCES " + Challenge.TABLE + "(" + Challenge.ID + ") "
                        + ")";

    }

    public class User {
        public static final String TABLE = "users";
        public static final String ID = "_id";
        public static final String SERVER_ID = "server_id";
        public static final String USERNAME = "username";
        public static final String IMAGE = "image";

        private static final String CREATE_TABLE =
                "CREATE TABLE " + TABLE + "( "
                        + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + SERVER_ID + " INTEGER NOT NULL UNIQUE, "
                        + USERNAME + " TEXT NOT NULL, "
                        + IMAGE + " BLOB "
                        + ")";
    }

    public class UserGames {
        public static final String TABLE = "usergames";
        public static final String ID = "_id";
        public static final String USERNAME_ID = "username_id";
        public static final String GAME_ID = "game_id";

        private static final String CREATE_TABLE =
                "CREATE TABLE " + TABLE + "( "
                        + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + USERNAME_ID + " INTEGER NOT NULL, "
                        + GAME_ID + " INTEGER NOT NULL, "
                        + "FOREIGN KEY(" + USERNAME_ID + ") REFERENCES " + User.TABLE + "(" + User.ID + "), "
                        + "FOREIGN KEY(" + GAME_ID + ") REFERENCES " + Game.TABLE + "(" + Game.ID + "), "
                        + "UNIQUE (" + USERNAME_ID + ", " + GAME_ID + ") "
                        + ")";
    }

    public class Challenge {
        public static final String TABLE = "challenges";
        public static final String ID = "_id";
        public static final String SERVER_ID = "server_id";
        public static final String GAME_ID = "game_id";
        public static final String STATUS = "status";
        public static final String TEXT_HINT = "text_hint";
        public static final String TEXT_TASK = "text_task";
        public static final String TYPE = "type";

        private static final String CREATE_TABLE =
                "CREATE TABLE " + TABLE + "( "
                        + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + SERVER_ID + " INTEGER NOT NULL UNIQUE, "
                        + GAME_ID + " INTEGER NOT NULL, "
                        + STATUS + " INTEGER NOT NULL, "
                        + TEXT_HINT + " TEXT NOT NULL, "
                        + TEXT_TASK + " TEXT NOT NULL, "
                        + TYPE + " INTEGER NOT NULL, "
                        + "FOREIGN KEY(" + GAME_ID + ") REFERENCES " + Game.TABLE + "(" + Game.ID + ") ON DELETE CASCADE "
                        + ")";
    }

    private static class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(Game.CREATE_TABLE);
            db.execSQL(User.CREATE_TABLE);
            db.execSQL(UserGames.CREATE_TABLE);
            db.execSQL(Challenge.CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + Challenge.TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + UserGames.TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + User.TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + Game.TABLE);
            onCreate(db);
        }
    }

    public Database(Context context) {
        this.context = context;
        this.dbHelper = new DBHelper(context, NAME, null, VERSION);
    }

    public SQLiteDatabase getDatabase() {
        return db;
    }

    public void open() throws SQLiteException {
        try {
            db = dbHelper.getWritableDatabase();
        } catch (SQLiteException exception) {
            db = dbHelper.getReadableDatabase();
        }
    }

    public void close() {
        db.close();
    }

}
