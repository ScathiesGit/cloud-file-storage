package git.scathies.cloudfilestorage.integration;

import git.scathies.cloudfilestorage.repository.FileSystemObjectRepository;
import git.scathies.cloudfilestorage.service.FileSystemObjectService;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.errors.ErrorResponseException;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

public class FileSystemObjectServiceTest extends BaseIntegrationTest {

    @Autowired
    private FileSystemObjectService fileSystemObjectService;

    @Autowired
    private FileSystemObjectRepository fileSystemObjectRepository;

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @BeforeEach
    @SneakyThrows
    void setUp() {
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
        }

        List<String> paths = new ArrayList<>();
        var rawObjs = fileSystemObjectRepository.findAllByPrefix("");
        for (var rawObj : rawObjs) {
            paths.add(rawObj.get().objectName());
        }
        if (!paths.isEmpty()) {
            fileSystemObjectRepository.deleteAll(paths);
        }
    }

    @Test
    @SneakyThrows
    void givenInputStreamWhenCreateFileThenFileShouldAppearInStorage() {
        var fileContent = "content for simple txt file".getBytes();
        var inputStream = new ByteArrayInputStream(fileContent);
        var path = "directory/text.txt";

        fileSystemObjectService.createFile(path, "text/plain", inputStream);

        try (var loadedInputStream = minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucketName)
                .object(path)
                .build())
        ) {
            assertThat(fileContent).isEqualTo(loadedInputStream.readAllBytes());
        }
    }

    @Test
    @SneakyThrows
    void givenValidNewFileNameWhenRenameFileThenFileNameUpdatedAndOldNameIsNotExist() {
        var newName = "name_updated.txt";
        var oldName = "text.txt";
        var bytes = "content for simple txt file".getBytes();
        var inputStream = new ByteArrayInputStream(bytes);
        var pathWithoutFilename = "directory/";
        fileSystemObjectService.createFile(pathWithoutFilename + oldName, "text/plain", inputStream);

        fileSystemObjectService.renameFile(pathWithoutFilename, oldName, newName);

        try (var loadedInputStream = minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucketName)
                .object(pathWithoutFilename + newName)
                .build())
        ) {
            assertAll(
                    () -> assertThat(bytes).isEqualTo(loadedInputStream.readAllBytes()),
                    () -> assertThatThrownBy(() -> minioClient.getObject(GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(pathWithoutFilename + oldName)
                            .build()))
                            .isInstanceOf(ErrorResponseException.class)
            );
        }
    }

    @Test
    @SneakyThrows
    void givenPathWhenRemoveFileThenStorageNotContainIt() {
        var path = "directory/text.txt";
        fileSystemObjectService.createFile(path, "text/plain",
                new ByteArrayInputStream("content for simple txt file".getBytes()));

        fileSystemObjectService.removeFile(path);

        List<String> pathsToResources = new ArrayList<>();
        var resources = fileSystemObjectRepository.findAllByPrefix("");
        for (var resource : resources) {
            pathsToResources.add(resource.get().objectName());
        }
        assertThat(pathsToResources).doesNotContain(path);
    }

    @Test
    @SneakyThrows
    void givenPathWhenCreateFolderThenFindInStorage() {
        var path = "new/directory/";

        fileSystemObjectService.createFolder(path);

        List<String> paths = new ArrayList<>();
        var rawItems = fileSystemObjectRepository.findAllByPrefix(path);
        for (var rawItem : rawItems) {
            paths.add(rawItem.get().objectName());
        }
        assertThat(paths).contains(path).hasSize(1);
    }

    @Test
    @SneakyThrows
    void givenNewNameFolderWhenRenameFolderThenMoveAllContent() {
        var expectedNumberUpdatedObjects = 4;
        var parent = "new/directory/";
        var folderChild1 = parent + "child1/";
        var folderChild2 = folderChild1 + "child2/";
        var fileChild1 = parent + "another/test/file.txt";
        var newName = "updateName";
        var newParent = "new/" + newName + "/";
        var newFolderChild1 = newParent + "child1/";
        var newFolderChild2 = newFolderChild1 + "child2/";
        var newFileChild1 = newParent + "another/test/file.txt";
        fileSystemObjectService.createFolder(parent);
        fileSystemObjectService.createFolder(folderChild1);
        fileSystemObjectService.createFolder(folderChild2);
        fileSystemObjectService.createFile(fileChild1, "plain/text", new ByteArrayInputStream(new byte[] {}));

        fileSystemObjectService.renameFolder(parent, newName);

        List<String> oldPaths = new ArrayList<>();
        var rawOldItems = fileSystemObjectRepository.findAllByPrefix(parent);
        for (var rawOldItem : rawOldItems) {
            oldPaths.add(rawOldItem.get().objectName());
        }
        List<String> newPaths = new ArrayList<>();
        var rawNewItems = fileSystemObjectRepository.findAllByPrefix(newParent);
        for (var rawNewItem : rawNewItems) {
            newPaths.add(rawNewItem.get().objectName());
        }
        assertAll(
                () -> assertThat(oldPaths).hasSize(0),
                () -> assertThat(newPaths).hasSize(expectedNumberUpdatedObjects)
                        .contains(newParent, newFolderChild1, newFolderChild2, newFileChild1)
        );
    }

    @Test
    @SneakyThrows
    void whenRemoveFolderThenAllChildrenAreDeleted() {
        var parent = "new/directory/";
        var folderChild1 = parent + "child1/";
        var folderChild2 = folderChild1 + "child2/";
        var fileChild1 = parent + "another/test/file.txt";
        fileSystemObjectService.createFolder(parent);
        fileSystemObjectService.createFolder(folderChild1);
        fileSystemObjectService.createFolder(folderChild2);
        fileSystemObjectService.createFile(fileChild1, "plain/text", new ByteArrayInputStream(new byte[] {}));

        fileSystemObjectService.removeFolder(parent);

        List<String> paths = new ArrayList<>();
        var rawItems = fileSystemObjectRepository.findAllByPrefix(parent);
        for (var rawItem : rawItems) {
            paths.add(rawItem.get().objectName());
        }
        List<String> allPaths = new ArrayList<>();
        var rawAllItems = fileSystemObjectRepository.findAllByPrefix("");
        for (var rawItem : rawAllItems) {
            allPaths.add(rawItem.get().objectName());
        }
        assertAll(
                () -> assertThat(paths).hasSize(0),
                () -> assertThat(allPaths).hasSize(1)
        );
    }
}
