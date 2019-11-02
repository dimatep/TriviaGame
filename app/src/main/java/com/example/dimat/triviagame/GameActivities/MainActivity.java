package com.example.dimat.triviagame.GameActivities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.example.dimat.triviagame.R;
import com.example.dimat.triviagame.classPackage.Question;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.ExecutionException;

import tyrantgit.explosionfield.ExplosionField;

import static java.lang.Thread.sleep;

public class MainActivity extends Activity {

    public static final String EXTRA_DIFFICULTY = "extraDifficulty";

    private ImageView logoIv;
    private Button highScoreBtn;
    private ImageButton newGameBtn;
    ExplosionField explosionField;
    private ImageButton infoBtn;
    private TextView loginBtn;
    private Spinner difficultySpinner;
    private EditText playerName;
    private static String response = "not connected"; //for server connection

    private MediaPlayer musicPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        playMusic(); //start background music

        //my fonts
        Typeface myCustomFont = Typeface.createFromAsset(getAssets(), "fonts/Raleway-ExtraBold.ttf");
        Typeface myCustomFont2 = Typeface.createFromAsset(getAssets(), "fonts/Raleway-SemiBold.ttf");

        logoIv = findViewById(R.id.logo_image);
        highScoreBtn = findViewById(R.id.high_score_btn);
        highScoreBtn.setTypeface(myCustomFont);

        loginBtn = findViewById(R.id.login_btn);
        loginBtn.setTypeface(myCustomFont);

        if (response.equals("success")) {
            loginBtn.setText("Connected");
            loginBtn.setTextColor(Color.GREEN);
        }

        difficultySpinner = findViewById(R.id.difficulty_spinner);
        playerName = findViewById(R.id.name_input);
        playerName.setTypeface(myCustomFont2);
        newGameBtn = findViewById(R.id.new_game_btn);
        infoBtn = findViewById(R.id.info_btn);

        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bounce); //bouncing logo animation
        logoIv.startAnimation(animation);

        String[] difficultyLevels = Question.getAllDifficultyLevels();
        ArrayAdapter<String> adapterDifficulty = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, difficultyLevels);
        adapterDifficulty.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); //looks better with this drop down
        difficultySpinner.setAdapter(adapterDifficulty);


        infoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                YoYo.with(Techniques.Swing)
                        .duration(500)
                        .repeat(0)
                        .playOn(infoBtn);
                openDialog();
            }
        });

        highScoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                YoYo.with(Techniques.Pulse)
                        .duration(500)
                        .repeat(0)
                        .playOn(highScoreBtn);
                if (response.equals("not connected")) {
                    Toast.makeText(MainActivity.this, "Please login to server", Toast.LENGTH_LONG).show();
                } else {
                    showHighscore();
                }
            }
        });


        loginBtn.setOnClickListener(new View.OnClickListener() { //
            @Override
            public void onClick(View v) {
                YoYo.with(Techniques.Pulse)
                        .duration(500)
                        .repeat(0)
                        .playOn(loginBtn);
                if (response.equals("not connected")) {
                    Toast.makeText(MainActivity.this, "Connecting to server...", Toast.LENGTH_SHORT).show();
                    try {
                        response = new GetConnection().execute().get(); //get connection
                        if (response.equals("success")) {
                            Toast.makeText(MainActivity.this, "Successfully connected", Toast.LENGTH_SHORT).show();
                            loginBtn.setText("Connected");
                            loginBtn.setTextColor(Color.GREEN);
                        }
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                } else if (response.equals("success")) {
                    Toast.makeText(MainActivity.this, "Already connected", Toast.LENGTH_SHORT).show();
                }
            }
        });

        newGameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (response.equals("not connected")) {
                    Toast.makeText(MainActivity.this, "Please login to server", Toast.LENGTH_LONG).show();
                } else if (playerName.getText().toString().equals("")) {
                    Toast.makeText(MainActivity.this, "Please enter your name", Toast.LENGTH_LONG).show();
                } else {
                    explosionField = ExplosionField.attach2Window(MainActivity.this);
                    explosionField.explode(newGameBtn);
                    startGame();
                }
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    public void openDialog() { //for info image button
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);

        alertDialogBuilder.setTitle("Information")
                .setMessage("1.Enter your name\n2.Login to server\n3.Play and Enjoy!\n\n\nCreated by Dima Tepliakov")
                .setPositiveButton("Thanks!", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, do nothing
                    }
                }).show();

    }

    private void showHighscore() {
        stopMusic();
        Intent intent = new Intent(MainActivity.this, Highscore.class);
        startActivity(intent);
    }

    private void startGame() {
        stopMusic(); //stop background music
        String name = playerName.getText().toString();
        String difficulty = difficultySpinner.getSelectedItem().toString();//easy medium or hard

        Intent intent = new Intent(MainActivity.this, GameActivity.class);
        intent.putExtra(EXTRA_DIFFICULTY, difficulty); //save the difficulty for next intent
        intent.putExtra("name", name); //save the name for next intent
        startActivity(intent);
    }

    public class GetConnection extends AsyncTask<Void, Void, String> {

        String message;
        String modifiedMessage = null;

        @Override
        protected String doInBackground(Void... params) {

            Socket clientSocket = null;
            DataOutputStream outToServer = null;
            BufferedReader inFromServer = null;
            String ip = "192.168.43.137";

            try {
                clientSocket = new Socket(ip, 10001);
                outToServer = new DataOutputStream(clientSocket.getOutputStream());
                inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }

            message = "1";
            try {
                outToServer.writeBytes(message + '\n');
                modifiedMessage = inFromServer.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return modifiedMessage; //get "success" string
        }
    }

    //music methods
    public void playMusic() {
        if (musicPlayer == null) {
            musicPlayer = MediaPlayer.create(MainActivity.this, R.raw.menusong);
        }
        musicPlayer.start();
    }

    public void pauseMusic() {
        if (musicPlayer != null) {
            musicPlayer.pause();
        }
    }

    public void stopMusic() {
        if (musicPlayer != null) {
            musicPlayer.release();
        }
        musicPlayer = null;
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopMusic();
    }

    @Override
    protected void onResume() {
        playMusic();
        super.onResume();
    }
}
