package com.src.filmtracker.models;

public record LibraryItemDto(
    Object id, 
    String auth_id, 
    Integer tvmaze_id, 
    String created_at
) {}