package git.scathies.cloudfilestorage.service;

import git.scathies.cloudfilestorage.model.FileSystemObject;
import git.scathies.cloudfilestorage.model.User;
import git.scathies.cloudfilestorage.repository.FileSystemObjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchFileSystemObjectServiceImpl implements SearchFileSystemObjectService {

    private final FileSystemObjectRepository fileSystemObjectRepository;

    @Override
    public List<String> getRootFolderContent(User user) {
        return fileSystemObjectRepository.findAllInRootFolder(user)
                .stream()
                .map(FileSystemObject::getName)
                .toList();
    }

    @Override
    public List<String> getFolderContent(User user, String path) {
        return fileSystemObjectRepository.findAllInFirstLevel(user, path)
                .stream()
                .map(FileSystemObject::getName)
                .map(objName -> objName.replaceFirst(path, ""))
                .toList();
    }

    @Override
    public List<String> search(User user, String name) {
        return fileSystemObjectRepository.findAllPathsToItem(user, name);
    }
}
