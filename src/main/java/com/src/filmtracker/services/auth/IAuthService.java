package com.src.filmtracker.services.auth;

import com.src.filmtracker.models.common.ApiResponse;
import com.src.filmtracker.models.auth.AuthResponse;
import com.src.filmtracker.models.auth.LoginRequest;
import com.src.filmtracker.models.auth.RegisterRequest;
import com.src.filmtracker.models.auth.RegisterResponse;
import com.src.filmtracker.models.auth.ResendVerificationRequest;
import com.src.filmtracker.models.auth.VerifyEmailRequest;

import java.util.concurrent.CompletableFuture;

public interface IAuthService {
    CompletableFuture<AuthResponse> login(LoginRequest request);
    CompletableFuture<RegisterResponse> register(RegisterRequest request);
    CompletableFuture<AuthResponse> verifyEmail(VerifyEmailRequest request);
    CompletableFuture<ApiResponse> resendVerification(ResendVerificationRequest request);
}