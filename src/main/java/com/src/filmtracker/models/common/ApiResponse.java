package com.src.filmtracker.models.common;

public record ApiResponse<T>(String message, T data) {}