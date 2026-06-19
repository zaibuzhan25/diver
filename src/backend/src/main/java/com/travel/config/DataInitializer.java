package com.travel.config;

import com.travel.model.Destination;
import com.travel.model.User;
import com.travel.repository.DestinationRepository;
import com.travel.repository.UserRepository;
import at.favre.lib.crypto.bcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepo;
    private final DestinationRepository destRepo;
    private final JdbcTemplate jdbcTemplate;

    public DataInitializer(UserRepository userRepo, DestinationRepository destRepo,
                                JdbcTemplate jdbcTemplate) {
        this.userRepo = userRepo;
        this.destRepo = destRepo;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        // 修复 notifications 表结构（Hibernate ddl-auto=update 不会修改已有列的 nullability）
        try {
            jdbcTemplate.execute("ALTER TABLE notifications MODIFY user_id BIGINT NULL");
            System.out.println("已修复 notifications.user_id 允许 NULL");
        } catch (Exception ignored) {
            // 列已支持 NULL 或表不存在，忽略
        }
        // 清除残留的 editor 角色（本系统只有 admin / user 两个角色）
        try {
            jdbcTemplate.execute("UPDATE users SET role = 'user' WHERE role = 'editor'");
            System.out.println("已清除 editor 角色（改为 user）");
        } catch (Exception ignored) {
            // users 表不存在或列不存在，忽略
        }
        // 确保 is_active 列存在
        try {
            jdbcTemplate.execute("ALTER TABLE notifications ADD COLUMN is_active BIT NOT NULL DEFAULT 1");
            System.out.println("已添加 notifications.is_active 列");
        } catch (Exception ignored) {
            // 列已存在，忽略
        }

        // 确保 favorites 表存在（Hibernate ddl-auto=update 可能不会自动建表）
        try {
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS favorites (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "user_id BIGINT NOT NULL, " +
                    "destination_id BIGINT NOT NULL, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "UNIQUE KEY uk_user_dest (user_id, destination_id), " +
                    "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (destination_id) REFERENCES destinations(id) ON DELETE CASCADE)");
            System.out.println("已确保 favorites 表存在");
        } catch (Exception e) {
            System.out.println("favorites 表检查异常: " + e.getMessage());
        }

        if (destRepo.count() == 0) {
            destRepo.save(createDest("北京", "中国", "北京", "北京", "历史文化名城，故宫、长城所在地", "历史文化"));
            destRepo.save(createDest("上海", "中国", "上海", "上海", "国际化大都市，外滩、东方明珠", "都市体验"));
            destRepo.save(createDest("西藏拉萨", "中国", "西藏", "拉萨", "神秘高原圣地，布达拉宫所在地", "民族文化"));
            destRepo.save(createDest("云南丽江", "中国", "云南", "丽江", "古城与雪山的完美结合", "休闲度假"));
            destRepo.save(createDest("四川成都", "中国", "四川", "成都", "美食之都，熊猫故乡", "美食文化"));
            destRepo.save(createDest("广西桂林", "中国", "广西", "桂林", "山水甲天下，漓江风光", "自然风光"));
            destRepo.save(createDest("新疆喀什", "中国", "新疆", "喀什", "丝路重镇，异域风情", "民族文化"));
            destRepo.save(createDest("海南三亚", "中国", "海南", "三亚", "热带海滨旅游城市", "休闲度假"));
            System.out.println("已预置 8 个目的地数据");
        }
        if (userRepo.count() == 0) {
            User admin = createUser("张伟", "13800138001", "zhangwei@example.com", 34, "男", "高端商务旅客");
            admin.setRole("admin");
            userRepo.save(admin);
            userRepo.save(createUser("李娜", "13900139002", "lina@example.com", 32, "女", "喜欢文化体验"));
            userRepo.save(createUser("王芳", "13700137003", "wangfang@example.com", 37, "女", "亲子游达人"));
            System.out.println("已预置 3 个用户（1个管理员 + 2个旅行者，密码 123456）");
        } else {
            // 兼容旧数据：修复密码为空或角色为空的用户
            int fixed = 0;
            for (User u : userRepo.findAll()) {
                boolean changed = false;
                if (u.getPassword() == null || u.getPassword().isEmpty()) {
                    u.setPassword(BCrypt.withDefaults().hashToString(12, "123456".toCharArray()));
                    changed = true;
                }
                String correctRole = "13800138001".equals(u.getPhone()) ? "admin" : "user";
                if (u.getRole() == null || !correctRole.equals(u.getRole())) {
                    u.setRole(correctRole);
                    changed = true;
                }
                if (changed) { userRepo.save(u); fixed++; }
            }
            if (fixed > 0) {
                System.out.println("已修复 " + fixed + " 个用户的密码/角色");
            }
        }
    }

    private Destination createDest(String name, String country, String province, String city, String desc, String cat) {
        Destination d = new Destination();
        d.setName(name);
        d.setCountry(country);
        d.setProvince(province);
        d.setCity(city);
        d.setDescription(desc);
        d.setCategory(cat);
        return d;
    }

    private User createUser(String name, String phone, String email, int age, String gender, String notes) {
        User u = new User();
        u.setName(name);
        u.setPhone(phone);
        u.setEmail(email);
        u.setAge(age);
        u.setGender(gender);
        u.setNotes(notes);
        u.setPassword(BCrypt.withDefaults().hashToString(12, "123456".toCharArray()));
        return u;
    }
}
