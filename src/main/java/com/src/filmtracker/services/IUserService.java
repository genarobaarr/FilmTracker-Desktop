package com.src.filmtracker.services;

import com.src.filmtracker.models.UserDto;
import java.util.concurrent.CompletableFuture;

public interface IUserService {
    CompletableFuture<UserDto> getProfile();
}