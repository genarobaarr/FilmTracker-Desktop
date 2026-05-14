package com.src.filmtracker.services.notifications;

import com.src.filmtracker.models.notifications.NotificationResponse;
import com.src.filmtracker.models.notifications.UnreadCountResponse;
import java.util.concurrent.CompletableFuture;

public interface INotificationService {
    CompletableFuture<NotificationResponse> getNotifications(int page);
    CompletableFuture<UnreadCountResponse> getUnreadCount();
    CompletableFuture<Void> markAsRead(Integer notificationId);
    CompletableFuture<Void> markAllAsRead();
    CompletableFuture<Void> deleteNotification(Integer notificationId);
}