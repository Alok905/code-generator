package com.alok.projects.lovable_clone.service.impl;

import com.alok.projects.lovable_clone.dto.project.FileContentResponse;
import com.alok.projects.lovable_clone.dto.project.FileNode;
import com.alok.projects.lovable_clone.entity.Project;
import com.alok.projects.lovable_clone.entity.ProjectFile;
import com.alok.projects.lovable_clone.error.ResourceNotFoundException;
import com.alok.projects.lovable_clone.mapper.ProjectFileMapper;
import com.alok.projects.lovable_clone.repository.ProjectFileRepository;
import com.alok.projects.lovable_clone.repository.ProjectRepository;
import com.alok.projects.lovable_clone.service.ProjectFileService;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProjectFileServiceImpl implements ProjectFileService {

    private final ProjectRepository projectRepository;
    private final ProjectFileRepository projectFileRepository;
    private final MinioClient minioClient;
    private final ProjectFileMapper projectFileMapper;

    @Value("${minio.project-bucket}")
    private String projectBucket;

    @Override
    public List<FileNode> getFileTree(Long projectId) {
        List<ProjectFile> projectFileList = projectFileRepository.findByProjectId(projectId);
        return projectFileMapper.toListOfFileNode(projectFileList);
    }

    @Override
    public FileContentResponse getFileContent(Long projectId, String path) {
        String objectName = projectId + "/" + path;

        try(
                InputStream is = minioClient.getObject(
                        GetObjectArgs.builder()
                                .bucket(projectBucket)
                                .object(objectName)
                                .build()
                )
                ) {
            String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return new FileContentResponse(path, content);
        } catch (Exception e) {
            log.error("Failed to read file: {}/{}", projectId, path, e);
            throw new RuntimeException("Failed to read file content.", e);
        }
    }

    @Override
    public void saveFile(Long projectId, String filePath, String fileContent) {
        Project project = projectRepository.findById(projectId)
                        .orElseThrow(
                                () -> new ResourceNotFoundException("Project", projectId.toString())
                        );
        String cleanPath = filePath.startsWith("/") ? filePath.substring(1) : filePath;
        String objectKey = projectId + "/" + cleanPath;  /// 34/src/routes/UserRouter.jsx

        try {
            /// we can directly store the string, but sometimes if we want to store images and all then it'll be helpful
            byte[] contentBytes = fileContent.getBytes(StandardCharsets.UTF_8);
            InputStream inputStream = new ByteArrayInputStream(contentBytes);

            ///  saving the file content
            String fileContentType = determineContentType(filePath);
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(projectBucket)
                            .object(objectKey)
                            .stream(inputStream, contentBytes.length, -1)
                            .contentType(fileContentType)
                            .build()
            );

            ///  saving the metadata
            ProjectFile file = projectFileRepository.findByProjectIdAndPath(projectId, cleanPath)
                    .orElseGet(() -> ProjectFile.builder()
                            .project(project)
                            .path(filePath)
                            .minioObjectKey(objectKey)
                            .createdAt(Instant.now())
                            .build()
                    );

            file.setUpdatedAt(Instant.now());
            projectFileRepository.save(file);

            log.info("Saved file: {}", objectKey);
        } catch (Exception e) {
            log.error("Failed to save file {}/{}", projectId, cleanPath, e);
            throw new RuntimeException(e);
        }


        // save the file metadata in minio
        // save the file content in postgres
    }

    private String determineContentType(String path) {
        String type = URLConnection.guessContentTypeFromName(path);
        if (type != null)   return type;
        if (path.endsWith(".js") || path.endsWith(".jsx") || path.endsWith(".ts") || path.endsWith(".tsx"))  return "text/javascript";
        if (path.endsWith(".json")) return "application/json";
        if (path.endsWith(".css")) return "text/css";

        return "text/plain";
    }
}
