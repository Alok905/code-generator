package com.alok.projects.lovable_clone.service.impl;

import com.alok.projects.lovable_clone.entity.Project;
import com.alok.projects.lovable_clone.entity.ProjectFile;
import com.alok.projects.lovable_clone.error.ResourceNotFoundException;
import com.alok.projects.lovable_clone.repository.ProjectFileRepository;
import com.alok.projects.lovable_clone.repository.ProjectRepository;
import com.alok.projects.lovable_clone.service.ProjectTemplateService;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProjectTemplateServiceImpl implements ProjectTemplateService {
    private final ProjectFileRepository projectFileRepository;

    private final MinioClient minioClient;
    private final ProjectRepository projectRepository;

    private final String STARTER_BUCKET_NAME = "project-starter";
    private final String REACT_VITE_STARTER_OBJECT_NAME = "test-project";

    @Value("${minio.project-bucket}")
    private String TARGET_BUCKET_NAME;

    @Override
    public void initializeProjectFromTemplate(Long projectId) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Project", projectId.toString())
                );

        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(STARTER_BUCKET_NAME)
                            .prefix(REACT_VITE_STARTER_OBJECT_NAME + "/")
                            .recursive(true)
                            .build()
            );

            List<ProjectFile> filesToSave = new ArrayList<>();
            for (Result<Item> result : results) {
                Item item = result.get();

                String sourceKey = item.objectName(); /// path
                /// just getting the path from starter bucket to project bucket by altering sourceKey
                String cleanPath = sourceKey.replaceFirst(REACT_VITE_STARTER_OBJECT_NAME + "/", "");
                String destKey = projectId + cleanPath;

                minioClient.copyObject(
                        CopyObjectArgs.builder()
                                .bucket(TARGET_BUCKET_NAME)
                                .object(destKey)
                                .source(
                                        CopySource.builder()
                                                .bucket(STARTER_BUCKET_NAME)
                                                .object(sourceKey)
                                                .build()
                                )
                                .build()
                );

                ProjectFile pf = ProjectFile.builder()
                        .project(project)
                        .path(cleanPath)
                        .minioObjectKey(destKey)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build();
                filesToSave.add(pf);
            }
            projectFileRepository.saveAll(filesToSave);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
