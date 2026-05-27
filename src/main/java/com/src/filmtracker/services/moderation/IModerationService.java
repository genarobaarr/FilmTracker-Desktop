package com.src.filmtracker.services.moderation;

import com.src.filmtracker.models.moderation.ReportRequest;
import java.util.concurrent.CompletableFuture;

public interface IModerationService {
    CompletableFuture<Void> createReport(ReportRequest request);
}