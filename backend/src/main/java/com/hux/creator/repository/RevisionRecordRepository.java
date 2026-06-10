package com.hux.creator.repository;

import com.hux.creator.model.entity.RevisionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RevisionRecordRepository extends JpaRepository<RevisionRecord, Long> {

    List<RevisionRecord> findByProjectId(Long projectId);

    List<RevisionRecord> findByContentPlanId(Long contentPlanId);

    List<RevisionRecord> findByContentPlanIdAndSectionName(Long contentPlanId, String sectionName);
}