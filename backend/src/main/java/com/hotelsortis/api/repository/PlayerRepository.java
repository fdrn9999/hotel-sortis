package com.hotelsortis.api.repository;

import com.hotelsortis.api.entity.Player;
import com.hotelsortis.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    Optional<Player> findByUsername(String username);
    boolean existsByUsername(String username);
    Optional<Player> findByUser(User user);
}
