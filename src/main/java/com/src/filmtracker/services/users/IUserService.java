package com.src.filmtracker.services.users;

import com.src.filmtracker.models.users.UserDto;
import com.src.filmtracker.models.users.UpdateProfileRequest;
import java.util.concurrent.CompletableFuture;

public interface IUserService {
    CompletableFuture<UserDto> getProfile();
    CompletableFuture<UserDto> getUserById(String authId);
    CompletableFuture<UserDto> getUserByUsername(String username);
    CompletableFuture<UserDto> updateProfile(UpdateProfileRequest request);
}