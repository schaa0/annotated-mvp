package de.hda.simple_example.service;

import de.hda.simple_example.model.SearchResult;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GithubService {
    @GET("/search/repositories")
    Call<SearchResult> searchRepositories(@Query("q") String query, @Query("page") int page);
}
