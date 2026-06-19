package com.travel.controller;

import com.travel.model.Favorite;
import com.travel.model.User;
import com.travel.model.Destination;
import com.travel.repository.FavoriteRepository;
import com.travel.repository.UserRepository;
import com.travel.repository.DestinationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/favorites")
@CrossOrigin(origins = "*")
public class FavoriteController {

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DestinationRepository destinationRepository;

    // 获取当前用户的收藏列表（从 token 获取用户）
    @GetMapping
    public ResponseEntity<?> getFavorites(@RequestParam Long userId) {
        try {
            List<Favorite> favs = favoriteRepository.findByUser_Id(userId);
            List<Map<String, Object>> result = favs.stream().map(f -> {
                Map<String, Object> m = new HashMap<>();
                Destination d = f.getDestination();
                m.put("id", f.getId());
                m.put("destinationId", d.getId());
                m.put("name", d.getName());
                m.put("category", d.getCategory());
                m.put("country", d.getCountry());
                m.put("city", d.getCity());
                m.put("imageUrl", d.getImageUrl());
                return m;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "获取收藏失败: " + e.getMessage()));
        }
    }

    // 切换收藏状态（收藏/取消收藏）
    @PostMapping("/toggle")
    public ResponseEntity<?> toggleFavorite(@RequestParam Long destinationId,
                                          @RequestParam Long userId) {
        try {
            Favorite existing = favoriteRepository.findByUser_IdAndDestination_Id(userId, destinationId);
            if (existing != null) {
                // 已收藏，取消收藏
                favoriteRepository.delete(existing);
                Map<String, Object> result = new HashMap<>();
                result.put("favorited", false);
                return ResponseEntity.ok(result);
            } else {
                // 未收藏，添加收藏
                Optional<User> userOpt = userRepository.findById(userId);
                Optional<Destination> destOpt = destinationRepository.findById(destinationId);
                if (!userOpt.isPresent()) {
                    return ResponseEntity.badRequest().body(Map.of("error", "用户不存在"));
                }
                if (!destOpt.isPresent()) {
                    return ResponseEntity.badRequest().body(Map.of("error", "目的地不存在"));
                }
                Favorite fav = new Favorite();
                fav.setUser(userOpt.get());
                fav.setDestination(destOpt.get());
                favoriteRepository.save(fav);
                Map<String, Object> result = new HashMap<>();
                result.put("favorited", true);
                return ResponseEntity.ok(result);
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "操作失败: " + e.getMessage()));
        }
    }

    // 取消收藏
    @DeleteMapping("/{destinationId}")
    public ResponseEntity<?> removeFavorite(@PathVariable Long destinationId,
                                           @RequestParam Long userId) {
        try {
            Favorite fav = favoriteRepository.findByUser_IdAndDestination_Id(userId, destinationId);
            if (fav == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "未收藏该目的地"));
            }
            favoriteRepository.delete(fav);
            return ResponseEntity.ok(Map.of("message", "取消收藏成功"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "操作失败: " + e.getMessage()));
        }
    }
}
