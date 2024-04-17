package git.scathies.cloudfilestorage.repository;

import git.scathies.cloudfilestorage.model.DownloadObject;
import git.scathies.cloudfilestorage.model.FileSystemObject;
import git.scathies.cloudfilestorage.model.User;
import io.minio.*;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Repository
@RequiredArgsConstructor
public class MinioFileSystemObjectRepository implements FileSystemObjectRepository {

    private final MinioClient minioClient;

    @Value("${file-storage.bucket-name}")
    private final String bucketName;

    @Value("${file-storage.root-folder-template")
    private final String rootFolderTemplate;

    @Override
    public void save(String path, String contentType, InputStream inputStream) {
        putObject(path, contentType, inputStream);
    }

    @Override
    public void createRootFolder(User user) {
        putObject(rootFolderTemplate.formatted(user.getId()), "octet/binary-stream",
                new ByteArrayInputStream(new byte[]{}));
    }

    @Override
    public List<FileSystemObject> findAllByPrefix(String prefix) {
        return find(prefix, true);
    }

    @Override
    public List<FileSystemObject> findAllInRootFolder(Long userId) {
        return find(rootFolderTemplate.formatted(userId), false);
    }

    @Override
    public List<FileSystemObject> findAllInFirstLevel(String path, Long userId) {
        return find(rootFolderTemplate.formatted(userId) + path, false);
    }

    @Override
    public void update(String oldPath, String newPath) {
        copy(oldPath, newPath);
        delete(oldPath);
    }

    @Override
    public void updateAll(Map<String, String> newPathByOldPath) {
        for (Map.Entry<String, String> entry : newPathByOldPath.entrySet()) {
            if (entry.getKey().endsWith("/")) {
                save(entry.getValue(), "binary/octet-stream", new ByteArrayInputStream(new byte[]{}));
            } else {
                copy(entry.getKey(), entry.getValue());
            }
        }
        deleteAll(newPathByOldPath.keySet().stream().toList());
    }

    @Override
    public void delete(String path) {
        removeObject(path);
        restoreParent(path);
    }

    @Override
    public void deleteAll(List<String> paths) {
        List<DeleteObject> deleteObjects = new ArrayList<>();
        paths.forEach(path -> deleteObjects.add(new DeleteObject(path)));
        minioClient.removeObjects(RemoveObjectsArgs.builder()
                        .bucket(bucketName)
                        .objects(deleteObjects)
                        .build())
                .forEach(lazyRemoval -> {
                    try {
                        lazyRemoval.get();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @Override
    public DownloadObject download(User user, String path) {
        DownloadObject downloadObject;
        if (path.endsWith("/")) {
            try (var buffer = new ByteArrayOutputStream(); var zip = new ZipOutputStream(buffer)) {
                find(rootFolderTemplate.formatted(user.getId() + path), true).stream()
                        .map(FileSystemObject::getName)
                        .map(this::getObject)
                        .forEach(resp -> {
                            try {
                                var entry = new ZipEntry(resp.object());
                                zip.putNextEntry(entry);
                                zip.write(resp.readAllBytes());
                                resp.close();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                downloadObject = new DownloadObject(buffer.toByteArray(), "application/zip");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            var getObject = getObject(rootFolderTemplate.formatted(user.getId()) + path);
            try {
                downloadObject = new DownloadObject(getObject.readAllBytes(), getObject.headers().get("Content-Type"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return downloadObject;
    }

    @Override
    public void upload(String basePath, List<MultipartFile> files) {
        if (files.size() > 1) {
            List<SnowballObject> uploadObjects = new ArrayList<>();
            files.forEach(file -> {
                try {
                    uploadObjects.add(new SnowballObject(
                            basePath + file.getOriginalFilename(),
                            file.getInputStream(),
                            file.getInputStream().available(),
                            null));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            try {
                minioClient.uploadSnowballObjects(UploadSnowballObjectsArgs.builder()
                        .bucket(bucketName)
                        .objects(uploadObjects)
                        .build());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            var file = files.get(0);
            try {
                putObject(basePath + file.getOriginalFilename(), file.getContentType(), file.getInputStream());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private GetObjectResponse getObject(String path) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(path)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void putObject(String path, String contentType, InputStream inputStream) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(path)
                    .contentType(contentType)
                    .stream(inputStream, inputStream.available(), -1)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    private List<FileSystemObject> find(String prefix, boolean isRecursive) {
        var rawItems = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(bucketName)
                .prefix(prefix)
                .recursive(isRecursive)
                .build());

        List<Item> items = new ArrayList<>();
        try {
            for (var rawItem : rawItems) {
                items.add(rawItem.get());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return items.stream()
                .map(this::toFileSystemObject)
                .toList();
    }

    private void restoreParent(String path) {
        path = path.endsWith("/")
                ? path.substring(0, path.lastIndexOf("/", path.length() - 2) + 1)
                : path.substring(0, path.lastIndexOf("/") + 1);
        if (!path.isEmpty()) {
            save(path, "binary/octet-stream", new ByteArrayInputStream(new byte[]{}));
        }
    }

    private FileSystemObject toFileSystemObject(Item item) {
        return FileSystemObject.builder()
                .name(item.objectName())
                .userMetadata(item.userMetadata())
                .lastModified(item.lastModified())
                .size(item.size())
                .build();
    }
}
