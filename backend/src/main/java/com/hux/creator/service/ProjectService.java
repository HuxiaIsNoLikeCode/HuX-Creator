package com.hux.creator.service;

import com.hux.creator.model.dto.CreateProjectDTO;
import com.hux.creator.model.dto.UpdateProjectDTO;
import com.hux.creator.model.vo.ProjectVO;

import java.util.List;

public interface ProjectService {

    ProjectVO createProject(Long userId, CreateProjectDTO dto);

    ProjectVO updateProject(Long projectId, UpdateProjectDTO dto);

    void deleteProject(Long projectId);

    ProjectVO getProject(Long projectId);

    List<ProjectVO> getProjectList(Long userId);
}
