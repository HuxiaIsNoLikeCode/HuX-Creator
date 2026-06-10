package com.hux.creator.controller;

import com.hux.creator.common.Result;
import com.hux.creator.model.dto.GenerateContentDTO;
import com.hux.creator.model.dto.OptimizeContentDTO;
import com.hux.creator.model.dto.RegenerateStepDTO;
import com.hux.creator.model.vo.ContentPlanVO;
import com.hux.creator.service.ContentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/content")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;

    @PostMapping("/generate")
    public Result<ContentPlanVO> generateContent(@Valid @RequestBody GenerateContentDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        ContentPlanVO vo = contentService.generateContent(userId, dto);
        return Result.success(vo);
    }

    @PostMapping("/optimize")
    public Result<ContentPlanVO> optimizeContent(@Valid @RequestBody OptimizeContentDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        ContentPlanVO vo = contentService.optimizeContent(userId, dto);
        return Result.success(vo);
    }

    @GetMapping("/{projectId}")
    public Result<ContentPlanVO> getContent(@PathVariable Long projectId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        ContentPlanVO vo = contentService.getContent(userId, projectId);
        if (vo == null) {
            return Result.success(null);
        }
        return Result.success(vo);
    }

    @PostMapping("/regenerate-step")
    public Result<ContentPlanVO> regenerateStep(@Valid @RequestBody RegenerateStepDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        ContentPlanVO vo = contentService.regenerateStep(userId, dto);
        return Result.success(vo);
    }
}
