package git.scathies.cloudfilestorage.service;

import git.scathies.cloudfilestorage.repository.FileSystemObjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FileSystemObjectServiceImpl implements FileSystemObjectService {

    private final FileSystemObjectRepository fileSystemObjectRepository;

    public void createFile(String path, String contentType, InputStream inputStream) {
        fileSystemObjectRepository.save(path, contentType, inputStream);
    }

    public void renameFile(String path, String oldName, String newName) {
        fileSystemObjectRepository.update(path + newName, path + oldName);
    }

    public void moveFile(String sourcePath, String destinationPath) {
        fileSystemObjectRepository.update(sourcePath, destinationPath);
    }

    public void removeFile(String path) {
        fileSystemObjectRepository.delete(path);
    }

    public void createFolder(String path) {
        fileSystemObjectRepository.save(path, "binary/octet-stream",
                new ByteArrayInputStream(new byte[]{}));
    }

    public void renameFolder(String path, String name) {
        var newPrefix = path.substring(0, path.lastIndexOf("/", path.length() - 2) + 1) + name;
        var objectsFileSystem = fileSystemObjectRepository.findAllByPrefix(path);
        Map<String, String> oldPathToNewPath = new HashMap<>();
        try {
            for (var objectFileSystem : objectsFileSystem) {
                var pathToObject = objectFileSystem.get().objectName();
                oldPathToNewPath.put(pathToObject, pathToObject.replaceFirst(path, newPrefix));
            }
            fileSystemObjectRepository.updateAll(oldPathToNewPath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void removeFolder(String path) {
        var objectsFileSystem = fileSystemObjectRepository.findAllByPrefix(path);
        ArrayList<String> paths = new ArrayList<>();
        try {
            for (var objectFileSystem : objectsFileSystem) {
                paths.add(objectFileSystem.get().objectName());
            }
            fileSystemObjectRepository.deleteAll(paths);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
