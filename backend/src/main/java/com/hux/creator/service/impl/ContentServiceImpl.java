package com.hux.creator.service.impl;

import com.hux.creator.ai.ModelAdapter;
import com.hux.creator.ai.ModelAdapterRegistry;
import com.hux.creator.ai.ModelProvider;
import com.hux.creator.ai.generator.CoverGenerator;
import com.hux.creator.ai.generator.HookGenerator;
import com.hux.creator.ai.generator.MaterialGenerator;
import com.hux.creator.ai.generator.PromptGenerator;
import com.hux.creator.ai.generator.ScriptGenerator;
import com.hux.creator.ai.generator.StoryboardGenerator;
import com.hux.creator.ai.generator.TitleGenerator;
import com.hux.creator.ai.generator.TopicAnalyzer;
import com.hux.creator.model.dto.GenerateContentDTO;
import com.hux.creator.model.dto.OptimizeContentDTO;
import com.hux.creator.model.dto.RegenerateStepDTO;
import com.hux.creator.model.entity.ContentPlan;
import com.hux.creator.model.entity.Project;
import com.hux.creator.model.entity.RevisionRecord;
import com.hux.creator.model.vo.ContentPlanVO;
import com.hux.creator.repository.ContentPlanRepository;
import com.hux.creator.repository.ProjectRepository;
import com.hux.creator.repository.RevisionRecordRepository;
import com.hux.creator.service.ContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentServiceImpl implements ContentService {

    private static final int MAX_RETRY = 3;
    private static final long RETRY_DELAY_MS = 3000;

    private final ProjectRepository projectRepository;
    private final ContentPlanRepository contentPlanRepository;
    private final RevisionRecordRepository revisionRecordRepository;
    private final ModelAdapterRegistry modelAdapterRegistry;
    private final TopicAnalyzer topicAnalyzer;
    private final TitleGenerator titleGenerator;
    private final HookGenerator hookGenerator;
    private final ScriptGenerator scriptGenerator;
    private final StoryboardGenerator storyboardGenerator;
    private final MaterialGenerator materialGenerator;
    private final PromptGenerator promptGenerator;
    private final CoverGenerator coverGenerator;

    @Override
    public ContentPlanVO generateContent(Long userId, GenerateContentDTO dto) {
        log.info("开始生成内容，项目ID: {}, 用户ID: {}", dto.getProjectId(), userId);

        Project project = projectRepository.findByIdAndDeleted(dto.getProjectId(), 0)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
        if (!project.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此项目");
        }

        ContentPlan contentPlan = contentPlanRepository.findFirstByProjectIdOrderByCreateTimeDesc(dto.getProjectId())
                .orElseGet(() -> {
                    ContentPlan plan = new ContentPlan();
                    plan.setProjectId(dto.getProjectId());
                    plan.setGenerationStep(0);
                    return plan;
                });

        int startStep = contentPlan.getGenerationStep() != null ? contentPlan.getGenerationStep() : 0;
        if (startStep > 0) {
            log.info("断点续传：从步骤 {}/8 继续生成", startStep);
        }

        Map<String, String> variables = buildVariablesMap(project);

        ModelAdapter adapter = resolveAdapter(dto.getModelProvider());
        log.info("使用模型适配器: {}", adapter.getProvider());

        try {
            if (startStep < 1) {
                executeStep(contentPlan, 1, "选题分析", () -> {
                    String result = retryGenerate(() -> topicAnalyzer.generate(variables, adapter), "选题分析");
                    contentPlan.setTopicAnalysis(cleanAiResponse(result));
                });
            }

            if (startStep < 2) {
                executeStep(contentPlan, 2, "标题生成", () -> {
                    Map<String, String> titleVars = new HashMap<>(variables);
                    titleVars.put("topicAnalysis", contentPlan.getTopicAnalysis());
                    String result = retryGenerate(() -> titleGenerator.generate(titleVars, adapter), "标题生成");
                    contentPlan.setTitleOptions(cleanAiResponse(result));
                });
            }

            if (startStep < 3) {
                executeStep(contentPlan, 3, "Hook生成", () -> {
                    String chosenTitle = extractFirstTitle(contentPlan.getTitleOptions());
                    Map<String, String> hookVars = new HashMap<>(variables);
                    hookVars.put("titleOptions", contentPlan.getTitleOptions());
                    hookVars.put("chosenTitle", chosenTitle);
                    String result = retryGenerate(() -> hookGenerator.generate(hookVars, adapter), "Hook生成");
                    contentPlan.setHook(cleanAiResponse(result));
                });
            }

            if (startStep < 4) {
                executeStep(contentPlan, 4, "脚本生成", () -> {
                    Map<String, String> scriptVars = new HashMap<>(variables);
                    scriptVars.put("hook", contentPlan.getHook());
                    String result = retryGenerate(() -> scriptGenerator.generate(scriptVars, adapter), "脚本生成");
                    contentPlan.setScript(cleanAiResponse(result));
                });
            }

            if (startStep < 5) {
                executeStep(contentPlan, 5, "分镜生成", () -> {
                    Map<String, String> storyboardVars = new HashMap<>(variables);
                    storyboardVars.put("script", contentPlan.getScript());
                    String result = retryGenerate(() -> storyboardGenerator.generate(storyboardVars, adapter), "分镜生成");
                    contentPlan.setStoryboard(cleanAiResponse(result));
                });
            }

            if (startStep < 6) {
                executeStep(contentPlan, 6, "素材清单", () -> {
                    Map<String, String> materialVars = new HashMap<>(variables);
                    materialVars.put("storyboard", contentPlan.getStoryboard());
                    String result = retryGenerate(() -> materialGenerator.generate(materialVars, adapter), "素材清单");
                    contentPlan.setMaterialList(cleanAiResponse(result));
                });
            }

            if (startStep < 7) {
                executeStep(contentPlan, 7, "提示词", () -> {
                    Map<String, String> promptVars = new HashMap<>(variables);
                    promptVars.put("storyboard", contentPlan.getStoryboard());
                    String result = retryGenerate(() -> promptGenerator.generate(promptVars, adapter), "提示词");
                    parsePrompts(contentPlan, cleanAiResponse(result));
                });
            }

            if (startStep < 8) {
                executeStep(contentPlan, 8, "封面文案", () -> {
                    String chosenTitle = extractFirstTitle(contentPlan.getTitleOptions());
                    Map<String, String> coverVars = new HashMap<>(variables);
                    coverVars.put("chosenTitle", chosenTitle);
                    coverVars.put("hook", contentPlan.getHook());
                    coverVars.put("script", contentPlan.getScript());
                    String result = retryGenerate(() -> coverGenerator.generate(coverVars, adapter), "封面文案");
                    parseCoverAndPublish(contentPlan, cleanAiResponse(result));
                });
            }

            contentPlan.setStatus(1);
            contentPlanRepository.save(contentPlan);

            project.setStatus(1);
            projectRepository.save(project);

            log.info("内容全部生成完成，项目ID: {}", dto.getProjectId());

        } catch (Exception e) {
            contentPlan.setStatus(2);
            contentPlanRepository.save(contentPlan);
            log.error("内容生成中断，已保存到步骤 {}/8，错误: {}", contentPlan.getGenerationStep(), e.getMessage());
            throw new RuntimeException("内容生成失败（已完成 " + contentPlan.getGenerationStep() + "/8 步），可重新生成继续: " + e.getMessage());
        }

        return convertToVO(contentPlan);
    }

    @Override
    public ContentPlanVO regenerateStep(Long userId, RegenerateStepDTO dto) {
        log.info("重新生成步骤{}, 项目ID: {}, 用户ID: {}", dto.getStep(), dto.getProjectId(), userId);

        Project project = projectRepository.findByIdAndDeleted(dto.getProjectId(), 0)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
        if (!project.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此项目");
        }

        ContentPlan contentPlan = contentPlanRepository.findFirstByProjectIdOrderByCreateTimeDesc(dto.getProjectId())
                .orElseThrow(() -> new RuntimeException("请先生成完整内容方案后再使用单步生成功能"));

        Map<String, String> variables = buildVariablesMap(project);
        ModelAdapter adapter = resolveAdapter(dto.getModelProvider());

        log.info("步骤{} 使用模型适配器: {}", dto.getStep(), adapter.getProvider());

        switch (dto.getStep()) {
            case 1 -> {
                String result = retryGenerate(() -> topicAnalyzer.generate(variables, adapter), "选题分析");
                contentPlan.setTopicAnalysis(cleanAiResponse(result));
            }
            case 2 -> {
                Map<String, String> titleVars = new HashMap<>(variables);
                titleVars.put("topicAnalysis", contentPlan.getTopicAnalysis());
                String result = retryGenerate(() -> titleGenerator.generate(titleVars, adapter), "标题生成");
                contentPlan.setTitleOptions(cleanAiResponse(result));
            }
            case 3 -> {
                String chosenTitle = extractFirstTitle(contentPlan.getTitleOptions());
                Map<String, String> hookVars = new HashMap<>(variables);
                hookVars.put("titleOptions", contentPlan.getTitleOptions());
                hookVars.put("chosenTitle", chosenTitle);
                String result = retryGenerate(() -> hookGenerator.generate(hookVars, adapter), "Hook生成");
                contentPlan.setHook(cleanAiResponse(result));
            }
            case 4 -> {
                Map<String, String> scriptVars = new HashMap<>(variables);
                scriptVars.put("hook", contentPlan.getHook());
                String result = retryGenerate(() -> scriptGenerator.generate(scriptVars, adapter), "脚本生成");
                contentPlan.setScript(cleanAiResponse(result));
            }
            case 5 -> {
                Map<String, String> storyboardVars = new HashMap<>(variables);
                storyboardVars.put("script", contentPlan.getScript());
                String result = retryGenerate(() -> storyboardGenerator.generate(storyboardVars, adapter), "分镜生成");
                contentPlan.setStoryboard(cleanAiResponse(result));
            }
            case 6 -> {
                Map<String, String> materialVars = new HashMap<>(variables);
                materialVars.put("storyboard", contentPlan.getStoryboard());
                String result = retryGenerate(() -> materialGenerator.generate(materialVars, adapter), "素材清单");
                contentPlan.setMaterialList(cleanAiResponse(result));
            }
            case 7 -> {
                Map<String, String> promptVars = new HashMap<>(variables);
                promptVars.put("storyboard", contentPlan.getStoryboard());
                String result = retryGenerate(() -> promptGenerator.generate(promptVars, adapter), "提示词");
                parsePrompts(contentPlan, cleanAiResponse(result));
            }
            case 8 -> {
                String chosenTitle = extractFirstTitle(contentPlan.getTitleOptions());
                Map<String, String> coverVars = new HashMap<>(variables);
                coverVars.put("chosenTitle", chosenTitle);
                coverVars.put("hook", contentPlan.getHook());
                coverVars.put("script", contentPlan.getScript());
                String result = retryGenerate(() -> coverGenerator.generate(coverVars, adapter), "封面文案");
                parseCoverAndPublish(contentPlan, cleanAiResponse(result));
            }
            default -> throw new RuntimeException("不支持的步骤: " + dto.getStep());
        }

        contentPlanRepository.save(contentPlan);
        log.info("步骤{} 重新生成完成", dto.getStep());
        return convertToVO(contentPlan);
    }

    @Override
    @Transactional
    public ContentPlanVO optimizeContent(Long userId, OptimizeContentDTO dto) {
        Project project = projectRepository.findByIdAndDeleted(dto.getProjectId(), 0)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
        if (!project.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此项目");
        }

        ContentPlan contentPlan = contentPlanRepository.findFirstByProjectIdOrderByCreateTimeDesc(dto.getProjectId())
                .orElseThrow(() -> new RuntimeException("请先生成内容方案"));

        String originalContent = getSectionContent(contentPlan, dto.getSection());
        if (originalContent == null) {
            throw new RuntimeException("该板块暂无内容，请先生成内容方案");
        }

        ModelAdapter adapter = modelAdapterRegistry.getDefaultAdapter();
        String optimizedContent = retryGenerate(() -> optimizeSection(dto.getSection(), originalContent, dto.getInstruction(), adapter), "优化" + dto.getSection());

        RevisionRecord record = new RevisionRecord();
        record.setProjectId(dto.getProjectId());
        record.setContentPlanId(contentPlan.getId());
        record.setSectionName(dto.getSection());
        record.setOriginalContent(originalContent);
        record.setOptimizedContent(optimizedContent);
        record.setInstruction(dto.getInstruction());
        revisionRecordRepository.save(record);

        setSectionContent(contentPlan, dto.getSection(), cleanAiResponse(optimizedContent));
        contentPlan = contentPlanRepository.save(contentPlan);

        return convertToVO(contentPlan);
    }

    @Override
    public ContentPlanVO getContent(Long userId, Long projectId) {
        Project project = projectRepository.findByIdAndDeleted(projectId, 0)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
        if (!project.getUserId().equals(userId)) {
            throw new RuntimeException("无权访问此项目");
        }

        return contentPlanRepository.findFirstByProjectIdOrderByCreateTimeDesc(projectId)
                .map(this::convertToVO)
                .orElse(null);
    }

    private void executeStep(ContentPlan contentPlan, int step, String stepName, Runnable action) {
        log.info("步骤{}/8: {}...", step, stepName);
        action.run();
        contentPlan.setGenerationStep(step);
        contentPlanRepository.save(contentPlan);
        log.info("步骤{}/8 完成并已保存", step);
    }

    private ModelAdapter resolveAdapter(String modelProvider) {
        if (modelProvider != null && !modelProvider.isEmpty()) {
            return modelAdapterRegistry.getAdapter(ModelProvider.valueOf(modelProvider.toUpperCase()));
        }
        return modelAdapterRegistry.getDefaultAdapter();
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

    private String cleanAiResponse(String response) {
        if (response == null) return null;
        response = response.replaceAll("```json\\s*", "").replaceAll("```\\s*$", "");
        response = response.trim();
        if (response.startsWith("\"") && response.endsWith("\"")) {
            try {
                response = response.substring(1, response.length() - 1);
            } catch (Exception ignored) {}
        }
        return response;
    }

    private Map<String, String> buildVariablesMap(Project project) {
        Map<String, String> variables = new HashMap<>();
        variables.put("title", project.getTitle());
        variables.put("platform", project.getPlatform() != null ? project.getPlatform() : "");
        variables.put("targetUser", project.getTargetUser() != null ? project.getTargetUser() : "");
        variables.put("duration", project.getDuration() != null ? String.valueOf(project.getDuration()) : "");
        variables.put("style", project.getStyle() != null ? project.getStyle() : "");
        variables.put("description", project.getDescription() != null ? project.getDescription() : "");
        return variables;
    }

    private String extractFirstTitle(String titleOptions) {
        if (titleOptions == null || titleOptions.isEmpty()) {
            return "";
        }
        String[] lines = titleOptions.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty() && !trimmed.startsWith("#") && !trimmed.startsWith("标题")) {
                return trimmed.replaceFirst("^\\d+[.、)）]\\s*", "").replaceFirst("^[*_]+|[*_]+$", "");
            }
        }
        return titleOptions;
    }

    private void parsePrompts(ContentPlan contentPlan, String result) {
        if (result == null) return;

        String imageMarker = "【图片提示词】";
        String videoMarker = "【视频提示词】";

        int imageIdx = result.indexOf(imageMarker);
        int videoIdx = result.indexOf(videoMarker);

        if (imageIdx >= 0 && videoIdx >= 0) {
            if (imageIdx < videoIdx) {
                contentPlan.setImagePrompt(result.substring(imageIdx + imageMarker.length(), videoIdx).trim());
                contentPlan.setVideoPrompt(result.substring(videoIdx + videoMarker.length()).trim());
            } else {
                contentPlan.setVideoPrompt(result.substring(videoIdx + videoMarker.length(), imageIdx).trim());
                contentPlan.setImagePrompt(result.substring(imageIdx + imageMarker.length()).trim());
            }
        } else {
            contentPlan.setImagePrompt(result);
            contentPlan.setVideoPrompt("");
        }
    }

    private void parseCoverAndPublish(ContentPlan contentPlan, String coverAndPublish) {
        if (coverAndPublish == null) return;

        String coverMarker = "【封面文案】";
        String publishMarker = "【发布文案】";
        int coverIdx = coverAndPublish.indexOf(coverMarker);
        int publishIdx = coverAndPublish.indexOf(publishMarker);

        if (coverIdx >= 0 && publishIdx >= 0) {
            if (coverIdx < publishIdx) {
                contentPlan.setCoverCopy(coverAndPublish.substring(coverIdx + coverMarker.length(), publishIdx).trim());
                contentPlan.setPublishCopy(coverAndPublish.substring(publishIdx + publishMarker.length()).trim());
            } else {
                contentPlan.setPublishCopy(coverAndPublish.substring(publishIdx + publishMarker.length(), coverIdx).trim());
                contentPlan.setCoverCopy(coverAndPublish.substring(coverIdx + coverMarker.length()).trim());
            }
        } else {
            contentPlan.setCoverCopy(coverAndPublish);
            contentPlan.setPublishCopy("");
        }
    }

    private String getSectionContent(ContentPlan contentPlan, String section) {
        return switch (section.toUpperCase()) {
            case "TOPIC_ANALYSIS", "TOPICANALYSIS" -> contentPlan.getTopicAnalysis();
            case "TITLE_OPTIONS", "TITLEOPTIONS", "TITLE" -> contentPlan.getTitleOptions();
            case "HOOK" -> contentPlan.getHook();
            case "SCRIPT" -> contentPlan.getScript();
            case "STORYBOARD" -> contentPlan.getStoryboard();
            case "MATERIAL_LIST", "MATERIALLIST", "MATERIAL" -> contentPlan.getMaterialList();
            case "IMAGE_PROMPT", "IMAGEPROMPT", "IMAGE" -> contentPlan.getImagePrompt();
            case "VIDEO_PROMPT", "VIDEOPROMPT", "VIDEO" -> contentPlan.getVideoPrompt();
            case "COVER_COPY", "COVERCOPY", "COVER" -> contentPlan.getCoverCopy();
            case "PUBLISH_COPY", "PUBLISHCOPY", "PUBLISH" -> contentPlan.getPublishCopy();
            default -> throw new RuntimeException("不支持的板块: " + section);
        };
    }

    private void setSectionContent(ContentPlan contentPlan, String section, String content) {
        switch (section.toUpperCase()) {
            case "TOPIC_ANALYSIS", "TOPICANALYSIS" -> contentPlan.setTopicAnalysis(content);
            case "TITLE_OPTIONS", "TITLEOPTIONS", "TITLE" -> contentPlan.setTitleOptions(content);
            case "HOOK" -> contentPlan.setHook(content);
            case "SCRIPT" -> contentPlan.setScript(content);
            case "STORYBOARD" -> contentPlan.setStoryboard(content);
            case "MATERIAL_LIST", "MATERIALLIST", "MATERIAL" -> contentPlan.setMaterialList(content);
            case "IMAGE_PROMPT", "IMAGEPROMPT", "IMAGE" -> contentPlan.setImagePrompt(content);
            case "VIDEO_PROMPT", "VIDEOPROMPT", "VIDEO" -> contentPlan.setVideoPrompt(content);
            case "COVER_COPY", "COVERCOPY", "COVER" -> contentPlan.setCoverCopy(content);
            case "PUBLISH_COPY", "PUBLISHCOPY", "PUBLISH" -> contentPlan.setPublishCopy(content);
            default -> throw new RuntimeException("不支持的板块: " + section);
        }
    }

    private String optimizeSection(String section, String originalContent, String instruction, ModelAdapter adapter) {
        return switch (section.toUpperCase()) {
            case "TOPIC_ANALYSIS", "TOPICANALYSIS" ->
                    topicAnalyzer.optimize(originalContent, instruction, adapter);
            case "TITLE_OPTIONS", "TITLEOPTIONS", "TITLE" ->
                    titleGenerator.optimize(originalContent, instruction, adapter);
            case "HOOK" ->
                    hookGenerator.optimize(originalContent, instruction, adapter);
            case "SCRIPT" ->
                    scriptGenerator.optimize(originalContent, instruction, adapter);
            case "STORYBOARD" ->
                    storyboardGenerator.optimize(originalContent, instruction, adapter);
            case "MATERIAL_LIST", "MATERIALLIST", "MATERIAL" ->
                    materialGenerator.optimize(originalContent, instruction, adapter);
            case "IMAGE_PROMPT", "IMAGEPROMPT", "IMAGE" ->
                    promptGenerator.optimize(originalContent, instruction, adapter);
            case "VIDEO_PROMPT", "VIDEOPROMPT", "VIDEO" ->
                    promptGenerator.optimize(originalContent, instruction, adapter);
            case "COVER_COPY", "COVERCOPY", "COVER" ->
                    coverGenerator.optimize(originalContent, instruction, adapter);
            case "PUBLISH_COPY", "PUBLISHCOPY", "PUBLISH" ->
                    coverGenerator.optimize(originalContent, instruction, adapter);
            default -> throw new RuntimeException("不支持的板块: " + section);
        };
    }

    private ContentPlanVO convertToVO(ContentPlan contentPlan) {
        ContentPlanVO vo = new ContentPlanVO();
        BeanUtils.copyProperties(contentPlan, vo);
        return vo;
    }
}
