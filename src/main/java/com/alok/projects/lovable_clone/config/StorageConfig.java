package com.alok.projects.lovable_clone.config;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration

/**
 * it'll check all the keys under "minio" inside application.yml and map those to the variables
 * variable names[camelCase] & properties[kebab-case] of minio names should be same.
 * It's alternative of writing "@Value"
 */
@ConfigurationProperties(prefix = "minio") ///
@Data
public class StorageConfig {

//    @Value("${minio.url}")
//    private String url;
//
//    @Value("${minio.access-key}")
//    private String accessKey;
//
//    @Value("${minio.secret-key}")
//    private String secretKey;
//

    String url; /// minio.url
    String accessKey; /// minio.access-key
    String secretKey; /// minio.secret-key

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(url)
                .credentials(accessKey, secretKey)
                .build();
    }
}
