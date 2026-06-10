package com.hux.creator.repository;

import com.hux.creator.model.entity.Script;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ScriptRepository extends JpaRepository<Script, Long> {
    List<Script> findByProjectIdAndDeletedOrderByRoundDesc(Long projectId, Integer deleted);
    Optional<Script> findByProjectIdAndStatusAndDeleted(Long projectId, Integer status, Integer deleted);
    Optional<Script> findFirstByProjectIdAndDeletedOrderByRoundDesc(Long projectId, Integer deleted);
}
