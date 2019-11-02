package com.example.dimat.triviagame.classPackage;

public class Score implements Comparable<Score> {

    private String name;
    private int score;
    private String difficulty;

    public Score() {
    }

    public Score(String name, int score, String difficulty) {
        this.name = name;
        this.score = score;
        this.difficulty = difficulty;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public int compareTo(Score other) { //for the sort method of collections
        if (this.getScore() < other.getScore()) return 1;
        else if (this.getScore() > other.getScore()) return -1;
        else return 0;
    }
}