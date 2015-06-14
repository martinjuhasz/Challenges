package de.medieninf.mobcomp.challenges.activities;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import de.medieninf.mobcomp.challenges.R;
import de.medieninf.mobcomp.challenges.database.Database;
import de.medieninf.mobcomp.challenges.database.DatabaseProviderFascade;
import de.medieninf.mobcomp.challenges.services.GameService;

/**
 * Created by Martin Juhasz on 14/06/15.
 */
public class PhotoChallengeActivity extends Activity {

    private TextView taskTextView;
    private Cursor challenge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_photochallenge);
        this.taskTextView = (TextView)findViewById(R.id.tv_photochallenge_task_text);

        // get challenge from extras
        if (getIntent().getExtras() == null) {
            throw new RuntimeException("challenge id must be given to display PhotoChallengeActivity");
        }
        int challengeID = getIntent().getExtras().getInt(GameService.EXTRA_KEY_CHALLENGE_ID);
        this.challenge = DatabaseProviderFascade.getChallenge(challengeID, getContentResolver());
        if (this.challenge == null) {
            throw new RuntimeException("challenge id must be given to display PhotoChallengeActivity");
        }

        this.taskTextView.setText(this.challenge.getString(this.challenge.getColumnIndex(Database.Challenge.TEXT_TASK)));
    }


}
