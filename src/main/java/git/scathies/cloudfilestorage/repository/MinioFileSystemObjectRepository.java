package git.scathies.cloudfilestorage.repository;

import io.minio.*;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class MinioFileSystemObjectRepository implements FileSystemObjectRepository {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private final String bucketName;

    // создание файла? добавление нового пути
    // переименование файла? изменение пути (меняется последний элемент пути - имя)
    // перемещение файла в другую папку? изменение пути (средний элемент, какая-то папка)
    // удаление файла? удаление пути

    // создание папки? добавление нового пути
    // переименование папки? групповое изменение родительского пути и всех дочерних
    //      родительский путь - сама папка, дочерние - все вложенные эл-ты папки
    // перемещение папки? то же самое что и переименование
    // удаление папки? групповое удаление

    @Override
    public void save(String path, String contentType, InputStream inputStream) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(path)
                    .stream(inputStream, inputStream.available(), -1)
                    .contentType(contentType)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Iterable<Result<Item>> findAllByPrefix(String prefix) {
        return minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(bucketName)
                .recursive(true)
                .prefix(prefix)
                .build());
    }

    @Override
    public void update(String oldPath, String newPath) {
        copy(oldPath, newPath);
        delete(oldPath);
    }

    @Override
    public void updateAll(Map<String, String> newPathByOldPath) {
        var sortedDescKey = sortDescending(newPathByOldPath.keySet().stream().toList());
        for (String path : sortedDescKey) {
            var newPath = newPathByOldPath.get(path);
            if (newPath.endsWith("/")) {
                save(newPath, "binary/octet-stream", new ByteArrayInputStream(new byte[]{}));
            } else {
                copy(path, newPath);
            }
        }
        deleteAll(sortedDescKey);
    }

    @Override
    public void delete(String path) {
        removeObject(path);
        restoreParent(path);
    }

    @Override
    public void deleteAll(List<String> paths) {
        var sortedPaths = sortDescending(paths);
        sortedPaths.forEach(this::removeObject);
        restoreParent(sortedPaths.get(paths.size() - 1));
    }

    private void copy(String sourcePath, String destinationPath) {
        try {
            minioClient.copyObject(CopyObjectArgs.builder()
                    .bucket(bucketName)
                    .object(destinationPath)
                    .source(CopySource.builder()
                            .bucket(bucketName)
                            .object(sourcePath)
                            .build())
                    .build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void removeObject(String path) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(path)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void restoreParent(String path) {
        path = path.endsWith("/")
                ? path.substring(0, path.lastIndexOf("/", path.length() - 2) + 1)
                : path.substring(0, path.lastIndexOf("/") + 1);
        if (!path.isEmpty()) {
            save(path, "binary/octet-stream", new ByteArrayInputStream(new byte[]{}));
        }
    }

    private List<String> sortDescending(List<String> paths) {
        return paths.stream()
                .map(path -> path.replace("/", "/ ").split("/"))
                .sorted((o1, o2) -> o2.length - o1.length)
                .map(array -> String.join("", array))
                .map(path -> path.replace(" ", "/"))
                .toList();
    }
}
