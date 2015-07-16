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
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.HashMap;
import java.util.Map;

import de.medieninf.mobcomp.challenges.R;
import de.medieninf.mobcomp.challenges.database.Database;

/**
 * Created by Martin Juhasz on 30/06/15.
 */
public class SubmissionsListAdapter extends CursorAdapter {

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
        imageView.setImageBitmap(null);
        String bitmapPath = cursor.getString(cursor.getColumnIndex(Database.Submission.CONTENT_URI));
        imageLoader.displayImage(bitmapPath, imageView);
    }
}
