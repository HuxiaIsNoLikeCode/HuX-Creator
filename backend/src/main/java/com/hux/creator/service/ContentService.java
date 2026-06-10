package com.hux.creator.service;

import com.hux.creator.model.dto.GenerateContentDTO;
import com.hux.creator.model.dto.OptimizeContentDTO;
import com.hux.creator.model.dto.RegenerateStepDTO;
import com.hux.creator.model.vo.ContentPlanVO;

public interface ContentService {

    ContentPlanVO generateContent(Long userId, GenerateContentDTO dto);

    ContentPlanVO optimizeContent(Long userId, OptimizeContentDTO dto);

    ContentPlanVO getContent(Long userId, Long projectId);

    ContentPlanVO regenerateStep(Long userId, RegenerateStepDTO dto);
}
