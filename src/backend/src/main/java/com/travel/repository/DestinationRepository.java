package com.travel.repository;

import com.travel.model.Destination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DestinationRepository extends JpaRepository<Destination, Long> {
    List<Destination> findByCategory(String category);

    @Query("SELECT d FROM Destination d WHERE " +
           "(:kw IS NULL OR d.name LIKE %:kw% OR d.province LIKE %:kw% OR d.city LIKE %:kw% OR d.description LIKE %:kw%) " +
           "AND (:cat IS NULL OR d.category = :cat)")
    List<Destination> search(@Param("kw") String kw, @Param("cat") String cat);

    @Query("SELECT d.name, COUNT(p) FROM TravelPlan p JOIN p.destinations d GROUP BY d.name ORDER BY COUNT(p) DESC")
    List<Object[]> countByPlanCount();
}
