package com.src.filmtracker.services;

import com.src.filmtracker.models.LibraryItemDto;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ILibraryService {
    CompletableFuture<List<LibraryItemDto>> getFavorites();
    CompletableFuture<Void> addFavorite(Integer tvmazeId);
    CompletableFuture<Void> removeFavorite(Integer tvmazeId);
    
    CompletableFuture<List<LibraryItemDto>> getWatchlist();
    CompletableFuture<Void> addWatchlist(Integer tvmazeId);
    CompletableFuture<Void> removeWatchlist(Integer tvmazeId);
}