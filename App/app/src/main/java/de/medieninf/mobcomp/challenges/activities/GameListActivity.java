package de.medieninf.mobcomp.challenges.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import de.medieninf.mobcomp.challenges.R;
import de.medieninf.mobcomp.challenges.database.Database;
import de.medieninf.mobcomp.challenges.services.GameService;
import de.medieninf.mobcomp.challenges.services.GameServiceListener;


public class GameListActivity extends Activity implements GameServiceListener {

    //instance variables
    final static String TAG = GameListActivity.class.getSimpleName();

    // Services
    private boolean gameServiceFound;
    private GameService gameService;
    private ServiceConnection gameServiceConnection;

    // GUI Elements
    private ListView gamesList;
    private Button newGameButton;

    GameListLoader loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_game_list);

        // GUI
        gamesList = (ListView) findViewById(R.id.gamelist_listview);
        newGameButton = (Button)findViewById(R.id.btn_game_list_start_game);

        newGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newGameButtonClicked();
            }
        });

        // create adapter
        GameListAdapter gameListAdapter = new GameListAdapter(this);
        gamesList.setAdapter(gameListAdapter);
        // create and start loader
        loader = new GameListLoader(gameListAdapter, this);
        getLoaderManager().initLoader(GameListLoader.ID, null, loader);
        gamesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                currentGameClicked(position);
            }
        });


        gameServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                gameService = ((GameService.GameServiceBinder) service).getService();
                checkLogin();
                gameService.updateGames();
                gameService.addListener(GameListActivity.this);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (gameServiceFound) {
            gameServiceFound = false;
            gameService = null;
            unbindService(gameServiceConnection);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (gameService != null) {
            gameService.updateGames();
            Log.i(TAG, "udpate games");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_game_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void checkLogin() {
        if (gameService == null) {
            return;
        }

        if (!gameService.isUserLoggedIn()) {
            Log.i(TAG, "checkLogin: unregistered");
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
        } else {
            Log.i(TAG, "checkLogin: registered");
        }
    }

    private void currentGameClicked(int position) {
        if (gameService == null) {
            return;
        }

        Cursor gameCursor = ((GameListAdapter)gamesList.getAdapter()).getCursor();
        gameCursor.moveToPosition(position);

        int gameID = gameCursor.getInt(gameCursor.getColumnIndex(Database.Game.ID));
        Intent challengeIntent = gameService.getIntentForChallengeActivity(gameID);
        if (challengeIntent != null) {
            startActivity(challengeIntent);
        }
    }

    private void newGameButtonClicked() {
        Intent newGameIntent = new Intent(this, StartGameActivity.class);
        startActivity(newGameIntent);
    }

    @Override
    public void userRegistrationUpdated(boolean successfully) {

    }

    @Override
    public void gamesUpdated(boolean successfully) {
        getLoaderManager().restartLoader(GameListLoader.ID, null, loader);
    }
}
