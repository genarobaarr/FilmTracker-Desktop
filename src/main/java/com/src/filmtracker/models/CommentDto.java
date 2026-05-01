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
}