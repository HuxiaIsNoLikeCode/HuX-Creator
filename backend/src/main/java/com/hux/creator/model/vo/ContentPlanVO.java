package com.hux.creator.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ContentPlanVO {

    private Long id;

    private Long projectId;

    private String topicAnalysis;

    private String titleOptions;

    private String hook;

    private String script;

    private String storyboard;

    private String materialList;

    private String imagePrompt;

    private String videoPrompt;

    private String coverCopy;

    private String publishCopy;

    private Integer status;

    private Integer generationStep;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
