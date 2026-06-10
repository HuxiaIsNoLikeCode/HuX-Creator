package com.hux.creator.model.dto;

import lombok.Data;

@Data
public class UpdateProjectDTO {

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
}
