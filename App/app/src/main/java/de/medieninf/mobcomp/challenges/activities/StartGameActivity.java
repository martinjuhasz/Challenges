package de.medieninf.mobcomp.challenges.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import de.medieninf.mobcomp.challenges.R;
import de.medieninf.mobcomp.challenges.services.GameController;
import de.medieninf.mobcomp.challenges.services.GameControllerCallback;
import de.medieninf.mobcomp.challenges.services.GameService;

/**
 * Created by Martin Juhasz on 15/06/15.
 */
public class StartGameActivity extends Activity {

    // Services
    private boolean gameServiceFound;
    private GameService gameService;
    private ServiceConnection gameServiceConnection;

    private NewGameUserAdapter newGameUserAdapter;
    private NewGameUserLoader newGameUserLoader;

    // UI
    private EditText titleText;
    private EditText usernameText;
    private Button addUserButton;
    private ListView addedUsersListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_game);
        titleText = (EditText)findViewById(R.id.et_add_game_title);
        usernameText = (EditText)findViewById(R.id.et_add_game_username);
        addUserButton = (Button)findViewById(R.id.btn_add_game_add_user);
        addedUsersListView = (ListView)findViewById(R.id.lv_add_game_users);

        addUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addUserButtonClicked();
            }
        });

        // add services
        gameServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                gameService = ((GameService.GameServiceBinder) service).getService();
                setNewUsersListAdapter();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                gameServiceFound = false;
                gameService = null;
            }
        };
        Intent gameServiceIntent = new Intent(this, GameService.class);
        gameServiceFound = bindService(gameServiceIntent, gameServiceConnection, Context.BIND_AUTO_CREATE);

        // List view
        this.newGameUserAdapter = new NewGameUserAdapter(this);
        this.addedUsersListView.setAdapter(this.newGameUserAdapter);
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
        getMenuInflater().inflate(R.menu.menu_new_game, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_startgame) {
            startGameButtonClicked();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setNewUsersListAdapter() {
        if (gameService == null || gameService.getGameController().getNewGameUsers().size() <= 0) {
            return;
        }

        this.newGameUserLoader = new NewGameUserLoader(this.newGameUserAdapter, this, gameService.getGameController().getNewGameUsers());
        getLoaderManager().restartLoader(NewGameUserLoader.ID, null, this.newGameUserLoader);

    }

    private void addUserButtonClicked() {
        if (gameService == null) {
            return;
        }

        usernameText.setEnabled(false);
        addUserButton.setEnabled(false);

        // TODO:check username string for bad characters
        final String username = usernameText.getText().toString();

        gameService.getGameController().addUserToNewGame(username, new GameControllerCallback() {
            @Override
            public void userAdded(boolean successfully) {
                if (successfully) {
                    usernameText.setText("");
                    setNewUsersListAdapter();
                }
                usernameText.setEnabled(true);
                addUserButton.setEnabled(true);
            }
        });
    }

    private void startGameButtonClicked() {
        if (gameService == null) {
            return;
        }

        String title = titleText.getText().toString();
        if (title.trim().isEmpty()) {
            // TODO: implement visual error
            return;
        }

        if (gameService.getGameController().getNewGameUsers().size() <= 0) {
            // TODO: implement visual error
            return;
        }

        gameService.getGameController().startNewGame(title, new GameControllerCallback(){
            @Override
            public void gameCreated(boolean successfully) {
                if (successfully) {
                    finish();
                }
            }
        });
    }
}