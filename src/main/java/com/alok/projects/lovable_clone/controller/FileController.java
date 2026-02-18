package com.alok.projects.lovable_clone.controller;

import com.alok.projects.lovable_clone.dto.project.FileContentResponse;
import com.alok.projects.lovable_clone.dto.project.FileNode;
import com.alok.projects.lovable_clone.service.ProjectFileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/projects/{projectId}/files")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class FileController {
    ProjectFileService projectFileService;

    @GetMapping
    @PreAuthorize("@security.canViewProject(#projectId)")  /// --------- extra
    public ResponseEntity<List<FileNode>> getFileTree(@PathVariable Long projectId) {
        return ResponseEntity.ok(projectFileService.getFileTree(projectId));
    }

    @GetMapping("/{*path}")
    public ResponseEntity<FileContentResponse> getFile(
            @PathVariable Long projectId,
            @PathVariable String path
    ) {
        return ResponseEntity.ok(projectFileService.getFileContent(projectId, path));
    }
}
