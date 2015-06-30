package de.medieninf.mobcomp.challenges.activities;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nhaarman.listviewanimations.itemmanipulation.dragdrop.OnItemMovedListener;
import com.nhaarman.listviewanimations.util.Swappable;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.HashMap;
import java.util.Map;

import de.medieninf.mobcomp.challenges.R;
import de.medieninf.mobcomp.challenges.database.Database;

/**
 * Created by Martin Juhasz on 30/06/15.
 */
public class SubmissionsListAdapter extends CursorAdapter implements Swappable, OnItemMovedListener {

    final static String TAG = SubmissionsListAdapter.class.getSimpleName();
    private Map<Integer, Integer> swappedPositions;
    private final ImageLoader imageLoader;

    public SubmissionsListAdapter(Context context) {
        super(context,null,false);
        swappedPositions = new HashMap<>();
        this.imageLoader = ImageLoader.getInstance();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.listitem_submission, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ImageView imageView = (ImageView)view.findViewById(R.id.iv_rate_cell_image);
        // Clear image since the original one gets loaded asynchronously
        //imageView.setImageBitmap(null);
        String bitmapPath = cursor.getString(cursor.getColumnIndex(Database.Submission.CONTENT_URI));
        imageLoader.displayImage(bitmapPath, imageView);
    }

    @Override
    public void swapItems(int destination, int source) {
        final int origDestination = getSwappedPositionIfExists(destination);
        swappedPositions.put(destination, getSwappedPositionIfExists(source));
        notifyDataSetChanged();
        swappedPositions.put(source, origDestination);
    }

    private int getSwappedPositionIfExists(int source) {
        return swappedPositions.containsKey(source) ? swappedPositions.get(source) : source;
    }

    @Override
    public long getItemId(int position) {
        int swappedPosition = getSwappedPositionIfExists(position);
        return super.getItemId(swappedPosition);
    }

    @Override
    public Object getItem(int position) {
        int swappedPosition = getSwappedPositionIfExists(position);
        return super.getItem(swappedPosition);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int swappedPosition = getSwappedPositionIfExists(position);
        return super.getView(swappedPosition, convertView, parent);
    }

    @Override
    public void onItemMoved(int source, int destination) {
        swappedPositions.clear();

        Log.i(TAG, "TODO: persist");
        //persistenceManager.swapPriorities(getItemId(source), getItemId(destination));
        //changeCursor(persistenceManager.getAllToDosCursor());
    }
}
