package git.scathies.cloudfilestorage.service;

import git.scathies.cloudfilestorage.configuration.properties.ServiceConfigProperties;
import git.scathies.cloudfilestorage.dto.request.*;
import git.scathies.cloudfilestorage.dto.response.ReadStorageItemResp;
import git.scathies.cloudfilestorage.model.DownloadObject;
import git.scathies.cloudfilestorage.model.StorageItem;
import git.scathies.cloudfilestorage.model.User;
import git.scathies.cloudfilestorage.repository.StorageItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StorageItemServiceImpl implements StorageItemService {

    private final StorageItemRepository storageItemRepository;

    private final ServiceConfigProperties configProps;

    @Override
    public void createRootFolder(User user) {
        storageItemRepository.saveFolder(
                new StorageItem("", configProps.getRootFolderTemplate().formatted(user.getId()))
        );
    }

    public void createFolder(User user, CreateFolderReq dto) {
        var basePath = configProps.getRootFolderTemplate().formatted(user.getId()) + dto.getPath();
        var fullPath = dto.getParentFolder() == null ? basePath : basePath + dto.getParentFolder();
        var storageItem = new StorageItem(fullPath, dto.getNewFolderName());
        storageItemRepository.saveFolder(storageItem);
    }

    @Override
    public List<ReadStorageItemResp> getFolderContent(User user, ReadStorageItemReq dto) {
        var rootFolder = configProps.getRootFolderTemplate().formatted(user.getId());
        var item = dto.getName() == null ? new StorageItem("", rootFolder)
                : new StorageItem(rootFolder + dto.getPath(), dto.getName());

        return storageItemRepository.findAllInFirstLevel(item)
                .stream()
                .map(this::toReadStorageItemResp)
                .peek(readDto -> readDto.setPath(readDto.getPath().replaceFirst(rootFolder, "")))
                .toList();
    }

    @Override
    public void renameStorageItem(User user, RenameStorageItemReq dto) {
        var storageItem = new StorageItem(
                configProps.getRootFolderTemplate().formatted(user.getId())
                        + dto.getPathToDirectory(), dto.getOldName());
        if (dto.isFolder()) {
            storageItemRepository.renameFolder( storageItem, dto.getNewName());
        } else {
            storageItemRepository.renameFile(storageItem, dto.getNewName());
        }
    }

    @Override
    public void deleteStorageItem(User user, DeleteStorageItemReq dto) {
        var rootFolder = configProps.getRootFolderTemplate().formatted(user.getId());
        var fullPath = dto.getPath() == null ? rootFolder + "/" + dto.getRemovalItemName()
                : rootFolder + dto.getPath() + dto.getParentFolder() + "/" + dto.getRemovalItemName();
        if (dto.isFolder()) {
            storageItemRepository.deleteFolder(fullPath);
        } else {
            storageItemRepository.deleteFile(fullPath);
        }
    }

    @Override
    public DownloadObject downloadStorageItem(User user, DownloadStorageItemReq dto) {
        var basePath = configProps.getRootFolderTemplate().formatted(user.getId()) + dto.getPath();
        if (dto.isFolder()) {
            return storageItemRepository.downloadFolder(basePath, dto.getName());
        } else {
            return storageItemRepository.downloadFile(basePath, dto.getName());
        }
    }

    @Override
    public void upload(User user, UploadStorageItemReq dto) {
        var fullPath = dto.getPath() == null ? configProps.getRootFolderTemplate().formatted(user.getId())
                : configProps.getRootFolderTemplate().formatted(user.getId()) + dto.getPath() + dto.getName();
        storageItemRepository.upload(fullPath, dto.getFiles());
    }

    private ReadStorageItemResp toReadStorageItemResp(StorageItem item) {
        return ReadStorageItemResp.builder()
                .path(item.getPath())
                .isFolder(item.isFolder())
                .size(item.getSize())
                .name(item.getName())
                .build();
    }
} //120  109  105  101
