package com.src.filmtracker.models.shows;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public record ShowFullResponse(
    @SerializedName("show") 
    Show show,
    
    @SerializedName("cast") 
    List<CastDto> cast,
    
    @SerializedName("seasons") 
    List<SeasonDto> seasons
) {}