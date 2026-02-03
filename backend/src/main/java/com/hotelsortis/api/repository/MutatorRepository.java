package com.hotelsortis.api.repository;

import com.hotelsortis.api.entity.Mutator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MutatorRepository extends JpaRepository<Mutator, String> {
}
