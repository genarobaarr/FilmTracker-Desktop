package com.src.filmtracker.utils;

import com.src.filmtracker.models.auth.AuthResponse;
import com.src.filmtracker.models.users.UserDto;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import java.util.concurrent.ScheduledExecutorService;

public class SessionManager {
    
    private static SessionManager instance;
    private UserDto currentUser;
    private String token;
    private ScheduledExecutorService scheduler;
    private Runnable onExpirationCallback;

    private SessionManager() {
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        
        return instance;
    }

    public void setOnExpirationCallback(Runnable callback) {
        this.onExpirationCallback = callback;
    }

    public void login(AuthResponse authResponse) {
        if (authResponse != null) {
            if (authResponse.data() != null) {
                this.currentUser = authResponse.data().user();
                this.token = authResponse.data().token();
                iniciarTemporizadorSesion();
            }
        }
    }
    
    private void iniciarTemporizadorSesion() {
        detenerTemporizadorSesion();
        
        this.scheduler = newSingleThreadScheduledExecutor();
        this.scheduler.schedule(() -> {
            if (this.onExpirationCallback != null) {
                this.onExpirationCallback.run();
            }
        }, 15, java.util.concurrent.TimeUnit.MINUTES);
    }
    
    private void detenerTemporizadorSesion() {
        if (this.scheduler != null) {
            if (!this.scheduler.isShutdown()) {
                this.scheduler.shutdownNow();
            }
        }
    }
    
    public void updateUser(UserDto updatedUser) {
        if (updatedUser != null) {
            this.currentUser = updatedUser;
        }
    }

    public void logout() {
        this.currentUser = null;
        this.token = null;
        detenerTemporizadorSesion();
    }

    public UserDto getCurrentUser() { 
        return currentUser; 
    }
    
    public String getToken() { 
        return token; 
    }
    
    public boolean isAuthenticated() { 
        if (token != null) {
            if (!token.isEmpty()) {
                return true;
            }
        }
        
        return false;
    }
}