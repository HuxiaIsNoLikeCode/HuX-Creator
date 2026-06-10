package com.hux.creator.repository;

import com.hux.creator.model.entity.ContentPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContentPlanRepository extends JpaRepository<ContentPlan, Long> {

    List<ContentPlan> findByProjectId(Long projectId);

    Optional<ContentPlan> findFirstByProjectIdOrderByCreateTimeDesc(Long projectId);

    List<ContentPlan> findByProjectIdAndStatus(Long projectId, Integer status);
}