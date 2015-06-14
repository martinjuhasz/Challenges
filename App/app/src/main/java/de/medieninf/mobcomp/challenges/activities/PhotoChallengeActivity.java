package de.medieninf.mobcomp.challenges.activities;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import de.medieninf.mobcomp.challenges.services.GameService;

/**
 * Created by Martin Juhasz on 14/06/15.
 */
public class PhotoChallengeActivity extends Activity {

    private int challengeID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().getExtras() == null) {
            throw new RuntimeException("challenge id must be given to display PhotoChallengeActivity");
        }

        this.challengeID = getIntent().getExtras().getInt(GameService.EXTRA_KEY_CHALLENGE_ID);
        Log.i("ASD", this.challengeID+"");


    }
}
