package com.hux.creator.model.entity;

import com.hux.creator.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "t_generation_round")
public class GenerationRound extends BaseEntity {

    @Column(nullable = false)
    private Long projectId;

    @Column(nullable = false)
    private Integer roundNumber;

    @Column(length = 20)
    private String mode;

    private Long scriptId;

    @Column(columnDefinition = "TEXT")
    private String model1Result;

    @Column(columnDefinition = "TEXT")
    private String model2Result;

    @Column(columnDefinition = "TEXT")
    private String model3Result;

    @Column(columnDefinition = "TEXT")
    private String model4Result;

    private Float model5Score;

    @Column(columnDefinition = "TEXT")
    private String model5Suggestions;

    @Column(columnDefinition = "INTEGER DEFAULT 0")
    private Integer status = 0;
}
