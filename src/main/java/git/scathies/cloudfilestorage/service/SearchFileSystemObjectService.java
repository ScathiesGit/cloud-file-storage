package git.scathies.cloudfilestorage.service;

import java.util.List;

public interface SearchFileSystemObjectService {

    List<String> getRootFolderContent(Long userId);

    List<String> getFolderContent(String path, Long userId);

    List<String> search(String name, String rootFolder);
}
