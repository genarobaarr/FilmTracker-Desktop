package com.src.filmtracker.models;

import com.google.gson.annotations.SerializedName;

public record CommentDto(
    Object id, 
    Object review_id, 
    Object auth_id,
    String content, 
    Object likes_count, 
    String created_at, 
    String updated_at,
    
    @SerializedName(value = "liked_by_me", alternate = {"is_liked", "isLiked", "liked"})
    Object likedByMe
) {
    public String getSafeId() { 
        if (id == null) {
            return "";
        }
        
        String strId = String.valueOf(id);
        
        if (strId.endsWith(".0")) {
            return strId.substring(0, strId.length() - 2);
        }
        
        return strId;
    }
    
    public String getOwnerId() { 
        if (auth_id == null) {
            return "";
        }
        
        return String.valueOf(auth_id);
    }
    
    public int getLikesCount() {
        try { 
            if (likes_count == null) {
                return 0;
            }
            
            String val = String.valueOf(likes_count);
            
            if (val.endsWith(".0")) {
                val = val.substring(0, val.length() - 2);
            }
            
            return Integer.parseInt(val); 
        } catch (Exception e) { 
            return 0; 
        }
    }
    
    public boolean getIsLikedValue() {
        if (likedByMe == null) {
            return false;
        }
        
        if (likedByMe instanceof Boolean) {
            return (Boolean) likedByMe;
        }
        
        String s = likedByMe.toString().trim().toLowerCase();
        
        if (s.equals("true")) {
            return true;
        }
        
        if (s.equals("1")) {
            return true;
        }
        
        if (s.equals("1.0")) {
            return true;
        }
        
        return false;
    }
}