package com.travel.repository;

import com.travel.model.Favorite;
import com.travel.model.User;
import com.travel.model.Destination;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Favorite> findByUser(User user);
    List<Favorite> findByUser_Id(Long userId);
    Favorite findByUser_IdAndDestination_Id(Long userId, Long destinationId);
    void deleteByUser_IdAndDestination_Id(Long userId, Long destinationId);
}
