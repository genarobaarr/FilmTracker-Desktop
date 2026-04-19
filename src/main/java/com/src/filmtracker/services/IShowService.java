package com.src.filmtracker.services;

import com.src.filmtracker.models.HomeResponse;
import java.util.concurrent.CompletableFuture;

public interface IShowService {
    CompletableFuture<HomeResponse> getHomeData();
}