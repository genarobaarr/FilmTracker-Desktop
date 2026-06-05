package com.src.filmtracker.utils;

import java.util.regex.Pattern;

public final class InputValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^\\w{3,20}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");

    private InputValidator() {
        // Constructor oculto por ser clase utilitaria estática
    }

    public static boolean isNullOrEmpty(String input) {
        return input == null || input.trim().isEmpty();
    }

    public static boolean isValidEmail(String email) {
        return !isNullOrEmpty(email) && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidUsername(String username) {
        return !isNullOrEmpty(username) && USERNAME_PATTERN.matcher(username).matches();
    }

    public static boolean isValidPassword(String password) {
        return !isNullOrEmpty(password) && PASSWORD_PATTERN.matcher(password).matches();
    }

    public static boolean exceedsMaxLength(String input, int maxLength) {
        return !isNullOrEmpty(input) && input.length() > maxLength;
    }
}