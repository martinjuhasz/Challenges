package de.medieninf.mobcomp.challenges.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.nhaarman.listviewanimations.itemmanipulation.DynamicListView;

import de.medieninf.mobcomp.challenges.R;

/**
 * Created by Martin Juhasz on 30/06/15.
 */
public class RateActivity extends Activity {

    private DynamicListView submissionsListView;
    private SubmissionsListLoader submissionsListLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_rate);

        submissionsListView = (DynamicListView)findViewById(R.id.lv_rate_submissions);

        SubmissionsListAdapter submissionsListAdapter = new SubmissionsListAdapter(this);
        submissionsListView.setAdapter(submissionsListAdapter);
        submissionsListView.setOnItemMovedListener(submissionsListAdapter);
        submissionsListLoader = new SubmissionsListLoader(submissionsListAdapter, this);
        getLoaderManager().initLoader(SubmissionsListLoader.ID, null, submissionsListLoader);
        submissionsListView.enableDragAndDrop();
        submissionsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                submissionsListView.startDragging(position);
                return true;
            }
        });
    }
}
