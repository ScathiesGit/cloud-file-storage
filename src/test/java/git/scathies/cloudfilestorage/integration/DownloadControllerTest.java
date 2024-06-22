package git.scathies.cloudfilestorage.integration;

import git.scathies.cloudfilestorage.BaseTest;
import git.scathies.cloudfilestorage.model.User;
import git.scathies.cloudfilestorage.repository.StorageItemRepository;
import io.minio.MinioClient;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
public class DownloadControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StorageItemRepository storageItemRepository;

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
//        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
//            minioClient.makeBucket(MakeBucketArgs.builder()
//                    .bucket(bucketName)
//                    .build());
//        }
//
//        fileSystemObjectRepository.saveRootFolder(user);
//        fileSystemObjectRepository.deleteAll(fileSystemObjectRepository.findAllInRootFolder(user)
//                .stream().map(FileSystemObject::getName).toList());
    }

//    @Test
//    @SneakyThrows
//    void givenExistPathToFileWhenDownloadThenReturnFile() {
//        var baos = new ByteArrayOutputStream();
//        baos.write("test text".getBytes(StandardCharsets.UTF_8));
//        var path = rootFolderTemplate.formatted(user.getId()) + "file.txt";
//        fileSystemObjectRepository.saveFile(path, "text/plain", new ByteArrayInputStream(baos.toByteArray()));
//
//        var result = mockMvc.perform(get("/download")
//                .param("path", "file.txt")
//                .sessionAttr("user", user))
//                .andExpect(status().isOk())
//                .andReturn();
//        var resp = result.getResponse().getContentAsByteArray();
//
//        assertThat(resp).isEqualTo(baos.toByteArray());
//    }
}
