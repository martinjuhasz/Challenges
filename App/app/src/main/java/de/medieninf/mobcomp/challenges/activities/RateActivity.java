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
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.h6ah4i.android.widget.advrecyclerview.decoration.SimpleListDividerDecorator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;

import de.medieninf.mobcomp.challenges.R;
import de.medieninf.mobcomp.challenges.database.DatabaseProviderFascade;
import de.medieninf.mobcomp.challenges.services.GameService;
import de.medieninf.mobcomp.challenges.services.GameServiceAdapter;
import de.medieninf.mobcomp.challenges.services.GameServiceListener;

/**
 * Created by Martin Juhasz on 30/06/15.
 */
public class RateActivity extends Activity {

    // Services
    private boolean gameServiceFound;
    private GameService gameService;
    private ServiceConnection gameServiceConnection;

    private RecyclerView submissionsRecyclerView;
    private Button submitButton;

    private SubmissionsListLoader submissionsListLoader;
    private RecyclerViewDragDropManager recyclerViewDragDropManager;
    private DraggableSubmissionsAdapter submissionsAdapter;
    private int challengeID;
    private GameServiceAdapter gameServiceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_rate);
        submissionsRecyclerView = (RecyclerView)findViewById(R.id.rv_rate_submissions);
        submitButton = (Button)findViewById(R.id.btn_rate);

        // get challenge from extras
        if (getIntent().getExtras() == null) {
            throw new RuntimeException("challenge id must be given to display RateActivity");
        }
        challengeID = getIntent().getExtras().getInt(GameService.EXTRA_KEY_CHALLENGE_ID);

        // setup list view
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        submissionsRecyclerView.setLayoutManager(layoutManager);
        recyclerViewDragDropManager = new RecyclerViewDragDropManager();
        submissionsAdapter = new DraggableSubmissionsAdapter(this);
        RecyclerView.Adapter wrappedAdapter = recyclerViewDragDropManager.createWrappedAdapter(submissionsAdapter);
        submissionsRecyclerView.setAdapter(wrappedAdapter);
        recyclerViewDragDropManager.attachRecyclerView(submissionsRecyclerView);

        // setup submit
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitClicked();
            }
        });

        // setup gameservice
        gameServiceAdapter = new GameServiceAdapter() {
            @Override
            public void ratingSent(boolean successfully) {
                ratingSentCallback(successfully);
            }
        };
        gameServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                gameService = ((GameService.GameServiceBinder) service).getService();
                gameService.addListener(gameServiceAdapter);
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

    private void submitClicked() {
        if (this.gameService == null) {
            return;
        }

        this.submitButton.setEnabled(false);
        this.gameService.submitChallengeRating(this.challengeID);
    }

    public void ratingSentCallback(boolean successfully) {
        if (successfully) {
            finish();
        } else {
            this.submitButton.setEnabled(true);
        }
    }
}
