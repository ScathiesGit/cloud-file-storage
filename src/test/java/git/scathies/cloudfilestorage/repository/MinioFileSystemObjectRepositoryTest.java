package git.scathies.cloudfilestorage.repository;

import git.scathies.cloudfilestorage.BaseTest;
import git.scathies.cloudfilestorage.model.FileSystemObject;
import git.scathies.cloudfilestorage.model.User;
import io.minio.BucketExistsArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest
class MinioFileSystemObjectRepositoryTest extends BaseTest {

    @Autowired
    private FileSystemObjectRepository fileSystemObjectRepository;

    @Autowired
    private MinioClient minioClient;

    @Value("${file-storage.bucket-name}")
    private String bucketName;

    @Value("${file-storage.root-folder-template}")
    private String rootFolderTemplate;

    private final User mockUser = new User(1L, "test-user", "test-pass");

    @BeforeEach
    @SneakyThrows
    void setUp() {
        if (!minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(bucketName)
                .build())) {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
        }

        fileSystemObjectRepository.delete(mockUser, "", "");
        var list = fileSystemObjectRepository.findAllInRootFolder(mockUser);
        assertThat(list).hasSize(0);
    }

    @Test
    void givenValidParamWhenSaveFolderThenWillCreated() {
        var path = "dir1/dir2/";
        var name = "desired";

        fileSystemObjectRepository.saveFolder(mockUser, path, name);

        var paths = fileSystemObjectRepository.findAllPathsToItem(mockUser, name);
        assertAll(
                () -> assertThat(paths).hasSize(1),
                () -> assertThat(paths).contains(path)
        );
    }

    @Test
    void givenNotValidNameWhenSaveFolderThenThrownRuntimeException() {
        var path = "directory/";
        var name = "dir*+-=?./.";

        assertThatThrownBy(
                () -> fileSystemObjectRepository.saveFolder(mockUser, path, name)
        ).isInstanceOf(RuntimeException.class);
    }

    @Test
    @SneakyThrows
    void whenSaveRootFolderThenWillCreated() {
        var expectedRootFolder = rootFolderTemplate.formatted(mockUser.getId());

        fileSystemObjectRepository.saveRootFolder(mockUser);

        var rawItems = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(bucketName)
                .prefix(expectedRootFolder)
                .build());
        var path = rawItems.iterator().next().get().objectName();
        assertThat(path).contains(expectedRootFolder);
    }

    @Test
    void whenFindAllInRootFolderThenReturnAllItemRootFolder() {
        var item1 = "text.txt";
        var pathToItem2 = "dir1/";
        var nameItem2 = "dir2";
        var mockMultipartFile = new MockMultipartFile(
                item1, item1, "text/plain", new byte[]{});

        fileSystemObjectRepository.upload(mockUser, "", List.of(mockMultipartFile));
        fileSystemObjectRepository.saveFolder(mockUser, pathToItem2, nameItem2);

        var files = fileSystemObjectRepository.findAllInRootFolder(mockUser).stream()
                .map(FileSystemObject::getName)
                .toList();
        assertAll(
                () -> assertThat(files).hasSize(2),
                () -> assertThat(files).contains(item1).contains(pathToItem2)
        );
    }

    @Test
    void whenFindAllInFirstLevelThenReturnAllItemInFolder() {
        var baseDir = "baseDir/";
        var item1 = "text.txt";
        var pathToItem1 = baseDir + item1;
        var item2 = "dir1";
        var pathToItem2 = baseDir + item2 + "/";

        fileSystemObjectRepository.saveFolder(mockUser, "", baseDir);
        fileSystemObjectRepository.upload(mockUser, baseDir, List.of(
                new MockMultipartFile(item1, item1, "text/plain", new byte[]{})
        ));
        fileSystemObjectRepository.saveFolder(mockUser, baseDir, item2);

        var files = fileSystemObjectRepository.findAllInFirstLevel(mockUser, baseDir).stream()
                .map(FileSystemObject::getName)
                .toList();
        System.out.println();
        assertAll(
                () -> assertThat(files).hasSize(2),
                () -> assertThat(files).contains(pathToItem1).contains(pathToItem2)
        );
    }

    @Test
    void givenExistItemWhenFindAllPathsToItemThenReturnAllPathsToItem() {
        var itemName = "test";
        var pathToItem1 = "path/to/test/dir/";
        var item1 = "test.txt";
        var pathToItem2 = "test/path/dir1/dir2/test/";
        var item2 = "folder";
        var expectedPaths = List.of("path/to/", "path/to/test/dir/",
                "/", "test/path/dir1/dir2/");
        fileSystemObjectRepository.upload(mockUser, pathToItem1, List.of(new MockMultipartFile(
                item1, item1, "text/plain", new byte[]{}
        )));
        fileSystemObjectRepository.saveFolder(mockUser, pathToItem2, item2);

        var actualPaths = fileSystemObjectRepository.findAllPathsToItem(mockUser, itemName);

        assertThat(actualPaths).isEqualTo(expectedPaths);
    }

    @Test
    void givenNotExistItemWhenFindAllPathsToItemThenReturnEmptyList() {
        var found = fileSystemObjectRepository.findAllPathsToItem(mockUser, "test-item");

        assertThat(found).hasSize(0);
    }

    @Test
    void givenFolderNameWhenUpdateThenMoveFilesByNewPaths() {
        var basePath = "base/path/";
        var oldName = "old/";
        var newName = "new/";
        var oldPath = basePath + oldName;
        var newPath = basePath + newName;
        var item1 = "text.txt";
        var pathToItem2 = "dir1/";
        var nameItem2 = "dir2";
        fileSystemObjectRepository.upload(mockUser, oldPath, List.of(
                new MockMultipartFile(item1, item1, "text/plain", new byte[]{})));
        fileSystemObjectRepository.saveFolder(mockUser, oldPath + pathToItem2, nameItem2);

        fileSystemObjectRepository.update(mockUser, basePath, oldName, newName);

        var oldItems = fileSystemObjectRepository.findAllInFirstLevel(mockUser, oldPath);
        var newItems = fileSystemObjectRepository.findAllInFirstLevel(mockUser, newPath);
        assertAll(
                () -> assertThat(oldItems).hasSize(0),
                () -> assertThat(newItems).hasSize(2),
                () -> assertThat(newItems)
                        .contains(new FileSystemObject(newPath + item1, 0L, null))
                        .contains(new FileSystemObject(newPath + pathToItem2, 0L, null))
        );
    }

    @Test
    void givenNewFileNameWhenUpdateThenMoveFileByNewPath() {
        var basePath = "base/path/";
        var fileName = "text.txt";
        var newFileName = "rename.txt";
        fileSystemObjectRepository.upload(mockUser, basePath, List.of(
                new MockMultipartFile(fileName, fileName, "text/plain", new byte[]{})
        ));

        fileSystemObjectRepository.update(mockUser, basePath, fileName, newFileName);

        var oldItem = fileSystemObjectRepository.findAllPathsToItem(mockUser, fileName);
        var updatedItem = fileSystemObjectRepository.findAllPathsToItem(mockUser, newFileName);
        assertAll(
                () -> assertThat(oldItem).hasSize(0),
                () -> assertThat(updatedItem).hasSize(1)
        );
    }

    @Test
    void givenFolderNameWhenDeleteThenDeleteAllFilesInFolder() {
        var baseFolder = "base-folder/";
        var folderForDelete = "delete/";
        var pathToFile1 = baseFolder + folderForDelete;
        var filename1 = "file1.txt";
        var pathToFile2 = baseFolder + folderForDelete + "dir/";
        var filename2 = "file2.txt";
        fileSystemObjectRepository.saveFolder(mockUser, "", "base-folder");
        fileSystemObjectRepository.saveFolder(mockUser, baseFolder, "delete");
        fileSystemObjectRepository.upload(mockUser, pathToFile1, List.of(new MockMultipartFile(
                filename1, filename1, "text/plain", new byte[]{}
        )));
        fileSystemObjectRepository.upload(mockUser, pathToFile2, List.of(new MockMultipartFile(
                filename2, filename2, "text/plain", new byte[]{}
        )));

        fileSystemObjectRepository.delete(mockUser, baseFolder, folderForDelete);

        var rootContent = fileSystemObjectRepository.findAllInRootFolder(mockUser);
        var baseFolderContent = fileSystemObjectRepository.findAllInFirstLevel(mockUser, "base-folder/");
        assertAll(
                () -> assertThat(rootContent).hasSize(1),
                () -> assertThat(baseFolderContent).hasSize(0)
        );
    }

    @Test
    void givenFileNameWhenDeleteThenDeleteFile() {
        var baseFolder = "base/";
        var fileNameRemove = "remove.txt";
        fileSystemObjectRepository.saveFolder(mockUser, baseFolder, "folder");
        fileSystemObjectRepository.upload(mockUser, baseFolder + "folder/", List.of(new MockMultipartFile(
                fileNameRemove, fileNameRemove, "text/plain", new byte[]{}
        )));

        fileSystemObjectRepository.delete(mockUser, baseFolder + "folder/", fileNameRemove);

        var foundItem = fileSystemObjectRepository.findAllPathsToItem(mockUser, fileNameRemove);
        assertThat(foundItem).isEmpty();
    }

    @Test
    @SneakyThrows
    void givenExistPathToFileWhenDownloadThenReturnDownloadObject() {
        var baos = new ByteArrayOutputStream();
        baos.write("string for test".getBytes(StandardCharsets.UTF_8));
        var path = "path/folder/";
        var fileName = "test.txt";
        fileSystemObjectRepository.upload(mockUser, path, List.of(new MockMultipartFile(
                fileName, fileName, "text/plain", baos.toByteArray()
        )));

        var downloaded = fileSystemObjectRepository.download(mockUser, path + fileName);
        var contentAsBytes = downloaded.content();
        assertAll(
                () -> assertThat(contentAsBytes).isEqualTo(baos.toByteArray()),
                () -> assertThat(downloaded.name()).isEqualTo(fileName)
        );
    }

    @Test
    @SneakyThrows
    void givenExistPathToFolderWhenDownloadThenReturnDownloadObject() {
        var pathToDownloadFolder = "base/path/";
        var folderNameForDownload = "download";
        var fileName1 = "test1.txt";
        var fileName2 = "test2.txt";
        fileSystemObjectRepository.saveFolder(mockUser, pathToDownloadFolder, folderNameForDownload);
        var baos1 = new ByteArrayOutputStream();
        baos1.write("text for file 1".getBytes(StandardCharsets.UTF_8));
        var baos2 = new ByteArrayOutputStream();
        baos2.write("text for file 2".getBytes(StandardCharsets.UTF_8));
        fileSystemObjectRepository.upload(mockUser, pathToDownloadFolder + folderNameForDownload + "/", List.of(
                new MockMultipartFile(fileName1, fileName1, "text/plain", baos1.toByteArray())));
        fileSystemObjectRepository.upload(mockUser, pathToDownloadFolder + folderNameForDownload + "/", List.of(
                new MockMultipartFile(fileName2, fileName2, "text/plain", baos2.toByteArray())));

        var downloaded = fileSystemObjectRepository.download(mockUser,
                pathToDownloadFolder + folderNameForDownload + "/");

        Map<String, byte[]> contentByFileName = new HashMap<>();
        try (var bais = new ByteArrayInputStream(downloaded.content());
             var zis = new ZipInputStream(bais)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    contentByFileName.put(entry.getName(), zis.readAllBytes());
                }
            }
        }
        assertThat(contentByFileName)
                .hasSize(2)
                .containsExactlyInAnyOrderEntriesOf(Map.ofEntries(
                        Map.entry(fileName1, baos1.toByteArray()),
                        Map.entry(fileName2, baos2.toByteArray()))
                );
    }

    @Test
    @SneakyThrows
    void whenUploadThenLoadAllFilesToFileStorage() {
        var name1 = "test1.txt";
        var name2 = "test2.txt";
        var baos1 = new ByteArrayOutputStream();
        var baos2 = new ByteArrayOutputStream();
        baos1.write("test1".getBytes(StandardCharsets.UTF_8));
        baos2.write("test2".getBytes(StandardCharsets.UTF_8));
        var baseFolder = "base/";

        fileSystemObjectRepository.upload(mockUser, baseFolder, List.of(
                new MockMultipartFile(name1, name1, "text/plain", baos1.toByteArray())));
        fileSystemObjectRepository.upload(mockUser, baseFolder, List.of(
                new MockMultipartFile(name2, name2, "text/plain", baos2.toByteArray())));

        var file1 = fileSystemObjectRepository.download(mockUser, baseFolder + name1);
        var file2 = fileSystemObjectRepository.download(mockUser, baseFolder + name2);
        assertAll(
                () -> assertThat(file1.name()).isEqualTo(name1),
                () -> assertThat(file1.content()).isEqualTo(baos1.toByteArray()),
                () -> assertThat(file2.name()).isEqualTo(name2),
                () -> assertThat(file2.content()).isEqualTo(baos2.toByteArray())
        );
    }
}