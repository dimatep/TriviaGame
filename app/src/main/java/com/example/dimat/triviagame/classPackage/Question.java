package com.example.dimat.triviagame.classPackage;

public class Question {
    public static final String DIFFICULTY_EASY = "Easy";
    public static final String DIFFICULTY_MEDIUM = "Medium";
    public static final String DIFFICULTY_HARD = "Hard";

    private String question;
    private String option1;
    private String option2;
    private String option3;
    private String option4;
    private String difficulty;
    private int answerNumber;

    public Question() {
    }

    public Question(String question, String option1, String option2, String option3, String option4, int answerNumber, String difficulty) {
        this.question = question;
        this.option1 = option1;
        this.option2 = option2;
        this.option3 = option3;
        this.option4 = option4;
        this.answerNumber = answerNumber;
        this.difficulty = difficulty;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getOption1() {
        return option1;
    }

    public void setOption1(String option1) {
        this.option1 = option1;
    }

    public String getOption2() {
        return option2;
    }

    public void setOption2(String option2) {
        this.option2 = option2;
    }

    public String getOption3() {
        return option3;
    }

    public void setOption3(String option3) {
        this.option3 = option3;
    }

    public String getOption4() {
        return option4;
    }

    public void setOption4(String option4) {
        this.option4 = option4;
    }

    public int getAnswerNumber() {
        return answerNumber;
    }

    public void setAnswerNumber(int answerNumber) {
        this.answerNumber = answerNumber;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    //for the spinner
    public static String[] getAllDifficultyLevels() { //static- we dont need an object in our question class the retreat this difficulty levels
        return new String[]{DIFFICULTY_EASY, DIFFICULTY_MEDIUM, DIFFICULTY_HARD};
    }
}
