package com.example.clubperfomencetracker;

public class CodeforcesResponse {
    private String status;
    private StandingsResult result;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public StandingsResult getResult() { return result; }
    public void setResult(StandingsResult result) { this.result = result; }
}