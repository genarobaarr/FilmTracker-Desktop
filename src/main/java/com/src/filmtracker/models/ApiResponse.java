package com.src.filmtracker.models;

public record ApiResponse<T>(String message, T data) {}