package de.medieninf.mobcomp.challenges.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.medieninf.mobcomp.challenges.R;
import de.medieninf.mobcomp.challenges.database.Database;
import de.medieninf.mobcomp.challenges.database.DatabaseProviderFascade;
import de.medieninf.mobcomp.challenges.services.GameService;
import de.medieninf.mobcomp.challenges.services.GameServiceAdapter;

/**
 * Created by Martin Juhasz on 14/06/15.
 */
public class PhotoChallengeActivity extends Activity{

    private static final int REQUEST_TAKE_PHOTO = 1;

    private Uri currentPhotoUri;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
    private File storageDir;

    private int challengeID;
    private Cursor challenge;
    private ImageView imageView;

    //Services
    private boolean gameServiceFound;
    private GameService gameService;
    private ServiceConnection gameServiceConnection;

    GameServiceAdapter gameServiceAdapter = new GameServiceAdapter () {

    };
    private Button btSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_photochallenge);
        TextView taskTextView = (TextView) findViewById(R.id.tv_photochallenge_task_text);

        // get challenge from extras
        if (getIntent().getExtras() == null) {
            throw new RuntimeException("challenge id must be given to display PhotoChallengeActivity");
        }
        challengeID = getIntent().getExtras().getInt(GameService.EXTRA_KEY_CHALLENGE_ID);
        this.challenge = DatabaseProviderFascade.getChallenge(challengeID, getContentResolver());
        if (this.challenge == null) {
            throw new RuntimeException("challenge id must be given to display PhotoChallengeActivity");
        }

        taskTextView.setText(this.challenge.getString(this.challenge.getColumnIndex(Database.Challenge.TEXT_TASK)));

        challenge.close();

        Button btTakePhoto = (Button) findViewById(R.id.bt_photo_challenge_action);
        btSubmit = (Button) findViewById(R.id.bt_photo_challenge_submit);
        btSubmit.setVisibility(View.INVISIBLE);

        btTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhotoClicked();
            }
        });
        btSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitClicked();
            }
        });
        imageView = (ImageView) findViewById(R.id.iv_photo_challenge);

        storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);


        // Game Service
        gameServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                gameService = ((GameService.GameServiceBinder)service).getService();
                gameService.addListener(gameServiceAdapter);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            if(currentPhotoUri != null) {
                ImageLoader.getInstance().displayImage(currentPhotoUri.toString(), imageView);
                btSubmit.setVisibility(View.VISIBLE);
            }
        }
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

    private void takePhotoClicked() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                File photoFile = createImageFile();
                // Save a file: path for use with ACTION_VIEW intents
                currentPhotoUri = Uri.fromFile(photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
        }
    }

    private void submitClicked(){
        gameService.saveChallengeSubmission(challengeID, currentPhotoUri);
        PhotoChallengeActivity.this.finish();
    }

    private File createImageFile() throws IOException {
        String timeStamp = sdf.format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return image;
    }
}
