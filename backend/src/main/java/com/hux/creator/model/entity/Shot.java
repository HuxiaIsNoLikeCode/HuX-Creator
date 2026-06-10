package com.hux.creator.model.entity;

import com.hux.creator.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "t_shot")
public class Shot extends BaseEntity {

    @Column(nullable = false)
    private Long sceneId;

    @Column(nullable = false)
    private Integer shotNumber;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 100)
    private String cameraAngle;

    @Column(length = 100)
    private String cameraMovement;

    private Integer duration;

    @Column(columnDefinition = "TEXT")
    private String dialogue;

    @Column(length = 50)
    private String visualType;

    @Column(length = 100)
    private String transition;

    @Column(columnDefinition = "TEXT")
    private String firstFramePrompt;

    @Column(length = 500)
    private String firstFrameUrl;

    @Column(columnDefinition = "TEXT")
    private String lastFramePrompt;

    @Column(length = 500)
    private String lastFrameUrl;
}
