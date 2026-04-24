package com.bali.backend.repository;

import com.bali.backend.model.VillageGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VillageGroupRepository extends JpaRepository<VillageGroup, Long> {
    Optional<VillageGroup> findByName(String name);
}
