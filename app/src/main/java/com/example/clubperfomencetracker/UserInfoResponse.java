package com.example.clubperfomencetracker;

import java.util.List;

public class UserInfoResponse {
    private String status;
    private List<User> result;

    public String getStatus() { return status; }
    public List<User> getResult() { return result; }

    public static class User {
        private String handle;
        private String organization;
        private int rating;
        private String rank;

        public String getHandle() { return handle; }
        public String getOrganization() { return organization; }
        public int getRating() { return rating; }
        public String getRank() { return rank; }
    }
}