package com.hotelsortis.api.repository;

import com.hotelsortis.api.entity.CampaignProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CampaignProgressRepository extends JpaRepository<CampaignProgress, Long> {

    Optional<CampaignProgress> findByPlayerId(Long playerId);
}
