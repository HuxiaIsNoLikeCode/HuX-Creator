package com.hux.creator.model.entity;

import com.hux.creator.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "t_script")
public class Script extends BaseEntity {

    @Column(nullable = false)
    private Long projectId;

    @Column(nullable = false)
    private Integer round;

    @Column(columnDefinition = "INTEGER DEFAULT 1")
    private Integer version = 1;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(length = 20)
    private String videoType;

    private Float narrativeRhythm;

    private Integer wordCount;

    private Integer estimatedDuration;

    @Column(columnDefinition = "INTEGER DEFAULT 0")
    private Integer status = 0;

    private Float score;
}
