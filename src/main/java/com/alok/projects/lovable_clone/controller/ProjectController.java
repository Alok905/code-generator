package com.alok.projects.lovable_clone.controller;

import com.alok.projects.lovable_clone.dto.project.ProjectRequest;
import com.alok.projects.lovable_clone.dto.project.ProjectResponse;
import com.alok.projects.lovable_clone.dto.project.ProjectSummaryResponse;
import com.alok.projects.lovable_clone.service.ProjectService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ProjectController {
    ProjectService projectService;

    @GetMapping
    public ResponseEntity<List<ProjectSummaryResponse>> getMyProjects() {
        return  ResponseEntity.ok(projectService.getUserProjects());
    }

    //NOTE: project is collaborative; but owner will be one; in ProjectResponse, user is that owner
    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable(name = "projectId") Long id) {
        return ResponseEntity.ok(projectService.getUserProjectById(id));
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@RequestBody @Valid ProjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.createProject(request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(@PathVariable Long id, @RequestBody @Valid ProjectRequest request) {
        return ResponseEntity.ok(projectService.updateProject(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
