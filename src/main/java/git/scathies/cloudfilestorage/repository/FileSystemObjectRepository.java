package git.scathies.cloudfilestorage.repository;

import git.scathies.cloudfilestorage.model.DownloadObject;
import git.scathies.cloudfilestorage.model.FileSystemObject;
import git.scathies.cloudfilestorage.model.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

public interface FileSystemObjectRepository {

    void saveFile(String path, String contentType, InputStream inputStream);

    void saveFolder(User user, String path, String name);

    void saveRootFolder(User user);

    List<FileSystemObject> findAllInRootFolder(User user);

    List<FileSystemObject> findAllInFirstLevel(User user, String path);

    List<String> findAllPathsByItemName(User user, String name);

    void update(User user, String path, String oldName, String newName);

    void delete(User user, String path, String name);

    DownloadObject download(User user, String path);

    void upload(User user, String path, List<MultipartFile> files);
}
