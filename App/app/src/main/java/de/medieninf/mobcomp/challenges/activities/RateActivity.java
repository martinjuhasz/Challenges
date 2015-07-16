package de.medieninf.mobcomp.challenges.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ListView;

import com.h6ah4i.android.widget.advrecyclerview.decoration.SimpleListDividerDecorator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;

import de.medieninf.mobcomp.challenges.R;
import de.medieninf.mobcomp.challenges.database.DatabaseProviderFascade;
import de.medieninf.mobcomp.challenges.services.GameService;

/**
 * Created by Martin Juhasz on 30/06/15.
 */
public class RateActivity extends Activity {

    // Services
    private boolean gameServiceFound;
    private GameService gameService;
    private ServiceConnection gameServiceConnection;


    private RecyclerView submissionsRecyclerView;
    private SubmissionsListLoader submissionsListLoader;
    private RecyclerViewDragDropManager recyclerViewDragDropManager;
    private DraggableSubmissionsAdapter submissionsAdapter;
    private int challengeID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_rate);

        // get challenge from extras
        if (getIntent().getExtras() == null) {
            throw new RuntimeException("challenge id must be given to display RateActivity");
        }
        challengeID = getIntent().getExtras().getInt(GameService.EXTRA_KEY_CHALLENGE_ID);


        submissionsRecyclerView = (RecyclerView)findViewById(R.id.rv_rate_submissions);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        submissionsRecyclerView.setLayoutManager(layoutManager);

        recyclerViewDragDropManager = new RecyclerViewDragDropManager();

        submissionsAdapter = new DraggableSubmissionsAdapter(this);
        RecyclerView.Adapter wrappedAdapter = recyclerViewDragDropManager.createWrappedAdapter(submissionsAdapter);

        submissionsRecyclerView.setAdapter(wrappedAdapter);
        recyclerViewDragDropManager.attachRecyclerView(submissionsRecyclerView);

        // gameservice
        gameServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                gameService = ((GameService.GameServiceBinder) service).getService();
                setupListLoader();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                gameServiceFound = false;
                gameService = null;
            }
        };
        Intent gameServiceIntent = new Intent(this, GameService.class);
        gameServiceFound = bindService(gameServiceIntent, gameServiceConnection, Context.BIND_AUTO_CREATE);



    }

    private void setupListLoader() {
        if (this.gameService == null) {
            return;
        }
        submissionsListLoader = new SubmissionsListLoader(this.submissionsAdapter, this, challengeID, this.gameService.getUserId());
        getLoaderManager().initLoader(SubmissionsListLoader.ID, null, submissionsListLoader);
    }
}
