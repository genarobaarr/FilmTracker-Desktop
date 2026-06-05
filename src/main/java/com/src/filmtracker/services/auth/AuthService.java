package com.src.filmtracker.services.auth;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.src.filmtracker.models.auth.AccountModeratedException;
import com.src.filmtracker.models.common.ApiResponse;
import com.src.filmtracker.models.auth.AuthResponse;
import com.src.filmtracker.models.auth.ChangePasswordRequest;
import com.src.filmtracker.models.auth.ForgotPasswordRequest;
import com.src.filmtracker.models.auth.LoginRequest;
import com.src.filmtracker.models.auth.RegisterRequest;
import com.src.filmtracker.models.auth.RegisterResponse;
import com.src.filmtracker.models.auth.ResendVerificationRequest;
import com.src.filmtracker.models.auth.ResetPasswordRequest;
import com.src.filmtracker.models.auth.VerifyEmailRequest;
import com.src.filmtracker.utils.AppConstants;
import com.src.filmtracker.utils.SessionManager;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class AuthService implements IAuthService {
    
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String HEADER_ACCEPT = "Accept";
    private static final String HEADER_AUTH = "Authorization";
    private static final String TYPE_JSON = "application/json";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String KEY_MESSAGE = "message";
    private static final String ERROR_SERVER_PREFIX = "Error del servidor: ";
    private static final String STATUS_UNKNOWN = "UNKNOWN";
    private static final String STATUS_BANNED = "BANNED";
    private static final String STATUS_SUSPENDED = "SUSPENDED";
    private static final String KEYWORD_BANNED = "baneada";
    private static final String KEYWORD_SUSPENDED = "suspendida";

    private final HttpClient client;
    private final Gson gson;

    public AuthService() {
        this.client = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    @Override
    public CompletableFuture<AuthResponse> login(LoginRequest request) {
        return executePost(AppConstants.AUTH_LOGIN_URL, request, AuthResponse.class);
    }

    @Override
    public CompletableFuture<RegisterResponse> register(RegisterRequest request) {
        return executePost(AppConstants.AUTH_REGISTER_URL, request, RegisterResponse.class);
    }

    @Override
    public CompletableFuture<AuthResponse> verifyEmail(VerifyEmailRequest request) {
        return executePost(AppConstants.AUTH_VERIFY_EMAIL_URL, request, AuthResponse.class);
    }

    @Override
    public CompletableFuture<ApiResponse<Object>> resendVerification(ResendVerificationRequest request) {
        return executePostApiResponse(AppConstants.AUTH_RESEND_VERIFICATION_URL, request);
    }
    
    @Override
    public CompletableFuture<ApiResponse<Object>> changePassword(ChangePasswordRequest request) {
        String jsonBody = gson.toJson(request);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(AppConstants.AUTH_CHANGE_PASSWORD_URL))
                .header(HEADER_CONTENT_TYPE, TYPE_JSON)
                .header(HEADER_AUTH, BEARER_PREFIX + SessionManager.getInstance().getToken())
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return client.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    evaluarErrorGenerico(response);
                    
                    return gson.fromJson(response.body(), new TypeToken<ApiResponse<Object>>(){}.getType());
                });
    }
    
    @Override
    public CompletableFuture<ApiResponse<Object>> forgotPassword(ForgotPasswordRequest request) {
        return executePostApiResponse(AppConstants.AUTH_FORGOT_PASSWORD_URL, request);
    }

    @Override
    public CompletableFuture<ApiResponse<Object>> resetPassword(ResetPasswordRequest request) {
        return executePostApiResponse(AppConstants.AUTH_RESET_PASSWORD_URL, request);
    }

    private <T> CompletableFuture<T> executePost(String url, Object bodyData, Class<T> responseClass) {
        String jsonBody = gson.toJson(bodyData);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header(HEADER_CONTENT_TYPE, TYPE_JSON)
                .header(HEADER_ACCEPT, TYPE_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> {
                evaluarErrorAutenticacion(response);
                
                return gson.fromJson(response.body(), responseClass);
            });
    }

    private CompletableFuture<ApiResponse<Object>> executePostApiResponse(String url, Object bodyData) {
        String jsonBody = gson.toJson(bodyData);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header(HEADER_CONTENT_TYPE, TYPE_JSON)
                .header(HEADER_ACCEPT, TYPE_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> {
                evaluarErrorAutenticacion(response);
                
                return gson.fromJson(response.body(), new TypeToken<ApiResponse<Object>>(){}.getType());
            });
    }

    private void evaluarErrorAutenticacion(HttpResponse<String> response) {
        int status = response.statusCode();
        
        if (status == 401 || status == 403) {
            lanzarExcepcionAuth(response.body());
        }
        
        evaluarErrorGenerico(response);
    }

    private void evaluarErrorGenerico(HttpResponse<String> response) {
        if (response.statusCode() >= 400) {
            throw new IllegalStateException(ERROR_SERVER_PREFIX + response.statusCode());
        }
    }

    private void lanzarExcepcionAuth(String responseBody) {
        JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
        String message = AppConstants.MESSAGE_ERROR_AUTH;
        
        if (json.has(KEY_MESSAGE)) {
            message = json.get(KEY_MESSAGE).getAsString();
        }
        
        String status = STATUS_UNKNOWN;
        String msgLower = message.toLowerCase();
        
        if (msgLower.contains(KEYWORD_BANNED)) {
            status = STATUS_BANNED;
        } else if (msgLower.contains(KEYWORD_SUSPENDED)) {
            status = STATUS_SUSPENDED;
        }
        
        throw new AccountModeratedException(message, status, message, null);
    }
}