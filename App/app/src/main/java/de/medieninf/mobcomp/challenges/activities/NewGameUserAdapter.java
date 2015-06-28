package de.medieninf.mobcomp.challenges.activities;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import de.medieninf.mobcomp.challenges.R;
import de.medieninf.mobcomp.challenges.database.Database;

/**
 * Created by Martin Juhasz on 28/06/15.
 */
public class NewGameUserAdapter extends CursorAdapter {

    private LayoutInflater layoutInflater;

    public NewGameUserAdapter(Context context) {
        super(context, null, false);
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return layoutInflater.inflate(R.layout.listitem_user, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView usernameView = (TextView)view.findViewById(R.id.tv_startgame_row_username);
        usernameView.setText(cursor.getString(cursor.getColumnIndex(Database.User.USERNAME)));
    }

}
