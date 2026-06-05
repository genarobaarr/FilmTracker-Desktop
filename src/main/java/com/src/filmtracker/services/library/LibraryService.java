package com.src.filmtracker.services.library;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.src.filmtracker.models.library.LibraryItemDto;
import com.src.filmtracker.models.library.LibraryRequest;
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
    
    private static final String HEADER_AUTH = "Authorization";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String TYPE_JSON = "application/json";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String KEY_DATA = "data";
    private static final String METHOD_POST = "POST";
    private static final String ERROR_PREFIX = "API Error: ";
    private static final String PAGE_PARAMETER = "?page=";

    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    @Override
    public CompletableFuture<List<LibraryItemDto>> getFavorites() {
        return fetchLibraryList(AppConstants.FAVORITES_URL);
    }

    @Override
    public CompletableFuture<List<LibraryItemDto>> getFavoritesByUser(String authId) {
        return fetchLibraryList(AppConstants.FAVORITES_URL + "/user/" + authId);
    }

    @Override
    public CompletableFuture<Void> addFavorite(Integer tvmazeId) {
        LibraryRequest req = new LibraryRequest(tvmazeId);
        return executePostPutVoid(AppConstants.FAVORITES_URL, req, METHOD_POST);
    }

    @Override
    public CompletableFuture<Void> removeFavorite(Integer tvmazeId) {
        String url = AppConstants.FAVORITES_URL + "/" + tvmazeId;
        return executeDelete(url);
    }

    @Override
    public CompletableFuture<List<LibraryItemDto>> getWatchlist() {
        return fetchLibraryList(AppConstants.WATCHLIST_URL);
    }

    @Override
    public CompletableFuture<Void> addWatchlist(Integer tvmazeId) {
        LibraryRequest req = new LibraryRequest(tvmazeId);
        return executePostPutVoid(AppConstants.WATCHLIST_URL, req, METHOD_POST);
    }

    @Override
    public CompletableFuture<Void> removeWatchlist(Integer tvmazeId) {
        String url = AppConstants.WATCHLIST_URL + "/" + tvmazeId;
        return executeDelete(url);
    }

    @Override
    public CompletableFuture<List<LibraryItemDto>> getFavoritesPaged(int page) {
        String url = AppConstants.FAVORITES_URL + PAGE_PARAMETER + page;
        return executeGetList(url);
    }

    @Override
    public CompletableFuture<List<LibraryItemDto>> getFavoritesByUserPaged(String authId, int page) {
        String url = AppConstants.FAVORITES_URL + "/user/" + authId + PAGE_PARAMETER + page;
        return executeGetList(url);
    }

    @Override
    public CompletableFuture<List<LibraryItemDto>> getWatchlistPaged(int page) {
        String url = AppConstants.WATCHLIST_URL + PAGE_PARAMETER + page;
        return executeGetList(url);
    }
    
    private CompletableFuture<List<LibraryItemDto>> fetchLibraryList(String url) {
        Type type = TypeToken.getParameterized(List.class, LibraryItemDto.class).getType();
        CompletableFuture<List<LibraryItemDto>> future = executeGet(url, type, KEY_DATA);
        return future.exceptionally(ex -> new ArrayList<>());
    }

    private CompletableFuture<List<LibraryItemDto>> executeGetList(String url) {
        Type type = TypeToken.getParameterized(List.class, LibraryItemDto.class).getType();
        HttpRequest req = buildRequestBuilder(url).GET().build();
        
        return client.sendAsync(req, HttpResponse.BodyHandlers.ofString()).thenApply(response -> {
            com.src.filmtracker.App.checkHttpResponse(response);
            
            if (response.statusCode() >= 400) {
                throw new IllegalStateException(ERROR_PREFIX + response.statusCode());
            }
            
            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            
            if (json.has(KEY_DATA)) {
                return gson.fromJson(json.get(KEY_DATA), type);
            }
            
            return new ArrayList<>();
        });
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
        
        HttpRequest req = buildRequestBuilder(url)
                .method(method, HttpRequest.BodyPublishers.ofString(json))
                .build();
                
        return sendAndIgnore(req);
    }

    private CompletableFuture<Void> executeDelete(String url) {
        HttpRequest req = buildRequestBuilder(url).DELETE().build();
        return sendAndIgnore(req);
    }

    private HttpRequest.Builder buildRequestBuilder(String url) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header(HEADER_CONTENT_TYPE, TYPE_JSON);
        
        if (SessionManager.getInstance().isAuthenticated()) {
            builder.header(HEADER_AUTH, BEARER_PREFIX + SessionManager.getInstance().getToken());
        }
        
        return builder;
    }

    private CompletableFuture<Void> sendAndIgnore(HttpRequest request) {
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(res -> {
            com.src.filmtracker.App.checkHttpResponse(res);
            
            if (res.statusCode() >= 400) {
                throw new IllegalStateException(ERROR_PREFIX + res.statusCode());
            }
            
            return null;
        });
    }

    private <T> CompletableFuture<T> sendAndParse(HttpRequest request, Type responseType, String extractionKey) {
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(response -> {
            com.src.filmtracker.App.checkHttpResponse(response);
            
            if (response.statusCode() >= 400) {
                throw new IllegalStateException(ERROR_PREFIX + response.statusCode());
            }
            
            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            
            if (extractionKey != null && json.has(extractionKey)) {
                return gson.fromJson(json.get(extractionKey), responseType);
            }
            
            if (json.has(KEY_DATA)) {
                return gson.fromJson(json.get(KEY_DATA), responseType);
            }
            
            return gson.fromJson(json, responseType);
        });
    }
}