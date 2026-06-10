package com.hux.creator.model.entity;

import com.hux.creator.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "t_scene")
public class Scene extends BaseEntity {

    @Column(nullable = false)
    private Long scriptId;

    @Column(nullable = false)
    private Integer sceneNumber;

    @Column(length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 200)
    private String location;

    @Column(length = 100)
    private String mood;

    private Integer estimatedDuration;

    @Column(length = 50)
    private String visualType;

    @Column(length = 100)
    private String transition;

    @Column(columnDefinition = "TEXT")
    private String backgroundPrompt;

    @Column(length = 500)
    private String backgroundUrl;
}
