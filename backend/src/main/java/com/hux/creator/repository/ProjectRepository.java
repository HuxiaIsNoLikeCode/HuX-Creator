package com.hux.creator.repository;

import com.hux.creator.model.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByUserId(Long userId);

    Page<Project> findByUserId(Long userId, Pageable pageable);

    List<Project> findByUserIdAndStatus(Long userId, Integer status);

    List<Project> findByPlatform(String platform);

    Optional<Project> findByIdAndDeleted(Long id, Integer deleted);

    List<Project> findByUserIdAndDeletedOrderByCreateTimeDesc(Long userId, Integer deleted);
}