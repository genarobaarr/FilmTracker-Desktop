package com.src.filmtracker.services;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.src.filmtracker.models.LibraryItemDto;
import com.src.filmtracker.models.LibraryRequest;
import com.src.filmtracker.utils.AppConstants;
import com.src.filmtracker.utils.SessionManager;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class LibraryService implements ILibraryService {
    
    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    @Override
    public CompletableFuture<List<LibraryItemDto>> getFavorites() {
        Type type = TypeToken.getParameterized(List.class, LibraryItemDto.class).getType();
        CompletableFuture<List<LibraryItemDto>> future = executeGet(AppConstants.FAVORITES_URL, type, "data");
        return future.exceptionally(ex -> {
            return new ArrayList<LibraryItemDto>();
        });
    }

    @Override
    public CompletableFuture<Void> addFavorite(Integer tvmazeId) {
        LibraryRequest req = new LibraryRequest(tvmazeId);
        return executePostPutVoid(AppConstants.FAVORITES_URL, req, "POST");
    }

    @Override
    public CompletableFuture<Void> removeFavorite(Integer tvmazeId) {
        String url = AppConstants.FAVORITES_URL + "/" + tvmazeId;
        return executeDelete(url);
    }

    @Override
    public CompletableFuture<List<LibraryItemDto>> getWatchlist() {
        Type type = TypeToken.getParameterized(List.class, LibraryItemDto.class).getType();
        CompletableFuture<List<LibraryItemDto>> future = executeGet(AppConstants.WATCHLIST_URL, type, "data");
        return future.exceptionally(ex -> {
            return new ArrayList<LibraryItemDto>();
        });
    }

    @Override
    public CompletableFuture<Void> addWatchlist(Integer tvmazeId) {
        LibraryRequest req = new LibraryRequest(tvmazeId);
        return executePostPutVoid(AppConstants.WATCHLIST_URL, req, "POST");
    }

    @Override
    public CompletableFuture<Void> removeWatchlist(Integer tvmazeId) {
        String url = AppConstants.WATCHLIST_URL + "/" + tvmazeId;
        return executeDelete(url);
    }

    private <T> CompletableFuture<T> executeGet(String url, Type type, String key) {
        HttpRequest req = buildRequestBuilder(url).GET().build();
        return sendAndParse(req, type, key);
    }

    private CompletableFuture<Void> executePostPutVoid(String url, Object body, String method) {
        String json = "{}";
        if (body != null) {
            json = gson.toJson(body);
        }
        HttpRequest req = buildRequestBuilder(url).method(method, HttpRequest.BodyPublishers.ofString(json)).build();
        return sendAndIgnore(req);
    }

    private CompletableFuture<Void> executeDelete(String url) {
        HttpRequest req = buildRequestBuilder(url).DELETE().build();
        return sendAndIgnore(req);
    }

    private HttpRequest.Builder buildRequestBuilder(String url) {
        HttpRequest.Builder builder = HttpRequest.newBuilder();
        builder.uri(URI.create(url));
        builder.header("Content-Type", "application/json");
        
        if (SessionManager.getInstance().isAuthenticated()) {
            builder.header("Authorization", "Bearer " + SessionManager.getInstance().getToken());
        }
        
        return builder;
    }

    private CompletableFuture<Void> sendAndIgnore(HttpRequest request) {
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(res -> {
            if (res.statusCode() >= 400) {
                throw new RuntimeException("API Error: " + res.statusCode());
            }
            return null;
        });
    }

    private <T> CompletableFuture<T> sendAndParse(HttpRequest request, Type responseType, String extractionKey) {
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(response -> {
            if (response.statusCode() >= 400) {
                throw new RuntimeException("API Error: " + response.statusCode());
            }
            
            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            if (extractionKey != null) {
                if (json.has(extractionKey)) {
                    return gson.fromJson(json.get(extractionKey), responseType);
                }
            }
            
            if (json.has("data")) {
                return gson.fromJson(json.get("data"), responseType);
            }
            
            return gson.fromJson(json, responseType);
        });
    }
}