package com.example.dimat.triviagame.GameActivities;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.example.dimat.triviagame.R;
import com.example.dimat.triviagame.classPackage.Question;
import com.example.dimat.triviagame.classPackage.Score;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import tyrantgit.explosionfield.ExplosionField;


public class GameActivity extends Activity {
    public static final String HIGH_SCORE = "highScore";
    private static final long COUNTDOWN_IN_MILLIS = 20000; //20 seconds for each question
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis;
    private ColorStateList textColorDefaultCountdown;
    ExplosionField explosionField;

    private TextView textViewName;
    private TextView textViewQuestion;
    private TextView textViewScore;
    private TextView textViewQuestionCount;
    private TextView textViewDifficulty;
    private TextView textViewCountdown;
    private RadioGroup answerGroup;
    private RadioButton rb1;
    private RadioButton rb2;
    private RadioButton rb3;
    private RadioButton rb4;
    private Button confirmNextBtn;

    private ArrayList<Question> difficultyQuestions;
    private Gson gson;
    private int questionCounter;
    private int questionCountTotal;
    private Question currentQuestion;

    private String playerName;
    private int score;
    private String difficulty;
    private boolean answered;
    private String response;

    private long backPressedTime;
    private MediaPlayer musicPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        playMusic();

        //my fonts
        Typeface myCustomFont = Typeface.createFromAsset(getAssets(), "fonts/Raleway-ExtraBold.ttf");
        Typeface myCustomFont2 = Typeface.createFromAsset(getAssets(), "fonts/Raleway-SemiBold.ttf");
        textViewName = findViewById(R.id.textview_name);
        textViewName.setTypeface(myCustomFont2);

        textViewQuestion = findViewById(R.id.textview_question);
        textViewQuestion.setTypeface(myCustomFont);

        textViewScore = findViewById(R.id.textview_score);
        textViewScore.setTypeface(myCustomFont2);

        textViewQuestionCount = findViewById(R.id.textview_questionNumber);
        textViewQuestionCount.setTypeface(myCustomFont2);

        textViewDifficulty = findViewById(R.id.textview_difficulty);
        textViewDifficulty.setTypeface(myCustomFont2);

        textViewCountdown = findViewById(R.id.textview_countdown);
        textViewCountdown.setTypeface(myCustomFont);

        answerGroup = findViewById(R.id.radio_btn_group);
        rb1 = findViewById(R.id.answer1);
        rb1.setTypeface(myCustomFont);
        rb2 = findViewById(R.id.answer2);
        rb2.setTypeface(myCustomFont);
        rb3 = findViewById(R.id.answer3);
        rb3.setTypeface(myCustomFont);
        rb4 = findViewById(R.id.answer4);
        rb4.setTypeface(myCustomFont);
        confirmNextBtn = findViewById(R.id.confirm_btn);
        confirmNextBtn.setTypeface(myCustomFont);

        textColorDefaultCountdown = textViewCountdown.getTextColors(); // get the color of the countdown timer

        Intent intent = getIntent(); //will get the EXTRA_DIFFICULTY level
        difficulty = intent.getStringExtra(MainActivity.EXTRA_DIFFICULTY);//get user's difficulty
        playerName = intent.getStringExtra("name");
        textViewName.setText(playerName + "'s ");
        textViewName.setTypeface(myCustomFont2);
        textViewDifficulty.setText("Difficulty: " + difficulty);
        textViewDifficulty.setTypeface(myCustomFont2);

        try {
            response = new GetQuestions().execute().get(); //get all question from database
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        if (response != null) {
            gson = new Gson();
            Question questions[] = gson.fromJson(response, Question[].class); // convert from json to question object
            difficultyQuestions = new ArrayList<>();
            for (Question elem : questions) {
                if (elem.getDifficulty().equals(difficulty)) {
                    difficultyQuestions.add(elem);
                }
            }
        }

        questionCountTotal = difficultyQuestions.size(); //get the number of questions we have with this difficulty
        Collections.shuffle(difficultyQuestions); //shuffle our question list to get them in random order

        showNextQuestion();


        confirmNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!answered) { //check if the question not answered, then lock and check the answer
                    if (rb1.isChecked() || rb2.isChecked() || rb3.isChecked() || rb4.isChecked()) {
                        checkAnswer();
                    } else { //nothing is selected
                        Toast.makeText(GameActivity.this, "Please select an answer", Toast.LENGTH_SHORT).show();
                    }
                } else { //answered == true
                    YoYo.with(Techniques.RubberBand)
                            .duration(500)
                            .repeat(0)
                            .playOn(confirmNextBtn);
                    showNextQuestion(); //show next question
                }
            }
        });
    }

    public class GetQuestions extends AsyncTask<Void, Void, String> {

        String message;
        String modifiedMessage = null;

        @Override
        protected String doInBackground(Void... params) {

            Socket clientSocket = null;
            DataOutputStream outToServer = null;
            BufferedReader inFromServer = null;
            String ip = "192.168.43.137";

            try {
                clientSocket = new Socket(ip, 10001); // create new socket on port
                outToServer = new DataOutputStream(clientSocket.getOutputStream()); //Send Client Choice
                inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }

            message = "2"; //sql.getQuestions
            try {
                outToServer.writeBytes(message + '\n');
                modifiedMessage = inFromServer.readLine(); //get from the database all the questions
            } catch (IOException e) {
                e.printStackTrace();
            }

            return modifiedMessage;//get json string with all the questions
        }
    }

    private void showNextQuestion() {
        rb1.setBackgroundResource(R.drawable.answer_btn_style);
        rb2.setBackgroundResource(R.drawable.answer_btn_style);
        rb3.setBackgroundResource(R.drawable.answer_btn_style);
        rb4.setBackgroundResource(R.drawable.answer_btn_style);
        answerGroup.clearCheck(); //uncheck all the answers

        if (questionCounter < questionCountTotal) {//only if we have questions left we can show the next one
            currentQuestion = difficultyQuestions.get(questionCounter);
            textViewQuestion.setText(currentQuestion.getQuestion()); //update question text view with new question
            rb1.setText(currentQuestion.getOption1());
            rb2.setText(currentQuestion.getOption2());
            rb3.setText(currentQuestion.getOption3());
            rb4.setText(currentQuestion.getOption4());

            questionCounter++; //starts at 1
            textViewQuestionCount.setText("Question: " + questionCounter + "/" + questionCountTotal); //update the question counter text by 1
            answered = false; //after clicking confirm next button we want to lock the answer instead of moving to next question
            confirmNextBtn.setText("Confirm");

            timeLeftInMillis = COUNTDOWN_IN_MILLIS;
            startCountDown();
        } else {
            finishGame();
        }
    }

    private void startCountDown() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) { //update every second
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountDownTextView(); //call this method every second
            }

            @Override
            public void onFinish() {
                timeLeftInMillis = 0;
                updateCountDownTextView();
                checkAnswer(); //if the time is finished we want to check the answer that as been selected (if selected)
            }
        }.start();
    }

    private void updateCountDownTextView() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60; //get what left after dividing by 60

        String timeLeftFormat = String.format(Locale.getDefault(), " %02d:%02d", minutes, seconds); //format our time to string to update textView
        textViewCountdown.setText(timeLeftFormat);//update countdown
        // textViewCountdown.setTypeface(myCustomFont);

        if (timeLeftInMillis < 5000) {//if less than 5 seconds left set countdown color to red
            textViewCountdown.setTextColor(Color.RED);
        } else { //left more than 10 seconds set color to white
            textViewCountdown.setTextColor(textColorDefaultCountdown);
        }
    }

    private void checkAnswer() {
        answered = true;

        countDownTimer.cancel(); //stop the countdown timer!

        RadioButton selectedRb = findViewById(answerGroup.getCheckedRadioButtonId()); //get the id of selected answer
        int answerNumber = answerGroup.indexOfChild(selectedRb) + 1; //the id of button starts at 0

        if (answerNumber == currentQuestion.getAnswerNumber()) { //the user selected the correct answer
            score++;
            textViewScore.setText("Score: " + score);//update score
        }
        showSolution(); //call this method no matter if the answer is correct or wrong
    }

    private void showSolution() {
        //the wrong answers with paint to red and the correct one will paint to green
        rb1.setBackgroundResource(R.drawable.wrong_answer_style);
        rb2.setBackgroundResource(R.drawable.wrong_answer_style);
        rb3.setBackgroundResource(R.drawable.wrong_answer_style);
        rb4.setBackgroundResource(R.drawable.wrong_answer_style);

        switch (currentQuestion.getAnswerNumber()) {
            case 1:
                rb1.setBackgroundResource(R.drawable.correct_answer_style);
                textViewQuestion.setText("Answer 1 is correct");
                break;
            case 2:
                rb2.setBackgroundResource(R.drawable.correct_answer_style);
                ;
                textViewQuestion.setText("Answer 2 is correct");
                break;
            case 3:
                rb3.setBackgroundResource(R.drawable.correct_answer_style);
                textViewQuestion.setText("Answer 3 is correct");
                break;
            case 4:
                rb4.setBackgroundResource(R.drawable.correct_answer_style);
                textViewQuestion.setText("Answer 4 is correct");
                break;
        }

        if (questionCounter < questionCountTotal) { //there are questions left
            confirmNextBtn.setText("Next");
        } else { //it was the last question
            confirmNextBtn.setText("Finish Game");
        }
    }

    private void finishGame() {

        String response = " ";
        try {
            response = new setFinalScore().execute().get();//set the new score to database
            if (response.equals("success"))
                Toast.makeText(GameActivity.this, "Your Score has been updated", Toast.LENGTH_SHORT).show();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        explosionField = ExplosionField.attach2Window(GameActivity.this); //explode the button
        explosionField.explode(confirmNextBtn);
        stopMusic();
        String finalScore = Integer.toString(score);
        Intent intent = new Intent(GameActivity.this, GameOverActivity.class);

        intent.putExtra("finalScore", finalScore);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() { //avoid exits from game by mistake
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            Intent intent = new Intent(GameActivity.this, MainActivity.class);
            stopMusic();
            startActivity(intent);
        } else { //more than 2 second passed from the last time the user pushed back button
            Toast.makeText(this, "Press back again to finish", Toast.LENGTH_SHORT).show();
        }
        backPressedTime = System.currentTimeMillis();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) { //make sure that we started it
            countDownTimer.cancel(); //if activity closes the countdown cancels
        }
    }

    private String newScore() { //gonna create json string
        Gson gson = new Gson();
        Score scoreObj = new Score(playerName, score, difficulty); //object of a new score
        String gsonScoreString = gson.toJson(scoreObj); //converting score object to object json string
        return gsonScoreString;
    }

    public class setFinalScore extends AsyncTask<Void, Void, String> {

        String message;
        String modifiedMessage = null;

        @Override
        protected String doInBackground(Void... params) {

            Socket clientSocket = null;
            DataOutputStream outToServer = null;
            BufferedReader inFromServer = null;
            String ip = "192.168.43.137";

            try {
                clientSocket = new Socket(ip, 10001); // create new socket on port 10001
                inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); //to check if succeeded
                outToServer = new DataOutputStream(clientSocket.getOutputStream()); // to send client's action (4)
            } catch (IOException e) {
                e.printStackTrace();
            }

            message = "4";
            String gsonScoreString = newScore(); //gonna get gson string of score object
            try {
                outToServer.writeBytes(message + '\n'); // for case 4
                outToServer.writeBytes(gsonScoreString + '\n');
                modifiedMessage = inFromServer.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return modifiedMessage;
        }
    }


    //music methods
    public void playMusic() {
        if (musicPlayer == null) {
            musicPlayer = MediaPlayer.create(GameActivity.this, R.raw.questionsong);
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
