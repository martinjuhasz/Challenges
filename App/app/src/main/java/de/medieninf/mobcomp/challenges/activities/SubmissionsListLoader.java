package de.medieninf.mobcomp.challenges.activities;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.CursorAdapter;

import java.util.List;

import de.medieninf.mobcomp.challenges.database.Database;
import de.medieninf.mobcomp.challenges.database.DatabaseProvider;

/**
 * Created by Martin Juhasz on 30/06/15.
 */
public class SubmissionsListLoader implements LoaderManager.LoaderCallbacks<Cursor>  {

    final static int ID = 2;

    private Uri submissionsUri;
    private CursorAdapter adapter;
    private Context context;
    private String[] projection;
    private String selection;
    private String[] selectionArgs;

    public SubmissionsListLoader(CursorAdapter adapter, Context context){
        this.adapter = adapter;
        this.context = context;
        this.projection = new String[]{Database.Submission.ID, Database.Submission.USER_ID, Database.Submission.CONTENT_URI};
        this.submissionsUri = DatabaseProvider.CONTENT_URI.buildUpon().appendPath(DatabaseProvider.SUBMISSION_STRING).build();
        this.selection = null;
        this.selectionArgs = null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = new CursorLoader(context, submissionsUri, projection, selection, selectionArgs, null);
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
