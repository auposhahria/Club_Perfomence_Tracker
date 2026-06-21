package com.example.clubperfomencetracker;

public class Ranking {
    private int rank;
    private String userName;
    private int score;
    private String date;
    private double weight;
    private boolean isCurrentUser;

    public Ranking(int rank, String userName, int score, String date, double weight) {
        this.rank = rank;
        this.userName = userName;
        this.score = score;
        this.date = date;
        this.weight = weight;
        this.isCurrentUser = false;
    }

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }
    public String getUserName() { return userName; }
    public int getScore() { return score; }
    public String getDate() { return date; }
    public double getWeight() { return weight; }
    public boolean isCurrentUser() { return isCurrentUser; }
    public void setCurrentUser(boolean currentUser) { isCurrentUser = currentUser; }
}