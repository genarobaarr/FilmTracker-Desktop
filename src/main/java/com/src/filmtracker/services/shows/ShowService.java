package com.src.filmtracker.services.shows;

import com.src.filmtracker.models.shows.ShowsByGenreResponse;
import com.src.filmtracker.models.shows.ShowEpisodesResponse;
import com.src.filmtracker.models.shows.ShowFullResponse;
import com.src.filmtracker.models.shows.SearchResponse;
import com.src.filmtracker.models.shows.HomeResponse;
import com.src.filmtracker.models.shows.Show;
import com.src.filmtracker.models.shows.EpisodeDto;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.src.filmtracker.utils.AppConstants;
import com.src.filmtracker.utils.SessionManager;

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
    
    private static final String HEADER_ACCEPT = "Accept";
    private static final String HEADER_AUTH = "Authorization";
    private static final String TYPE_JSON = "application/json";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ERROR_PREFIX = "Error: ";
    private static final String KEY_DATA = "data";

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
                .thenApply(response -> {
                    com.src.filmtracker.App.checkHttpResponse(response);
                    return response.body();
                })
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
        String encodedGenre = URLEncoder.encode(genre, StandardCharsets.UTF_8);
        String url = AppConstants.SHOWS_BY_GENRE_URL + encodedGenre;
        
        return client.sendAsync(createRequest(url), HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    com.src.filmtracker.App.checkHttpResponse(response);
                    return response.body();
                })
                .thenApply(json -> {
                    ShowsByGenreResponse response = gson.fromJson(json, ShowsByGenreResponse.class);
                    return response != null && response.results() != null ? response.results() : List.of();
                });
    }
    
    @Override
    public CompletableFuture<List<EpisodeDto>> getShowEpisodes(Integer id) {
        String url = AppConstants.SHOWS_SERVICE_URL + "/" + id + "/episodes";
        
        return client.sendAsync(createRequest(url), HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    com.src.filmtracker.App.checkHttpResponse(response);
                    return response.body();
                })
                .thenApply(json -> {
                    ShowEpisodesResponse response = gson.fromJson(json, ShowEpisodesResponse.class);
                    return response != null && response.episodes() != null ? response.episodes() : List.of();
                });
    }
    
    @Override
    public CompletableFuture<Show> getShowDetails(Integer tvmazeId) {
        String url = AppConstants.SHOWS_SERVICE_URL + "/" + tvmazeId;
        
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header(HEADER_ACCEPT, TYPE_JSON)
                .GET();

        if (SessionManager.getInstance().isAuthenticated()) {
            builder.header(HEADER_AUTH, BEARER_PREFIX + SessionManager.getInstance().getToken());
        }

        return client.sendAsync(builder.build(), HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    com.src.filmtracker.App.checkHttpResponse(response);
                    
                    if (response.statusCode() >= 400) {
                        throw new IllegalStateException(ERROR_PREFIX + response.statusCode());
                    }
                    
                    com.google.gson.JsonObject json = com.google.gson.JsonParser.parseString(response.body()).getAsJsonObject();
                    
                    if (json.has(KEY_DATA)) {
                        return gson.fromJson(json.get(KEY_DATA), Show.class);
                    }
                    
                    return gson.fromJson(json, Show.class);
                });
    }

    private <T> CompletableFuture<T> executeGet(String url, Class<T> responseClass) {
        return client.sendAsync(createRequest(url), HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    com.src.filmtracker.App.checkHttpResponse(response);
                    return response.body();
                })
                .thenApply(json -> gson.fromJson(json, responseClass));
    }

    private HttpRequest createRequest(String url) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header(HEADER_ACCEPT, TYPE_JSON)
                .GET();

        if (SessionManager.getInstance().isAuthenticated()) {
            builder.header(HEADER_AUTH, BEARER_PREFIX + SessionManager.getInstance().getToken());
        }

        return builder.build();
    }
}