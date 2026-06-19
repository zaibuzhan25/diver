package com.travel.controller;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.travel.model.User;
import com.travel.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepo;

    // 简单的 Token 存储：token -> userId
    private static final ConcurrentHashMap<String, Long> tokenStore = new ConcurrentHashMap<>();

    public AuthController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    /**
     * 登录
     * POST /api/auth/login
     * Body: { "phone": "13800138001", "password": "123456" }
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        String password = body.get("password");

        if (phone == null || phone.isEmpty()) {
            return badRequest("请输入手机号");
        }
        if (password == null || password.isEmpty()) {
            return badRequest("请输入密码");
        }

        User user = userRepo.findByPhone(phone);
        if (user == null) {
            return badRequest("手机号未注册");
        }

        // BCrypt 密码验证
        BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());
        if (!result.verified) {
            return badRequest("密码错误");
        }

        // 生成 Token
        String token = UUID.randomUUID().toString().replace("-", "");
        tokenStore.put(token, user.getId());

        Map<String, Object> resp = new HashMap<>();
        resp.put("token", token);
        resp.put("user", userToMap(user));
        return ResponseEntity.ok(resp);
    }

    /**
     * 注册
     * POST /api/auth/register
     * Body: { "name": "新用户", "phone": "138...", "password": "..." }
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        String phone = body.get("phone");
        String password = body.get("password");

        if (name == null || name.trim().isEmpty()) {
            return badRequest("请输入姓名");
        }
        if (phone == null || phone.trim().isEmpty()) {
            return badRequest("请输入手机号");
        }
        if (password == null || password.length() < 6) {
            return badRequest("密码至少6位");
        }

        if (userRepo.existsByPhone(phone)) {
            return badRequest("该手机号已注册");
        }

        // BCrypt 密码加密
        String hashedPwd = BCrypt.withDefaults().hashToString(12, password.toCharArray());

        User user = new User();
        user.setName(name.trim());
        user.setPhone(phone.trim());
        user.setPassword(hashedPwd);
        user.setCreatedAt(LocalDateTime.now());
        user.setCountry("中国");
        userRepo.save(user);

        // 注册成功自动登录
        String token = UUID.randomUUID().toString().replace("-", "");
        tokenStore.put(token, user.getId());

        Map<String, Object> resp = new HashMap<>();
        resp.put("token", token);
        resp.put("user", userToMap(user));
        return ResponseEntity.ok(resp);
    }

    /**
     * 获取当前登录用户
     * GET /api/auth/me
     * Header: Authorization: Bearer <token>
     */
    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader(value = "Authorization", required = false) String auth) {
        String token = extractToken(auth);
        if (token == null || !tokenStore.containsKey(token)) {
            return badRequest("未登录或登录已过期");
        }

        Long userId = tokenStore.get(token);
        User user = userRepo.findById(userId).orElse(null);
        if (user == null) {
            tokenStore.remove(token);
            return badRequest("用户不存在");
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("user", userToMap(user));
        return ResponseEntity.ok(resp);
    }

    /**
     * 登出
     * POST /api/auth/logout
     * Header: Authorization: Bearer <token>
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String auth) {
        String token = extractToken(auth);
        if (token != null) {
            tokenStore.remove(token);
        }
        Map<String, String> resp = new HashMap<>();
        resp.put("message", "已登出");
        return ResponseEntity.ok(resp);
    }

    /**
     * 从 Authorization 头提取 Token
     */
    private String extractToken(String auth) {
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7);
        }
        return null;
    }

    /**
     * 将用户转为 Map（不含密码）
     */
    private Map<String, Object> userToMap(User u) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", u.getId());
        m.put("name", u.getName());
        m.put("phone", u.getPhone());
        m.put("email", u.getEmail());
        m.put("gender", u.getGender());
        m.put("age", u.getAge());
        m.put("role", u.getRole());
        m.put("notes", u.getNotes());
        m.put("createdAt", u.getCreatedAt());
        return m;
    }

    private ResponseEntity<Map<String, String>> badRequest(String msg) {
        Map<String, String> err = new HashMap<>();
        err.put("error", msg);
        return ResponseEntity.badRequest().body(err);
    }
}
