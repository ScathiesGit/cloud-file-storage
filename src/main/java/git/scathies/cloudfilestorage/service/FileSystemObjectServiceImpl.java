package git.scathies.cloudfilestorage.service;

import git.scathies.cloudfilestorage.dto.DownloadObject;
import git.scathies.cloudfilestorage.repository.FileSystemObjectRepository;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
public class FileSystemObjectServiceImpl implements FileSystemObjectService {

    private final FileSystemObjectRepository fileSystemObjectRepository;

    public void createFile(String path, String contentType, InputStream inputStream) {
        fileSystemObjectRepository.save(path, contentType, inputStream);
    }

    public void renameFile(String path, String newName) {
        var newPath = path.replace(Paths.get(path).getFileName().toString(), newName);
        fileSystemObjectRepository.update(path, newPath);
    }

//    public void moveFile(String sourcePath, String destinationPath) {
//        fileSystemObjectRepository.update(sourcePath, destinationPath);
//    }

    public void removeFile(String path) {
        fileSystemObjectRepository.delete(path);
    }

    public void createFolder(String path) {
        fileSystemObjectRepository.save(path, "binary/octet-stream",
                new ByteArrayInputStream(new byte[]{}));
    }

    public void renameFolder(String path, String name) {
        var newPrefix = path.substring(0, path.lastIndexOf("/", path.length() - 2) + 1) + name + "/";
        var objectsFileSystem = fileSystemObjectRepository.findAllByPrefix(path);

        Map<String, String> oldPathToNewPath = objectsFileSystem.stream().collect(
                toMap(Item::objectName, value -> value.objectName().replaceFirst(path, newPrefix)));

        fileSystemObjectRepository.updateAll(oldPathToNewPath);
    }

    public void removeFolder(String path) {
        var paths = fileSystemObjectRepository.findAllByPrefix(path)
                .stream()
                .map(Item::objectName)
                .toList();
        fileSystemObjectRepository.deleteAll(paths);
    }

    @Override
    public DownloadObject download(String path) {
        byte[] content;
        String contentType;
        if (path.endsWith("/")) {
            try (var buffer = new ByteArrayOutputStream(); var zip = new ZipOutputStream(buffer)) {
                fileSystemObjectRepository.findAllByPrefix(path)
                        .stream()
                        .map(Item::objectName)
                        .map(fileSystemObjectRepository::download)
                        .forEach(resp -> {
                            try {
                                var entry = new ZipEntry(resp.object().replace(path, ""));
                                zip.putNextEntry(entry);
                                zip.write(resp.readAllBytes());
                                resp.close();
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        });
                content = buffer.toByteArray();
                contentType = "application/zip";
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            try (var resp = fileSystemObjectRepository.download(path)) {
                contentType = resp.headers().get("Content-type");
                content = resp.readAllBytes();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return new DownloadObject(content, contentType);
    }

    @Override
    public void upload(String basePath, List<MultipartFile> files) {
        fileSystemObjectRepository.upload(basePath, files);
    }
}
