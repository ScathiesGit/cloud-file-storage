package git.scathies.cloudfilestorage.service;

import git.scathies.cloudfilestorage.model.User;

import java.util.List;

public interface SearchFileSystemObjectService {

    List<String> getRootFolderContent(User user);

    List<String> getFolderContent(User user, String path);

    List<String> search(User user, String name);
}
