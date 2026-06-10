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
@Table(name = "t_revision_record")
public class RevisionRecord extends BaseEntity {

    @Column(nullable = false)
    private Long projectId;

    @Column(nullable = false)
    private Long contentPlanId;

    @Column(nullable = false, length = 100)
    private String sectionName;

    @Column(columnDefinition = "TEXT")
    private String originalContent;

    @Column(columnDefinition = "TEXT")
    private String optimizedContent;

    @Column(columnDefinition = "TEXT")
    private String instruction;
}