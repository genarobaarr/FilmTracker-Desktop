package com.src.filmtracker.models.auth;

public class AccountModeratedException extends RuntimeException {
    
    private final String accountStatus;
    private final String moderationReason;
    private final String suspendedUntil;

    public AccountModeratedException(String message, String accountStatus, String moderationReason, String suspendedUntil) {
        super(message);
        this.accountStatus = accountStatus;
        this.moderationReason = moderationReason;
        this.suspendedUntil = suspendedUntil;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public String getModerationReason() {
        return moderationReason;
    }

    public String getSuspendedUntil() {
        return suspendedUntil;
    }
}