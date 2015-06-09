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

import de.medieninf.mobcomp.challenges.R;
import de.medieninf.mobcomp.challenges.services.GameService;


public class GameListActivity extends Activity {

    //instance variables
    final static String TAG = GameListActivity.class.getSimpleName();

    // Services
    private boolean gameServiceFound;
    private GameService gameService;
    private ServiceConnection gameServiceConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_game_list);

        gameServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                gameService = ((GameService.GameServiceBinder)service).getService();
                checkLogin();
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
}
