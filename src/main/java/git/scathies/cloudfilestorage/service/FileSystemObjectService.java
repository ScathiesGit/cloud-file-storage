package git.scathies.cloudfilestorage.service;

import git.scathies.cloudfilestorage.model.DownloadObject;
import git.scathies.cloudfilestorage.model.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

public interface FileSystemObjectService {

    void createFile(String path, String contentType, InputStream inputStream);

    void renameFile(String path, String newName);

    void removeFile(String fullPath);

    void createFolder(User user, String path);

    void renameFolder(String fullPath, String folderName);

    void removeFolder(String path);

    DownloadObject download(User user, String path);

    void upload(String basePath, List<MultipartFile> files);
}
