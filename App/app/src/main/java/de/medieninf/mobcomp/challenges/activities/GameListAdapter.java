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
 * Created by Michael Weilbächer on 11.06.15.
 */
public class GameListAdapter extends CursorAdapter {

    final static String TAG = GameListAdapter.class.getSimpleName();

    public GameListAdapter(Context context) {
        // http://stackoverflow.com/questions/16026782/using-custom-cursoradapter-with-loadermanger-initialize-with-null-cursor
        super(context,null,false);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.listitem_gamelist,parent,false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView gametext = (TextView)view.findViewById(R.id.gamelist_gametext);
        gametext.setText(cursor.getString(cursor.getColumnIndex(Database.Game.TITLE)));

        TextView usertext = (TextView)view.findViewById(R.id.gamelist_usertext);
        usertext.setText("Users");
    }
}
