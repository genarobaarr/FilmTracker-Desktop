package com.src.filmtracker.models.reviews;

import com.google.gson.annotations.SerializedName;
import com.src.filmtracker.utils.DtoHelper;

public record CommentDto(
    Object id, 
    Object review_id, 
    Object auth_id,
    String content, 
    Object likes_count, 
    String created_at, 
    String updated_at,
    
    @SerializedName(value = "image_url", alternate = {"imageUrl"})
    String image_url,
    
    @SerializedName(value = "liked_by_me", alternate = {"is_liked", "isLiked", "liked"})
    Object likedByMe
) {
    public String getSafeId() { 
        return DtoHelper.parseSafeId(id);
    }
    
    public String getOwnerId() { 
        return DtoHelper.parseOwnerId(auth_id);
    }

    public String getImageUrl() {
        return DtoHelper.parseImageUrl(image_url);
    }
    
    public int getLikesCount() {
        return DtoHelper.parseCount(likes_count);
    }
    
    public boolean getIsLikedValue() {
        return DtoHelper.parseIsLiked(likedByMe);
    }
}