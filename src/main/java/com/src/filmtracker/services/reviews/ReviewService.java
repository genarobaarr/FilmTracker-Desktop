package com.src.filmtracker.services.reviews;

import com.src.filmtracker.models.reviews.ReviewPaginationResponse;
import com.src.filmtracker.models.reviews.CommentPaginationResponse;
import com.src.filmtracker.models.reviews.CommentRequest;
import com.src.filmtracker.models.reviews.ReviewRequest;
import com.src.filmtracker.models.reviews.CommentDto;
import com.src.filmtracker.models.reviews.ReviewDto;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.src.filmtracker.models.reviews.ReviewSummaryDto;
import com.src.filmtracker.utils.AppConstants;
import com.src.filmtracker.utils.SessionManager;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class ReviewService implements IReviewService {
    
    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    @Override
    public CompletableFuture<ReviewPaginationResponse> getShowReviews(Integer tvmazeId, int page) {
        String url = AppConstants.REVIEWS_URL + "/show/" + tvmazeId + "?page=" + page;
        
        return executeGet(url, ReviewPaginationResponse.class);
    }

    @Override
    public CompletableFuture<ReviewDto> createReview(ReviewRequest request) {
        return executePostAndParse(AppConstants.REVIEWS_URL, request, ReviewDto.class, "review");
    }

    @Override
    public CompletableFuture<ReviewDto> updateReview(String reviewId, ReviewRequest request) {
        String url = AppConstants.REVIEWS_URL + "/" + reviewId;
        
        HttpRequest req = createRequestBuilder(url)
                .PUT(HttpRequest.BodyPublishers.ofString(gson.toJson(request)))
                .build();
                
        return sendAndParse(req, ReviewDto.class, "review");
    }

    @Override
    public CompletableFuture<Void> deleteReview(String reviewId) {
        String url = AppConstants.REVIEWS_URL + "/" + reviewId;
        
        HttpRequest req = createRequestBuilder(url)
                .DELETE()
                .build();
                
        return sendAndIgnore(req);
    }

    @Override
    public CompletableFuture<Void> toggleReviewLike(String reviewId, boolean isCurrentlyLiked) {
        String url = AppConstants.REVIEWS_URL + "/" + reviewId + "/like";
        
        HttpRequest req = createRequestBuilder(url)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
                
        return sendAndIgnore(req);
    }

    @Override
    public CompletableFuture<CommentPaginationResponse> getReviewComments(String reviewId, int page) {
        String url = AppConstants.REVIEWS_URL + "/" + reviewId + "/comments?page=" + page;
        
        return executeGet(url, CommentPaginationResponse.class);
    }

    @Override
    public CompletableFuture<CommentDto> createComment(String reviewId, CommentRequest request, java.io.File imageFile) {
        String url = AppConstants.REVIEWS_URL + "/" + reviewId + "/comments";
        String boundary = "Boundary-" + System.currentTimeMillis();

        try {
            byte[] body = buildMultipartBodyWithTextAndFile(request.content(), imageFile, boundary);
            
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                    .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                    .build();

            return sendAndParse(req, CommentDto.class, "comment");
        } catch (java.io.IOException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<CommentDto> updateComment(String commentId, CommentRequest request) {
        String url = AppConstants.COMMENTS_URL + "/" + commentId;
        
        HttpRequest req = createRequestBuilder(url)
                .PUT(HttpRequest.BodyPublishers.ofString(gson.toJson(request)))
                .build();
                
        return sendAndParse(req, CommentDto.class, "comment");
    }

    @Override
    public CompletableFuture<Void> deleteComment(String commentId) {
        String url = AppConstants.COMMENTS_URL + "/" + commentId;
        
        HttpRequest req = createRequestBuilder(url)
                .DELETE()
                .build();
                
        return sendAndIgnore(req);
    }

    @Override
    public CompletableFuture<Void> toggleCommentLike(String commentId, boolean isCurrentlyLiked) {
        String url = AppConstants.COMMENTS_URL + "/" + commentId + "/like";
        
        HttpRequest req = createRequestBuilder(url)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
                
        return sendAndIgnore(req);
    }

    @Override
    public CompletableFuture<ReviewPaginationResponse> getUserReviews(String authId, int page) {
        String url = AppConstants.REVIEWS_URL + "/user/" + authId + "?page=" + page;
        
        return executeGet(url, ReviewPaginationResponse.class);
    }

    @Override
    public CompletableFuture<ReviewSummaryDto> getUserSummary(String authId) {
        String url = AppConstants.REVIEWS_URL + "/user/" + authId + "/summary";
        
        HttpRequest request = createRequestBuilder(url).GET().build();
        
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(response -> {
            if (response.statusCode() >= 400) {
                return new ReviewSummaryDto(authId, 0, 0);
            }
            
            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            
            if (json.has("data")) {
                return gson.fromJson(json.get("data"), ReviewSummaryDto.class);
            }
            
            return gson.fromJson(json, ReviewSummaryDto.class);
        });
    }

    @Override
    public CompletableFuture<Void> uploadReviewImage(String reviewId, java.io.File imageFile) {
        String url = AppConstants.REVIEWS_URL + "/" + reviewId + "/image";
        
        return executeImageUpload(url, imageFile);
    }

    @Override
    public CompletableFuture<Void> uploadCommentImage(String commentId, java.io.File imageFile) {
        String url = AppConstants.COMMENTS_URL + "/" + commentId + "/image";
        
        return executeImageUpload(url, imageFile);
    }

    private CompletableFuture<Void> executeImageUpload(String url, java.io.File file) {
        String boundary = "Boundary-" + System.currentTimeMillis();
        
        try {
            byte[] body = buildMultipartBody(file, boundary);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                    .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                    .build();

            return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(res -> {
                        if (res.statusCode() >= 400) {
                            throw new RuntimeException("Upload failed: " + res.statusCode());
                        }
                        
                        return null;
                    });
        } catch (java.io.IOException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    private byte[] buildMultipartBodyWithTextAndFile(String content, java.io.File file, String boundary) throws java.io.IOException {
        java.io.ByteArrayOutputStream byteStream = new java.io.ByteArrayOutputStream();
        java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.OutputStreamWriter(byteStream, java.nio.charset.StandardCharsets.UTF_8), true);

        writer.append("--");
        writer.append(boundary);
        writer.append("\r\n");
        writer.append("Content-Disposition: form-data; name=\"content\"\r\n\r\n");
        writer.append(content);
        writer.append("\r\n");

        if (file != null) {
            writer.append("--");
            writer.append(boundary);
            writer.append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"image\"; filename=\"");
            writer.append(file.getName());
            writer.append("\"\r\n");

            String mimeType = java.nio.file.Files.probeContentType(file.toPath());
            
            if (mimeType == null) {
                mimeType = "image/jpeg";
            }

            writer.append("Content-Type: ");
            writer.append(mimeType);
            writer.append("\r\n\r\n");
            writer.flush();

            java.nio.file.Files.copy(file.toPath(), byteStream);
            byteStream.flush();

            writer.append("\r\n");
        }

        writer.append("--");
        writer.append(boundary);
        writer.append("--\r\n");
        writer.flush();

        return byteStream.toByteArray();
    }

    private byte[] buildMultipartBody(java.io.File file, String boundary) throws java.io.IOException {
        java.io.ByteArrayOutputStream byteStream = new java.io.ByteArrayOutputStream();
        java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.OutputStreamWriter(byteStream, java.nio.charset.StandardCharsets.UTF_8), true);
        
        writer.append("--");
        writer.append(boundary);
        writer.append("\r\n");
        writer.append("Content-Disposition: form-data; name=\"image\"; filename=\"");
        writer.append(file.getName());
        writer.append("\"\r\n");
        
        String mimeType = java.nio.file.Files.probeContentType(file.toPath());
        
        if (mimeType == null) {
            mimeType = "image/jpeg";
        }
        
        writer.append("Content-Type: ");
        writer.append(mimeType);
        writer.append("\r\n\r\n");
        writer.flush();
        
        java.nio.file.Files.copy(file.toPath(), byteStream);
        
        writer.append("\r\n--");
        writer.append(boundary);
        writer.append("--\r\n");
        writer.flush();
        
        return byteStream.toByteArray();
    }

    private <T> CompletableFuture<T> executeGet(String url, Class<T> responseClass) {
        HttpRequest request = createRequestBuilder(url).GET().build();
        
        return sendAndParse(request, responseClass, null);
    }

    private <T> CompletableFuture<T> executePostAndParse(String url, Object bodyData, Class<T> responseClass, String extractionKey) {
        String jsonBody = gson.toJson(bodyData);
        
        HttpRequest request = createRequestBuilder(url)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
                
        return sendAndParse(request, responseClass, extractionKey);
    }

    private HttpRequest.Builder createRequestBuilder(String url) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json");
        
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

    private <T> CompletableFuture<T> sendAndParse(HttpRequest request, Class<T> responseType, String extractionKey) {
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