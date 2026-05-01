package com.src.filmtracker.models;

public record ReviewDto(
    Object id, 
    Object auth_id,
    Integer tvmaze_id, 
    Integer rating, 
    String title, 
    String content, 
    Object likes_count,
    Object comments_count,
    String created_at, 
    String updated_at
) {
    public String getSafeId() { 
        return id != null ? String.valueOf(id) : ""; 
    }
    
    public String getOwnerId() { 
        return auth_id != null ? String.valueOf(auth_id) : ""; 
    }
    
    public int getLikesCount() {
        try { 
            return likes_count != null ? Integer.parseInt(String.valueOf(likes_count)) : 0; 
        }
        catch (Exception e) { 
            return 0; 
        }
    }
    
    public int getCommentsCount() {
        try { 
            return comments_count != null ? Integer.parseInt(String.valueOf(comments_count)) : 0; 
        }
        catch (Exception e) { 
            return 0; 
        }
    }
}