package com.travel.repository;

import com.travel.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByNameContainingOrPhoneContaining(String name, String phone);
    boolean existsByPhone(String phone);
    User findByPhone(String phone);
}
