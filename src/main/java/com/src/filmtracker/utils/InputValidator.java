package com.src.filmtracker.utils;

import java.util.regex.Pattern;

public class InputValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d\\w\\W]{8,}$");

    public static boolean isNullOrEmpty(String input) {
        if (input == null) {
            return true;
        }
        
        if (input.trim().isEmpty()) {
            return true;
        }
        
        return false;
    }

    public static boolean isValidEmail(String email) {
        if (isNullOrEmpty(email)) {
            return false;
        }
        
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidUsername(String username) {
        if (isNullOrEmpty(username)) {
            return false;
        }
        
        return USERNAME_PATTERN.matcher(username).matches();
    }

    public static boolean isValidPassword(String password) {
        if (isNullOrEmpty(password)) {
            return false;
        }
        
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    public static boolean exceedsMaxLength(String input, int maxLength) {
        if (isNullOrEmpty(input)) {
            return false;
        }
        
        if (input.length() > maxLength) {
            return true;
        }
        
        return false;
    }
}