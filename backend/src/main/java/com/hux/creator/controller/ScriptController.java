package com.hux.creator.controller;

import com.hux.creator.common.Result;
import com.hux.creator.model.entity.Material;
import com.hux.creator.model.entity.Scene;
import com.hux.creator.model.entity.Script;
import com.hux.creator.model.entity.Shot;
import com.hux.creator.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/script")
@RequiredArgsConstructor
public class ScriptController {

    private final ScriptRepository scriptRepository;
    private final SceneRepository sceneRepository;
    private final ShotRepository shotRepository;
    private final MaterialRepository materialRepository;
    private final ProjectRepository projectRepository;

    @GetMapping("/{projectId}")
    public Result<Map<String, Object>> getScript(@PathVariable Long projectId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        projectRepository.findByIdAndDeleted(projectId, 0)
                .filter(p -> p.getUserId().equals(userId))
                .orElseThrow(() -> new RuntimeException("项目不存在或无权访问"));

        Script script = scriptRepository.findFirstByProjectIdAndDeletedOrderByRoundDesc(projectId, 0)
                .orElse(null);
        if (script == null) {
            return Result.success(null);
        }

        List<Scene> scenes = sceneRepository.findByScriptIdAndDeletedOrderBySceneNumber(script.getId(), 0);
        
        Map<String, Object> result = new HashMap<>();
        result.put("script", script);
        result.put("scenes", scenes);
        return Result.success(result);
    }

    @GetMapping("/scene/{sceneId}/shots")
    public Result<List<Shot>> getShots(@PathVariable Long sceneId) {
        List<Shot> shots = shotRepository.findBySceneIdAndDeletedOrderByShotNumber(sceneId, 0);
        return Result.success(shots);
    }

    @GetMapping("/scene/{sceneId}/materials")
    public Result<List<Material>> getMaterials(@PathVariable Long sceneId) {
        List<Material> materials = materialRepository.findBySceneIdAndDeleted(sceneId, 0);
        return Result.success(materials);
    }

    @GetMapping("/shot/{shotId}/materials")
    public Result<List<Material>> getShotMaterials(@PathVariable Long shotId) {
        List<Material> materials = materialRepository.findByShotIdAndDeleted(shotId, 0);
        return Result.success(materials);
    }
}