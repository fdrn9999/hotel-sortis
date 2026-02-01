package com.hotelsortis.api.repository;

import com.hotelsortis.api.entity.Boss;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BossRepository extends JpaRepository<Boss, String> {

    Optional<Boss> findByFloor(Integer floor);
}
