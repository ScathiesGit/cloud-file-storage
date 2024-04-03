package git.scathies.cloudfilestorage.configuration;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfiguration {

    @Bean(name = "minioClient")
    public MinioClient minioClient(@Value("${minio.username}") String username,
                                   @Value("${minio.password}") String password,
                                   @Value("${minio.endpoint}") String endpoint) {

        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(username, password)
                .build();
    }
}
