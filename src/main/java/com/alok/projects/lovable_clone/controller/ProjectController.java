package com.alok.projects.lovable_clone.controller;

import com.alok.projects.lovable_clone.dto.project.ProjectRequest;
import com.alok.projects.lovable_clone.dto.project.ProjectResponse;
import com.alok.projects.lovable_clone.dto.project.ProjectSummaryResponse;
import com.alok.projects.lovable_clone.service.ProjectService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ProjectController {
    ProjectService projectService;

    @GetMapping
    public ResponseEntity<List<ProjectSummaryResponse>> getMyProjects() {
        Long userId = 1L; //TODO: update later with real Spring Security
        return  ResponseEntity.ok(projectService.getUserProjects(userId));
    }

    //NOTE: project is collaborative; but owner will be one; in ProjectResponse, user is that owner
    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable Long id) {
        Long userId = 1L; //TODO: update later with real Spring Security
        return ResponseEntity.ok(projectService.getUserProjectById(userId));
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@RequestBody ProjectRequest request) {
        Long userId = 1L; //TODO: update later with real Spring Security
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.createProject(request, userId));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(@PathVariable Long id, @RequestBody ProjectRequest request) {
        Long userId = 1L; //TODO: update later with real Spring Security
        return ResponseEntity.ok(projectService.updateProject(id, request, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        Long userId = 1L; //TODO: update later with real Spring Security
        projectService.softDelete(id, userId);
        return ResponseEntity.noContent().build();
    }
}
