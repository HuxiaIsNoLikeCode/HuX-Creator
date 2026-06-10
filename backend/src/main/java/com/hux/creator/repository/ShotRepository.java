package com.hux.creator.repository;

import com.hux.creator.model.entity.Shot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ShotRepository extends JpaRepository<Shot, Long> {
    List<Shot> findBySceneIdAndDeletedOrderByShotNumber(Long sceneId, Integer deleted);
    void deleteBySceneId(Long sceneId);
}
