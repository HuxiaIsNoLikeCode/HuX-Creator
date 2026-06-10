package com.hux.creator.model.vo;

import lombok.Data;
import java.util.List;

@Data
public class GenerationProgressVO {

    private Long projectId;
    private Integer currentRound;
    private Integer maxRounds;
    private String status;
    private Integer currentModel;
    private String currentModelName;
    private Float score;
    private Boolean passed;
    private List<RoundVO> rounds;

    @Data
    public static class RoundVO {
        private Integer roundNumber;
        private String mode;
        private Integer status;
        private Float score;
        private String suggestions;
    }
}
