package com.example.clubperfomencetracker;

import java.util.List;

public class CfModels {

    // ── contest.standings ─────────────────────────────────────────

    public static class StandingsResponse {
        public String status;
        public StandingsResult result;
    }

    public static class StandingsResult {
        public List<RankRow> rows;
    }

    public static class RankRow {
        public int rank;
        public double points;
        public Party party;
        public List<ProblemResult> problemResults;
    }

    public static class ProblemResult {
        public double points;
        public int rejectedAttemptCount;
        public String type; // FINAL or PRELIMINARY
    }

    public static class Party {
        public List<Member> members;
    }

    public static class Member {
        public String handle;
    }

    // ── user.info ─────────────────────────────────────────────────

    public static class UserInfoResponse {
        public String status;
        public List<UserInfo> result;
    }

    public static class UserInfo {
        public String handle;
        public String organization;
        public int rating;
        public String rank;
    }

    // ── user.rating ───────────────────────────────────────────────

    public static class RatingChangeResponse {
        public String status;
        public List<RatingChange> result;
    }

    public static class RatingChange {
        public int contestId;
        public String contestName;
        public String handle;
        public int rank;
        public long ratingUpdateTimeSeconds;
        public int oldRating;
        public int newRating;
    }
}