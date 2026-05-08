package com.src.filmtracker.models.shows;

import com.google.gson.annotations.SerializedName;
import com.src.filmtracker.models.common.ImageDto;
import java.util.List;

public record Show(
    @SerializedName(value = "tvmazeId", alternate = {"id", "tvmaze_id"}) 
    Integer tvmazeId,
    
    @SerializedName("name") 
    String name,
    
    @SerializedName("type") 
    String type,
    
    @SerializedName("language") 
    String language,
    
    @SerializedName("genres") 
    List<String> genres,
    
    @SerializedName("status") 
    String status,
    
    @SerializedName("rating") 
    RatingDto rating,
    
    @SerializedName("image") 
    ImageDto image,
    
    @SerializedName("summary") 
    String summary
) {}