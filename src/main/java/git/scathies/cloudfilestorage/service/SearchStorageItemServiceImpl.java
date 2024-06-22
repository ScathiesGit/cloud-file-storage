package git.scathies.cloudfilestorage.service;

import git.scathies.cloudfilestorage.configuration.properties.ServiceConfigProperties;
import git.scathies.cloudfilestorage.dto.response.StorageItemDtoResp;
import git.scathies.cloudfilestorage.model.User;
import git.scathies.cloudfilestorage.repository.StorageItemRepository;
import git.scathies.cloudfilestorage.util.PathUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchStorageItemServiceImpl implements SearchStorageItemService {

    private final StorageItemRepository storageItemRepository;

    private final ServiceConfigProperties configProps;

    @Override
    public List<StorageItemDtoResp> search(User user, String name) {
        var rootFolder = configProps.getRootFolderTemplate().formatted(user.getId());

        return storageItemRepository.findAllAtFolder(rootFolder)
                .stream()
                .filter(storageItem -> PathUtil.isContains(
                        storageItem.getPath() + storageItem.getName(), name))
                .flatMap(storageItem -> PathUtil.extractPathTo(
                                name, storageItem.getPath() + storageItem.getName()
                        )
                        .stream())
                .distinct()
                .map(path -> path.replaceFirst(rootFolder, ""))
                .map(this::toStorageItemDtoResp)
                .toList();
    }

    private StorageItemDtoResp toStorageItemDtoResp(String rawPath) {
        var path = Paths.get(rawPath);
        var parentPath = path.getParent();

        String pathAsString = null;
        String name;

        if (parentPath == null) {
            name = "/";
        } else if (parentPath.endsWith("/") || parentPath.endsWith("\\")) {
            pathAsString = "/";
            name = path.getFileName().toString();
        } else {
            pathAsString = (path.getParent() + "/").replaceAll("\\\\", "/");
            name = path.getFileName().toString();
        }

        return StorageItemDtoResp.builder()
                .path(pathAsString)
                .name(name)
                .build();
    }
}
