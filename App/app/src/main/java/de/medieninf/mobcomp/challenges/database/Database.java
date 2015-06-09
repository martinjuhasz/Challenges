package de.medieninf.mobcomp.challenges.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

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
        public static final String ROUNDS = "rounds";
        public static final String SUBMITTED = "submitted";

        private static final String CREATE_TABLE =
                "CREATE TABLE " + TABLE + "( "
                        + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + ROUNDS + " INTEGER NOT NULL, "
                        + SUBMITTED + " INTEGER NOT NULL DEFAULT 0"
                        + ")";

    }

    private static class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(Game.CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
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
