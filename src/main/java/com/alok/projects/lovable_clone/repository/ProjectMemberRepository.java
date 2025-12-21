package com.alok.projects.lovable_clone.repository;

import com.alok.projects.lovable_clone.entity.ProjectMember;
import com.alok.projects.lovable_clone.entity.ids.ProjectMemberId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface ProjectMemberRepository extends JpaRepository<ProjectMember, ProjectMemberId> {

    List<ProjectMember> findByIdProjectId(Long projectId);
}
