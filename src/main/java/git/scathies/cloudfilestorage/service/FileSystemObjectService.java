package git.scathies.cloudfilestorage.service;

import java.io.InputStream;

public interface FileSystemObjectService {

    void createFile(String path, String contentType, InputStream inputStream);

    void renameFile(String path, String oldName, String newName);

    void moveFile(String sourcePath, String destinationPath);

    void removeFile(String fullPath);

    void createFolder(String path);

    void renameFolder(String fullPath, String folderName);

    void removeFolder(String path);
}
