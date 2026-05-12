package com.src.filmtracker.models.moderation;

import com.google.gson.annotations.SerializedName;
import com.src.filmtracker.models.common.PaginationDto;
import java.util.List;

public record MyReportsResponse(
    @SerializedName("reports")
    List<ReportDto> reports,
    PaginationDto pagination
) {
}