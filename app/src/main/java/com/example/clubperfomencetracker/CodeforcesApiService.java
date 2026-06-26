package com.example.clubperfomencetracker;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface CodeforcesApiService {
    @GET("contest.standings")
    Call<CodeforcesResponse> getOrganizationStandings(
        @Query("contestId") int contestId,
        @Query("handles") String handles,
        @Query("showUnofficial") boolean showUnofficial
    );

    @GET("contest.standings")
    Call<CodeforcesResponse> getContestStandings(
        @Query("contestId") int contestId,
        @Query("showUnofficial") boolean showUnofficial,
        @Query("from") int from,
        @Query("count") int count
    );

    @GET("user.info")
    Call<UserInfoResponse> getUserInfo(
        @Query("handles") String handles
    );
}