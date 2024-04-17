package git.scathies.cloudfilestorage.integration;

import git.scathies.cloudfilestorage.model.FileSystemObject;
import git.scathies.cloudfilestorage.model.User;
import git.scathies.cloudfilestorage.repository.FileSystemObjectRepository;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
public class DownloadControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FileSystemObjectRepository fileSystemObjectRepository;

    @Value("${file-storage.bucket-name}")
    private String bucketName;

    @Value("${file-storage.root-folder-template}")
    private String rootFolderTemplate;

    @Autowired
    private MinioClient minioClient;

    private User user = new User(1L, "user", "pass");

    @BeforeEach
    @SneakyThrows
    void setUp() {
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
        }

        fileSystemObjectRepository.saveRootFolder(user);
        fileSystemObjectRepository.deleteAll(fileSystemObjectRepository.findAllInRootFolder(user)
                .stream().map(FileSystemObject::getName).toList());
    }

    @Test
    @SneakyThrows
    void givenExistPathToFileWhenDownloadThenReturnFile() {
        var baos = new ByteArrayOutputStream();
        baos.write("test text".getBytes(StandardCharsets.UTF_8));
        var path = rootFolderTemplate.formatted(user.getId()) + "file.txt";
        fileSystemObjectRepository.saveFile(path, "text/plain", new ByteArrayInputStream(baos.toByteArray()));

        var result = mockMvc.perform(get("/download")
                .param("path", "file.txt")
                .sessionAttr("user", user))
                .andExpect(status().isOk())
                .andReturn();
        var resp = result.getResponse().getContentAsByteArray();

        assertThat(resp).isEqualTo(baos.toByteArray());
    }
}
