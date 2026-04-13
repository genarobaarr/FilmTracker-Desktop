package com.src.filmtracker.services;

import com.google.gson.Gson;
import com.src.filmtracker.models.HomeResponse;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class TvMazeApiService {
    
    private static final String BASE_URL = "http://localhost:3001/api/shows";
    private final HttpClient client;
    private final Gson gson;

    public TvMazeApiService() {
        this.client = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    public CompletableFuture<HomeResponse> getHomeData() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/home?limit=20"))
                .header("Accept", "application/json")
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(json -> gson.fromJson(json, HomeResponse.class));
    }
}
