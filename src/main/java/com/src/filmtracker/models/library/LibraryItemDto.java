package com.src.filmtracker.models.library;

public record LibraryItemDto(
    Object id, 
    String auth_id, 
    Integer tvmaze_id, 
    String created_at
) {}