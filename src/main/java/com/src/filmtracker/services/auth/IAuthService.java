package com.src.filmtracker.services.auth;

import com.src.filmtracker.models.auth.*;
import com.src.filmtracker.models.common.ApiResponse;
import java.util.concurrent.CompletableFuture;

public interface IAuthService {
    CompletableFuture<AuthResponse> login(LoginRequest request);
    CompletableFuture<RegisterResponse> register(RegisterRequest request);
    CompletableFuture<AuthResponse> verifyEmail(VerifyEmailRequest request);
    CompletableFuture<ApiResponse<Object>> resendVerification(ResendVerificationRequest request);
    CompletableFuture<ApiResponse<Object>> changePassword(ChangePasswordRequest request);
    CompletableFuture<ApiResponse<Object>> forgotPassword(ForgotPasswordRequest request);
    CompletableFuture<ApiResponse<Object>> resetPassword(ResetPasswordRequest request);
}