package com.src.filmtracker.services;

import com.src.filmtracker.models.HomeResponse;
import com.src.filmtracker.models.Show;
import com.src.filmtracker.models.ShowFullResponse;
import com.src.filmtracker.models.EpisodeDto;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IShowService {
    CompletableFuture<HomeResponse> getHomeData();
    CompletableFuture<List<Show>> searchShows(String query);
    CompletableFuture<ShowFullResponse> getFullShowDetails(Integer id);
    CompletableFuture<List<EpisodeDto>> getShowEpisodes(Integer id);
}