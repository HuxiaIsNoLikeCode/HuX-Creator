package com.hux.creator.repository;

import com.hux.creator.model.entity.Scene;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SceneRepository extends JpaRepository<Scene, Long> {
    List<Scene> findByScriptIdAndDeletedOrderBySceneNumber(Long scriptId, Integer deleted);
    void deleteByScriptId(Long scriptId);
}
