package com.hux.creator.service;

import com.hux.creator.model.vo.GenerationProgressVO;

public interface GenerationService {

    void startGeneration(Long userId, Long projectId, String modelProvider);

    GenerationProgressVO getProgress(Long userId, Long projectId);
}
