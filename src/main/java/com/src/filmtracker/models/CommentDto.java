package com.src.filmtracker.models;

public record CommentDto(
    String id, 
    String review_id, 
    String auth_id,
    String content, 
    String likes_count, 
    String created_at, 
    String updated_at
) {
    public String getSafeId() 
    { 
        return id != null ? id : ""; 
    }
    
    public String getOwnerId() 
    { 
        return auth_id != null ? auth_id : ""; 
    }
    
    public int getLikesCount() {
        try { 
            return likes_count != null ? Integer.parseInt(likes_count) : 0; 
        }
        catch (Exception e) { 
            return 0; 
        }
    }
}