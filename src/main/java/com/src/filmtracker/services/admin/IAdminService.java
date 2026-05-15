package com.src.filmtracker.services.admin;

import com.src.filmtracker.models.admin.AccountStatusDto;
import com.src.filmtracker.models.admin.AdminReportResponse;
import com.src.filmtracker.models.admin.AdminActionRequest;
import com.src.filmtracker.models.admin.AuthStatsDto;
import com.src.filmtracker.models.admin.ModerationStatsDto;
import com.src.filmtracker.models.admin.ReviewStatsDto;
import com.src.filmtracker.models.users.UserDto;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IAdminService {
    CompletableFuture<List<UserDto>> searchUsers(String query);
    CompletableFuture<AccountStatusDto> getAccountStatus(String authId);
    CompletableFuture<Void> suspendUser(String authId, String duration, String reason);
    CompletableFuture<Void> banUser(String authId, String reason);
    CompletableFuture<Void> unbanUser(String authId);
    CompletableFuture<AdminReportResponse> getAdminReports(String status, int page);
    CompletableFuture<Void> executeReportAction(String reportId, AdminActionRequest request);
    CompletableFuture<Void> dismissReport(String reportId, String note);
    CompletableFuture<Void> deleteReviewDirectly(String reviewId);
    CompletableFuture<AuthStatsDto> getAuthStats();
    CompletableFuture<ReviewStatsDto> getReviewStats();
    CompletableFuture<ModerationStatsDto> getModerationStats();
    CompletableFuture<com.src.filmtracker.models.admin.AdminUserDetailDto> getAdminUserDetails(String authId);
    CompletableFuture<Void> removeProfilePhotoDirectly(String authId);
    CompletableFuture<Void> removeReviewImageDirectly(String reviewId);
    CompletableFuture<Void> removeCommentImageDirectly(String commentId);
    CompletableFuture<Void> deleteCommentDirectly(String commentId);
}