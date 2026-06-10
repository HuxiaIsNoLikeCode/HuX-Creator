package com.hux.creator.controller;

import com.hux.creator.common.Result;
import com.hux.creator.model.dto.StartGenerationDTO;
import com.hux.creator.model.vo.GenerationProgressVO;
import com.hux.creator.service.GenerationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@RestController
@RequestMapping("/api/generation")
@RequiredArgsConstructor
public class GenerationController {

    private final GenerationService generationService;
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    @PostMapping("/start")
    public Result<String> startGeneration(@Valid @RequestBody StartGenerationDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long projectId = dto.getProjectId();
        String modelProvider = dto.getModelProvider();

        CompletableFuture.runAsync(() -> {
            try {
                generationService.startGeneration(userId, projectId, modelProvider);
            } catch (Exception e) {
                log.error("异步生成任务失败，项目ID: {}, 错误: {}", projectId, e.getMessage(), e);
            }
        }, EXECUTOR);

        return Result.success("生成任务已启动");
    }

    @GetMapping("/progress/{projectId}")
    public Result<GenerationProgressVO> getProgress(@PathVariable Long projectId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        GenerationProgressVO vo = generationService.getProgress(userId, projectId);
        return Result.success(vo);
    }
}
