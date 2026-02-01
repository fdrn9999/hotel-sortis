package com.hotelsortis.api.repository;

import com.hotelsortis.api.entity.Floor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FloorRepository extends JpaRepository<Floor, Integer> {

    List<Floor> findAllByOrderByIdAsc();

    List<Floor> findByFloorType(Floor.FloorType floorType);
}
