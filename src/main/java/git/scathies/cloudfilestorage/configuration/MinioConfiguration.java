package git.scathies.cloudfilestorage.configuration;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfiguration {

    @Bean(name = "minioClient")
    public MinioClient minioClient(@Value("${file-storage.username}") String username,
                                   @Value("${file-storage.password}") String password,
                                   @Value("${file-storage.endpoint}") String endpoint,
                                   @Value("${file-storage.bucket-name}") String bucketName) {
        var minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(username, password)
                .build();

        try {
            if (!minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build())) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return minioClient;
    }
}
