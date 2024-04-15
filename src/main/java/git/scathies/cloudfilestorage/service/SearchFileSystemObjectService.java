package git.scathies.cloudfilestorage.service;

import java.util.List;

public interface SearchFileSystemObjectService {

    List<String> getContentFolder(String path);

    List<String> search(String name, String rootFolder);
}
