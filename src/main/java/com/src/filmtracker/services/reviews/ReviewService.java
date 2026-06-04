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
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class ReviewService implements IReviewService {
    
    private static final String HEADER_ACCEPT = "Accept";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String HEADER_AUTH = "Authorization";
    private static final String TYPE_JSON = "application/json";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String KEY_DATA = "data";
    private static final String KEY_REVIEW = "review";
    private static final String KEY_COMMENT = "comment";
    private static final String ERROR_API = "API Error: ";
    
    private static final String MULTIPART_FORM_DATA = "multipart/form-data; boundary=";
    private static final String CRLF = "\r\n";
    private static final String TWO_DASHES = "--";
    private static final String DEFAULT_MIME_TYPE = "image/jpeg";

    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    @Override
    public CompletableFuture<ReviewPaginationResponse> getShowReviews(Integer tvmazeId, int page) {
        String url = AppConstants.REVIEWS_URL + "/show/" + tvmazeId + "?page=" + page;
        return executeGet(url, ReviewPaginationResponse.class);
    }

    @Override
    public CompletableFuture<ReviewDto> createReview(ReviewRequest request) {
        return executePostAndParse(AppConstants.REVIEWS_URL, request, ReviewDto.class, KEY_REVIEW);
    }

    @Override
    public CompletableFuture<ReviewDto> updateReview(String reviewId, ReviewRequest request) {
        String url = AppConstants.REVIEWS_URL + "/" + reviewId;
        
        HttpRequest req = createRequestBuilder(url)
                .PUT(HttpRequest.BodyPublishers.ofString(gson.toJson(request)))
                .build();
                
        return sendAndParse(req, ReviewDto.class, KEY_REVIEW);
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
        HttpRequest.Builder builder = createRequestBuilder(url);
        
        if (isCurrentlyLiked) {
            builder.DELETE();
        } else {
            builder.POST(HttpRequest.BodyPublishers.noBody());
        }
        
        return sendAndIgnore(builder.build());
    }

    @Override
    public CompletableFuture<Void> toggleCommentLike(String commentId, boolean isCurrentlyLiked) {
        String url = AppConstants.COMMENTS_URL + "/" + commentId + "/like";
        HttpRequest.Builder builder = createRequestBuilder(url);
        
        if (isCurrentlyLiked) {
            builder.DELETE();
        } else {
            builder.POST(HttpRequest.BodyPublishers.noBody());
        }
        
        return sendAndIgnore(builder.build());
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
                    .header(HEADER_CONTENT_TYPE, MULTIPART_FORM_DATA + boundary)
                    .header(HEADER_AUTH, BEARER_PREFIX + SessionManager.getInstance().getToken())
                    .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                    .build();

            return sendAndParse(req, CommentDto.class, KEY_COMMENT);
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
                
        return sendAndParse(req, CommentDto.class, KEY_COMMENT);
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
    public CompletableFuture<ReviewPaginationResponse> getUserReviews(String authId, int page) {
        String url = AppConstants.REVIEWS_URL + "/user/" + authId + "?page=" + page;
        return executeGet(url, ReviewPaginationResponse.class);
    }

    @Override
    public CompletableFuture<ReviewSummaryDto> getUserSummary(String authId) {
        String url = AppConstants.REVIEWS_URL + "/user/" + authId + "/summary";
        
        HttpRequest request = createRequestBuilder(url).GET().build();
        
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(response -> {
            com.src.filmtracker.App.checkHttpResponse(response);
            
            if (response.statusCode() >= 400) {
                return new ReviewSummaryDto(authId, 0, 0);
            }
            
            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            
            if (json.has(KEY_DATA)) {
                return gson.fromJson(json.get(KEY_DATA), ReviewSummaryDto.class);
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
                    .header(HEADER_CONTENT_TYPE, MULTIPART_FORM_DATA + boundary)
                    .header(HEADER_AUTH, BEARER_PREFIX + SessionManager.getInstance().getToken())
                    .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                    .build();

            return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(res -> {
                        com.src.filmtracker.App.checkHttpResponse(res);
                        
                        if (res.statusCode() >= 400) {
                            throw new IllegalStateException("Upload failed: " + res.statusCode());
                        }
                        
                        return null;
                    });
        } catch (java.io.IOException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    private byte[] buildMultipartBodyWithTextAndFile(String content, java.io.File file, String boundary) throws java.io.IOException {
        java.io.ByteArrayOutputStream byteStream = new java.io.ByteArrayOutputStream();
        java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.OutputStreamWriter(byteStream, StandardCharsets.UTF_8), true);

        writer.append(TWO_DASHES).append(boundary).append(CRLF);
        writer.append("Content-Disposition: form-data; name=\"content\"").append(CRLF).append(CRLF);
        writer.append(content).append(CRLF);

        if (file != null) {
            writer.append(TWO_DASHES).append(boundary).append(CRLF);
            writer.append("Content-Disposition: form-data; name=\"image\"; filename=\"").append(file.getName()).append("\"").append(CRLF);

            String mimeType = java.nio.file.Files.probeContentType(file.toPath());
            if (mimeType == null) {
                mimeType = DEFAULT_MIME_TYPE;
            }

            writer.append(HEADER_CONTENT_TYPE).append(": ").append(mimeType).append(CRLF).append(CRLF);
            writer.flush();

            java.nio.file.Files.copy(file.toPath(), byteStream);
            byteStream.flush();

            writer.append(CRLF);
        }

        writer.append(TWO_DASHES).append(boundary).append(TWO_DASHES).append(CRLF);
        writer.flush();

        return byteStream.toByteArray();
    }

    private byte[] buildMultipartBody(java.io.File file, String boundary) throws java.io.IOException {
        java.io.ByteArrayOutputStream byteStream = new java.io.ByteArrayOutputStream();
        java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.OutputStreamWriter(byteStream, StandardCharsets.UTF_8), true);
        
        writer.append(TWO_DASHES).append(boundary).append(CRLF);
        writer.append("Content-Disposition: form-data; name=\"image\"; filename=\"").append(file.getName()).append("\"").append(CRLF);
        
        String mimeType = java.nio.file.Files.probeContentType(file.toPath());
        if (mimeType == null) {
            mimeType = DEFAULT_MIME_TYPE;
        }
        
        writer.append(HEADER_CONTENT_TYPE).append(": ").append(mimeType).append(CRLF).append(CRLF);
        writer.flush();
        
        java.nio.file.Files.copy(file.toPath(), byteStream);
        
        writer.append(CRLF).append(TWO_DASHES).append(boundary).append(TWO_DASHES).append(CRLF);
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
                .header(HEADER_ACCEPT, TYPE_JSON)
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
                throw new IllegalStateException(ERROR_API + res.statusCode());
            }
            
            return null;
        });
    }

    private <T> CompletableFuture<T> sendAndParse(HttpRequest request, Class<T> responseType, String extractionKey) {
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(response -> {
            com.src.filmtracker.App.checkHttpResponse(response);
            
            if (response.statusCode() >= 400) {
                throw new IllegalStateException(ERROR_API + response.statusCode());
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