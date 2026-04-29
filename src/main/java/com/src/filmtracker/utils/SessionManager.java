package com.src.filmtracker.utils;

import com.src.filmtracker.models.AuthResponse;
import com.src.filmtracker.models.UserDto;

public class SessionManager {
    private static SessionManager instance;
    private UserDto currentUser;
    private String token;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void login(AuthResponse authResponse) {
        this.currentUser = authResponse.data().user();
        this.token = authResponse.data().token();
    }

    public void logout() {
        this.currentUser = null;
        this.token = null;
    }

    public UserDto getCurrentUser() { 
        return currentUser; 
    }
    
    public String getToken() { 
        return token; 
    }
    
    public boolean isAuthenticated() { 
        return token != null && !token.isEmpty(); 
    }
}