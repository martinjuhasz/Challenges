package de.medieninf.mobcomp.challenges.activities;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableItemViewHolder;
import com.nostra13.universalimageloader.core.ImageLoader;

import de.medieninf.mobcomp.challenges.R;
import de.medieninf.mobcomp.challenges.database.Database;
import de.medieninf.mobcomp.challenges.database.DatabaseProviderFascade;
import de.medieninf.mobcomp.challenges.external.CursorRecyclerViewAdapter;

/**
 * Created by Martin Juhasz on 06/07/15.
 */
public class DraggableSubmissionsAdapter extends CursorRecyclerViewAdapter<DraggableSubmissionsAdapter.SubmissionsViewHolder>
        implements DraggableItemAdapter<DraggableSubmissionsAdapter.SubmissionsViewHolder> {

    final static String TAG = DraggableSubmissionsAdapter.class.getSimpleName();
    private final ImageLoader imageLoader;
    private Context context;

    public static class SubmissionsViewHolder extends AbstractDraggableItemViewHolder {
        public FrameLayout mContainer;
        public View mDragHandle;
        public ImageView imageView;

        public SubmissionsViewHolder(View v) {
            super(v);

            imageView = (ImageView)v.findViewById(R.id.iv_rate_cell_image);
            mContainer = (FrameLayout) v.findViewById(R.id.container);
            mDragHandle = v.findViewById(R.id.drag_handle);
            //mTextView = (TextView) v.findViewById(android.R.id.text1);
        }
    }

    public DraggableSubmissionsAdapter(Context context){
        super(context, null);
        this.context = context;
        this.imageLoader = ImageLoader.getInstance();
        setHasStableIds(true);
    }

    @Override
    public void onBindViewHolder(SubmissionsViewHolder viewHolder, Cursor cursor) {
        viewHolder.imageView.setImageBitmap(null);
        String bitmapPath = cursor.getString(cursor.getColumnIndex(Database.Submission.CONTENT_URI));
        //Bitmap bitmap = imageLoader.loadImageSync(bitmapPath);
        imageLoader.displayImage(bitmapPath, viewHolder.imageView);
        //viewHolder.imageView.setImageBitmap(bitmap);
    }

    @Override
    public SubmissionsViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.listitem_submission, viewGroup, false);
        SubmissionsViewHolder vh = new SubmissionsViewHolder(itemView);
        return vh;
    }

    @Override
    public boolean onCheckCanStartDrag(SubmissionsViewHolder submissionsViewHolder, int position, int x, int y) {
        final View containerView = submissionsViewHolder.mContainer;
        final View dragHandleView = submissionsViewHolder.mDragHandle;

        final int offsetX = containerView.getLeft() + (int) (ViewCompat.getTranslationX(containerView) + 0.5f);
        final int offsetY = containerView.getTop() + (int) (ViewCompat.getTranslationY(containerView) + 0.5f);
        return hitTest(dragHandleView, x - offsetX, y - offsetY);
    }

    public static boolean hitTest(View v, int x, int y) {
        final int tx = (int) (ViewCompat.getTranslationX(v) + 0.5f);
        final int ty = (int) (ViewCompat.getTranslationY(v) + 0.5f);
        final int left = v.getLeft() + tx;
        final int right = v.getRight() + tx;
        final int top = v.getTop() + ty;
        final int bottom = v.getBottom() + ty;

        return (x >= left) && (x <= right) && (y >= top) && (y <= bottom);
    }

    @Override
    public ItemDraggableRange onGetItemDraggableRange(SubmissionsViewHolder submissionsViewHolder, int i) {
        return null;
    }

    @Override
    public void onMoveItem(int fromPosition, int toPosition) {
        if (fromPosition == toPosition) {
            return;
        }

        Cursor cursor = getCursor(fromPosition);
        if (cursor == null || cursor.getCount() <= 0) {
            return;
        }

        int itemID = cursor.getInt(cursor.getColumnIndex(Database.Submission.ID));
        DatabaseProviderFascade.submissionOrderChanged(itemID, fromPosition, toPosition, context.getContentResolver());

        notifyItemMoved(fromPosition, toPosition);
    }

}
