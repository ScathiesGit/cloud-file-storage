package git.scathies.cloudfilestorage.service;

import git.scathies.cloudfilestorage.model.FileSystemObject;
import git.scathies.cloudfilestorage.repository.FileSystemObjectRepository;
import git.scathies.cloudfilestorage.util.PathUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchFileSystemObjectServiceImpl implements SearchFileSystemObjectService {

    private final FileSystemObjectRepository fileSystemObjectRepository;

    @Override
    public List<String> getRootFolderContent(Long userId) {
        return fileSystemObjectRepository.findAllInRootFolder(userId)
                .stream()
                .map(FileSystemObject::getName)
                .toList();
    }

    @Override
    public List<String> getFolderContent(String path, Long userId) {
        return fileSystemObjectRepository.findAllInFirstLevel(path, userId)
                .stream()
                .map(FileSystemObject::getName)
                .map(objName -> objName.replaceFirst(path, ""))
                .toList();
    }

    @Override
    public List<String> search(String name, String rootFolder) {
        return fileSystemObjectRepository.findAllByPrefix(rootFolder).stream()
                .map(FileSystemObject::getName)
                .filter(path -> PathUtil.isContains(path, name))
                .flatMap(path -> PathUtil.getPathsTo(path, name).stream())
                .map(path -> path.replace(rootFolder, ""))
                .distinct()
                .toList();
    }
}
