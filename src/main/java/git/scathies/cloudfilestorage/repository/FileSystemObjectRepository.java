package git.scathies.cloudfilestorage.repository;

import io.minio.Result;
import io.minio.messages.Item;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface FileSystemObjectRepository {
    
    void save(String path, String contentType, InputStream inputStream);

    Iterable<Result<Item>> findAllByPrefix(String prefix);

    void update(String oldPath, String newPath);

    void updateAll(Map<String, String> oldPathToNewPath);

    void delete(String path);

    void deleteAll(List<String> paths);
}
