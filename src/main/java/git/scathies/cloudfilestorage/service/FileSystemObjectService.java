package git.scathies.cloudfilestorage.service;

public interface FileSystemObjectService {

    void createFile(String path, String filename, String contentType);

    void renameFile(String path, String oldName, String newName);

    void moveFile(String destinationPath, String sourcePath);

    void removeFile(String fullPath);

    void createFolder(String path);

    void renameFolder(String fullPath, String folderName);

    void removeFolder(String path);
}
