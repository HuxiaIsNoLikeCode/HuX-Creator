package com.hux.creator.controller;

import com.hux.creator.common.Result;
import com.hux.creator.ai.ModelAdapter;
import com.hux.creator.ai.ModelAdapterRegistry;
import com.hux.creator.model.entity.Material;
import com.hux.creator.model.entity.Scene;
import com.hux.creator.model.entity.Shot;
import com.hux.creator.repository.MaterialRepository;
import com.hux.creator.repository.SceneRepository;
import com.hux.creator.repository.ShotRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/frame")
@RequiredArgsConstructor
public class FrameGenerationController {

    private final SceneRepository sceneRepository;
    private final ShotRepository shotRepository;
    private final MaterialRepository materialRepository;

    @PostMapping("/scene/{sceneId}/background")
    public Result<Map<String, String>> generateSceneBackground(
            @PathVariable Long sceneId,
            @RequestBody(required = false) Map<String, String> body,
            HttpServletRequest request) {

        Scene scene = sceneRepository.findById(sceneId)
                .orElseThrow(() -> new RuntimeException("场景不存在"));

        String customPrompt = body != null ? body.get("prompt") : null;
        String prompt = customPrompt != null ? customPrompt :
            String.format("电影级场景背景，%s，%s氛围，16:9宽幅，高品质",
                scene.getLocation() != null ? scene.getLocation() : "室内场景",
                scene.getMood() != null ? scene.getMood() : "温馨");

        scene.setBackgroundPrompt(prompt);
        scene.setBackgroundUrl("pending://" + sceneId);
        sceneRepository.save(scene);

        Map<String, String> result = new HashMap<>();
        result.put("prompt", prompt);
        result.put("status", "prompt_generated");
        return Result.success(result);
    }

    @PostMapping("/shot/{shotId}/first-frame")
    public Result<Map<String, String>> generateFirstFrame(
            @PathVariable Long shotId,
            @RequestBody(required = false) Map<String, String> body,
            HttpServletRequest request) {

        Shot shot = shotRepository.findById(shotId)
                .orElseThrow(() -> new RuntimeException("镜头不存在"));

        String customPrompt = body != null ? body.get("prompt") : null;
        String prompt = customPrompt != null ? customPrompt :
            String.format("电影级画面，%s，%s角度，%s运镜，高品质，16:9",
                shot.getDescription() != null ? shot.getDescription() : "场景画面",
                shot.getCameraAngle() != null ? shot.getCameraAngle() : "中景",
                shot.getCameraMovement() != null ? shot.getCameraMovement() : "固定");

        shot.setFirstFramePrompt(prompt);
        shot.setFirstFrameUrl("pending://" + shotId + "-first");
        shotRepository.save(shot);

        Map<String, String> result = new HashMap<>();
        result.put("prompt", prompt);
        result.put("status", "prompt_generated");
        return Result.success(result);
    }

    @PostMapping("/shot/{shotId}/last-frame")
    public Result<Map<String, String>> generateLastFrame(
            @PathVariable Long shotId,
            @RequestBody(required = false) Map<String, String> body,
            HttpServletRequest request) {

        Shot shot = shotRepository.findById(shotId)
                .orElseThrow(() -> new RuntimeException("镜头不存在"));

        String customPrompt = body != null ? body.get("prompt") : null;
        String prompt = customPrompt != null ? customPrompt :
            String.format("电影级画面尾帧，%s，过渡到下一场景，16:9",
                shot.getDescription() != null ? shot.getDescription() : "场景画面");

        shot.setLastFramePrompt(prompt);
        shot.setLastFrameUrl("pending://" + shotId + "-last");
        shotRepository.save(shot);

        Map<String, String> result = new HashMap<>();
        result.put("prompt", prompt);
        result.put("status", "prompt_generated");
        return Result.success(result);
    }

    @PutMapping("/material/{materialId}/tag")
    public Result<String> updateMaterialTag(
            @PathVariable Long materialId,
            @RequestBody Map<String, String> body) {

        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("素材不存在"));

        material.setCharacterTag(body.get("characterTag"));
        materialRepository.save(material);

        return Result.success("标签更新成功");
    }

    @GetMapping("/scene/{sceneId}/characters")
    public Result<List<Material>> getSceneCharacters(@PathVariable Long sceneId) {
        List<Material> materials = materialRepository.findBySceneIdAndDeleted(sceneId, 0);
        List<Material> characters = materials.stream()
                .filter(m -> "PERSON".equals(m.getMaterialType()))
                .toList();
        return Result.success(characters);
    }
}
