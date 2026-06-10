package com.hux.creator.controller;

import com.hux.creator.common.Result;
import com.hux.creator.model.dto.CreateProjectDTO;
import com.hux.creator.model.dto.UpdateProjectDTO;
import com.hux.creator.model.vo.ProjectVO;
import com.hux.creator.service.ProjectService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/project")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping("/create")
    public Result<ProjectVO> createProject(@Valid @RequestBody CreateProjectDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        ProjectVO projectVO = projectService.createProject(userId, dto);
        return Result.success(projectVO);
    }

    @PutMapping("/{id}")
    public Result<ProjectVO> updateProject(@PathVariable Long id, @RequestBody UpdateProjectDTO dto) {
        ProjectVO projectVO = projectService.updateProject(id, dto);
        return Result.success(projectVO);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<ProjectVO> getProject(@PathVariable Long id) {
        ProjectVO projectVO = projectService.getProject(id);
        return Result.success(projectVO);
    }

    @GetMapping("/list")
    public Result<List<ProjectVO>> getProjectList(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<ProjectVO> projectVOList = projectService.getProjectList(userId);
        return Result.success(projectVOList);
    }
}
