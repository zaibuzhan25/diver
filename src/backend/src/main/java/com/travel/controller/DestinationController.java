package com.travel.controller;

import com.travel.model.Destination;
import com.travel.repository.DestinationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/destinations")
public class DestinationController {

    private final DestinationRepository destRepo;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public DestinationController(DestinationRepository destRepo) {
        this.destRepo = destRepo;
    }

    @GetMapping
    public List<Destination> getDestinations(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {
        if (keyword == null && category == null) return destRepo.findAll();
        String kw = (keyword != null && !keyword.isEmpty()) ? keyword : null;
        String cat = (category != null && !category.isEmpty()) ? category : null;
        return destRepo.search(kw, cat);
    }

    @GetMapping("/{id}")
    public Destination getDestination(@PathVariable Long id) {
        return destRepo.findById(id).orElseThrow(() -> new RuntimeException("目的地不存在"));
    }

    @PutMapping("/{id}")
    public Destination updateDestination(@PathVariable Long id, @RequestBody Destination data) {
        return destRepo.findById(id).map(d -> {
            d.setName(data.getName());
            d.setCountry(data.getCountry());
            d.setProvince(data.getProvince());
            d.setCity(data.getCity());
            d.setCategory(data.getCategory());
            d.setDescription(data.getDescription());
            return destRepo.save(d);
        }).orElseThrow(() -> new RuntimeException("目的地不存在"));
    }

    @PostMapping
    public Destination createDestination(@RequestBody Destination dest) {
        if (dest.getCountry() == null) dest.setCountry("中国");
        return destRepo.save(dest);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteDestination(@PathVariable Long id) {
        Destination d = destRepo.findById(id).orElse(null);
        if (d == null) {
            return ResponseEntity.notFound().build();
        }
        // 直接通过 SQL 删除关联表记录（避免懒加载异常）
        try {
            jdbcTemplate.update("DELETE FROM plan_destinations WHERE destination_id = ?", id);
        } catch (Exception ignored) {
            // 关联表不存在或无数据，忽略
        }
        destRepo.delete(d);
        Map<String, String> resp = new HashMap<>();
        resp.put("message", "删除成功");
        return ResponseEntity.ok(resp);
    }
}
