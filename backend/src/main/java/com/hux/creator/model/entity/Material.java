package com.hux.creator.model.entity;

import com.hux.creator.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "t_material")
public class Material extends BaseEntity {

    @Column(nullable = false)
    private Long sceneId;

    private Long shotId;

    @Column(length = 50, nullable = false)
    private String materialType;

    @Column(length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 200)
    private String position;

    @Column(length = 500)
    private String sourceSuggestion;

    @Column(columnDefinition = "TEXT")
    private String prompt;

    @Column(length = 100)
    private String characterTag;
}
