package com.hux.creator.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GenerateContentDTO {

    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    private String modelProvider;
}
