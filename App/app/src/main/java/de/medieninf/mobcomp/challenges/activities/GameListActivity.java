package de.medieninf.mobcomp.challenges.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import de.medieninf.mobcomp.challenges.R;
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

    GameListLoader loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_game_list);

        // GUI

        //ListView + Loader
        gamesList = (ListView) findViewById(R.id.gamelist_listview);
        // create adapter
        GameListAdapter gameListAdapter = new GameListAdapter(this);
        gamesList.setAdapter(gameListAdapter);
        // create and start loader
        loader = new GameListLoader(gameListAdapter, this);
        getLoaderManager().initLoader(GameListLoader.ID, null, loader);


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

    @Override
    public void userRegistrationUpdated(boolean successfully) {

    }

    @Override
    public void gamesUpdated(boolean successfully) {
        getLoaderManager().restartLoader(GameListLoader.ID,null,loader);
    }
}
