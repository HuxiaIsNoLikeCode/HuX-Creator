package com.hux.creator.repository;

import com.hux.creator.model.entity.GenerationRound;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface GenerationRoundRepository extends JpaRepository<GenerationRound, Long> {
    List<GenerationRound> findByProjectIdAndDeletedOrderByRoundNumber(Long projectId, Integer deleted);
    Optional<GenerationRound> findByProjectIdAndRoundNumberAndDeleted(Long projectId, Integer roundNumber, Integer deleted);
}
