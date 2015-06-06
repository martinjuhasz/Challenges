package de.medieninf.mobcomp.challenges.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import de.medieninf.mobcomp.challenges.R;
import de.medieninf.mobcomp.challenges.services.GameService;

/**
 * Created by Martin Juhasz on 06/06/15.
 */
public class LoginActivity extends Activity {

    final static String TAG = LoginActivity.class.getSimpleName();
    private EditText usernameEditText;
    private Button submitButton;

    // Services
    private boolean gameServiceFound;
    private GameService gameService;
    private ServiceConnection gameServiceConnection;
    private BroadcastReceiver gameServiceBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        usernameEditText = (EditText)findViewById(R.id.et_login_username);
        submitButton = (Button)findViewById(R.id.bt_login_submit);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitClicked();
            }
        });

        // Game Service
        gameServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                gameService = ((GameService.GameServiceBinder)service).getService();
            }
            @Override
            public void onServiceDisconnected(ComponentName name) {
                gameServiceFound = false;
                gameService = null;
            }
        };
        Intent gameServiceIntent = new Intent(this, GameService.class);
        gameServiceFound = bindService(gameServiceIntent, gameServiceConnection, Context.BIND_AUTO_CREATE);

        gameServiceBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                gameBroadcastReceived(intent);
            }
        };
        IntentFilter filter = new IntentFilter(GameService.BROADCAST_USER_REGISTERED);
        LocalBroadcastManager.getInstance(this).registerReceiver(gameServiceBroadcastReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (gameServiceFound) {
            gameServiceFound = false;
            gameService = null;
            unbindService(gameServiceConnection);
        }

        LocalBroadcastManager.getInstance(this).unregisterReceiver(gameServiceBroadcastReceiver);
    }

    private void submitClicked() {
        if (gameService == null) {
            return;
        }

        String username = usernameEditText.getText().toString().trim();
        if (username.isEmpty()) {
            // TODO: show user Error indicating wrong username
            return;
        }

        gameService.submitUserRegistration(username);
    }

    private void gameBroadcastReceived(Intent intent) {
        String action = intent.getAction();
        if (action.equals(GameService.BROADCAST_USER_REGISTERED)) {
            boolean registeredSuccessfull = intent.getExtras().getBoolean(GameService.BROADCAST_USER_REGISTERED_SUCCESSFULLY_EXTRA);
            Log.i(TAG, "registration status: " + registeredSuccessfull);
        }
    }

}
