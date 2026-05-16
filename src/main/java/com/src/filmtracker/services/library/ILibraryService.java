package com.src.filmtracker.services.library;

import com.src.filmtracker.models.library.LibraryItemDto;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ILibraryService {
    CompletableFuture<List<LibraryItemDto>> getFavorites();
    CompletableFuture<List<LibraryItemDto>> getFavoritesByUser(String authId);
    CompletableFuture<Void> addFavorite(Integer tvmazeId);
    CompletableFuture<Void> removeFavorite(Integer tvmazeId);
    
    CompletableFuture<List<LibraryItemDto>> getWatchlist();
    CompletableFuture<Void> addWatchlist(Integer tvmazeId);
    CompletableFuture<Void> removeWatchlist(Integer tvmazeId);
    CompletableFuture<List<LibraryItemDto>> getFavoritesPaged(int page);
    CompletableFuture<List<LibraryItemDto>> getFavoritesByUserPaged(String authId, int page);
    CompletableFuture<List<LibraryItemDto>> getWatchlistPaged(int page);
}