package com.hux.creator.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hux.creator.ai.ModelAdapter;
import com.hux.creator.ai.ModelAdapterRegistry;
import com.hux.creator.ai.ModelProvider;
import com.hux.creator.ai.generator.pipeline.*;
import com.hux.creator.model.entity.*;
import com.hux.creator.model.vo.GenerationProgressVO;
import com.hux.creator.repository.*;
import com.hux.creator.service.GenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenerationServiceImpl implements GenerationService {

    private static final int MAX_RETRY = 3;
    private static final long RETRY_DELAY_MS = 3000;
    private static final float PASS_SCORE = 90f;

    private final ProjectRepository projectRepository;
    private final ScriptRepository scriptRepository;
    private final SceneRepository sceneRepository;
    private final ShotRepository shotRepository;
    private final MaterialRepository materialRepository;
    private final GenerationRoundRepository generationRoundRepository;
    private final EvaluationRepository evaluationRepository;
    private final ModelAdapterRegistry modelAdapterRegistry;
    private final TypeClassifier typeClassifier;
    private final PipelineScriptGenerator pipelineScriptGenerator;
    private final CoherenceChecker coherenceChecker;
    private final MaterialMarker materialMarker;
    private final ContentEvaluator contentEvaluator;
    private final ObjectMapper objectMapper;

    @Override
    public void startGeneration(Long userId, Long projectId, String modelProvider) {
        Project project = projectRepository.findByIdAndDeleted(projectId, 0)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
        if (!project.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此项目");
        }

        ModelAdapter adapter = resolveAdapter(modelProvider);
        log.info("开始生成流程，项目ID: {}, 模型: {}", projectId, adapter.getProvider());

        int maxRounds = project.getMaxRounds() != null && project.getMaxRounds() > 0
                ? project.getMaxRounds()
                : ("PROFESSIONAL".equals(project.getGenerationMode()) ? 5 : 3);
        project.setMaxRounds(maxRounds);
        project.setCurrentRound(0);
        project.setStatus(2);
        projectRepository.save(project);

        String lastSuggestions = null;
        Script bestScript = null;
        Float bestScore = 0f;

        try {
            for (int round = 1; round <= maxRounds; round++) {
                project.setCurrentRound(round);
                projectRepository.save(project);

                GenerationRound genRound = new GenerationRound();
                genRound.setProjectId(projectId);
                genRound.setRoundNumber(round);
                genRound.setMode(project.getGenerationMode());
                genRound.setStatus(0);
                generationRoundRepository.save(genRound);

                log.info("===== 第 {}/{} 轮生成开始 =====", round, maxRounds);

                // -------- Model 1: TypeClassifier --------
                if (round == 1 || project.getVideoType() == null) {
                    log.info("[Model 1] 类型分类...");
                    Map<String, String> typeVars = buildProjectVariables(project);
                    String typeResult = retryGenerate(
                            () -> typeClassifier.generate(typeVars, adapter), "类型分类");
                    typeResult = cleanJsonResponse(typeResult);
                    genRound.setModel1Result(typeResult);

                    String videoType = parseVideoTypeFromJson(typeResult);
                    project.setVideoType(videoType);
                    projectRepository.save(project);
                    generationRoundRepository.save(genRound);
                    log.info("[Model 1] 视频类型: {}", videoType);
                } else {
                    genRound.setModel1Result("{\"videoType\":\"" + project.getVideoType() + "\",\"note\":\"沿用第1轮类型\"}");
                    generationRoundRepository.save(genRound);
                    log.info("[Model 1] 沿用已有视频类型: {}", project.getVideoType());
                }

                // -------- Model 2: PipelineScriptGenerator --------
                log.info("[Model 2] 脚本生成...");
                Map<String, String> scriptVars = buildScriptVariables(project, lastSuggestions);
                String scriptResult = retryGenerate(
                        () -> pipelineScriptGenerator.generate(scriptVars, adapter), "脚本生成");
                scriptResult = cleanJsonResponse(scriptResult);

                Script script = new Script();
                script.setProjectId(projectId);
                script.setRound(round);
                script.setVideoType(project.getVideoType());
                script.setNarrativeRhythm(project.getNarrativeRhythm());

                String scriptContent = parseScriptContentFromJson(scriptResult);
                script.setContent(scriptContent);
                script.setWordCount(scriptContent.length());
                int charPerMinute = 275;
                script.setEstimatedDuration(Math.max(1, scriptContent.length() * 60 / charPerMinute));
                script.setStatus(0);
                script = scriptRepository.save(script);

                genRound.setModel2Result(scriptResult);
                genRound.setScriptId(script.getId());
                generationRoundRepository.save(genRound);

                parseAndSaveScenesAndShots(script.getId(), scriptResult);
                log.info("[Model 2] 脚本生成完成，字数: {}", script.getWordCount());

                // -------- Model 3: CoherenceChecker --------
                log.info("[Model 3] 连贯性检查...");
                Map<String, String> coherenceVars = new HashMap<>();
                coherenceVars.put("script", scriptContent);
                coherenceVars.put("videoType", project.getVideoType() != null ? project.getVideoType() : "");
                coherenceVars.put("duration", project.getDuration() != null ? String.valueOf(project.getDuration()) : "");
                coherenceVars.put("targetUser", project.getTargetUser() != null ? project.getTargetUser() : "");
                String coherenceResult = retryGenerate(
                        () -> coherenceChecker.generate(coherenceVars, adapter), "连贯性检查");
                coherenceResult = cleanJsonResponse(coherenceResult);

                genRound.setModel3Result(coherenceResult);
                generationRoundRepository.save(genRound);

                List<Scene> scenes = sceneRepository.findByScriptIdAndDeletedOrderBySceneNumber(script.getId(), 0);
                log.info("[Model 3] 连贯性检查完成，当前场景数: {}", scenes.size());

                // -------- Model 4: MaterialMarker --------
                log.info("[Model 4] 素材标记...");
                Map<String, String> materialVars = new HashMap<>();
                materialVars.put("script", scriptContent);
                materialVars.put("coherenceResult", coherenceResult);
                materialVars.put("videoType", project.getVideoType() != null ? project.getVideoType() : "");
                materialVars.put("duration", project.getDuration() != null ? String.valueOf(project.getDuration()) : "");
                String materialResult = retryGenerate(
                        () -> materialMarker.generate(materialVars, adapter), "素材标记");
                materialResult = cleanJsonResponse(materialResult);

                genRound.setModel4Result(materialResult);
                generationRoundRepository.save(genRound);

                parseAndSaveMaterials(scenes, materialResult);
                log.info("[Model 4] 素材标记完成");

                // -------- Model 5: ContentEvaluator --------
                log.info("[Model 5] 内容评估...");
                Map<String, String> evalVars = new HashMap<>();
                evalVars.put("script", scriptContent);
                evalVars.put("videoType", project.getVideoType() != null ? project.getVideoType() : "");
                evalVars.put("duration", project.getDuration() != null ? String.valueOf(project.getDuration()) : "");
                evalVars.put("targetUser", project.getTargetUser() != null ? project.getTargetUser() : "");
                evalVars.put("platform", project.getPlatform() != null ? project.getPlatform() : "");
                evalVars.put("style", project.getStyle() != null ? project.getStyle() : "");
                String evalResult = retryGenerate(
                        () -> contentEvaluator.generate(evalVars, adapter), "内容评估");
                evalResult = cleanJsonResponse(evalResult);

                Evaluation evaluation = parseEvaluationFromJson(evalResult, projectId, script.getId(), round);
                evaluationRepository.save(evaluation);

                genRound.setModel5Score(evaluation.getOverallScore());
                genRound.setModel5Suggestions(evaluation.getSuggestions());
                genRound.setStatus(1);
                generationRoundRepository.save(genRound);

                Float score = evaluation.getOverallScore() != null ? evaluation.getOverallScore() : 0f;
                log.info("[Model 5] 评估完成，总分: {}, 通过: {}", score, score >= PASS_SCORE);

                if (score > bestScore) {
                    bestScore = score;
                    bestScript = script;
                }

                if (score >= PASS_SCORE) {
                    log.info("评分达到 {} 分以上，标记为最终版本", PASS_SCORE);
                    script.setStatus(2);
                    script.setScore(score);
                    scriptRepository.save(script);

                    evaluation.setPassed(true);
                    evaluationRepository.save(evaluation);

                    project.setFinalScore(score);
                    project.setStatus(1);
                    projectRepository.save(project);
                    break;
                }

                lastSuggestions = evaluation.getSuggestions();

                if (round == maxRounds) {
                    log.info("达到最大轮次 {}，标记最佳脚本(评分: {})为最终版本", maxRounds, bestScore);
                    if (bestScript != null) {
                        bestScript.setStatus(2);
                        bestScript.setScore(bestScore);
                        scriptRepository.save(bestScript);
                    }
                    project.setFinalScore(bestScore);
                    project.setStatus(1);
                    projectRepository.save(project);
                }

                log.info("===== 第 {}/{} 轮生成完成 =====", round, maxRounds);
            }
        } catch (Exception e) {
            log.error("生成流程中断，项目ID: {}, 错误: {}", projectId, e.getMessage(), e);
            project.setStatus(3);
            projectRepository.save(project);
            throw new RuntimeException("生成流程中断: " + e.getMessage(), e);
        }

        log.info("生成流程全部完成，项目ID: {}, 最终评分: {}", projectId, project.getFinalScore());
    }

    @Override
    public GenerationProgressVO getProgress(Long userId, Long projectId) {
        Project project = projectRepository.findByIdAndDeleted(projectId, 0)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
        if (!project.getUserId().equals(userId)) {
            throw new RuntimeException("无权访问此项目");
        }

        GenerationProgressVO vo = new GenerationProgressVO();
        vo.setProjectId(projectId);
        vo.setCurrentRound(project.getCurrentRound());
        vo.setMaxRounds(project.getMaxRounds());
        vo.setScore(project.getFinalScore());
        vo.setPassed(project.getFinalScore() != null && project.getFinalScore() >= PASS_SCORE);

        String status = switch (project.getStatus() != null ? project.getStatus() : 0) {
            case 0 -> "IDLE";
            case 1 -> "COMPLETED";
            case 2 -> "GENERATING";
            case 3 -> "FAILED";
            default -> "UNKNOWN";
        };
        vo.setStatus(status);

        List<GenerationRound> rounds = generationRoundRepository
                .findByProjectIdAndDeletedOrderByRoundNumber(projectId, 0);
        List<GenerationProgressVO.RoundVO> roundVOs = new ArrayList<>();
        for (GenerationRound round : rounds) {
            GenerationProgressVO.RoundVO rvo = new GenerationProgressVO.RoundVO();
            rvo.setRoundNumber(round.getRoundNumber());
            rvo.setMode(round.getMode());
            rvo.setStatus(round.getStatus());
            rvo.setScore(round.getModel5Score());
            rvo.setSuggestions(round.getModel5Suggestions());
            roundVOs.add(rvo);
        }
        vo.setRounds(roundVOs);

        if ("GENERATING".equals(status) && !rounds.isEmpty()) {
            GenerationRound current = rounds.get(rounds.size() - 1);
            if (current.getStatus() == 0) {
                resolveCurrentModel(current, vo);
            }
        }

        return vo;
    }

    private void resolveCurrentModel(GenerationRound current, GenerationProgressVO vo) {
        if (current.getModel5Score() != null) {
            vo.setCurrentModel(5);
            vo.setCurrentModelName("内容评估");
        } else if (current.getModel4Result() != null) {
            vo.setCurrentModel(5);
            vo.setCurrentModelName("内容评估");
        } else if (current.getModel3Result() != null) {
            vo.setCurrentModel(4);
            vo.setCurrentModelName("素材标记");
        } else if (current.getModel2Result() != null) {
            vo.setCurrentModel(3);
            vo.setCurrentModelName("连贯性检查");
        } else if (current.getModel1Result() != null) {
            vo.setCurrentModel(2);
            vo.setCurrentModelName("脚本生成");
        } else {
            vo.setCurrentModel(1);
            vo.setCurrentModelName("类型分类");
        }
    }

    private ModelAdapter resolveAdapter(String modelProvider) {
        if (modelProvider != null && !modelProvider.isEmpty()) {
            return modelAdapterRegistry.getAdapter(ModelProvider.valueOf(modelProvider.toUpperCase()));
        }
        return modelAdapterRegistry.getDefaultAdapter();
    }

    private Map<String, String> buildProjectVariables(Project project) {
        Map<String, String> variables = new HashMap<>();
        variables.put("title", project.getTitle() != null ? project.getTitle() : "");
        variables.put("platform", project.getPlatform() != null ? project.getPlatform() : "");
        variables.put("targetUser", project.getTargetUser() != null ? project.getTargetUser() : "");
        variables.put("duration", project.getDuration() != null ? String.valueOf(project.getDuration()) : "");
        variables.put("style", project.getStyle() != null ? project.getStyle() : "");
        variables.put("description", project.getDescription() != null ? project.getDescription() : "");
        variables.put("videoType", project.getVideoType() != null ? project.getVideoType() : "");
        variables.put("narrativeRhythm", project.getNarrativeRhythm() != null ? String.valueOf(project.getNarrativeRhythm()) : "0.7");
        variables.put("generationMode", project.getGenerationMode() != null ? project.getGenerationMode() : "QUICK");
        variables.put("visualStyle", project.getVisualStyle() != null ? project.getVisualStyle() : "AUTO");
        return variables;
    }

    private Map<String, String> buildScriptVariables(Project project, String suggestions) {
        Map<String, String> variables = buildProjectVariables(project);
        variables.put("videoType", project.getVideoType() != null ? project.getVideoType() : "AUTO");
        variables.put("typeInstruction", getTypeSpecificInstruction(project.getVideoType()));
        if (suggestions != null && !suggestions.isEmpty()) {
            variables.put("suggestions", suggestions);
        }
        return variables;
    }

    private String getTypeSpecificInstruction(String videoType) {
        if (videoType == null) {
            return "根据视频内容类型，合理安排叙事结构和视觉节奏。";
        }
        return switch (videoType.toUpperCase()) {
            case "MARKETING" ->
                    "重点关注产品展示顺序和镜头切换逻辑，确保核心卖点在前5秒出现，" +
                    "每个镜头突出一个产品特性，结尾设置明确的购买引导。";
            case "KNOWLEDGE" ->
                    "重点关注核心知识点的讲解逻辑和层次结构，确保信息由浅入深、层层递进，" +
                    "配合可视化辅助理解，每个知识节点之间设置过渡衔接。";
            case "STORY" ->
                    "重点关注人物动作描写和场景转换节奏，确保叙事线索清晰、" +
                    "情绪递进自然、转折点明确，场景之间使用恰当的过渡方式。";
            default -> "根据视频内容类型，合理安排叙事结构和视觉节奏。";
        };
    }

    private String cleanJsonResponse(String response) {
        if (response == null) {
            return "{}";
        }
        String cleaned = response.trim();
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        cleaned = cleaned.trim();
        if (cleaned.isEmpty()) {
            return "{}";
        }
        return cleaned;
    }

    @FunctionalInterface
    private interface AiGenerateTask {
        String execute();
    }

    private String retryGenerate(AiGenerateTask task, String stepName) {
        Exception lastException = null;
        for (int i = 1; i <= MAX_RETRY; i++) {
            try {
                return task.execute();
            } catch (Exception e) {
                lastException = e;
                log.warn("{} 第{}次尝试失败: {}", stepName, i, e.getMessage());
                if (i < MAX_RETRY) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS * i);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("任务被中断", ie);
                    }
                }
            }
        }
        throw new RuntimeException(stepName + " 失败（已重试" + MAX_RETRY + "次）: " + lastException.getMessage(), lastException);
    }

    private String parseVideoTypeFromJson(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);
            if (node.has("videoType")) {
                return node.get("videoType").asText();
            }
        } catch (Exception e) {
            log.warn("解析视频类型JSON失败，尝试文本匹配: {}", e.getMessage());
        }
        String upper = json.toUpperCase();
        if (upper.contains("MARKETING")) return "MARKETING";
        if (upper.contains("KNOWLEDGE")) return "KNOWLEDGE";
        if (upper.contains("STORY")) return "STORY";
        return "MARKETING";
    }

    private String parseScriptContentFromJson(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);
            if (node.has("content")) {
                return node.get("content").asText();
            }
            if (node.has("script")) {
                return node.get("script").asText();
            }
        } catch (Exception e) {
            log.debug("脚本内容非JSON格式，直接使用原始文本");
        }
        return json;
    }

    private void parseAndSaveScenesAndShots(Long scriptId, String json) {
        try {
            JsonNode node = objectMapper.readTree(json);
            JsonNode scenesNode = node.get("scenes");
            if (scenesNode == null || !scenesNode.isArray()) {
                log.debug("脚本结果中未包含scenes数组，跳过场景解析");
                return;
            }

            for (JsonNode sceneNode : scenesNode) {
                Scene scene = new Scene();
                scene.setScriptId(scriptId);
                scene.setSceneNumber(getIntValue(sceneNode, "sceneNumber", 0));
                scene.setTitle(getStringValue(sceneNode, "title", ""));
                scene.setDescription(getStringValue(sceneNode, "description", ""));
                scene.setLocation(getStringValue(sceneNode, "location", ""));
                scene.setMood(getStringValue(sceneNode, "mood", ""));
                scene.setEstimatedDuration(getIntValue(sceneNode, "estimatedDuration", 0));
                scene.setVisualType(getStringValue(sceneNode, "visualType", ""));
                scene.setTransition(getStringValue(sceneNode, "transition", ""));
                scene = sceneRepository.save(scene);

                JsonNode shotsNode = sceneNode.get("shots");
                if (shotsNode != null && shotsNode.isArray()) {
                    for (JsonNode shotNode : shotsNode) {
                        Shot shot = new Shot();
                        shot.setSceneId(scene.getId());
                        shot.setShotNumber(getIntValue(shotNode, "shotNumber", 0));
                        shot.setDescription(getStringValue(shotNode, "description", ""));
                        shot.setCameraAngle(getStringValue(shotNode, "cameraAngle", ""));
                        shot.setCameraMovement(getStringValue(shotNode, "cameraMovement", ""));
                        shot.setDuration(getIntValue(shotNode, "duration", 0));
                        shot.setDialogue(getStringValue(shotNode, "dialogue", ""));
                        shot.setVisualType(getStringValue(shotNode, "visualType", ""));
                        shot.setTransition(getStringValue(shotNode, "transition", ""));
                        shotRepository.save(shot);
                    }
                }
            }
            log.info("解析并保存了 {} 个场景", scenesNode.size());
        } catch (Exception e) {
            log.warn("解析场景/镜头数据失败，跳过结构化解析: {}", e.getMessage());
        }
    }

    private void parseAndSaveMaterials(List<Scene> scenes, String json) {
        try {
            JsonNode node = objectMapper.readTree(json);
            JsonNode materialsNode = node.get("materials");
            if (materialsNode == null || !materialsNode.isArray()) {
                log.debug("素材结果中未包含materials数组，跳过素材解析");
                return;
            }

            Map<Integer, Scene> sceneMap = new HashMap<>();
            for (Scene scene : scenes) {
                sceneMap.put(scene.getSceneNumber(), scene);
            }

            Map<String, Shot> shotMap = new HashMap<>();
            for (Scene scene : scenes) {
                List<Shot> shots = shotRepository.findBySceneIdAndDeletedOrderByShotNumber(scene.getId(), 0);
                for (Shot shot : shots) {
                    shotMap.put(scene.getSceneNumber() + "-" + shot.getShotNumber(), shot);
                }
            }

            int count = 0;
            for (JsonNode matNode : materialsNode) {
                int sceneNum = getIntValue(matNode, "sceneNumber", 0);
                int shotNum = getIntValue(matNode, "shotNumber", 0);

                Scene targetScene = sceneMap.get(sceneNum);
                if (targetScene == null && !scenes.isEmpty()) {
                    targetScene = scenes.get(0);
                }
                if (targetScene == null) {
                    continue;
                }

                Material material = new Material();
                material.setSceneId(targetScene.getId());

                Shot targetShot = shotMap.get(sceneNum + "-" + shotNum);
                if (targetShot != null) {
                    material.setShotId(targetShot.getId());
                }

                material.setMaterialType(getStringValue(matNode, "materialType", "IMAGE"));
                material.setName(getStringValue(matNode, "name", ""));
                material.setDescription(getStringValue(matNode, "description", ""));
                material.setPosition(getStringValue(matNode, "position", ""));
                material.setSourceSuggestion(getStringValue(matNode, "sourceSuggestion", ""));
                material.setPrompt(getStringValue(matNode, "prompt", ""));
                materialRepository.save(material);
                count++;
            }
            log.info("解析并保存了 {} 个素材", count);
        } catch (Exception e) {
            log.warn("解析素材数据失败，跳过素材解析: {}", e.getMessage());
        }
    }

    private Evaluation parseEvaluationFromJson(String json, Long projectId, Long scriptId, int round) {
        Evaluation evaluation = new Evaluation();
        evaluation.setProjectId(projectId);
        evaluation.setScriptId(scriptId);
        evaluation.setRoundNumber(round);

        try {
            JsonNode node = objectMapper.readTree(json);
            evaluation.setOverallScore(getFloatValue(node, "overallScore", 0f));
            evaluation.setCompletenessScore(getFloatValue(node, "completenessScore", 0f));
            evaluation.setCoherenceScore(getFloatValue(node, "coherenceScore", 0f));
            evaluation.setVisualFitScore(getFloatValue(node, "visualFitScore", 0f));
            evaluation.setCreativityScore(getFloatValue(node, "creativityScore", 0f));
            evaluation.setSuggestions(getStringValue(node, "suggestions", ""));
            evaluation.setPassed(evaluation.getOverallScore() != null && evaluation.getOverallScore() >= PASS_SCORE);
        } catch (Exception e) {
            log.warn("解析评估JSON失败，使用默认值: {}", e.getMessage());
            evaluation.setOverallScore(0f);
            evaluation.setCompletenessScore(0f);
            evaluation.setCoherenceScore(0f);
            evaluation.setVisualFitScore(0f);
            evaluation.setCreativityScore(0f);
            evaluation.setSuggestions("评估结果解析失败，请重试");
            evaluation.setPassed(false);
        }

        return evaluation;
    }

    private String getStringValue(JsonNode node, String field, String defaultValue) {
        if (node != null && node.has(field) && !node.get(field).isNull()) {
            return node.get(field).asText(defaultValue);
        }
        return defaultValue;
    }

    private int getIntValue(JsonNode node, String field, int defaultValue) {
        if (node != null && node.has(field) && !node.get(field).isNull()) {
            return node.get(field).asInt(defaultValue);
        }
        return defaultValue;
    }

    private float getFloatValue(JsonNode node, String field, float defaultValue) {
        if (node != null && node.has(field) && !node.get(field).isNull()) {
            return (float) node.get(field).asDouble(defaultValue);
        }
        return defaultValue;
    }
}
