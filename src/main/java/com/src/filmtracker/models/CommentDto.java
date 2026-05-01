package com.src.filmtracker.models;

public record CommentDto(
    Object id, 
    Object review_id, 
    Object auth_id,
    String content, 
    Object likes_count, 
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
}