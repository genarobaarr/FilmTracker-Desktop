package com.src.filmtracker.services.moderation;

import com.src.filmtracker.models.moderation.ReportRequest;
import com.src.filmtracker.models.moderation.MyReportsResponse;
import java.util.concurrent.CompletableFuture;

public interface IModerationService {
    CompletableFuture<Void> createReport(ReportRequest request);
    CompletableFuture<MyReportsResponse> getMyReports(int page);
}