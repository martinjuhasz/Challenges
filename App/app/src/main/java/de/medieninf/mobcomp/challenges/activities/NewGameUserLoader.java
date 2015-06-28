package de.medieninf.mobcomp.challenges.activities;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.CursorAdapter;

import java.util.ArrayList;
import java.util.List;

import de.medieninf.mobcomp.challenges.database.Database;
import de.medieninf.mobcomp.challenges.database.DatabaseProvider;

/**
 * Created by nakih on 11.06.15.
 */
public class NewGameUserLoader implements LoaderManager.LoaderCallbacks<Cursor> {

    final static int ID = 2;

    private Uri usersUri;
    private CursorAdapter adapter;
    private Context context;
    private String[] projection;
    private String selection;
    private String[] selectionArgs;
    private List<Integer> userIds;

    public NewGameUserLoader(CursorAdapter adapter, Context context, List<Integer> userIds){

        this.adapter = adapter;
        this.context = context;
        this.projection = new String[]{Database.User.ID, Database.User.USERNAME};
        this.usersUri = DatabaseProvider.CONTENT_URI.buildUpon().appendPath(DatabaseProvider.USER_STRING).build();
        this.userIds = userIds;
        setSelectionFromUsers();
    }

    private void setSelectionFromUsers() {
        String newSelection = Database.User.ID + " IN (";
        List<String> newSelectionArgs = new ArrayList<>();
        for (int i = 0; i < userIds.size(); i++) {
            newSelection += "?";
            if (i < userIds.size() - 1) {
                newSelection+= ",";
            }
            newSelectionArgs.add(userIds.get(i).toString());
        }
        newSelection += ")";

        this.selection = newSelection;
        this.selectionArgs = newSelectionArgs.toArray(new String[newSelectionArgs.size()]);
        Log.i("", this.selectionArgs.toString());
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (this.userIds == null || this.userIds.size() <= 0) {
            return null;
        }
        CursorLoader loader = new CursorLoader(context, usersUri, projection, selection, selectionArgs, null);
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
