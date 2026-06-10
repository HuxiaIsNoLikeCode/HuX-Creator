package com.hux.creator.repository;

import com.hux.creator.model.entity.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {
    List<Evaluation> findByProjectIdAndDeletedOrderByRoundNumber(Long projectId, Integer deleted);
    Optional<Evaluation> findByProjectIdAndRoundNumberAndDeleted(Long projectId, Integer roundNumber, Integer deleted);
    Optional<Evaluation> findFirstByProjectIdAndDeletedOrderByRoundNumberDesc(Long projectId, Integer deleted);
}
