package com.mvp.example.photostream.service;

import com.mvp.example.photostream.model.SearchResult;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GithubService {
    @GET("/search/repositories")
    Call<SearchResult> searchRepositories(@Query("q") String query, @Query("page") int page);
}
