package git.scathies.cloudfilestorage.service;

import git.scathies.cloudfilestorage.dto.request.*;
import git.scathies.cloudfilestorage.dto.response.ReadStorageItemResp;
import git.scathies.cloudfilestorage.model.DownloadObject;
import git.scathies.cloudfilestorage.model.User;

import java.util.List;

public interface StorageItemService {

    void createRootFolder(User user);

    void createFolder(User user, CreateFolderReq dto);

    List<ReadStorageItemResp> getFolderContent(User user, ReadStorageItemReq dto);

    void renameStorageItem(User user, RenameStorageItemReq dto);

    void deleteStorageItem(User user, DeleteStorageItemReq dto);

    DownloadObject downloadStorageItem(User user, DownloadStorageItemReq dto);

    void upload(User user, UploadStorageItemReq dto);
}
