package de.medieninf.mobcomp.challenges.activities;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.CursorAdapter;

import de.medieninf.mobcomp.challenges.database.Database;
import de.medieninf.mobcomp.challenges.database.DatabaseProvider;

/**
 * Created by nakih on 11.06.15.
 */
public class GameListLoader implements LoaderManager.LoaderCallbacks<Cursor>{

    private Uri gamesUri;
    private CursorAdapter adapter;
    private Context context;
    private String[] projection;

    public GameListLoader(CursorAdapter adapter, Context context){

        this.adapter = adapter;
        this.context = context;
        this.gamesUri = DatabaseProvider.CONTENT_URI.buildUpon().appendPath(DatabaseProvider.GAME_STRING).build();

        //The Cursor must include a column named "_id" or this class will not work
        //https://developer.android.com/reference/android/widget/CursorAdapter.html
        this.projection = new String[]{Database.Game.TITLE,"_id"};

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = new CursorLoader(context,gamesUri,projection,null,null,null);
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }
}
