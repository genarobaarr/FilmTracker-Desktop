package com.src.filmtracker.services.shows;

import com.src.filmtracker.models.shows.ShowFullResponse;
import com.src.filmtracker.models.shows.HomeResponse;
import com.src.filmtracker.models.shows.Show;
import com.src.filmtracker.models.shows.EpisodeDto;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IShowService {
    CompletableFuture<HomeResponse> getHomeData();
    CompletableFuture<List<Show>> searchShows(String query);
    CompletableFuture<ShowFullResponse> getFullShowDetails(Integer id);
    CompletableFuture<List<Show>> getShowsByGenre(String genre);
    CompletableFuture<List<EpisodeDto>> getShowEpisodes(Integer id);
    CompletableFuture<Show> getShowDetails(Integer tvmazeId);
}