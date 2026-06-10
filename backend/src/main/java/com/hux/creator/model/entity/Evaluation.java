package com.hux.creator.model.entity;

import com.hux.creator.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "t_evaluation")
public class Evaluation extends BaseEntity {

    @Column(nullable = false)
    private Long projectId;

    @Column(nullable = false)
    private Long scriptId;

    @Column(nullable = false)
    private Integer roundNumber;

    private Float overallScore;

    private Float completenessScore;

    private Float coherenceScore;

    private Float visualFitScore;

    private Float creativityScore;

    @Column(columnDefinition = "TEXT")
    private String suggestions;

    @Column(columnDefinition = "BIT DEFAULT 0")
    private Boolean passed = false;
}
