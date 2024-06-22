package git.scathies.cloudfilestorage.repository;

import git.scathies.cloudfilestorage.configuration.properties.FileStorageConfigProperties;
import git.scathies.cloudfilestorage.exception.DeleteException;
import git.scathies.cloudfilestorage.exception.DownloadException;
import git.scathies.cloudfilestorage.exception.FileStorageException;
import git.scathies.cloudfilestorage.exception.UploadException;
import git.scathies.cloudfilestorage.model.DownloadObject;
import git.scathies.cloudfilestorage.model.StorageItem;
import io.minio.*;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
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
public class MinioStorageItemRepository implements StorageItemRepository {

    private final MinioClient minioClient;

    private final FileStorageConfigProperties configProps;

    private final String folderContentType = "octet/binary-stream";

    @Override
    public void saveFolder(StorageItem item) {
        var fullPath = item.getPath() + "/" + item.getName() + "/";
        putObject(fullPath, folderContentType, new ByteArrayInputStream(new byte[]{}));
    }

    @Override
    public List<StorageItem> findAllInFirstLevel(StorageItem item) {
        return find(item.getPath() + item.getName() + "/", false).stream()
                .map(this::toStorageItem)
                .toList();
    }

    @Override
    public List<StorageItem> findAllAtFolder(String folder) {
        return find(folder + "/", true).stream()
                .map(this::toStorageItem)
                .toList();
    }

    @Override
    public void renameFolder(StorageItem item, String newName) {
        var updatedPathsByOldPaths = find(item.getPath() + item.getName(), true)
                .stream()
                .map(Item::objectName)
                .collect(toMap(identity(), oldFullPath ->
                        oldFullPath.replaceFirst(
                                item.getPath() + item.getName(), item.getPath() + newName + "/")));

        updatedPathsByOldPaths.forEach((oldPath, newPath) -> {
            if (oldPath.endsWith("/")) {
                putObject(newPath, folderContentType, new ByteArrayInputStream(new byte[]{}));
            } else {
                copy(oldPath, newPath);
            }
        });

        multipleRemoval(new ArrayList<>(updatedPathsByOldPaths.keySet()));
    }

    @Override
    public void renameFile(StorageItem storageItem, String newName) {
        copy(storageItem.getPath() + storageItem.getName(), storageItem.getPath() + newName);
        deleteFile(storageItem.getPath() + storageItem.getName());
    }

    @Override
    public void deleteFile(String path) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(configProps.getBucketName())
                    .object(path)
                    .build());
        } catch (Exception e) {
            throw new FileStorageException(e);
        }

        restoreParentFolder(
                (Paths.get(path).getParent() + "/").replaceAll("\\\\", "/")
        );
    }

    @Override
    public void deleteFolder(String path) {
        List<String> deletePaths = new ArrayList<>();
        deletePaths.add(path);
        find(path, true).forEach(item ->
                deletePaths.add(item.objectName())
        );

        multipleRemoval(deletePaths);
    }

    @Override
    public DownloadObject downloadFile(String path, String name) {
        try (var getObject = getObject(path + name)) {
            return new DownloadObject(
                    name,
                    getObject.readAllBytes(),
                    getObject.headers().get("Content-Type")
            );
        } catch (IOException e) {
            throw new DownloadException(e);
        }
    }

    @Override
    public DownloadObject downloadFolder(String path, String name) {
        try (var buffer = new ByteArrayOutputStream();
             var zip = new ZipOutputStream(buffer)) {
            find(path + name, true).stream()
                    .map(item -> getObject(item.objectName()))
                    .forEach(resp -> {
                        try {
                            String fileName = resp.object().replace(
                                    path + name, "");
                            var entry = new ZipEntry(fileName);
                            zip.putNextEntry(entry);
                            zip.write(resp.readAllBytes());
                            resp.close();
                        } catch (IOException e) {
                            throw new DownloadException(e);
                        }
                    });
            zip.close();
            return new DownloadObject(
                    name,
                    buffer.toByteArray(),
                    "application/zip"
            );
        } catch (IOException e) {
            throw new DownloadException(e);
        }
    }

    @Override
    public void upload(String path, List<MultipartFile> files) {
        if (files.size() > 1) {
            List<SnowballObject> uploadObjects = new ArrayList<>();
            files.forEach(file -> {
                try {
                    uploadObjects.add(new SnowballObject(
                            path + "/" + file.getOriginalFilename(),
                            file.getInputStream(),
                            file.getInputStream().available(),
                            null));
                } catch (IOException e) {
                    throw new UploadException(e);
                }
            });

            try {
                minioClient.uploadSnowballObjects(UploadSnowballObjectsArgs.builder()
                        .bucket(configProps.getBucketName())
                        .objects(uploadObjects)
                        .build());
            } catch (Exception e) {
                throw new FileStorageException(e);
            }
        } else {
            var file = files.get(0);
            try {
                putObject(path + "/" + file.getOriginalFilename(), file.getContentType(), file.getInputStream());
            } catch (IOException e) {
                throw new UploadException(e);
            }
        }
    }

    private void multipleRemoval(List<String> fileNames) {
        List<DeleteObject> deleteObjects = new ArrayList<>();
        fileNames.forEach(fileName -> deleteObjects.add(new DeleteObject(fileName)));

        minioClient.removeObjects(RemoveObjectsArgs.builder()
                        .bucket(configProps.getBucketName())
                        .objects(deleteObjects)
                        .build())
                .forEach(lazyRemoval -> {
                    try {
                        lazyRemoval.get();
                    } catch (Exception e) {
                        throw new DeleteException(e);
                    }
                });
    }

    private void putObject(String path, String contentType, InputStream inputStream) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(configProps.getBucketName())
                    .object(path)
                    .contentType(contentType)
                    .stream(inputStream, inputStream.available(), -1)
                    .build());
        } catch (Exception e) {
            throw new FileStorageException(e);
        }
    }

    private List<Item> find(String prefix, boolean isRecursive) {
        var rawItems = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(configProps.getBucketName())
                .prefix(prefix)
                .recursive(isRecursive)
                .build());

        List<Item> items = new ArrayList<>();
        try {
            for (var rawItem : rawItems) {
                if (!rawItem.get().objectName().equals(prefix)) {
                    items.add(rawItem.get());
                }
            }
        } catch (Exception e) {
            throw new FileStorageException(e);
        }
        return items;
    }

    private void copy(String sourcePath, String destinationPath) {
        try {
            minioClient.copyObject(CopyObjectArgs.builder()
                    .bucket(configProps.getBucketName())
                    .object(destinationPath)
                    .source(CopySource.builder()
                            .bucket(configProps.getBucketName())
                            .object(sourcePath)
                            .build())
                    .build());
        } catch (Exception e) {
            throw new FileStorageException(e);
        }
    }

    private void restoreParentFolder(String path) {
        putObject(path, folderContentType, new ByteArrayInputStream(new byte[]{}));
    }

    private GetObjectResponse getObject(String path) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(configProps.getBucketName())
                    .object(path)
                    .build());
        } catch (Exception e) {
            throw new FileStorageException(e);
        }
    }

    private StorageItem toStorageItem(Item item) {
        var fullPath = Paths.get(item.objectName());
        return StorageItem.builder()
                .path(fullPath.getParent().toString().replace("\\", "/") + "/")
                .name(fullPath.getFileName().toString())
                .isFolder(item.objectName().endsWith("/"))
                .size(item.size())
                .build();
    }
}
