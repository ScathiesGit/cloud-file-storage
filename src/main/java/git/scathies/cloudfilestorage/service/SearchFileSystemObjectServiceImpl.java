package git.scathies.cloudfilestorage.service;

import git.scathies.cloudfilestorage.repository.FileSystemObjectRepository;
import git.scathies.cloudfilestorage.util.PathUtil;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchFileSystemObjectServiceImpl implements SearchFileSystemObjectService {

    private final FileSystemObjectRepository fileSystemObjectRepository;

    @Override
    public List<String> getContentFolder(String path) {
        return fileSystemObjectRepository.findAllInFirstLevel(path)
                .stream()
                .map(Item::objectName)
                .map(objName -> objName.replaceFirst(path, ""))
                .toList();
    }

    @Override
    public List<String> search(String name, String rootFolder) {
        return fileSystemObjectRepository.findAllByPrefix(rootFolder).stream()
                .map(Item::objectName)
                .filter(path -> PathUtil.isContains(path, name))
                .flatMap(path -> PathUtil.getPathsTo(path, name).stream())
                .map(path -> path.replace(rootFolder, ""))
                .distinct()
                .toList();
    }
}
