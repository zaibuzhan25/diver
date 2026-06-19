package com.travel.repository;

import com.travel.model.TravelPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TravelPlanRepository extends JpaRepository<TravelPlan, Long> {

    List<TravelPlan> findByStatus(String status);

    List<TravelPlan> findByUserId(Long userId);

    List<TravelPlan> findByStatusIn(List<String> statuses);

    @Query("SELECT p FROM TravelPlan p WHERE " +
           "(:keyword IS NULL OR p.planName LIKE %:keyword%) AND " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:userId IS NULL OR p.user.id = :userId) " +
           "ORDER BY p.createdAt DESC")
    List<TravelPlan> searchPlans(@Param("keyword") String keyword,
                                  @Param("status") String status,
                                  @Param("userId") Long userId);

    @Query("SELECT p.status, COUNT(p) FROM TravelPlan p GROUP BY p.status")
    List<Object[]> countByStatusGroup();

    @Query("SELECT FUNCTION('YEAR', p.createdAt), FUNCTION('MONTH', p.createdAt), COUNT(p) " +
           "FROM TravelPlan p WHERE p.createdAt >= :since " +
           "GROUP BY FUNCTION('YEAR', p.createdAt), FUNCTION('MONTH', p.createdAt) " +
           "ORDER BY FUNCTION('YEAR', p.createdAt), FUNCTION('MONTH', p.createdAt)")
    List<Object[]> countByMonth(@Param("since") java.time.LocalDateTime since);
}
