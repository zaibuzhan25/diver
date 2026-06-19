package com.travel.controller;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.travel.model.User;
import com.travel.model.TravelPlan;
import com.travel.repository.UserRepository;
import com.travel.repository.TravelPlanRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepo;
    private final TravelPlanRepository planRepo;

    public UserController(UserRepository userRepo, TravelPlanRepository planRepo) {
        this.userRepo = userRepo;
        this.planRepo = planRepo;
    }

    @GetMapping
    public List<User> getUsers(@RequestParam(required = false) String keyword) {
        if (keyword != null && !keyword.isEmpty()) {
            return userRepo.findByNameContainingOrPhoneContaining(keyword, keyword);
        }
        return userRepo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        User user = userRepo.findById(id).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();

        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("name", user.getName());
        result.put("phone", user.getPhone());
        result.put("email", user.getEmail());
        result.put("age", user.getAge());
        result.put("gender", user.getGender());
        result.put("role", user.getRole());
        result.put("notes", user.getNotes());
        result.put("createdAt", user.getCreatedAt());
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
        // 验证姓名：不能为空，2-20个字符，只能是中文/英文/空格
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            return badRequest("姓名不能为空");
        }
        String name = user.getName().trim();
        if (name.length() < 2 || name.length() > 20) {
            return badRequest("姓名长度需在2-20个字符之间");
        }
        if (!name.matches("^[\\u4e00-\\u9fa5a-zA-Z·\\s]+$")) {
            return badRequest("姓名只能包含中文、英文和空格");
        }
        user.setName(name);

        // 验证手机号：11位中国大陆手机号
        if (user.getPhone() == null || user.getPhone().trim().isEmpty()) {
            return badRequest("手机号不能为空");
        }
        String phone = user.getPhone().trim();
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            return badRequest("请输入正确的11位手机号");
        }
        // 手机号不可重复
        if (userRepo.existsByPhone(phone)) {
            return badRequest("该手机号已存在");
        }
        user.setPhone(phone);

        // 验证邮箱格式（如果填写了的话）
        if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
            String email = user.getEmail().trim();
            if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
                return badRequest("邮箱格式不正确");
            }
            user.setEmail(email);
        }

        user.setCreatedAt(LocalDateTime.now());
        if (user.getCountry() == null) user.setCountry("中国");
        if (user.getRole() == null || user.getRole().isEmpty()) user.setRole("user");
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            user.setPassword(BCrypt.withDefaults().hashToString(12, "123456".toCharArray()));
        }
        return ResponseEntity.ok(userRepo.save(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User user) {
        User existing = userRepo.findById(id).orElse(null);
        if (existing == null) return ResponseEntity.notFound().build();

        // 验证姓名
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            return badRequest("姓名不能为空");
        }
        String name = user.getName().trim();
        if (name.length() < 2 || name.length() > 20) {
            return badRequest("姓名长度需在2-20个字符之间");
        }
        if (!name.matches("^[\\u4e00-\\u9fa5a-zA-Z·\\s]+$")) {
            return badRequest("姓名只能包含中文、英文和空格");
        }
        existing.setName(name);

        // 验证手机号
        if (user.getPhone() == null || user.getPhone().trim().isEmpty()) {
            return badRequest("手机号不能为空");
        }
        String phone = user.getPhone().trim();
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            return badRequest("请输入正确的11位手机号");
        }
        // 手机号不可与其他人重复
        User phoneUser = userRepo.findByPhone(phone);
        if (phoneUser != null && !phoneUser.getId().equals(id)) {
            return badRequest("该手机号已被其他用户使用");
        }
        existing.setPhone(phone);

        // 验证邮箱格式
        if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
            String email = user.getEmail().trim();
            if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
                return badRequest("邮箱格式不正确");
            }
            existing.setEmail(email);
        } else {
            existing.setEmail(null);
        }

        existing.setAge(user.getAge());
        existing.setGender(user.getGender());
        existing.setNotes(user.getNotes());
        // 角色更新（仅允许标准值）
        if (user.getRole() != null && !user.getRole().isEmpty()) {
            String role = user.getRole().trim();
            if (!role.equals("user") && !role.equals("admin")) {
                return badRequest("无效的角色值，允许：user, admin");
            }
            existing.setRole(role);
        }
        if (user.getCountry() != null) existing.setCountry(user.getCountry());
        return ResponseEntity.ok(userRepo.save(existing));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        userRepo.deleteById(id);
        Map<String, String> resp = new HashMap<>();
        resp.put("message", "删除成功");
        return ResponseEntity.ok(resp);
    }

    private ResponseEntity<Map<String, String>> badRequest(String msg) {
        Map<String, String> err = new HashMap<>();
        err.put("error", msg);
        return ResponseEntity.badRequest().body(err);
    }
}
