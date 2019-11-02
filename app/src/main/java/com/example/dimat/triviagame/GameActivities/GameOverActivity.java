package com.example.dimat.triviagame.GameActivities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.example.dimat.triviagame.R;

import tyrantgit.explosionfield.ExplosionField;

public class GameOverActivity extends Activity {

    private ImageView gameOverLogo;
    private TextView finalScoreTv;
    private Button homepageBtn;
    ExplosionField explosionField;
    Animation smalltobig;
    MediaPlayer musicPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gameover);
        playMusic();
        //my fonts
        Typeface myCustomFont = Typeface.createFromAsset(getAssets(), "fonts/Raleway-ExtraBold.ttf");
        Typeface myCustomFont2 = Typeface.createFromAsset(getAssets(), "fonts/Raleway-SemiBold.ttf");
        smalltobig = AnimationUtils.loadAnimation(this, R.anim.smalltobig); //animation for the imageview

        gameOverLogo = findViewById(R.id.gameover_imageview);
        gameOverLogo.startAnimation(smalltobig);

        finalScoreTv = findViewById(R.id.finalscore_textview);
        finalScoreTv.setTypeface(myCustomFont);

        homepageBtn = findViewById(R.id.homepage_btn);
        homepageBtn.setTypeface(myCustomFont2);

        Intent intent = getIntent();
        finalScoreTv.setText("Your Score: " + intent.getStringExtra("finalScore"));

        homepageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //back to homepage
                YoYo.with(Techniques.Shake)
                        .duration(500)
                        .repeat(0)
                        .playOn(homepageBtn);
                stopMusic();
                Intent intent = new Intent(GameOverActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "Return to home page", Toast.LENGTH_SHORT).show();
    }

    //music methods
    public void playMusic() {
        if (musicPlayer == null) {
            musicPlayer = MediaPlayer.create(GameOverActivity.this, R.raw.gameoversong);
        }
        musicPlayer.start();
    }

    public void pauseMusic(View v) {
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
}
