package git.scathies.cloudfilestorage.repository;

import git.scathies.cloudfilestorage.model.DownloadObject;
import git.scathies.cloudfilestorage.model.StorageItem;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface StorageItemRepository {

    void saveFolder(StorageItem item);

    List<StorageItem> findAllInFirstLevel(StorageItem item);

    List<StorageItem> findAllAtFolder(String folder);

    void renameFolder(StorageItem item, String newName);

    void renameFile(StorageItem item, String newName);

    void deleteFile(String path);

    void deleteFolder(String path);

    DownloadObject downloadFile(String path, String name);

    DownloadObject downloadFolder(String path, String name);

    void upload(String path, List<MultipartFile> files);
}
