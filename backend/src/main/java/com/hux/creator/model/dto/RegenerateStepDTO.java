package com.hux.creator.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegenerateStepDTO {

    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @NotNull(message = "步骤编号不能为空")
    @Min(value = 1, message = "步骤编号最小为1")
    @Max(value = 8, message = "步骤编号最大为8")
    private Integer step;

    private String modelProvider;
}
