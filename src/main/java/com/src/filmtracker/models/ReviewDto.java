package com.src.filmtracker.models;

public record ReviewDto(
    String id, 
    String auth_id,
    Integer tvmaze_id, 
    Integer rating, 
    String title, 
    String content, 
    String likes_count,
    String comments_count,
    String created_at, 
    String updated_at
) {
    public String getSafeId() { 
        return id != null ? id : ""; 
    }
    
    public String getOwnerId() { 
        return auth_id != null ? auth_id : ""; 
    }
    
    public int getLikesCount() {
        try { return likes_count != null ? Integer.parseInt(likes_count) : 0; }
        catch (Exception e) { 
            return 0; 
        }
    }
    
    public int getCommentsCount() {
        try { 
            return comments_count != null ? Integer.parseInt(comments_count) : 0; 
        }
        catch (Exception e) { 
            return 0; 
        }
    }
}