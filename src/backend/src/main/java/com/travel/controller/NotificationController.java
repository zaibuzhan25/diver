package com.travel.controller;

import com.travel.model.Notification;
import com.travel.repository.NotificationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationRepository notifRepo;

    public NotificationController(NotificationRepository notifRepo) {
        this.notifRepo = notifRepo;
    }

    @GetMapping
    public ResponseEntity<?> list(@RequestParam Long userId) {
        List<Notification> list = notifRepo.findByUserIdOrderByCreatedAtDesc(userId);
        long unread = notifRepo.countUnreadByUserId(userId);

        List<Map<String, Object>> result = list.stream().map(n -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", n.getId());
            m.put("userId", n.getUserId());
            m.put("type", n.getType());
            m.put("title", n.getTitle());
            m.put("message", n.getMessage());
            m.put("relatedPlanId", n.getRelatedPlanId());
            m.put("isRead", n.getIsRead());
            m.put("createdAt", n.getCreatedAt());
            return m;
        }).toList();

        Map<String, Object> resp = new HashMap<>();
        resp.put("items", result);
        resp.put("unreadCount", unread);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Object>> unreadCount(@RequestParam Long userId) {
        long count = notifRepo.countUnreadByUserId(userId);
        Map<String, Object> resp = new HashMap<>();
        resp.put("unreadCount", count);
        return ResponseEntity.ok(resp);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Map<String, String>> markRead(@PathVariable Long id, @RequestParam Long userId) {
        notifRepo.markAsRead(userId, id);
        Map<String, String> resp = new HashMap<>();
        resp.put("message", "ok");
        return ResponseEntity.ok(resp);
    }

    @PutMapping("/read-all")
    public ResponseEntity<Map<String, String>> markAllRead(@RequestParam Long userId) {
        notifRepo.markAllAsRead(userId);
        Map<String, String> resp = new HashMap<>();
        resp.put("message", "ok");
        return ResponseEntity.ok(resp);
    }

    // ===== 系统公告 =====
    @GetMapping("/announcements")
    public ResponseEntity<?> listAnnouncements() {
        List<Notification> list = notifRepo.findActiveAnnouncements();
        List<Map<String, Object>> result = list.stream().map(n -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", n.getId());
            m.put("title", n.getTitle());
            m.put("message", n.getMessage());
            m.put("createdAt", n.getCreatedAt());
            return m;
        }).toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/announcements/all")
    public ResponseEntity<?> listAllAnnouncements() {
        List<Notification> list = notifRepo.findByTypeOrderByCreatedAtDesc("announcement");
        List<Map<String, Object>> result = list.stream().map(n -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", n.getId());
            m.put("title", n.getTitle());
            m.put("message", n.getMessage());
            m.put("isActive", n.getIsActive());
            m.put("createdAt", n.getCreatedAt());
            return m;
        }).toList();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/announcements")
    public ResponseEntity<Map<String, Object>> createAnnouncement(@RequestBody Map<String, String> body) {
        Notification n = new Notification();
        n.setType("announcement");
        n.setTitle(body.get("title"));
        n.setMessage(body.get("message"));
        n.setIsActive(true);
        n.setCreatedAt(java.time.LocalDateTime.now());
        notifRepo.save(n);
        Map<String, Object> resp = new HashMap<>();
        resp.put("message", "公告已发布");
        resp.put("id", n.getId());
        return ResponseEntity.ok(resp);
    }

    @PutMapping("/announcements/{id}")
    public ResponseEntity<Map<String, String>> updateAnnouncement(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Notification n = notifRepo.findById(id).orElse(null);
        if (n == null) return ResponseEntity.notFound().build();
        if (body.containsKey("title")) n.setTitle(body.get("title"));
        if (body.containsKey("message")) n.setMessage(body.get("message"));
        if (body.containsKey("isActive")) n.setIsActive(Boolean.parseBoolean(body.get("isActive")));
        notifRepo.save(n);
        Map<String, String> resp = new HashMap<>();
        resp.put("message", "公告已更新");
        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/announcements/{id}")
    public ResponseEntity<Map<String, String>> deleteAnnouncement(@PathVariable Long id) {
        Notification n = notifRepo.findById(id).orElse(null);
        if (n == null) return ResponseEntity.notFound().build();
        n.setIsActive(false);
        notifRepo.save(n);
        Map<String, String> resp = new HashMap<>();
        resp.put("message", "公告已下架");
        return ResponseEntity.ok(resp);
    }
}
