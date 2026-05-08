package com.src.filmtracker.services;

import com.src.filmtracker.models.ApiResponse;
import com.src.filmtracker.models.AuthResponse;
import com.src.filmtracker.models.LoginRequest;
import com.src.filmtracker.models.ProfileResponse;
import com.src.filmtracker.models.RegisterRequest;
import com.src.filmtracker.models.RegisterResponse;
import com.src.filmtracker.models.ResendVerificationRequest;
import com.src.filmtracker.models.VerifyEmailRequest;

import java.util.concurrent.CompletableFuture;

public interface IAuthService {
    CompletableFuture<AuthResponse> login(LoginRequest request);
    CompletableFuture<RegisterResponse> register(RegisterRequest request);
    CompletableFuture<ProfileResponse> verifyEmail(VerifyEmailRequest request);
    CompletableFuture<ApiResponse> resendVerification(ResendVerificationRequest request);
}