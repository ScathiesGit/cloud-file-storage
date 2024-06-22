package git.scathies.cloudfilestorage.configuration;

import git.scathies.cloudfilestorage.configuration.properties.FileStorageConfigProperties;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class MinioConfiguration {

    private final FileStorageConfigProperties configProps;

    @Bean
    public MinioClient minioClient() {
        var minioClient = MinioClient.builder()
                .endpoint(configProps.getEndpoint())
                .credentials(configProps.getUsername(), configProps.getPassword())
                .build();

        try {
            if (!minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(configProps.getBucketName())
                    .build())) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(configProps.getBucketName())
                        .build());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return minioClient;
    }
}
