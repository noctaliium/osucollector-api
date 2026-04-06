package com.osucollector.api.user;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByOsuUserId(Integer osuUserId);
    Optional<User> findByUsername(String username);
    boolean existsByOsuUserId(Integer osuUserId);
}