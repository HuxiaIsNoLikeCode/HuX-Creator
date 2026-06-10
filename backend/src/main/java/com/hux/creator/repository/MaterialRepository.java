package com.hux.creator.repository;

import com.hux.creator.model.entity.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MaterialRepository extends JpaRepository<Material, Long> {
    List<Material> findBySceneIdAndDeleted(Long sceneId, Integer deleted);
    List<Material> findByShotIdAndDeleted(Long shotId, Integer deleted);
    void deleteBySceneId(Long sceneId);
}
