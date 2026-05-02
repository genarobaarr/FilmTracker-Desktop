package com.src.filmtracker.services;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.src.filmtracker.models.*;
import com.src.filmtracker.utils.AppConstants;
import com.src.filmtracker.utils.SessionManager;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class ReviewService implements IReviewService {
    
    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    
    private final Set<String> localLikedReviews = ConcurrentHashMap.newKeySet();
    private final Set<String> localLikedComments = ConcurrentHashMap.newKeySet();

    @Override
    public CompletableFuture<List<ReviewDto>> getShowReviews(Integer tvmazeId) {
        Type type = TypeToken.getParameterized(List.class, ReviewDto.class).getType();
        String url = AppConstants.REVIEWS_URL + "/show/" + tvmazeId;
        CompletableFuture<List<ReviewDto>> future = executeGet(url, type, "reviews");
        
        return future;
    }

    @Override
    public CompletableFuture<ReviewDto> createReview(ReviewRequest req) {
        return executePostPut(AppConstants.REVIEWS_URL, req, "POST", ReviewDto.class, "review");
    }

    @Override
    public CompletableFuture<ReviewDto> updateReview(String id, ReviewRequest req) {
        String url = AppConstants.REVIEWS_URL + "/" + id;
        return executePostPut(url, req, "PUT", ReviewDto.class, "review");
    }

    @Override
    public CompletableFuture<Void> deleteReview(String id) {
        String url = AppConstants.REVIEWS_URL + "/" + id;
        return executeDelete(url);
    }

    @Override
    public CompletableFuture<Void> toggleReviewLike(String id, boolean isCurrentlyLiked) {
        String url = AppConstants.REVIEWS_URL + "/" + id + "/like";
        if (isCurrentlyLiked) {
            localLikedReviews.remove(id);
            return executeDelete(url).exceptionally(ex -> {
                localLikedReviews.add(id);
                return null;
            });
        } else {
            localLikedReviews.add(id);
            return executePostPutVoid(url, null, "POST").exceptionally(ex -> {
                localLikedReviews.remove(id);
                return null;
            });
        }
    }

    @Override
    public CompletableFuture<Boolean> isReviewLikedByMe(String reviewId) {
        if (localLikedReviews.contains(reviewId)) {
            return CompletableFuture.completedFuture(true);
        }
        if (!SessionManager.getInstance().isAuthenticated()) {
            return CompletableFuture.completedFuture(false);
        }
        
        String url = AppConstants.REVIEWS_URL + "/" + reviewId + "/likes";
        HttpRequest req = buildRequestBuilder(url).GET().build();
        
        return client.sendAsync(req, HttpResponse.BodyHandlers.ofString()).thenApply(res -> {
            if (res.statusCode() >= 400) {
                return false;
            }
            
            String authId = SessionManager.getInstance().getCurrentUser().authId();
            String id = SessionManager.getInstance().getCurrentUser().id();
            String body = res.body();
            boolean liked = false;
            
            if (authId != null) {
                if (!authId.isBlank()) {
                    String match = "\"" + authId + "\"";
                    if (body.contains(match)) {
                        liked = true;
                    }
                }
            }
            
            if (!liked) {
                if (id != null) {
                    if (!id.isBlank()) {
                        String match = "\"" + id + "\"";
                        if (body.contains(match)) {
                            liked = true;
                        }
                    }
                }
            }
            
            if (liked) {
                localLikedReviews.add(reviewId);
            }
            
            return liked;
        });
    }

    @Override
    public CompletableFuture<List<CommentDto>> getReviewComments(String reviewId) {
        Type type = TypeToken.getParameterized(List.class, CommentDto.class).getType();
        String url = AppConstants.REVIEWS_URL + "/" + reviewId + "/comments";
        CompletableFuture<List<CommentDto>> future = executeGet(url, type, "comments");
        
        return future.exceptionally(ex -> {
            return new ArrayList<CommentDto>();
        });
    }

    @Override
    public CompletableFuture<CommentDto> createComment(String reviewId, CommentRequest req) {
        String url = AppConstants.REVIEWS_URL + "/" + reviewId + "/comments";
        return executePostPut(url, req, "POST", CommentDto.class, "comment");
    }

    @Override
    public CompletableFuture<CommentDto> updateComment(String id, CommentRequest req) {
        return executePostPut(AppConstants.COMMENTS_URL + "/" + id, req, "PUT", CommentDto.class, "comment");
    }

    @Override
    public CompletableFuture<Void> deleteComment(String id) {
        return executeDelete(AppConstants.COMMENTS_URL + "/" + id);
    }

    @Override
    public CompletableFuture<Void> toggleCommentLike(String id, boolean isCurrentlyLiked) {
        String url = AppConstants.COMMENTS_URL + "/" + id + "/like";
        if (isCurrentlyLiked) {
            localLikedComments.remove(id);
            return executeDelete(url).exceptionally(ex -> {
                localLikedComments.add(id);
                return null;
            });
        } else {
            localLikedComments.add(id);
            return executePostPutVoid(url, null, "POST").exceptionally(ex -> {
                localLikedComments.remove(id);
                return null;
            });
        }
    }

    @Override
    public CompletableFuture<Boolean> isCommentLikedByMe(String commentId) {
        if (localLikedComments.contains(commentId)) {
            return CompletableFuture.completedFuture(true);
        }
        if (!SessionManager.getInstance().isAuthenticated()) {
            return CompletableFuture.completedFuture(false);
        }
        
        String url = AppConstants.COMMENTS_URL + "/" + commentId + "/likes";
        HttpRequest req = buildRequestBuilder(url).GET().build();
        
        return client.sendAsync(req, HttpResponse.BodyHandlers.ofString()).thenApply(res -> {
            if (res.statusCode() >= 400) {
                return false;
            }
            
            String authId = SessionManager.getInstance().getCurrentUser().authId();
            String id = SessionManager.getInstance().getCurrentUser().id();
            String body = res.body();
            boolean liked = false;
            
            if (authId != null) {
                if (!authId.isBlank()) {
                    String match = "\"" + authId + "\"";
                    if (body.contains(match)) {
                        liked = true;
                    }
                }
            }
            
            if (!liked) {
                if (id != null) {
                    if (!id.isBlank()) {
                        String match = "\"" + id + "\"";
                        if (body.contains(match)) {
                            liked = true;
                        }
                    }
                }
            }
            
            if (liked) {
                localLikedComments.add(commentId);
            }
            
            return liked;
        });
    }
    
    @Override
    public CompletableFuture<ReviewPaginationResponse> getUserReviews(String authId, int page) {
        Type type = TypeToken.getParameterized(ReviewPaginationResponse.class).getType();
        String url = AppConstants.REVIEWS_URL + "/user/" + authId + "?page=" + page;
        CompletableFuture<ReviewPaginationResponse> future = executeGet(url, type, null);
        
        return future.exceptionally(ex -> {
            return new ReviewPaginationResponse(new ArrayList<>(), null);
        });
    }

    private <T> CompletableFuture<T> executeGet(String url, Type type, String key) {
        HttpRequest req = buildRequestBuilder(url).GET().build();
        return sendAndParse(req, type, key);
    }

    private <T> CompletableFuture<T> executePostPut(String url, Object body, String method, Type type, String key) {
        String json = "{}";
        if (body != null) {
            json = gson.toJson(body);
        }
        
        HttpRequest req = buildRequestBuilder(url).method(method, HttpRequest.BodyPublishers.ofString(json)).build();
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