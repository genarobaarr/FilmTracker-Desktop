package com.src.filmtracker.services.auth;

import com.google.gson.Gson;
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
    public CompletableFuture<ApiResponse> resendVerification(ResendVerificationRequest request) {
        return executePost(AppConstants.AUTH_RESEND_VERIFICATION_URL, request, ApiResponse.class);
    }
    
    @Override
    public CompletableFuture<ApiResponse> changePassword(ChangePasswordRequest request) {
        String jsonBody = gson.toJson(request);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(AppConstants.AUTH_CHANGE_PASSWORD_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return client.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    evaluarErrorGenerico(response);
                    
                    return gson.fromJson(response.body(), ApiResponse.class);
                });
    }
    
    @Override
    public CompletableFuture<ApiResponse> forgotPassword(ForgotPasswordRequest request) {
        return executePost(AppConstants.AUTH_FORGOT_PASSWORD_URL, request, ApiResponse.class);
    }

    @Override
    public CompletableFuture<ApiResponse> resetPassword(ResetPasswordRequest request) {
        return executePost(AppConstants.AUTH_RESET_PASSWORD_URL, request, ApiResponse.class);
    }

    private <T> CompletableFuture<T> executePost(String url, Object bodyData, Class<T> responseClass) {
        String jsonBody = gson.toJson(bodyData);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> {
                evaluarErrorAutenticacion(response);
                
                return gson.fromJson(response.body(), responseClass);
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
            throw new RuntimeException("Error del servidor: " + response.statusCode());
        }
    }

    private void lanzarExcepcionAuth(String responseBody) {
        com.google.gson.JsonObject json = com.google.gson.JsonParser.parseString(responseBody).getAsJsonObject();
        String message = AppConstants.MESSAGE_ERROR_AUTH;
        
        if (json.has("message")) {
            message = json.get("message").getAsString();
        }
        
        String status = "UNKNOWN";
        String msgLower = message.toLowerCase();
        
        if (msgLower.contains("baneada")) {
            status = "BANNED";
        } else if (msgLower.contains("suspendida")) {
            status = "SUSPENDED";
        }
        
        throw new AccountModeratedException(message, status, message, null);
    }
}