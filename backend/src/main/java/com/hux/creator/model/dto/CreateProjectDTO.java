package com.hux.creator.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateProjectDTO {

    @NotBlank(message = "项目标题不能为空")
    private String title;

    private String platform;

    private String targetUser;

    private Integer duration;

    private String style;

    private String description;

    private Float narrativeRhythm;

    private String visualStyle;

    private String generationMode;
}
