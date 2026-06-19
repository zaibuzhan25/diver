package com.travel.repository;

import com.travel.model.ItineraryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ItineraryItemRepository extends JpaRepository<ItineraryItem, Long> {
    List<ItineraryItem> findByPlanIdOrderByDayNumberAscSortOrderAsc(Long planId);
    void deleteByPlanId(Long planId);
}
