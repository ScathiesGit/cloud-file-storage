package git.scathies.cloudfilestorage.service;

import git.scathies.cloudfilestorage.model.DownloadObject;
import git.scathies.cloudfilestorage.model.FileSystemObject;
import git.scathies.cloudfilestorage.model.User;
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

    public void removeFile(String path) {
        fileSystemObjectRepository.delete(path);
    }

    public void createFolder(User user, String path) {
//        fileSystemObjectRepository.save(user, path, "binary/octet-stream",
//                new ByteArrayInputStream(new byte[]{}));
    }

    public void renameFolder(String path, String name) {
//        var newPrefix = path.substring(0, path.lastIndexOf("/", path.length() - 2) + 1) + name + "/";
//        var objectsFileSystem = fileSystemObjectRepository.findAllByPrefix(path);
//
//        Map<String, String> oldPathToNewPath = objectsFileSystem.stream().collect(
////                toMap(Item::objectName, value -> value.objectName().replaceFirst(path, newPrefix)));
//
//        fileSystemObjectRepository.updateAll(oldPathToNewPath);
    }

    public void removeFolder(String path) {
//        var paths = fileSystemObjectRepository.findAllByPrefix(path)
//                .stream()
//                .map(Item::objectName)
//                .toList();
//        fileSystemObjectRepository.deleteAll(paths);
    }

    @Override
    public DownloadObject download(User user, String path) {
        return fileSystemObjectRepository.download(user, path);
    }

    @Override
    public void upload(String basePath, List<MultipartFile> files) {
        fileSystemObjectRepository.upload(basePath, files);
    }
}
