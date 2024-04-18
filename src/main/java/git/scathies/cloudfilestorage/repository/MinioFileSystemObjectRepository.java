package git.scathies.cloudfilestorage.repository;

import git.scathies.cloudfilestorage.model.DownloadObject;
import git.scathies.cloudfilestorage.model.FileSystemObject;
import git.scathies.cloudfilestorage.model.User;
import git.scathies.cloudfilestorage.util.PathUtil;
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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@Repository
@RequiredArgsConstructor
public class MinioFileSystemObjectRepository implements FileSystemObjectRepository {

    private final MinioClient minioClient;

    @Value("${file-storage.bucket-name}")
    private final String bucketName;

    @Value("${file-storage.root-folder-template}")
    private final String rootFolderTemplate;

    private final String folderContentType = "octet/binary-stream";

    @Override
    public void saveFile(String path, String contentType, InputStream inputStream) {
        putObject(path, contentType, inputStream);
    }

    @Override
    public void saveFolder(User user, String path, String name) {
        putObject(rootFolderTemplate.formatted(user.getId()) + path + name + "/", folderContentType,
                new ByteArrayInputStream(new byte[]{}));
    }

    @Override
    public void saveRootFolder(User user) {
        putObject(rootFolderTemplate.formatted(user.getId()), folderContentType,
                new ByteArrayInputStream(new byte[]{}));
    }

    @Override
    public List<FileSystemObject> findAllInRootFolder(User user) {
        return find(getUserRootFolderPath(user), false).stream()
                .map(this::toFileSystemObject)
                .toList();
    }

    @Override
    public List<FileSystemObject> findAllInFirstLevel(User user, String path) {
        return find(getUserRootFolderPath(user) + path, false).stream()
                .map(this::toFileSystemObject)
                .toList();
    }

    @Override
    public List<String> findAllPathsByItemName(User user, String name) {
        return find(getUserRootFolderPath(user), true).stream()
                .map(this::toFileSystemObject)
                .filter(fileSystemObject -> PathUtil.isContains(fileSystemObject.getName(), name))
                .flatMap(fileSystemObject ->
                        PathUtil.getPathsTo(fileSystemObject.getName(), name).stream())
                .distinct()
                .toList();
    }

    @Override
    public void update(User user, String path, String oldName, String newName) {
        var oldParentPath = getUserRootFolderPath(user) + path;
        if (oldName.endsWith("/")) {
            find(oldParentPath + oldName, true)
                    .stream()
                    .map(Item::objectName)
                    .collect(toMap(identity(), oldFullPath -> oldFullPath.replaceFirst(
                            oldParentPath + oldName, oldParentPath + newName + "/")))
                    .forEach((oldPath, newPath) -> {
                        if (oldPath.endsWith("/")) {
                            putObject(newPath, folderContentType, new ByteArrayInputStream(new byte[]{}));
                        } else {
                            copy(oldPath, newPath);
                        }
                    });
        } else {
            copy(oldParentPath + oldName,
                    getUserRootFolderPath(user) + path + newName);
        }
        delete(user, path, oldName);
    }

    @Override
    public void delete(User user, String path, String name) {
        var fullPath = getUserRootFolderPath(user) + path + name;
        if (name.endsWith("/")) {
            List<DeleteObject> deleteObjects = new ArrayList<>();
            find(fullPath, true).forEach(
                    item -> deleteObjects.add(new DeleteObject(item.objectName())));

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
        } else {
            removeObject(fullPath);
            restoreFolder(getUserRootFolderPath(user) + path);
        }
    }

    @Override
    public DownloadObject download(User user, String path) {
        String name = Paths.get(path).getFileName().toString();
        byte[] content;
        String type;
        if (path.endsWith("/")) {
            content = downloadFolder(user, path);
            type = "application/zip";
        } else {
            try (var getObject = getObject(getUserRootFolderPath(user) + path)) {
                content = getObject.readAllBytes();
                type = getObject.headers().get("Content-Type");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return new DownloadObject(name, content, type);
    }

    @Override
    public void upload(User user, String path, List<MultipartFile> files) {
        var basePath = getUserRootFolderPath(user) + path;
        try {
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
                minioClient.uploadSnowballObjects(UploadSnowballObjectsArgs.builder()
                        .bucket(bucketName)
                        .objects(uploadObjects)
                        .build());

            } else {
                var file = files.get(0);
                putObject(basePath + file.getOriginalFilename(), file.getContentType(), file.getInputStream());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getUserRootFolderPath(User user) {
        return rootFolderTemplate.formatted(user.getId());
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

    private List<Item> find(String prefix, boolean isRecursive) {
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
        return items;
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

    private void restoreFolder(String path) {
        putObject(path, folderContentType, new ByteArrayInputStream(new byte[]{}));
    }

    private byte[] downloadFolder(User user, String path) {
        try (var buffer = new ByteArrayOutputStream(); var zip = new ZipOutputStream(buffer)) {
            find(getUserRootFolderPath(user) + path, true).stream()
                    .map(item -> getObject(item.objectName()))
                    .forEach(resp -> {
                        try {
                            String fileName = resp.object().replace(
                                    getUserRootFolderPath(user) + path, "");
                            if (!fileName.isEmpty()) {
                                var entry = new ZipEntry(fileName);
                                zip.putNextEntry(entry);
                                zip.write(resp.readAllBytes());
                                resp.close();
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
            return buffer.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
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

    private FileSystemObject toFileSystemObject(Item item) {
        return FileSystemObject.builder()
                .name(item.objectName().substring(item.objectName().indexOf("/") + 1))
                .userMetadata(item.userMetadata())
                .size(item.size())
                .build();
    }
}
