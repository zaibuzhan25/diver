package com.travel.controller;

import com.travel.model.TravelPlan;
import com.travel.model.ItineraryItem;
import com.travel.model.Destination;
import com.travel.model.User;
import com.travel.repository.TravelPlanRepository;
import com.travel.repository.ItineraryItemRepository;
import com.travel.repository.DestinationRepository;
import com.travel.model.Notification;
import com.travel.repository.NotificationRepository;
import com.travel.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@RestController
@RequestMapping("/api")
public class TravelPlanController {

    private final TravelPlanRepository planRepo;
    private final ItineraryItemRepository itemRepo;
    private final UserRepository userRepo;
    private final DestinationRepository destRepo;
    private final NotificationRepository notifRepo;

    public TravelPlanController(TravelPlanRepository planRepo,
                                ItineraryItemRepository itemRepo,
                                UserRepository userRepo,
                                DestinationRepository destRepo,
                                NotificationRepository notifRepo) {
        this.planRepo = planRepo;
        this.itemRepo = itemRepo;
        this.userRepo = userRepo;
        this.destRepo = destRepo;
        this.notifRepo = notifRepo;
    }

    // ========== Plans ==========

    @GetMapping("/plans")
    public List<Map<String, Object>> getPlans(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String userId) {

        Long uid = (userId != null && !userId.isEmpty()) ? Long.parseLong(userId) : null;
        String kw = (keyword != null && !keyword.isEmpty()) ? keyword : null;
        String st = (status != null && !status.isEmpty()) ? status : null;

        List<TravelPlan> plans = planRepo.searchPlans(kw, st, uid);
        return plans.stream().map(this::planToSummaryMap).toList();
    }

    @GetMapping("/plans/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getPlan(@PathVariable Long id) {
        TravelPlan plan = planRepo.findById(id).orElse(null);
        if (plan == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(planToDetailMap(plan));
    }

    @PostMapping("/plans")
    public ResponseEntity<Map<String, Object>> createPlan(@RequestBody Map<String, Object> body) {
        TravelPlan plan = new TravelPlan();
        plan.setPlanName((String) body.get("planName"));
        plan.setStartDate(LocalDate.parse((String) body.get("startDate")));
        plan.setEndDate(LocalDate.parse((String) body.get("endDate")));

        long days = ChronoUnit.DAYS.between(plan.getStartDate(), plan.getEndDate()) + 1;
        plan.setTotalDays((int) days);

        User user = userRepo.findById(Long.parseLong(body.get("userId").toString())).orElse(null);
        plan.setUser(user);

        if (body.get("budget") != null) plan.setBudget(parseDouble(body.get("budget")));
        if (body.get("actualCost") != null) plan.setActualCost(parseDouble(body.get("actualCost")));
        if (body.get("status") != null) plan.setStatus((String) body.get("status"));
        if (body.get("transportType") != null) plan.setTransportType((String) body.get("transportType"));
        if (body.get("hotelLevel") != null) plan.setHotelLevel((String) body.get("hotelLevel"));
        if (body.get("groupSize") != null) plan.setGroupSize(parseInt(body.get("groupSize")));
        if (body.get("notes") != null) plan.setNotes((String) body.get("notes"));

        plan = planRepo.save(plan);

        // 关联目的地
        @SuppressWarnings("unchecked")
        List<Object> destIds = (List<Object>) body.get("destinationIds");
        if (destIds != null && !destIds.isEmpty()) {
            final TravelPlan finalPlan = plan;
            for (Object did : destIds) {
                Long destId = Long.parseLong(did.toString());
                destRepo.findById(destId).ifPresent(d -> finalPlan.getDestinations().add(d));
            }
            plan = planRepo.save(plan);
        }

        // itinerary items
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("itinerary");
        if (items != null) {
            for (Map<String, Object> itemMap : items) {
                ItineraryItem item = new ItineraryItem();
                item.setPlan(plan);
                item.setDayNumber(parseInt(itemMap.get("dayNumber")));
                item.setDestinationName((String) itemMap.get("destinationName"));
                item.setActivity((String) itemMap.get("activity"));
                if (itemMap.get("date") != null) item.setDate(LocalDate.parse((String) itemMap.get("date")));
                if (itemMap.get("startTime") != null) item.setStartTime((String) itemMap.get("startTime"));
                if (itemMap.get("endTime") != null) item.setEndTime((String) itemMap.get("endTime"));
                if (itemMap.get("location") != null) item.setLocation((String) itemMap.get("location"));
                if (itemMap.get("cost") != null) item.setCost(parseDouble(itemMap.get("cost")));
                if (itemMap.get("transport") != null) item.setTransport((String) itemMap.get("transport"));
                if (itemMap.get("hotel") != null) item.setHotel((String) itemMap.get("hotel"));
                if (itemMap.get("notes") != null) item.setNotes((String) itemMap.get("notes"));
                if (itemMap.get("sortOrder") != null) item.setSortOrder(parseInt(itemMap.get("sortOrder")));
                itemRepo.save(item);
            }
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("id", plan.getId());
        resp.put("message", "创建成功");
        return ResponseEntity.ok(resp);
    }

    @PutMapping("/plans/{id}")
    public ResponseEntity<Map<String, String>> updatePlan(@PathVariable Long id,
                                                           @RequestBody Map<String, Object> body) {
        TravelPlan plan = planRepo.findById(id).orElse(null);
        if (plan == null) return ResponseEntity.notFound().build();

        plan.setPlanName((String) body.get("planName"));
        plan.setStartDate(LocalDate.parse((String) body.get("startDate")));
        plan.setEndDate(LocalDate.parse((String) body.get("endDate")));
        long days = ChronoUnit.DAYS.between(plan.getStartDate(), plan.getEndDate()) + 1;
        plan.setTotalDays((int) days);

        if (body.get("userId") != null) {
            User user = userRepo.findById(Long.parseLong(body.get("userId").toString())).orElse(null);
            plan.setUser(user);
        }
        if (body.get("budget") != null) plan.setBudget(parseDouble(body.get("budget")));
        if (body.get("actualCost") != null) plan.setActualCost(parseDouble(body.get("actualCost")));
        if (body.get("status") != null) plan.setStatus((String) body.get("status"));
        if (body.get("transportType") != null) plan.setTransportType((String) body.get("transportType"));
        if (body.get("hotelLevel") != null) plan.setHotelLevel((String) body.get("hotelLevel"));
        if (body.get("groupSize") != null) plan.setGroupSize(parseInt(body.get("groupSize")));
        if (body.get("notes") != null) plan.setNotes((String) body.get("notes"));
        plan.setUpdatedAt(LocalDateTime.now());

        planRepo.save(plan);

        Map<String, String> resp = new HashMap<>();
        resp.put("message", "更新成功");
        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/plans/{id}")
    public ResponseEntity<Map<String, String>> deletePlan(@PathVariable Long id) {
        planRepo.deleteById(id);
        Map<String, String> resp = new HashMap<>();
        resp.put("message", "删除成功");
        return ResponseEntity.ok(resp);
    }

    @PatchMapping("/plans/{id}/status")
    public ResponseEntity<Map<String, String>> updatePlanStatus(@PathVariable Long id,
                                                                  @RequestBody Map<String, String> body) {
        TravelPlan plan = planRepo.findById(id).orElse(null);
        if (plan == null) return ResponseEntity.notFound().build();
        String newStatus = body.get("status");
        plan.setStatus(newStatus);
        if (body.containsKey("auditorName")) plan.setAuditorName(body.get("auditorName"));
        if (body.containsKey("auditComment")) plan.setAuditComment(body.get("auditComment"));
        if (body.containsKey("auditTime")) plan.setAuditTime(LocalDateTime.parse(body.get("auditTime")));
        else plan.setAuditTime(LocalDateTime.now());
        plan.setUpdatedAt(LocalDateTime.now());
        planRepo.save(plan);

        // 自动创建通知
        User u = plan.getUser();
        if (u != null) {
            String notifTitle, notifMsg;
            if ("已确认".equals(newStatus)) {
                notifTitle = "方案已通过";
                notifMsg = "您的旅游方案「" + plan.getPlanName() + "」已通过审核";
            } else if ("已驳回".equals(newStatus)) {
                notifTitle = "方案已驳回";
                String reason = body.getOrDefault("auditComment", "");
                notifMsg = "您的旅游方案「" + plan.getPlanName() + "」已被驳回" +
                        (reason.isEmpty() ? "" : "，原因：" + reason);
            } else {
                notifTitle = "方案状态变更";
                notifMsg = "您的旅游方案「" + plan.getPlanName() + "」状态已变更为" + newStatus;
            }
            Notification notif = new Notification(u.getId(), "approval", notifTitle, notifMsg, plan.getId());
            notifRepo.save(notif);
        }

        Map<String, String> resp = new HashMap<>();
        resp.put("message", "操作成功");
        return ResponseEntity.ok(resp);
    }

    // ========== Itinerary Items ==========

    @PostMapping("/plans/{planId}/items")
    public ResponseEntity<Map<String, Object>> addItineraryItem(@PathVariable Long planId,
                                                                  @RequestBody ItineraryItem item) {
        TravelPlan plan = planRepo.findById(planId).orElse(null);
        if (plan == null) return ResponseEntity.notFound().build();
        item.setPlan(plan);
        if (item.getCost() == null) item.setCost(0.0);
        if (item.getSortOrder() == null) item.setSortOrder(0);
        itemRepo.save(item);

        // 自动汇总 actualCost
        Double totalCost = itemRepo.findByPlanIdOrderByDayNumberAscSortOrderAsc(planId)
                .stream()
                .mapToDouble(i -> i.getCost() != null ? i.getCost() : 0)
                .sum();
        plan.setActualCost(totalCost);
        planRepo.save(plan);

        Map<String, Object> resp = new HashMap<>();
        resp.put("id", item.getId());
        resp.put("activity", item.getActivity());
        resp.put("destinationName", item.getDestinationName());
        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<Map<String, String>> deleteItineraryItem(@PathVariable Long id) {
        ItineraryItem item = itemRepo.findById(id).orElse(null);
        if (item == null) {
            Map<String, String> resp = new HashMap<>();
            resp.put("message", "记录不存在");
            return ResponseEntity.ok(resp);
        }
        Long planId = item.getPlan().getId();
        itemRepo.deleteById(id);

        // 重新汇总 actualCost
        TravelPlan plan = planRepo.findById(planId).orElse(null);
        if (plan != null) {
            Double totalCost = itemRepo.findByPlanIdOrderByDayNumberAscSortOrderAsc(planId)
                    .stream()
                    .mapToDouble(i -> i.getCost() != null ? i.getCost() : 0)
                    .sum();
            plan.setActualCost(totalCost);
            planRepo.save(plan);
        }

        Map<String, String> resp = new HashMap<>();
        resp.put("message", "删除成功");
        return ResponseEntity.ok(resp);
    }

    // ========== Stats ==========

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        long totalUsers = userRepo.count();
        long totalPlans = planRepo.count();
        long activePlans = planRepo.findByStatusIn(List.of("已确认", "进行中")).size();
        long completedPlans = planRepo.findByStatus("已完成").size();

        List<TravelPlan> completed = planRepo.findByStatus("已完成");
        double revenue = completed.stream()
                .mapToDouble(p -> p.getActualCost() != null ? p.getActualCost() : 0)
                .sum();

        // 月度趋势（近12个月）
        LocalDateTime twelveMonthsAgo = LocalDateTime.now().minusMonths(12).withDayOfMonth(1).withHour(0).withMinute(0);
        List<Object[]> monthlyRows = planRepo.countByMonth(twelveMonthsAgo);
        List<Map<String, Object>> monthlyTrend = monthlyRows.stream().map(row -> {
            Map<String, Object> m = new HashMap<>();
            String monthStr = row[0] + "-" + String.format("%02d", (Integer) row[1]);
            m.put("month", monthStr);
            m.put("count", row[2]);
            return m;
        }).toList();

        // 热门目的地排行
        List<Object[]> destRows = destRepo.countByPlanCount();
        List<Map<String, Object>> topDestinations = destRows.stream().limit(8).map(row -> {
            Map<String, Object> m = new HashMap<>();
            m.put("name", row[0]);
            m.put("count", row[1]);
            return m;
        }).toList();

        // 状态分布
        List<Object[]> statusDist = planRepo.countByStatusGroup();
        List<Map<String, Object>> distList = statusDist.stream().map(row -> {
            Map<String, Object> m = new HashMap<>();
            m.put("status", row[0]);
            m.put("count", row[1]);
            return m;
        }).toList();

        List<TravelPlan> recent = planRepo.findAll();
        recent.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        List<Map<String, Object>> recentList = recent.stream()
                .limit(5)
                .map(this::planToSummaryMap)
                .toList();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("totalPlans", totalPlans);
        stats.put("activePlans", activePlans);
        stats.put("completedPlans", completedPlans);
        stats.put("totalRevenue", Math.round(revenue * 100.0) / 100.0);
        stats.put("statusDistribution", distList);
        stats.put("monthlyTrend", monthlyTrend);
        stats.put("topDestinations", topDestinations);
        stats.put("recentPlans", recentList);

        return ResponseEntity.ok(stats);
    }

    // ========== Helpers ==========

    private Map<String, Object> planToSummaryMap(TravelPlan p) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", p.getId());
        m.put("planName", p.getPlanName());
        m.put("startDate", p.getStartDate().toString());
        m.put("endDate", p.getEndDate().toString());
        m.put("totalDays", p.getTotalDays());
        m.put("budget", p.getBudget());
        m.put("actualCost", p.getActualCost());
        m.put("status", p.getStatus());
        m.put("transportType", p.getTransportType());
        m.put("hotelLevel", p.getHotelLevel());
        m.put("groupSize", p.getGroupSize());
        m.put("createdAt", p.getCreatedAt());
        User u = p.getUser();
        m.put("userName", u != null ? u.getName() : null);
        m.put("userPhone", u != null ? u.getPhone() : null);
        return m;
    }

    private Map<String, Object> planToDetailMap(TravelPlan p) {
        Map<String, Object> m = planToSummaryMap(p);
        m.put("notes", p.getNotes());
        m.put("transportType", p.getTransportType());
        m.put("auditorName", p.getAuditorName());
        m.put("auditTime", p.getAuditTime());
        m.put("auditComment", p.getAuditComment());

        // 目的地
        List<Map<String, Object>> dests = p.getDestinations().stream().map(d -> {
            Map<String, Object> dm = new HashMap<>();
            dm.put("id", d.getId());
            dm.put("name", d.getName());
            dm.put("province", d.getProvince());
            dm.put("city", d.getCity());
            dm.put("category", d.getCategory());
            return dm;
        }).toList();
        m.put("destinations", dests);

        List<Map<String, Object>> items = p.getItineraryItems().stream().map(i -> {
            Map<String, Object> im = new HashMap<>();
            im.put("id", i.getId());
            im.put("dayNumber", i.getDayNumber());
            im.put("date", i.getDate() != null ? i.getDate().toString() : null);
            im.put("destinationName", i.getDestinationName());
            im.put("activity", i.getActivity());
            im.put("startTime", i.getStartTime());
            im.put("endTime", i.getEndTime());
            im.put("location", i.getLocation());
            im.put("cost", i.getCost());
            im.put("transport", i.getTransport());
            im.put("hotel", i.getHotel());
            im.put("notes", i.getNotes());
            im.put("sortOrder", i.getSortOrder());
            return im;
        }).toList();
        m.put("itinerary", items);
        return m;
    }

    private Double parseDouble(Object val) {
        if (val instanceof Number n) return n.doubleValue();
        if (val instanceof String s) return Double.parseDouble(s);
        return 0.0;
    }

    private Integer parseInt(Object val) {
        if (val instanceof Number n) return n.intValue();
        if (val instanceof String s) return Integer.parseInt(s);
        return 0;
    }
}
