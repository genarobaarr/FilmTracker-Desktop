package com.src.filmtracker.services;

import com.google.gson.Gson;
import com.src.filmtracker.models.HomeResponse;
import com.src.filmtracker.utils.AppConstants;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class ApiService implements IShowService {
    
    private final HttpClient client;
    private final Gson gson;

    public ApiService() {
        this.client = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    @Override
    public CompletableFuture<HomeResponse> getHomeData() {
        String url = AppConstants.API_BASE_URL + "/home?limit=" + AppConstants.HOME_CAROUSEL_LIMIT;
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(json -> gson.fromJson(json, HomeResponse.class));
    }
}
