package com.hux.creator.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OptimizeContentDTO {

    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @NotBlank(message = "优化板块不能为空")
    private String section;

    @NotBlank(message = "优化指令不能为空")
    private String instruction;
}
