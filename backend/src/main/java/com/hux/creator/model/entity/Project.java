package com.hux.creator.model.entity;

import com.hux.creator.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "t_project")
public class Project extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 50)
    private String platform;

    @Column(length = 200)
    private String targetUser;

    @Column
    private Integer duration;

    @Column(length = 50)
    private String style;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "INTEGER DEFAULT 0")
    private Integer status = 0;

    @Column(nullable = false)
    private Long userId;

    @Column(length = 20)
    private String videoType;

    @Column(columnDefinition = "FLOAT DEFAULT 0.7")
    private Float narrativeRhythm = 0.7f;

    @Column(length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'AUTO'")
    private String visualStyle = "AUTO";

    @Column(length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'QUICK'")
    private String generationMode = "QUICK";

    @Column(columnDefinition = "INTEGER DEFAULT 0")
    private Integer currentRound = 0;

    @Column(columnDefinition = "INTEGER DEFAULT 3")
    private Integer maxRounds = 3;

    private Float finalScore;
}