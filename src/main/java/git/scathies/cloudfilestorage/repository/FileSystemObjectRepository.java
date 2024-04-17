package git.scathies.cloudfilestorage.repository;

import git.scathies.cloudfilestorage.model.FileSystemObject;
import git.scathies.cloudfilestorage.model.User;
import io.minio.GetObjectResponse;
import io.minio.messages.Item;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface FileSystemObjectRepository {
    
    void save(String path, String contentType, InputStream inputStream);

    void createRootFolder(User user);

    List<FileSystemObject> findAllByPrefix(String prefix);

    List<FileSystemObject> findAllInRootFolder(Long userId);

    List<FileSystemObject> findAllInFirstLevel(String path, Long userId);

    void update(String oldPath, String newPath);

    void updateAll(Map<String, String> oldPathToNewPath);

    void delete(String path);

    void deleteAll(List<String> paths);

    GetObjectResponse download(String path);

    void downloadAll(String path);

    void upload(String basePath, List<MultipartFile> files);
}
