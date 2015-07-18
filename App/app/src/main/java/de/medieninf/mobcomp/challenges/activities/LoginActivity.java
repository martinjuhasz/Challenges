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
import de.medieninf.mobcomp.challenges.services.GameServiceListener;

/**
 * Created by Martin Juhasz on 06/06/15.
 */
public class LoginActivity extends Activity implements GameServiceListener {

    final static String TAG = LoginActivity.class.getSimpleName();
    private EditText usernameEditText;
    private Button submitButton;
    private boolean isSubmitting;

    // Services
    private boolean gameServiceFound;
    private GameService gameService;
    private ServiceConnection gameServiceConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // GUI
        setContentView(R.layout.activity_login);
        usernameEditText = (EditText)findViewById(R.id.et_login_username);
        submitButton = (Button)findViewById(R.id.bt_login_submit);

        isSubmitting = false;

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
                gameService.addListener(LoginActivity.this);
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

    private void submitClicked() {
        if (gameService == null || isSubmitting) {
            return;
        }

        String username = usernameEditText.getText().toString().trim();
        if (username.isEmpty()) {
            // TODO: show user Error indicating wrong username
            return;
        }

        isSubmitting = true;
        submitButton.setEnabled(false);
        gameService.submitUserRegistration(username);
    }

    @Override
    public void userRegistrationUpdated(boolean successfully) {
        isSubmitting = false;
        submitButton.setEnabled(true);
        Log.i(TAG, "registration status: " + successfully);

        if (successfully) {
            Intent gameListIntent = new Intent(this, GameListActivity.class);
            startActivity(gameListIntent);
            finish();
        }
    }

    @Override
    public void gamesUpdated(boolean successfully) {

    }

    @Override
    public void ratingSent(boolean successfully) {

    }
}
