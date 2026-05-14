package com.src.filmtracker.models.admin;

import com.google.gson.annotations.SerializedName;
import com.src.filmtracker.models.common.PaginationDto;
import java.util.List;

public record AdminReportResponse(
    @SerializedName("reports") List<AdminReportDto> reports,
    PaginationDto pagination
) {
}