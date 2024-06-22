package git.scathies.cloudfilestorage.service;

import git.scathies.cloudfilestorage.dto.response.StorageItemDtoResp;
import git.scathies.cloudfilestorage.model.User;

import java.util.List;

public interface SearchStorageItemService {

    List<StorageItemDtoResp> search(User user, String name);

}
