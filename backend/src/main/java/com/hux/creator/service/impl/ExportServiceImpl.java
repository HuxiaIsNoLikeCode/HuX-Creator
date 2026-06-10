package com.hux.creator.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hux.creator.model.entity.ContentPlan;
import com.hux.creator.model.entity.Project;
import com.hux.creator.repository.ContentPlanRepository;
import com.hux.creator.repository.ProjectRepository;
import com.hux.creator.service.ExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class ExportServiceImpl implements ExportService {

    private final ProjectRepository projectRepository;
    private final ContentPlanRepository contentPlanRepository;
    private final ObjectMapper objectMapper;

    @Override
    public String exportMarkdown(Long userId, Long projectId) {
        ContentPlan contentPlan = getContentPlanWithAuth(userId, projectId);
        Project project = projectRepository.findByIdAndDeleted(projectId, 0)
                .orElseThrow(() -> new RuntimeException("项目不存在"));

        StringBuilder sb = new StringBuilder();
        sb.append("# ").append(project.getTitle()).append("\n\n");

        appendSection(sb, "话题分析", contentPlan.getTopicAnalysis());
        appendSection(sb, "标题方案", contentPlan.getTitleOptions());
        appendSection(sb, "开头钩子", contentPlan.getHook());
        appendSection(sb, "脚本文案", contentPlan.getScript());
        appendSection(sb, "分镜脚本", contentPlan.getStoryboard());
        appendSection(sb, "素材清单", contentPlan.getMaterialList());
        appendSection(sb, "图片提示词", contentPlan.getImagePrompt());
        appendSection(sb, "视频提示词", contentPlan.getVideoPrompt());
        appendSection(sb, "封面文案", contentPlan.getCoverCopy());
        appendSection(sb, "发布文案", contentPlan.getPublishCopy());

        return sb.toString();
    }

    @Override
    public String exportJson(Long userId, Long projectId) {
        ContentPlan contentPlan = getContentPlanWithAuth(userId, projectId);
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(contentPlan);
        } catch (Exception e) {
            throw new RuntimeException("导出JSON失败");
        }
    }

    @Override
    public byte[] exportWord(Long userId, Long projectId) {
        String text = exportMarkdown(userId, projectId);
        return text.getBytes(StandardCharsets.UTF_8);
    }

    private ContentPlan getContentPlanWithAuth(Long userId, Long projectId) {
        Project project = projectRepository.findByIdAndDeleted(projectId, 0)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
        if (!project.getUserId().equals(userId)) {
            throw new RuntimeException("无权访问此项目");
        }
        return contentPlanRepository.findFirstByProjectIdOrderByCreateTimeDesc(projectId)
                .orElseThrow(() -> new RuntimeException("暂无内容方案"));
    }

    private void appendSection(StringBuilder sb, String title, String content) {
        if (content != null && !content.isEmpty()) {
            sb.append("## ").append(title).append("\n\n");
            sb.append(content).append("\n\n");
        }
    }
}
