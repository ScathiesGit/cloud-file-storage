package git.scathies.cloudfilestorage.service;

import io.minio.*;
import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
@RequiredArgsConstructor
public class FileSystemObjectServiceImpl implements FileSystemObjectService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private final String bucketName;

    public void createFile(String path, String filename, String contentType) {
        try {
            minioClient.uploadObject(UploadObjectArgs.builder()
                    .bucket(bucketName)
                    .object(path + filename)
                    .contentType(contentType)
                    .filename(filename)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void renameFile(String path, String oldName, String newName) {
        try {
            copy(path + newName, path + oldName);
            removeFile(path + oldName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void moveFile(String destinationPath, String sourcePath) {
        try {
            copy(destinationPath, sourcePath);
            removeFile(sourcePath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void removeFile(String path) {
        try {
            removeObject(path);
            createFolder(path.substring(0, path.lastIndexOf("/") + 1));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void createFolder(String path) {
        try {
            putObject(path, new ByteArrayInputStream(new byte[]{}), 0, -1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void renameFolder(String fullPath, String folderName) {
        var newFullPath = fullPath
                .substring(0, fullPath.length() - 1)
                .substring(0, fullPath.lastIndexOf("/") + 1) + folderName;
        createFolder(newFullPath);

        var objectsFileSystem = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(bucketName)
                .recursive(true)
                .build());

        try {
            for (var objectFileSystem : objectsFileSystem) {
                var pathToObject = objectFileSystem.get().objectName();
                if (pathToObject.startsWith(fullPath)) {
                    var destinationPath = pathToObject.replaceFirst(fullPath, newFullPath);
                    if (pathToObject.endsWith("/")) {
                        moveFolder(pathToObject, destinationPath);
                    } else {
                        moveFile(destinationPath, pathToObject);
                    }
                }
            }
            removeObject(fullPath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void removeFolder(String path) {
        var objectsFileSystem = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(bucketName)
                .recursive(true)
                .build());
        try {
            for (var objectFileSystem : objectsFileSystem) {
                var pathToObject = objectFileSystem.get();
                if (pathToObject.objectName().startsWith(path)) {
                    if (pathToObject.objectName().endsWith("/")) {
                        removeFolder(pathToObject.objectName());
                    } else {
                        removeFile(pathToObject.objectName());
                    }
                }
            }
            removeObject(path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        var pathToRestoreFolders = Paths.get(path);
        if (pathToRestoreFolders.getParent() != null) {
            createFolder((pathToRestoreFolders.getParent() + "/").replace("\\", "/"));
        }
    }

    private void copy(String destinationPath, String sourcePath) throws ServerException, InsufficientDataException,
            ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException,
            InvalidResponseException, XmlParserException, InternalException {

        minioClient.copyObject(CopyObjectArgs.builder()
                .bucket(bucketName)
                .object(destinationPath)
                .source(CopySource.builder()
                        .bucket(bucketName)
                        .object(sourcePath)
                        .build())
                .build());
    }

    private void moveFolder(String sourcePath, String destinationPath) throws ServerException,
            InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException,
            InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

        putObject(destinationPath, new ByteArrayInputStream(new byte[]{}), 0, -1);
        removeObject(sourcePath);
    }

    private void putObject(String path, InputStream stream, long objSize, long partSize) throws ServerException,
            InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException,
            InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

        minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucketName)
                .object(path)
                .stream(stream, objSize, partSize)
                .build());
    }

    private void removeObject(String path) throws ServerException, InsufficientDataException, ErrorResponseException,
            IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException,
            InternalException {

        minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(bucketName)
                .object(path)
                .build());
    }
}
