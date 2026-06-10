package com.hux.creator.service.impl;

import com.hux.creator.model.dto.CreateProjectDTO;
import com.hux.creator.model.dto.UpdateProjectDTO;
import com.hux.creator.model.entity.Project;
import com.hux.creator.model.vo.ProjectVO;
import com.hux.creator.repository.ProjectRepository;
import com.hux.creator.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;

    @Override
    @Transactional
    public ProjectVO createProject(Long userId, CreateProjectDTO dto) {
        Project project = new Project();
        BeanUtils.copyProperties(dto, project);
        project.setUserId(userId);
        project.setStatus(0);
        project.setDeleted(0);
        project = projectRepository.save(project);
        return convertToVO(project);
    }

    @Override
    @Transactional
    public ProjectVO updateProject(Long projectId, UpdateProjectDTO dto) {
        Project project = projectRepository.findByIdAndDeleted(projectId, 0)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
        if (dto.getTitle() != null) {
            project.setTitle(dto.getTitle());
        }
        if (dto.getPlatform() != null) {
            project.setPlatform(dto.getPlatform());
        }
        if (dto.getTargetUser() != null) {
            project.setTargetUser(dto.getTargetUser());
        }
        if (dto.getDuration() != null) {
            project.setDuration(dto.getDuration());
        }
        if (dto.getStyle() != null) {
            project.setStyle(dto.getStyle());
        }
        if (dto.getDescription() != null) {
            project.setDescription(dto.getDescription());
        }
        if (dto.getStatus() != null) {
            project.setStatus(dto.getStatus());
        }
        if (dto.getVideoType() != null) {
            project.setVideoType(dto.getVideoType());
        }
        if (dto.getNarrativeRhythm() != null) {
            project.setNarrativeRhythm(dto.getNarrativeRhythm());
        }
        if (dto.getVisualStyle() != null) {
            project.setVisualStyle(dto.getVisualStyle());
        }
        if (dto.getGenerationMode() != null) {
            project.setGenerationMode(dto.getGenerationMode());
        }
        project = projectRepository.save(project);
        return convertToVO(project);
    }

    @Override
    @Transactional
    public void deleteProject(Long projectId) {
        Project project = projectRepository.findByIdAndDeleted(projectId, 0)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
        project.setDeleted(1);
        projectRepository.save(project);
    }

    @Override
    public ProjectVO getProject(Long projectId) {
        Project project = projectRepository.findByIdAndDeleted(projectId, 0)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
        return convertToVO(project);
    }

    @Override
    public List<ProjectVO> getProjectList(Long userId) {
        List<Project> projects = projectRepository.findByUserIdAndDeletedOrderByCreateTimeDesc(userId, 0);
        return projects.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    private ProjectVO convertToVO(Project project) {
        ProjectVO vo = new ProjectVO();
        BeanUtils.copyProperties(project, vo);
        return vo;
    }
}
