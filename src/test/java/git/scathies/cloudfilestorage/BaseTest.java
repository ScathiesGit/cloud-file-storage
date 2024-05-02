package git.scathies.cloudfilestorage;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
public class BaseTest {

    public static final MinIOContainer MINIO_CONTAINER = new MinIOContainer(DockerImageName.parse(
            "minio/minio:latest").asCompatibleSubstituteFor("minio/minio"));

    public static final MySQLContainer<?> MYSQL_CONTAINER = new MySQLContainer<>("mysql:8.3.0");

    static {
        MINIO_CONTAINER.start();
        MYSQL_CONTAINER.start();
    }

    @DynamicPropertySource
    static void propertyRegistry(DynamicPropertyRegistry registry) {
        registry.add("file-storage.endpoint", MINIO_CONTAINER::getS3URL);
        registry.add("spring.datasource.url", MYSQL_CONTAINER::getJdbcUrl);
    }
}
