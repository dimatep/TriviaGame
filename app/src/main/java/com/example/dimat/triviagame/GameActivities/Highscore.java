package com.example.dimat.triviagame.GameActivities;

import android.app.Activity;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.example.dimat.triviagame.R;
import com.example.dimat.triviagame.classPackage.Score;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class Highscore extends Activity {

    private LinearLayout easyTable, mediumTable, hardTable;
    private TextView titleTv;
    private TextView easyTitle, mediumTitle, hardTitle;
    private TextView easyRate, mediumRate, hardRate;
    private TextView easyName, mediumName, hardName;
    private TextView easyScore, mediumScore, hardScore;
    private String response;

    private Gson gson;
    List<Score> easyScores;
    List<Score> mediumScores;
    ArrayList<Score> hardScores;
    MediaPlayer musicPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_highscore);

        playMusic();

        //fonts for activity
        Typeface myCustomFont = Typeface.createFromAsset(getAssets(), "fonts/Raleway-ExtraBold.ttf");
        Typeface myCustomFont2 = Typeface.createFromAsset(getAssets(), "fonts/Raleway-SemiBold.ttf");

        //initialize all the values
        easyTable = findViewById(R.id.easy_table);
        mediumTable = findViewById(R.id.medium_table);
        hardTable = findViewById(R.id.hard_table);

        titleTv = findViewById(R.id.title_textview);
        titleTv.setTypeface(myCustomFont);

        easyTitle = findViewById(R.id.easy_table_title);
        easyTitle.setTypeface(myCustomFont);
        mediumTitle = findViewById(R.id.medium_table_title);
        mediumTitle.setTypeface(myCustomFont);
        hardTitle = findViewById(R.id.hard_table_title);
        hardTitle.setTypeface(myCustomFont);

        easyRate = findViewById(R.id.easy_rate);
        easyRate.setTypeface(myCustomFont2);
        easyName = findViewById(R.id.easy_name);
        easyName.setTypeface(myCustomFont2);
        easyScore = findViewById(R.id.easy_score);
        easyScore.setTypeface(myCustomFont2);

        mediumRate = findViewById(R.id.medium_rate);
        mediumRate.setTypeface(myCustomFont2);
        mediumName = findViewById(R.id.medium_name);
        mediumName.setTypeface(myCustomFont2);
        mediumScore = findViewById(R.id.medium_score);
        mediumScore.setTypeface(myCustomFont2);

        hardRate = findViewById(R.id.hard_rate);
        hardRate.setTypeface(myCustomFont2);
        hardName = findViewById(R.id.hard_name);
        hardName.setTypeface(myCustomFont2);
        hardScore = findViewById(R.id.hard_score);
        hardScore.setTypeface(myCustomFont2);

        try {
            response = new GetScores().execute().get(); //get all scores from database
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        if (response != null) {
            gson = new Gson();
            Score scores[] = gson.fromJson(response, Score[].class); // convert from json string to arrays of Score object
            easyScores = new ArrayList<>();
            mediumScores = new ArrayList<>();
            hardScores = new ArrayList<>();

            for (Score elem : scores) { //set each score to appropriate array list
                if (elem.getDifficulty().equals("Easy")) {
                    easyScores.add(elem);
                }
                if (elem.getDifficulty().equals("Medium")) {
                    mediumScores.add(elem);
                }
                if (elem.getDifficulty().equals("Hard")) {
                    hardScores.add(elem);
                }
            }
        }

        //sort all the arrays
        Collections.sort(easyScores);
        Collections.sort(mediumScores);
        Collections.sort(hardScores);

        for (int i = 0; i < 3; i++) { //fill all three tables
            tableFilling(i);
        }
    }


    private void tableFilling(int tableDif) {  //0=Easy, 1=Medium, 2=Hard
        ListView listView_easy = findViewById(R.id.easy_list);
        ListView listView_medium = findViewById(R.id.medium_list);
        ListView listView_hard = findViewById(R.id.hard_list);

        List<Map<String, Object>> list_line = new ArrayList<>();

        String[] from = {"no'", "name", "score"};
        int[] to = {R.id.number, R.id.name, R.id.score};

        if (tableDif == 0) {//easy table scores
            for (int i = 0; i < easyScores.size(); i++) {
                HashMap<String, Object> line = new HashMap<>();
                line.put("no'", (i + 1));
                line.put("name", easyScores.get(i).getName());
                line.put("score", easyScores.get(i).getScore());
                list_line.add(line);
            }
        }
        if (tableDif == 1) {//medium table scores
            for (int i = 0; i < mediumScores.size(); i++) {
                HashMap<String, Object> line = new HashMap<>();
                line.put("no'", (i + 1));
                line.put("name", mediumScores.get(i).getName());
                line.put("score", mediumScores.get(i).getScore());
                list_line.add(line);
            }
        }
        if (tableDif == 2) {//hard table scores
            for (int i = 0; i < hardScores.size(); i++) {
                HashMap<String, Object> line = new HashMap<>();
                line.put("no'", (i + 1));
                line.put("name", hardScores.get(i).getName());
                line.put("score", hardScores.get(i).getScore());
                list_line.add(line);
            }
        }
        switch (tableDif) {
            case 0: {
                SimpleAdapter simpleAdapter = new SimpleAdapter(this, list_line, R.layout.scores_list, from, to);
                listView_easy.setAdapter(simpleAdapter);
                break;
            }

            case 1: {
                SimpleAdapter simpleAdapter = new SimpleAdapter(this, list_line, R.layout.scores_list, from, to);
                listView_medium.setAdapter(simpleAdapter);
                break;
            }

            case 2: {
                SimpleAdapter simpleAdapter = new SimpleAdapter(this, list_line, R.layout.scores_list, from, to);
                listView_hard.setAdapter(simpleAdapter);
                break;
            }

        }
    }

    public class GetScores extends AsyncTask<Void, Void, String> {

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

            message = "3"; //sql.getScore
            try {
                outToServer.writeBytes(message + '\n');
                modifiedMessage = inFromServer.readLine(); //get from the database all the scores
            } catch (IOException e) {
                e.printStackTrace();
            }

            return modifiedMessage; //return gson sting with all scores
        }
    }

    @Override
    public void onBackPressed() {
        stopMusic();
        super.onBackPressed();
    }

    //music methods
    public void playMusic() {
        if (musicPlayer == null) {
            musicPlayer = MediaPlayer.create(Highscore.this, R.raw.scoresong);
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
}

