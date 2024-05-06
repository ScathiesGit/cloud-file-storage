package git.scathies.cloudfilestorage.service;

import git.scathies.cloudfilestorage.model.DownloadObject;
import git.scathies.cloudfilestorage.model.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileSystemObjectService {

    void remove(User user, String path, String name);

    void createFolder(User user, String path, String name);

    void rename(User user, String path, String oldName, String newName);

    DownloadObject download(User user, String path);

    void upload(User user, String path, List<MultipartFile> files);
}
