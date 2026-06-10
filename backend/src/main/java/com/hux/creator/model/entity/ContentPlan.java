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
@Table(name = "t_content_plan")
public class ContentPlan extends BaseEntity {

    @Column(nullable = false)
    private Long projectId;

    @Column(columnDefinition = "TEXT")
    private String topicAnalysis;

    @Column(columnDefinition = "TEXT")
    private String titleOptions;

    @Column(columnDefinition = "TEXT")
    private String hook;

    @Column(columnDefinition = "TEXT")
    private String script;

    @Column(columnDefinition = "TEXT")
    private String storyboard;

    @Column(columnDefinition = "TEXT")
    private String materialList;

    @Column(columnDefinition = "TEXT")
    private String imagePrompt;

    @Column(columnDefinition = "TEXT")
    private String videoPrompt;

    @Column(columnDefinition = "TEXT")
    private String coverCopy;

    @Column(columnDefinition = "TEXT")
    private String publishCopy;

    @Column(columnDefinition = "INTEGER DEFAULT 0")
    private Integer status = 0;

    @Column(columnDefinition = "INTEGER DEFAULT 0")
    private Integer generationStep = 0;
}