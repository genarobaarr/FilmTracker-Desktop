package com.src.filmtracker.services;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.src.filmtracker.models.HomeResponse;
import com.src.filmtracker.models.SearchResponse;
import com.src.filmtracker.models.Show;
import com.src.filmtracker.utils.AppConstants;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
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
        String url = AppConstants.SHOWS_SERVICE_URL + "/home?limit=" + AppConstants.HOME_CAROUSEL_LIMIT;
        return executeGet(url, HomeResponse.class);
    }

    @Override
    public CompletableFuture<List<Show>> searchShows(String query) {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = AppConstants.SHOWS_SERVICE_URL + "/search?q=" + encodedQuery;
        
        Type listType = new TypeToken<List<SearchResponse>>(){}.getType();
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(json -> {
                    List<SearchResponse> searchResults = gson.fromJson(json, listType);
                    
                    return searchResults.stream()
                            .map(SearchResponse::show)
                            .toList();
                });
    }

    private <T> CompletableFuture<T> executeGet(String url, Class<T> responseClass) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(json -> gson.fromJson(json, responseClass));
    }
}
