package com.example.clubperfomencetracker;

public class OrgRankResult {
    public final String handle;
    public final String organization;
    public final int orgRank;
    public final int orgTotal;
    public final int score;

    public OrgRankResult(String handle, String organization,
                         int orgRank, int orgTotal, int score) {
        this.handle       = handle;
        this.organization = organization;
        this.orgRank      = orgRank;
        this.orgTotal     = orgTotal;
        this.score        = score;
    }
}
