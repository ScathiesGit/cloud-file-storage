package git.scathies.cloudfilestorage.service;

import git.scathies.cloudfilestorage.dto.DownloadObject;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

public interface FileSystemObjectService {

    void createFile(String path, String contentType, InputStream inputStream);

    void renameFile(String path, String newName);

//    void moveFile(String sourcePath, String destinationPath);

    void removeFile(String fullPath);

    void createFolder(String path);

    void renameFolder(String fullPath, String folderName);

    void removeFolder(String path);

    DownloadObject download(String path);

    void upload(String basePath, List<MultipartFile> files);
}
