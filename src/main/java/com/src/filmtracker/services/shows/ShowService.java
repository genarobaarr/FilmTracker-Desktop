package com.src.filmtracker.services.shows;

import com.src.filmtracker.models.shows.ShowsByGenreResponse;
import com.src.filmtracker.models.shows.ShowEpisodesResponse;
import com.src.filmtracker.models.shows.ShowFullResponse;
import com.src.filmtracker.models.shows.SearchResponse;
import com.src.filmtracker.models.shows.HomeResponse;
import com.src.filmtracker.services.shows.IShowService;
import com.src.filmtracker.models.shows.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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

public class ShowService implements IShowService {
    private final HttpClient client;
    private final Gson gson;
    
    public ShowService() {
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

        return client.sendAsync(createRequest(url), HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(json -> {
                    List<SearchResponse> results = gson.fromJson(json, listType);
                    return results.stream().map(SearchResponse::show).toList();
                });
    }

    @Override
    public CompletableFuture<ShowFullResponse> getFullShowDetails(Integer id) {
        String url = AppConstants.SHOWS_SERVICE_URL + "/" + id + "/full";
        return executeGet(url, ShowFullResponse.class);
    }
    
    @Override
    public CompletableFuture<List<Show>> getShowsByGenre(String genre) {
        String encodedGenre = java.net.URLEncoder.encode(genre, java.nio.charset.StandardCharsets.UTF_8);
        String url = AppConstants.SHOWS_BY_GENRE_URL + encodedGenre;
        
        return client.sendAsync(createRequest(url), java.net.http.HttpResponse.BodyHandlers.ofString())
                .thenApply(java.net.http.HttpResponse::body)
                .thenApply(json -> {
                    ShowsByGenreResponse response = gson.fromJson(json, ShowsByGenreResponse.class);
                    return response != null && response.results() != null ? response.results() : List.of();
                });
    }
    
    @Override
    public CompletableFuture<List<EpisodeDto>> getShowEpisodes(Integer id) {
        String url = AppConstants.SHOWS_SERVICE_URL + "/" + id + "/episodes";
        
        return client.sendAsync(createRequest(url), HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(json -> {
                    ShowEpisodesResponse response = gson.fromJson(json, ShowEpisodesResponse.class);
                    
                    return response != null && response.episodes() != null ? response.episodes() : List.of();
                });
    }
    
    @Override
    public CompletableFuture<com.src.filmtracker.models.shows.Show> getShowDetails(Integer tvmazeId) {
        String url = AppConstants.SHOWS_SERVICE_URL + "/" + tvmazeId;
        
        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(url))
                .header("Accept", "application/json")
                .GET()
                .build();

        return client.sendAsync(request, java.net.http.HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() >= 400) {
                        throw new RuntimeException("Error: " + response.statusCode());
                    }
                    
                    com.google.gson.JsonObject json = com.google.gson.JsonParser.parseString(response.body()).getAsJsonObject();
                    
                    if (json.has("data")) {
                        return gson.fromJson(json.get("data"), com.src.filmtracker.models.shows.Show.class);
                    }
                    
                    return gson.fromJson(json, com.src.filmtracker.models.shows.Show.class);
                });
    }

    private <T> CompletableFuture<T> executeGet(String url, Class<T> responseClass) {
        return client.sendAsync(createRequest(url), HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(json -> gson.fromJson(json, responseClass));
    }

    private <T> CompletableFuture<List<T>> executeGetList(String url, Type type) {
        return client.sendAsync(createRequest(url), HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(json -> gson.fromJson(json, type));
    }

    private HttpRequest createRequest(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET()
                .build();
    }
}