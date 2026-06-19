package com.travel.repository;

import com.travel.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.isRead = false")
    long countUnreadByUserId(Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId AND n.id = :id")
    void markAsRead(Long userId, Long id);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId")
    void markAllAsRead(Long userId);

    // ===== 公告 =====
    @Query("SELECT n FROM Notification n WHERE n.type = 'announcement' AND n.isActive = true ORDER BY n.createdAt DESC")
    List<Notification> findActiveAnnouncements();

    List<Notification> findByTypeOrderByCreatedAtDesc(String type);
}
