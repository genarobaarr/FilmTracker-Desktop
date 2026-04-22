package com.src.filmtracker.services;

import com.src.filmtracker.models.AuthResponse;
import com.src.filmtracker.models.LoginRequest;
import com.src.filmtracker.models.RegisterRequest;
import com.src.filmtracker.models.RegisterResponse;

import java.util.concurrent.CompletableFuture;

public interface IAuthService {
    CompletableFuture<AuthResponse> login(LoginRequest request);
    CompletableFuture<RegisterResponse> register(RegisterRequest request);
}