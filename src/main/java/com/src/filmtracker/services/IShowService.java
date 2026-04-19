package com.src.filmtracker.services;

import com.src.filmtracker.models.HomeResponse;
import com.src.filmtracker.models.Show;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IShowService {
    CompletableFuture<HomeResponse> getHomeData();
    CompletableFuture<List<Show>> searchShows(String query);
}