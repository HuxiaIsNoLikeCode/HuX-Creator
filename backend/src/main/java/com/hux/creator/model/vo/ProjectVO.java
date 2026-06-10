package com.hux.creator.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProjectVO {

    private Long id;

    private String title;

    private String platform;

    private String targetUser;

    private Integer duration;

    private String style;

    private String description;

    private Integer status;

    private String videoType;

    private Float narrativeRhythm;

    private String visualStyle;

    private String generationMode;

    private Integer currentRound;

    private Integer maxRounds;

    private Float finalScore;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
