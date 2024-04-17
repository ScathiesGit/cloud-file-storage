package git.scathies.cloudfilestorage.configuration;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfiguration {

    @Bean(name = "minioClient")
    public MinioClient minioClient(@Value("${file-storage.username}") String username,
                                   @Value("${file-storage.password}") String password,
                                   @Value("${file-storage.endpoint}") String endpoint) {

        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(username, password)
                .build();
    }
}
